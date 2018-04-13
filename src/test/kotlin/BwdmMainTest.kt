import bwdm.BwdmMain
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class BwdmMainTest {

    @Test
    fun コマンドライン引数で適当な文字を入れて例外が出るかチェック() {
        //Assertions.assertThrows(IllegalArgumentException::class.java) {
        //    BwdmMain.hasOnlyOptionChar("9999999")
        //}
        Assertions.assertEquals(BwdmMain.hasOnlyOptionChar("9999999"), false)
    }
}