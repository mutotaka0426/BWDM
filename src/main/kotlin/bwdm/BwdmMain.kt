package bwdm

import bwdm.boundaryValueAnalysisUnit.BvaUnitMain
import bwdm.informationStore.InformationExtractor
import bwdm.symbolicExecutionUnit.SeUnitMain
import com.fujitsu.vdmj.lex.LexException
import com.fujitsu.vdmj.syntax.ParserException
import external.TimeMeasure

import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.ArrayList
import java.util.HashMap

/**
 * main class of BWDM
 */
object BwdmMain {

    /**
     * 立山さんが残した謎
     */
    private const val buildDate = "2018-1-24 PM19:03(JST)"

    /**
     * main method.
     *
     * @args args Command line arguments.
     */
    @Throws(LexException::class, ParserException::class, IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val tm = TimeMeasure()
        tm.start()

        exeBWDM(args)


        tm.finish()
        tm.printResult()

    }

    /**
     * processing of BWDM
     *
     * @args args Command line arguments.
     */
    @Throws(IOException::class, LexException::class, ParserException::class)
    private fun exeBWDM(args: Array<String>) {

        /**コマンド引数に応じて、下記の1-5の出力時の情報の表示フラグをON/OFFする */

        //1 諸情報 ファイルパス、生成したテストケース総数、関数、引数情報  default:OFF
        var showStandardInfo = false

        //2 生成した境界値に関する情報  default:OFF  -a
        var showBvsInfo = false

        //3 記号実行時に生成した条件式と戻り値に関する情報  default:OFF  -i
        var showSeConditionsInfo = false

        //4 境界値テストケース  default:ON  -b
        var showBvTestcases = true

        //5 記号実行テストケース  default:ON  -s
        var showSeTestcases = true

        /**情報フラグ終わり */


        //ヘルプ表示のフラグ  default:OFF  -h
        //ヘルプがONになったら、テストケース生成は行わない　ヘルプを出して終了
        var showHelp = false
        //バージョン表示のフラグ default:OFF -v
        //バージョン表示がONになった場合も、テストケース生成は行わない　バージョンを表示して終了
        var showVersion = false
        //コンソール表示かテキストファイル書き出しかのフラグ  default:コンソール表示  -f
        var displayOnConsole = true


        //コマンド引数のパース
        when (args.size){
            1 //ヘルプ表示 or バージョン表示 or オプション指定ミス or ファイル指定による通常実行の4パターン
            -> when {
                args[0].contains("-h") -> //ヘルプ表示フラグON
                    showHelp = true
                args[0].contains("-v") -> //バージョン表示フラグON
                    showVersion = true
                args[0].contains("-") -> {//オプション指定ミス ミスを指摘し、ヘルプ表示フラグON
                    println("不明なオプション: " + args[0])
                    showHelp = true
                }
                else -> { //オプション無しの実行
                    //オプションに変更は無く、諸情報とテストケースのみをコンソール表示
                }
            }

            2 //指定オプションによりフラグをON/OFF
            -> {
                //オプションが-だけ or オプション側に-が無い or ファイルパス側に-がある or オプションに変なものが混ざっている
                if (args[0] == "-" || !args[0].contains("-") || args[1].contains("-") || !hasOnlyOptionChar(args[0])) {
                    //ミスを指摘し、ヘルプ表示フラグON
                    println("不明なオプション: " + args[0])
                    showHelp = true
                }else if (args[0].contains("h")) { //ヘルプ　ヘルプだけ表示
                    showHelp = true
                }else if (args[0].contains("v")) { //バージョン　バージョンだけ表示
                    showVersion = true
                }else {
                    //ここ以降は、オプション指定が書式に沿った入力(のはず)
                    if (args[0].contains("n")) { //VDM仕様の基本情報を表示
                        showStandardInfo = true
                    }

                    if (args[0].contains("f")) { //ファイル書き出しON コンソール表示は行わない
                        displayOnConsole = false
                    }
                    if (args[0].contains("b")) { //境界値分析によるテストケースのみ出力
                        showSeTestcases = false
                    }
                    if (args[0].contains("s")) { //記号実行によるテストケースのみ出力
                        showBvTestcases = false
                    }
                    if (args[0].contains("b") && args[0].contains("s")) { //両方指定されていたらどちらも出力
                        showBvTestcases = true
                        showSeTestcases = true
                    }

                    if (args[0].contains("a")) { //生成した境界値に関する情報を出力
                        showBvsInfo = true
                    }
                    if (args[0].contains("i")) { //記号実行時に生成した条件式と戻り値の情報を出力
                        showSeConditionsInfo = true
                    }
                }
            }

            else //コマンドライン引数の数がおかしい
            -> {
                //ミスを指摘し、ヘルプ表示フラグON
                println("エラー : 引数の数が不正.")
                showHelp = true
            }
        }


        if (showVersion) {
            println("BWDM (Boundary Values/VDM)")
            println("Automatic Testcase Generation Tool based on VDM++ Specification")
            println("Version : 2.0")
            println("Built date : $buildDate")
            println("Copyright (C) 2018, Hiroki Tachiyama (University of Miyazaki).")
            System.exit(0)
        }

        /**ヘルプがONなら、表示して終了 */
        if (showHelp) {
            println("書式: bwdm [-naivhfbs] [file_name]\n")
            println("オプション一覧")
            println("追加表示")
            println("-n : VDM仕様の基本情報を表示する")
            println("-a : 境界値分析で生成した境界値を表示")
            println("-i : 記号実行で生成した条件式を表示")
            println()
            println("テストケース絞り込み")
            println("-b : 境界値分析によるテストケースのみ出力")
            println("-s : 記号実行によるテストケースのみ出力")
            println()
            println("出力先")
            println("-f : テキストファイル<file_name.tc>に書出")
            println("     デフォルト：コンソール表示")
            println()
            println("その他")
            println("-v : バージョン表示")
            println("-h : ヘルプを表示")

            System.exit(0)
        }


        /**テストケース生成処理 */
        var vdmPath: String? = null
        when (args.size) {
            1 -> vdmPath = args[0]
            2 -> vdmPath = args[1]
        }
        val extractInformation = InformationExtractor(vdmPath!!)
        val bvaUnitMain = BvaUnitMain(extractInformation)
        val seUnitMain = SeUnitMain(extractInformation)


        /**オプションに従って出力文字列を生成 */
        var buf = ""

        //1 諸情報
        if (showStandardInfo) {
            buf += "ファイルパス : " + File(vdmPath).canonicalPath + "\n"
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

        //2 境界値情報
        if (showBvsInfo) {
            buf += "各引数の境界値\n"
            val bvsList: HashMap<*, *> = bvaUnitMain.boundaryValueAnalyzer.boundaryValueList as HashMap<*, *>
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

        //3 記号実行情報
        if (showSeConditionsInfo) {
            buf += "記号実行情報\n"
            val carvList = extractInformation.conditionAndReturnValueList
            buf += "戻り値の数 : " + carvList.size + "\n"

            for (i in 0 until carvList.size) {
                buf += "制約 : "
                val carv = carvList.conditionAndReturnValues[i]
                val conditions = carv.conditions
                val bools = carv.bools

                //はじめの一つ以外は、前に and をつけてくっつける
                buf += if (bools[0] as Boolean) {
                    conditions[0] + " "
                } else {
                    "!( " + conditions[0] + " ) "
                }

                for (j in 1 until conditions.size) { //はじめの一個以外
                    buf += if (bools[j] as Boolean) {
                        "and " + conditions[j]
                    } else {
                        "and !( " + conditions[j] + " ) "
                    }
                }

                buf += ", 戻り値 : " + carv.returnStr + "\n"

            }

            buf += "\n"

        }

        //4 境界値テストケース
        if (showBvTestcases) {
            buf += "境界値分析によるテストケース\n"
            buf += bvaUnitMain.allTestcasesByBv
            buf += "\n"
        }

        //5 記号実行テストケース
        if (showSeTestcases) {
            buf += "記号実行によるテストケース\n"
            buf += seUnitMain.allTestcasesBySe
            buf += "\n"
        }

        //コンソール表示 or テキスト出力
        if (displayOnConsole) {
            print(buf)
        } else {
            val outputFile = File(extractInformation.vdmFilePath.replace("vdmpp", "tc"))
            val fw = FileWriter(outputFile)
            fw.write(buf)
            fw.close()
        }

    }

    /**
     * It judges whether anything other than characters that can be used as options is not included.
     *
     * @args args Command line arguments.
     * @return return Whether it is an available argument or not.
     */
    internal fun hasOnlyOptionChar(_optionStr: String): Boolean {
        //オプション文字として使えるものを消していって、最後に何か残っていたらダウト
        var optionStr = _optionStr

        optionStr = optionStr.replace("-", "")
        optionStr = optionStr.replace("v", "")
        optionStr = optionStr.replace("n", "")
        optionStr = optionStr.replace("f", "")
        optionStr = optionStr.replace("b", "")
        optionStr = optionStr.replace("s", "")
        optionStr = optionStr.replace("a", "")
        optionStr = optionStr.replace("i", "")
        optionStr = optionStr.replace("h", "")

        //if (optionStr != ""){
        //    throw IllegalArgumentException()
        //}
        return optionStr == ""
    }

}
