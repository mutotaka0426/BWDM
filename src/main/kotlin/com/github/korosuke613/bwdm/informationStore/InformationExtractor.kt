package com.github.korosuke613.bwdm.informationStore

import com.fujitsu.vdmj.ast.definitions.ASTDefinition
import com.fujitsu.vdmj.lex.Dialect
import com.fujitsu.vdmj.lex.LexException
import com.fujitsu.vdmj.lex.LexTokenReader
import com.fujitsu.vdmj.mapper.ClassMapper
import com.fujitsu.vdmj.syntax.DefinitionReader
import com.fujitsu.vdmj.syntax.ParserException
import com.fujitsu.vdmj.tc.definitions.TCDefinition
import com.fujitsu.vdmj.tc.definitions.TCExplicitFunctionDefinition
import com.fujitsu.vdmj.tc.definitions.TCExplicitOperationDefinition
import com.fujitsu.vdmj.tc.definitions.TCInstanceVariableDefinition
import java.io.File
import java.io.IOException


/**
 * information what got from VDM++ specification file by syntax analyse with VDMJ
 */
class InformationExtractor


@Throws(LexException::class, ParserException::class, IOException::class)
constructor(val vdmFilePath: String) {

    /**
     * a parameter to ArrayList of if-conditions.
     * ArrayList of ifConditions of each parameter.
     */
    //private val ifConditionBodies: HashMap<String, ArrayList<HashMap<String, String>>>

    val instanceVariables: LinkedHashMap<String, TCInstanceVariableDefinition> = LinkedHashMap()
    val explicitOperations: LinkedHashMap<String, TCExplicitOperationDefinition> = LinkedHashMap()
    val explicitFunctions: LinkedHashMap<String, FunctionDefinition> = LinkedHashMap()

    init {
        val vdmFile = File(vdmFilePath)
        val lexer = LexTokenReader(vdmFile, Dialect.VDM_PP)
        val parser = DefinitionReader(lexer)
        val astDefinitions = parser.readDefinitions()

        astDefinitions.forEach { astDefinition: ASTDefinition ->
            if (astDefinition.kind() == "instance variable") {
                lateinit var tcInstanceVariableDefinition: TCInstanceVariableDefinition
                try {
                    tcInstanceVariableDefinition = ClassMapper.getInstance(TCDefinition.MAPPINGS).init().convert<TCInstanceVariableDefinition>(astDefinition)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                instanceVariables[tcInstanceVariableDefinition.name.toString()] = tcInstanceVariableDefinition
            }
            if (astDefinition.kind() == "explicit operation") {
                lateinit var tcExplicitOperationDefinition: TCExplicitOperationDefinition
                try {
                    tcExplicitOperationDefinition = ClassMapper.getInstance(TCDefinition.MAPPINGS).init().convert<TCExplicitOperationDefinition>(astDefinition)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                explicitOperations[tcExplicitOperationDefinition.name.toString()] = tcExplicitOperationDefinition
            }
            if (astDefinition.kind() == "explicit function") {
                lateinit var tcFunctionDefinition: TCExplicitFunctionDefinition
                try {
                    tcFunctionDefinition = ClassMapper.getInstance(TCDefinition.MAPPINGS).init().convert<TCExplicitFunctionDefinition>(astDefinition)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val function = FunctionDefinition(tcFunctionDefinition)
                explicitFunctions[function.functionName] = function
            }
        }
    }/* Initializing fields*/
}
