package com.github.korosuke613.bwdm.boundaryValueAnalysisUnit

import com.github.korosuke613.bwdm.informationStore.FunctionDefinition
import com.github.korosuke613.bwdm.informationStore.InformationExtractor
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class BoundaryValueAnalyzerTest {
    lateinit var bva: BoundaryValueAnalyzer

    fun createFunction(fileName: String, _className: String? = null): FunctionDefinition? {
        val className = _className ?: fileName.split(".")[0]
        val resource = InformationExtractor::class.java.classLoader.getResource(fileName)
        val filePath = if (resource?.path == null) "" else resource.path
        val information = InformationExtractor(filePath)
        return information.explicitFunctions[className]
    }

    @Nested
    inner class Normal {
        init {
            val fd = createFunction("Arg2_Japanese.vdmpp", "SampleFunction")
            bva = BoundaryValueAnalyzer(fd!!, false)
        }

        @Test
        fun generateTypeBoundaryValueTest() {
            val expected = arrayOf(
                    longArrayOf(
                            2147483648, 2147483647, -2147483648, -2147483649,
                            10, 11, 0, -1, 19, 20),
                    longArrayOf(
                            4294967295, 4294967294,
                            0, -1, 1, -10, -11, -3, -2),
                    longArrayOf(
                            4294967296, 4294967295,
                            1, 0, 4, 3)
            )
            assertArrayEquals(expected[0], bva.boundaryValueList["a"]!!.toLongArray())
            assertArrayEquals(expected[1], bva.boundaryValueList["b"]!!.toLongArray())
            assertArrayEquals(expected[2], bva.boundaryValueList["c"]!!.toLongArray())
        }
    }

    @Nested
    inner class Pairwise {
        init {
            val fd = createFunction("pict.vdmpp", "problemFunction")
            bva = BoundaryValueAnalyzer(fd!!, true)
        }

        @Test
        fun generateTypeBoundaryValueTest() {
            val expected = arrayOf(
                    longArrayOf(
                            4294967295, 4294967294,
                            0, -1, 5, 4)
            )
            assertArrayEquals(expected[0], bva.boundaryValueList["a"]!!.toLongArray())
        }
    }
}