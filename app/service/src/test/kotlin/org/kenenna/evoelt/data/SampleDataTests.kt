package org.kenenna.evoelt.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.kenenna.evoelt.EvoEltSandboxConfig
import org.kenenna.evoelt.utils.SampleData
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@ContextConfiguration(classes = [EvoEltSandboxConfig::class])
class SampleDataTests {
    @Test
    fun `reverseLabels successfully reverses label order in an object`() {
        val labels = """
            {"abc": "one","xyz": []}
        """.trimIndent()
        val labelsGoal = """
            {"xyz": [],"abc": "one"}
        """.trimIndent()
        assertEquals(labelsGoal, SampleData.reverseLabels(labels))
    }

    @Test
    fun `reverseLabels successfully reverses label order in an array`() {
        val labels = """
            [1,2,3]
        """.trimIndent()
        val labelsGoal = """
            [3,2,1]
        """.trimIndent()
        assertEquals(labelsGoal, SampleData.reverseLabels(labels))
    }
}