package org.kenenna.evoelt.events

import org.kenenna.evoelt.jpa.repository.RawSequenceRepository
import org.kenenna.evoelt.jpa.repository.RawEventRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.configurationprocessor.json.JSONArray
import org.springframework.boot.configurationprocessor.json.JSONObject
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import java.util.*

@RestController
class EventController(
    @Autowired val rawSequenceRepository: RawSequenceRepository,
    @Autowired val rawEventRepository: RawEventRepository
) {
    @RequestMapping(value = ["/lookup/raw/sequence"], method = [RequestMethod.GET],
        produces = ["application/json; charset=utf-8"]
    )
    @ResponseBody
    fun lookupRawSequence(
        @RequestParam("raw_event_id") rawEventId: String,
        @PageableDefault(page = 0, size = 100, sort = ["rawSequenceOrderId"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): ResponseEntity<String> {
        val rawEventLookup = rawEventRepository.findById(UUID.fromString(rawEventId)).get()
        val rawSequence = rawSequenceRepository.findById(rawEventLookup.rawSequenceId).get()
        val rawEventLookupResults = rawEventRepository.findAllByRawSequenceIdAndRawSequenceOrderIsLessThanEqual(
            rawSequenceId = rawSequence.id!!,
            rawSequenceOrderId = rawEventLookup.rawSequenceOrderId,
            pageable = pageable
        )

        val rawEvents = JSONArray()
        rawEventLookupResults!!.forEach { rawEvent -> rawEvents.put(
            JSONObject()
                .put("id", rawEvent!!.id)
                .put("data", rawEvent.data)
                .put("order_id", rawEvent.rawSequenceOrderId)
                .put("created_dt", rawEvent.createdDt)
        )}
        return ResponseEntity.ok(
            JSONObject()
                .put("labels", JSONArray(rawSequence.labels))
                .put("events", rawEvents)
                .put("total_events", rawEventLookupResults.totalElements)
                .put("total_pages", rawEventLookupResults.totalPages)
                .put("pageable", JSONObject()
                    .put("page_offset", pageable.offset)
                    .put("page_number", pageable.pageNumber)
                    .put("page_size", pageable.pageSize)
                    .put("sort", pageable.sort.toString()
                    )
                )
            .toString()
        )
    }
}