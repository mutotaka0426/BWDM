package com.github.korosuke613.bwdm

import com.github.korosuke613.bwdm.boundaryValueAnalysisUnit.BvaUnitMain
import com.github.korosuke613.bwdm.informationStore.InformationExtractor
import com.github.korosuke613.bwdm.symbolicExecutionUnit.SeUnitMain
import com.github.korosuke613.bwdm.domainAnalysis.DomainAnalyser
import com.fujitsu.vdmj.lex.LexException
import com.fujitsu.vdmj.syntax.ParserException
import external.TimeMeasure
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*


/**
 * main class of BWDM
 */
object BwdmMain {
    // build日
    private const val buildDate = "2018-1-24 PM19:03(JST)"
    private lateinit var extractInformation: InformationExtractor
    private lateinit var bvaUnitMain: BvaUnitMain
    private lateinit var seUnitMain: SeUnitMain
    private lateinit var domainAnalyser: DomainAnalyser
    private val shell = Shell()
    private lateinit var buf: String

    /**
     * main method.
     *
     * @args args Command line arguments.
     */
    @Throws(LexException::class, ParserException::class, IOException::class)
    @JvmStatic
    fun main(vararg args: String) {
        execBWDM(*args)
    }

    /**
     * processing of BWDM
     *
     * @args args Command line arguments.
     */
    @Throws(IOException::class, LexException::class, ParserException::class)
    private fun execBWDM(vararg args: String) {

        val parser = CmdLineParser(shell)
        try {
            parser.parseArgument(*args)
        } catch (e: CmdLineException) {
            // 引数の型がおかしかったり、必須項目が足りないと例外
            System.err.println("Got Exception: " + e.message)
            parser.printUsage(System.err)
            System.exit(1)
        }

        //コマンド引数のパース
        when (args.size) {
            0->{
                printUsage(parser)
                return
            }
            1 //ヘルプ表示 or バージョン表示 or オプション指定ミス or ファイル指定による通常実行の4パターン
            -> {
                when {
                    shell.versionFlag -> printVersionInfo()
                    shell.usageFlag -> printUsage(parser)
                    else -> printUsage(parser)
                }
                return
            }
        }

        /**オプションに従って出力文字列を生成 */
        buf = ""
        val tm = TimeMeasure()
        tm.start()
        extractInformation = InformationExtractor(shell.vdmFileName!!)
        bvaUnitMain = BvaUnitMain(extractInformation, isPairwise = shell.showBvTestcasesWithPairwise)
        seUnitMain = SeUnitMain(extractInformation)
        domainAnalyser = DomainAnalyser(extractInformation)
        if (shell.showStandardInfo) {
            showStandardInfo()
        }
        if (shell.showBvsInfo) {
            showBvsInfo()
        }
        if (shell.showSeConditionsInfo) {
            showSeConditionInfo()
        }
        if (shell.showBvTestcases and !shell.showBvTestcasesWithPairwise) {
            buf += "境界値分析によるテストケース\n"
            buf += bvaUnitMain.allTestcasesByBv
            buf += "\n"
        } else if (shell.showBvTestcasesWithPairwise) {
            buf += "境界値分析によるテストケース（ペアワイズ法適用）\n"
            buf += bvaUnitMain.allTestcasesByBv
            buf += "\n"
        }
        if (shell.showSeTestcases) {
            buf += "記号実行によるテストケース\n"
            buf += seUnitMain.allTestcasesBySe
            buf += "\n"
        }
        if (shell.showDaTestcases) {
            buf += "ドメインテストによるテストケース\n"
            buf += domainAnalyser.allTestcasesByDa
            buf += "\n"
        }
        if (shell.displayOnConsole) {
            print(buf)
        }
        if (shell.writeFile) {
            outputFile(extractInformation.vdmFilePath.replace("vdmpp", "tc"))
        }else if (shell.writeFileName != null){
            outputFile(shell.writeFileName!!)
        }

        tm.finish()
        if (shell.printTimeMeasure) {
            tm.printResult()
        }
    }

    private fun printUsage(parser: CmdLineParser) {
        println("Usage:")
        println(" bwdm [options] vdmFileName")
        println()
        println("Options:")
        parser.printUsage(System.out)
        return
    }

    private fun printVersionInfo() {
        println("BWDM (Boundary Values/VDM)")
        println("Automatic Testcase Generation Tool based on VDM++ Specification")
        println("Version : 2.0")
        println("Built date : $buildDate")
        println("Copyright (C) 2018, Hiroki TACHIYAMA and Futa HIRAKOBA(University of Miyazaki).")
        return
    }

    private fun outputFile(file_name: String) {
        val outputFile = File(file_name)
        val fw = FileWriter(outputFile)
        fw.write(buf)
        fw.close()
    }

    private fun showStandardInfo() {
        buf += "ファイルパス : " + File(shell.vdmFileName).canonicalPath + "\n"
        buf += "関数名 : " + extractInformation.functionName + "\n"
        buf += "引数の型 : "
        for (i in 0 until extractInformation.argumentTypes.size) {
            buf += (extractInformation.parameters[i] + ":"
                    + extractInformation.argumentTypes[i] + " ")
        }
        buf += "\n"
        buf += "戻り値の型 : " + extractInformation.returnValue + "\n"
        val bvTestcaseNum = bvaUnitMain.boundaryValueAnalyzer.inputDataList.size
        val seTestcaseNum = seUnitMain.se.inputDataList.size
        buf += "生成テストケース数 : " + (bvTestcaseNum + seTestcaseNum) + "件"
        buf += "(境界値分析:$bvTestcaseNum/記号実行:$seTestcaseNum)"
        buf += "\n\n"
    }

    private fun showBvsInfo() {
        buf += "各引数の境界値\n"
        val bvsList: HashMap<*, *> = bvaUnitMain.boundaryValueAnalyzer.boundaryValueList
        val parameters = extractInformation.parameters
        for (i in parameters.indices) {
            val currentPrm = parameters[i]
            val bvs: ArrayList<*> = bvsList[currentPrm] as ArrayList<*>
            buf += "$currentPrm : "
            for (bv in bvs) {
                buf += bv.toString() + " "
            }
            buf += "\n"
        }
        buf += "\n"
    }

    private fun showSeConditionInfo() {
        buf += "記号実行情報\n"
        val carvList = extractInformation.conditionAndReturnValueList
        buf += "戻り値の数 : " + carvList.size + "\n"

        for (i in 0 until carvList.size) {
            buf += "制約 : "
            val carv = carvList.conditionAndReturnValues[i]
            val conditions = carv.conditions
            val bools = carv.bools

            //はじめの一つ以外は、前に and をつけてくっつける
            buf += if (bools[0]) {
                conditions[0] + " "
            } else {
                "!( " + conditions[0] + " ) "
            }

            for (j in 1 until conditions.size) { //はじめの一個以外
                buf += if (bools[j]) {
                    "and " + conditions[j]
                } else {
                    "and !( " + conditions[j] + " ) "
                }
            }
            buf += ", 戻り値 : " + carv.returnStr + "\n"
        }
        buf += "\n"
    }
}
