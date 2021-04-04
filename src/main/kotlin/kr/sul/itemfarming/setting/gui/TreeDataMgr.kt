package kr.sul.itemfarming.setting.gui

import com.google.gson.GsonBuilder
import kr.sul.itemfarming.Main.Companion.plugin
import kr.sul.itemfarming.setting.gui.node.NodeRank
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.*


object TreeDataMgr {
    val d = "ddasddfd"
    val rootNodeList = arrayListOf<NodeRank>()  // = 최상위 List
    private val settingFile = File("${plugin.dataFolder}/farmingSetting.json")

    // TODO: BackUp
    fun saveAll() {
        createFilesIfNotExist()

        // rootNodeList Json화 시키기
        // 개미굴에 물줄기 내려가듯
        val rootJsonArray = JSONArray()  // RootNodeList에 해당
        // NodeRank
        for (rank in rootNodeList) {
            val rankJson = JSONObject()
            rankJson["name"] = rank.name
            rankJson["chance"] = rank.chance
            val rankChildJsonArray = JSONArray()  // 각 rank의 childNodeList에 해당
            rankJson["childNodeList"] = rankChildJsonArray

            // NodeCategory
            for (category in rank.childNodeList) {
                val categoryJson = JSONObject()
                categoryJson["name"] = category.name
                categoryJson["chance"] = category.chance
                val categoryChildJsonArray = JSONArray()
                categoryJson["childNodeList"] = categoryChildJsonArray

                // NodeItem
                for (item in category.childNodeList) {
                    val itemJson = JSONObject()
                    itemJson["nameForReference"] = item.displayName
                    itemJson["itemStack"] = toBase64(item.item)
                    itemJson["chance"] = item.chance

                    // Json으로 객체 다 직렬화 한 후, 자신의 parentNode의 childNodeList에 자신을 추가
                    categoryChildJsonArray.add(itemJson)
                }
                rankChildJsonArray.add(categoryJson)
            }
            rootJsonArray.add(rankJson)
        }
        val gsonBuilder = GsonBuilder().setPrettyPrinting().create()
        val finalJson = gsonBuilder.toJson(rootJsonArray)

        val writter = FileWriter(settingFile)
        val bWritter = BufferedWriter(writter)
        try {
            bWritter.write(finalJson)
            bWritter.flush()
        } catch(ignored: IOException) {
        } finally {
            bWritter.close()
            writter.close()
        }
    }

    // Bukkit Obj포함 Base64 Encoder/Decoder
    private fun toBase64(any: Any): String {
        val outputStream = ByteArrayOutputStream()
        val dataOutput = BukkitObjectOutputStream(outputStream)
        dataOutput.writeObject(any)
        dataOutput.close()
        return Base64Coder.encodeLines(outputStream.toByteArray())
    }
    private fun<T: Any> fromBase64(base64Str: String): T {
        val inputStream = ByteArrayInputStream(Base64Coder.decodeLines(base64Str))
        val dataInput = BukkitObjectInputStream(inputStream)
        val returnVal = dataInput.readObject() as T
        dataInput.close()
        return returnVal
    }


    private fun createFilesIfNotExist() {
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }

        if (!settingFile.exists()) {
            settingFile.createNewFile()
        }
    }

//    fun loadAll() {
//        createFilesIfNotExist()
//        val jsonBuilder = GsonBuilder().create()
//
//        val fr = FileReader(settingFile)
//
//    }

/*
    fun loadAll() {
        createFilesIfNotExist()
        val setting = loadAsConfig(settingFile)
        setting.getKeys(true).forEach {

        }
    }
*/
}






//data class NodeA(val name: String, var childNodeList: ArrayList<NodeB>)
//class NodeB(val getParentNode: Supplier<NodeA>, val name: String) {
//    init {
//        getParentNode.get().childNodeList.add(this)
//    }
//}
//
//fun main() {
//    val nodeA = NodeA("just name of nodeA", arrayListOf())
//    val nodeB = NodeB({ nodeA }, "just name of nodeB")
//    val nodeB_2 = NodeB({ nodeA }, "just name of nodeB")
//
//    val toJsonObj = nodeA
//
//    try {
//        val jsonBuilder = GsonBuilder().setPrettyPrinting().create()
//        val json = jsonBuilder.toJson(toJsonObj)
//        println(json)
////        val writter = FileWriter(File("C:\\Test\\JsonTest.json"))
////        try {
////            writter.write(json)
////            writter.flush()
////        } catch(ignored: IOException) {
////        } finally {
////            writter.close()
////        }
//    } catch (ignored: Exception) {}
//
//}