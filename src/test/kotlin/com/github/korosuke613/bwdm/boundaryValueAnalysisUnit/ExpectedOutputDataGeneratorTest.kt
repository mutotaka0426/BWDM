package com.github.korosuke613.bwdm.boundaryValueAnalysisUnit

import com.github.korosuke613.bwdm.informationStore.FunctionDefinition
import com.github.korosuke613.bwdm.informationStore.IfElseExprSyntaxTree
import com.github.korosuke613.bwdm.informationStore.InformationExtractor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*

internal class ExpectedOutputDataGeneratorTest {
    lateinit var bva: BoundaryValueAnalyzer
    lateinit var expectedOutputDataGenerator: ExpectedOutputDataGenerator

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
            expectedOutputDataGenerator = ExpectedOutputDataGenerator(
                    fd,
                    Objects.requireNonNull<IfElseExprSyntaxTree>(fd.ifElseExprSyntaxTree).root,
                    bva.inputDataList
            )
        }

        @Test
        fun makeParsedConditionTest() {
            fun createMap(left: String, right: String, ope: String): Map<String, String> {
                return mapOf("left" to left, "right" to right, "operator" to ope)
            }

            val expected = arrayOf(
                    createMap("a", "10", "<="),
                    createMap("b", "-10", ">="),
                    createMap("b", "-3", "<="),
                    createMap("a", "20", "<")
            )

            val condition = arrayOf("a<=10", "b>=-10", "b<=-3", "a<20")

            expected.forEachIndexed { i, it ->
                assertEquals(it, ExpectedOutputDataGenerator.makeParsedCondition(condition[i]))
            }
        }

        @Test
        fun collectTestCasesTest() {
            val inputDataList = mapOf(
                    89 to mapOf<String, Long>("a" to 20, "b" to -2, "c" to 4294967296),
                    101 to mapOf<String, Long>("a" to 2147483647, "b" to 4294967294, "c" to 4294967295),
                    102 to mapOf<String, Long>("a" to -2147483648, "b" to 4294967294, "c" to 4294967295),
                    104 to mapOf<String, Long>("a" to 10, "b" to 4294967294, "c" to 4294967295)
            )

            val expected = mapOf(
                    89 to "Undefined Action",
                    101 to "\"aは20以上で、bは-3より大きいです\"",
                    102 to "\"aは-1以下で、bは0より大きいです\"",
                    104 to "\"aは-1より大きく10以下です\""
            )
            expected.forEach { (key, it) ->
                assertEquals(it, expectedOutputDataGenerator.expectedOutputDataList[key])
                assertEquals(inputDataList[key], bva.inputDataList[key])
            }
        }
    }
}