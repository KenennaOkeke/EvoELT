package org.kenenna.evoelt.utils

import java.util.*

class SampleData {
    companion object {
        fun getRawEventOneInSequenceOne(): String {
            return """
                    {
                        "data": "ABCD",
                        "labels": [
                            "user_id-3",
                            "instance_id-5"
                        ]
                    }
                """
        }

        fun getProcessedEventOneInSequenceOne(rawEventId: UUID = UUID.randomUUID()): String {
            return """
                    {
                        "data": "DCBA",
                        "raw_event_id": $rawEventId,
                        "labels": [
                            "type-reverse_string"
                        ]
                    }
                """
        }

        fun getRawEventTwoInSequenceOne(): String {
            return """
                    {
                        "data": "ABC",
                        "labels": [
                            "instance_id-5",
                            "user_id-3"
                        ]
                    }
                """
        }

        fun getProcessedEventTwoInSequenceOne(rawEventId: UUID = UUID.randomUUID()): String {
            return """
                    {
                        "data": "CBA",
                        "raw_event_id": $rawEventId,
                        "labels": [
                            "type-reverse_string"
                        ]
                    }
                """
        }

        fun getRawEventOneInSequenceTwo(): String {
            return """
                    {
                        "data": "WXYZ",
                        "labels": [
                            "user_id-1",
                            "instance_id-5"
                        ]
                    }
                """
        }

        fun getProcessedEventOneInSequenceTwo(rawEventId: UUID = UUID.randomUUID()): String {
            return """
                    {
                        "data": "ZYXW",
                        "raw_event_id": $rawEventId
                    }
                """
        }

        fun getRawEventTwoInSequenceTwo(): String {
            return """
                    {
                        "data": "XYZ",
                        "labels": [
                            "user_id-1",
                            "instance_id-5"
                        ]
                    }
                """
        }

        fun getProcessedEventTwoInSequenceTwo(rawEventId: UUID = UUID.randomUUID()): String {
            return """
                    {
                        "data": "ZYX",
                        "raw_event_id": $rawEventId
                    }
                """
        }

        fun getRawEventOneInSequenceThree(): String {
            return """
                    {
                        "data": "1234",
                        "labels": [
                            "user_id-1",
                            "instance_id-1"
                        ]
                    }
                """
        }

        fun getProcessedEventOneInSequenceThree(rawEventId: UUID = UUID.randomUUID()): String {
            return """
                    {
                        "data": "4321",
                        "raw_event_id": $rawEventId,
                        "labels": [
                            "type-reverse_int"
                        ]
                    }
                """
        }

        fun getRawEventTwoInSequenceThree(): String {
            return """
                    {
                        "data": "234",
                        "labels": [
                            "user_id-1",
                            "instance_id-1" 
                        ]
                    }
                """
        }

        fun getProcessedEventTwoInSequenceThree(rawEventId: UUID = UUID.randomUUID()): String {
            return """
                    {
                        "data": "432",
                        "raw_event_id": $rawEventId,
                        "labels": {
                            "type-reverse_int"
                        }
                    }
                """
        }

        fun reverseLabels(labels: String): String {
            val firstChar = labels[0].toString()
            val lastChar = labels[labels.length - 1].toString()
            return firstChar + labels.removePrefix(firstChar).removeSuffix(lastChar)
                .split(",").asReversed().joinToString(",") + lastChar
        }
    }
}