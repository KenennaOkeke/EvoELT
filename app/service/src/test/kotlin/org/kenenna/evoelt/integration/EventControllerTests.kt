package org.kenenna.evoelt.integration

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kenenna.evoelt.EvoEltSandboxConfig
import org.kenenna.evoelt.events.EventController
import org.kenenna.evoelt.jpa.entity.RawSequenceEntity
import org.kenenna.evoelt.jpa.entity.RawEventEntity
import org.kenenna.evoelt.jpa.repository.RawSequenceRepository
import org.kenenna.evoelt.jpa.repository.RawEventRepository
import org.kenenna.evoelt.jpa.repository.ProcessedSequenceRepository
import org.kenenna.evoelt.jpa.repository.ProcessedEventRepository
import org.kenenna.evoelt.utils.MessageBody
import org.kenenna.evoelt.utils.SampleData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.configurationprocessor.json.JSONArray
import org.springframework.boot.configurationprocessor.json.JSONObject
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.*

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [EventController::class]
)
@ContextConfiguration(classes = [EvoEltSandboxConfig::class])
class EventControllerTests(
    @Autowired val rawSequenceRepository: RawSequenceRepository,
    @Autowired val rawEventRepository: RawEventRepository,
    @Autowired val processedSequenceRepository: ProcessedSequenceRepository,
    @Autowired val processedEventRepository: ProcessedEventRepository,
) {
    lateinit var mockMvc : MockMvc

    @BeforeEach
    fun teardown() {
        processedEventRepository.deleteAll()
        processedSequenceRepository.deleteAll()
        rawEventRepository.deleteAll()
        rawSequenceRepository.deleteAll()
        mockMvc = MockMvcBuilders.standaloneSetup(
            EventController(rawSequenceRepository, rawEventRepository)
        ).setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver()).build()
    }

    companion object {
        @JvmStatic
        @AfterAll
        fun teardown(
            @Autowired rawSequenceRepository: RawSequenceRepository,
            @Autowired rawEventRepository: RawEventRepository,
            @Autowired processedSequenceRepository: ProcessedSequenceRepository,
            @Autowired processedEventRepository: ProcessedEventRepository,
        ) {
            processedEventRepository.deleteAll()
            processedSequenceRepository.deleteAll()
            rawEventRepository.deleteAll()
            rawSequenceRepository.deleteAll()
        }
    }

    @Test
    fun `lookupRawSequence successfully returns a raw sequence with all data types`() {
        val rawSequenceLabels = MessageBody(SampleData.getRawEventOneInSequenceOne()).getLabels()
        val rawSequence = rawSequenceRepository.save(RawSequenceEntity(labels = rawSequenceLabels))
        val rawEvent1 = rawEventRepository.save(RawEventEntity(
            rawSequenceId = rawSequence.id!!,
            rawSequenceOrderId = 1,
            data = JSONArray().put("wow").toString()
        ))
        val rawEvent2 = rawEventRepository.save(RawEventEntity(
            rawSequenceId = rawSequence.id!!,
            rawSequenceOrderId = 2,
            data = "double wow"
        ))
        val rawEvent3 = rawEventRepository.save(RawEventEntity(
            rawSequenceId = rawSequence.id!!,
            rawSequenceOrderId = 4, // 4 instead of 3, datetime shouldn't be relevant
            data = JSONObject().put("test", "woah").toString()
        ))
        val rawEvent4 = rawEventRepository.save(RawEventEntity(
            rawSequenceId = rawSequence.id!!,
            rawSequenceOrderId = 3, // 3 instead of 4, datetime shouldn't be relevant
            data = JSONObject().put("test", "woah").toString()
        ))
        val rawEvent5 = rawEventRepository.save(RawEventEntity(
            rawSequenceId = rawSequence.id!!,
            rawSequenceOrderId = 5,
            data = " "
        ))
        val rawEvent6 = rawEventRepository.save(RawEventEntity(
            rawSequenceId = rawSequence.id!!,
            rawSequenceOrderId = 6,
            data = ""
        ))

        val rawSequenceRequest = lookupRawSequence(rawEvent6.id!!)
        assertEquals(
            rawSequenceLabels,
            rawSequenceRequest["labels"].toString()
        )

        assertEquals(
            rawEvent6.id.toString(),
            rawSequenceRequest.optJSONArray("events").optJSONObject(0)["id"]
        )
        assertEquals(
            rawEvent6.data,
            rawSequenceRequest.optJSONArray("events").optJSONObject(0)["data"]
        )

        assertEquals(
            rawEvent5.id.toString(),
            rawSequenceRequest.optJSONArray("events").optJSONObject(1)["id"]
        )
        assertEquals(
            rawEvent5.data,
            rawSequenceRequest.optJSONArray("events").optJSONObject(1)["data"]
        )

        assertEquals(
            rawEvent4.id.toString(),
            rawSequenceRequest.optJSONArray("events").optJSONObject(3)["id"]
        )
        assertEquals(
            rawEvent4.data,
            rawSequenceRequest.optJSONArray("events").optJSONObject(3)["data"]
        )

        assertEquals(
            rawEvent3.id.toString(),
            rawSequenceRequest.optJSONArray("events").optJSONObject(2)["id"]
        )
        assertEquals(
            rawEvent3.data,
            rawSequenceRequest.optJSONArray("events").optJSONObject(2)["data"]
        )

        assertEquals(
            rawEvent2.id.toString(),
            rawSequenceRequest.optJSONArray("events").optJSONObject(4)["id"]
        )
        assertEquals(
            rawEvent2.data,
            rawSequenceRequest.optJSONArray("events").optJSONObject(4)["data"]
        )

        assertEquals(
            rawEvent1.id.toString(),
            rawSequenceRequest.optJSONArray("events").optJSONObject(5)["id"]
        )
        assertEquals(
            rawEvent1.data,
            rawSequenceRequest.optJSONArray("events").optJSONObject(5)["data"]
        )
    }

    @Test
    fun `lookupRawSequence successfully paginates and stop limits at 5 out of 6`() { //TODO: Expand test cases - fail if sort is changed
        val rawSequenceLabels = MessageBody(SampleData.getRawEventOneInSequenceOne()).getLabels()
        val rawSequence = rawSequenceRepository.save(RawSequenceEntity(labels = rawSequenceLabels))
        rawEventRepository.save(RawEventEntity(
            rawSequenceId = rawSequence.id!!,
            rawSequenceOrderId = 1,
            data = JSONArray().put("wow").toString()
        ))
        rawEventRepository.save(RawEventEntity(
            rawSequenceId = rawSequence.id!!,
            rawSequenceOrderId = 2,
            data = "double wow"
        ))
        rawEventRepository.save(RawEventEntity(
            rawSequenceId = rawSequence.id!!,
            rawSequenceOrderId = 4, // 4 instead of 3, datetime shouldn't be relevant
            data = JSONObject().put("test", "woah").toString()
        ))
        rawEventRepository.save(RawEventEntity(
            rawSequenceId = rawSequence.id!!,
            rawSequenceOrderId = 3, // 3 instead of 4, datetime shouldn't be relevant
            data = JSONObject().put("test", "woah").toString()
        ))
        val rawEvent5 = rawEventRepository.save(RawEventEntity(
            rawSequenceId = rawSequence.id!!,
            rawSequenceOrderId = 5,
            data = " "
        ))
        rawEventRepository.save(RawEventEntity(
            rawSequenceId = rawSequence.id!!,
            rawSequenceOrderId = 6,
            data = ""
        ))

        val rawEvents = rawEventRepository.findAll()
        assertEquals(6, rawEvents.size)

        var rawSequenceRequest = lookupRawSequence(rawEvent5.id!!, 2, 0)
        print(rawSequenceRequest.toString())

        assertEquals(5, rawSequenceRequest["total_events"])
        assertEquals(3, rawSequenceRequest["total_pages"])
        assertEquals(0, rawSequenceRequest.optJSONObject("pageable")["page_offset"])
        assertEquals(0, rawSequenceRequest.optJSONObject("pageable")["page_number"])
        assertEquals(2, rawSequenceRequest.optJSONObject("pageable")["page_size"])
        assertEquals("rawSequenceOrderId: DESC", rawSequenceRequest.optJSONObject("pageable")["sort"])

        assertEquals(
            2,
            rawSequenceRequest.optJSONArray("events").length()
        )

        rawSequenceRequest = lookupRawSequence(rawEvent5.id!!, 2, 1)
        assertEquals(
            2,
            rawSequenceRequest.optJSONArray("events").length()
        )

        rawSequenceRequest = lookupRawSequence(rawEvent5.id!!, 2, 2)
        assertEquals(
            1,
            rawSequenceRequest.optJSONArray("events").length()
        )
    }

    private fun lookupRawSequence(rawEventId: UUID, size: Int = 100, page: Int = 0): JSONObject {
        return JSONObject(this.mockMvc.perform(
            get("/lookup/raw/sequence")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .queryParam("raw_event_id", rawEventId.toString())
                .queryParam("size", size.toString())
                .queryParam("page", page.toString())
        ).andReturn().response.contentAsString)
    }
}