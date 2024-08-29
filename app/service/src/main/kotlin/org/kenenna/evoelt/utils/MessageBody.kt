package org.kenenna.evoelt.utils

import org.springframework.boot.configurationprocessor.json.JSONArray
import org.springframework.boot.configurationprocessor.json.JSONObject

class MessageBody(private val body: JSONObject) {
    constructor(string: String) : this(JSONObject(string))

    fun getLabels(): String {
        return JSONArray(body.optString("labels").ifBlank { "[]" }).toString() // TODO: Examine catching placement of array vs object /w consumption
    }

    fun getData(): String {
        return body.optString("data")
    }

    fun getRawEventId(): String {
        return body.optString("raw_event_id")
    }

    override fun toString(): String {
        return body.toString()
    }
}