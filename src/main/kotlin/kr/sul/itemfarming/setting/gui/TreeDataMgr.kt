package kr.sul.itemfarming.setting.gui

import com.google.gson.GsonBuilder
import kr.sul.itemfarming.Main.Companion.plugin
import kr.sul.itemfarming.setting.gui.node.*
import org.apache.commons.io.FileUtils
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.*
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*


// TODO: 데이터 Excel로 변환 추가.  (그것으로 확률 수정까지 가능하게)
object TreeDataMgr {
    val rootNodeList = arrayListOf<NodeRank>()  // = 최상위 List
    private val dataFile = File("${plugin.dataFolder}/settings_data.json")
    private val backUpFolder = File("${plugin.dataFolder}/backup")

    private const val NAME_KEY = "name"
    private const val CHANCE_KEY = "chance"
    private const val CHILD_NODE_LIST_KEY = "childNodeList"
    private const val NODE_ITEM_CLASS = "itemNodeClass"
    private const val NAME_FOR_REFERENCE_KEY = "nameForReference"
    private const val ITEM_STACK_KEY = "itemStack"
    private const val CS_PARENT_NODE_KEY = "itemStack"

    fun saveAll() {
        backUpData()
        createFilesIfNotExist()

        // rootNodeList Json화 시키기
        // 오른쪽으로 가며 자신 JSONObject 생성, 끝까지 찍고 왼쪽으로 돌아오며 상위의 JSONObject의 JSONArray(childNodeList)에 자신 담기
        // (Rank 만드려면 Category필요하고, Category 만드려면 Item 필요하기 때문)
        val rootJsonArray = JSONArray()  // RootNodeList에 해당
        // NodeRank
        for (rank in rootNodeList) {
            val rankJson = JSONObject()
            rankJson[NAME_KEY] = rank.name
            rankJson[CHANCE_KEY] = rank.chance
            val rankChildJsonArray = JSONArray()  // 각 rank의 childNodeList에 해당
            rankJson[CHILD_NODE_LIST_KEY] = rankChildJsonArray

            // NodeCategory
            for (category in rank.childNodeList) {
                val categoryJson = JSONObject()
                categoryJson[NAME_KEY] = category.name
                categoryJson[CHANCE_KEY] = category.chance
                val categoryChildJsonArray = JSONArray()
                categoryJson[CHILD_NODE_LIST_KEY] = categoryChildJsonArray

                // NodeItem
                for (item in category.childNodeList) {
                    val itemJson = JSONObject()
                    when (item) {
                        is NodeItemNormal -> {
                            itemJson[NODE_ITEM_CLASS] = NodeItemNormal::class.java.name
                            itemJson[NAME_FOR_REFERENCE_KEY] = item.displayName
                            itemJson[ITEM_STACK_KEY] = toBase64(item.item)  // 자신만의 것
                            itemJson[CHANCE_KEY] = item.chance
                        }
                        is NodeItemCrackShot -> {
                            itemJson[NODE_ITEM_CLASS] = NodeItemCrackShot::class.java.name
                            itemJson[NAME_FOR_REFERENCE_KEY] = item.displayName
                            itemJson[CS_PARENT_NODE_KEY] = item.csParentNode  // 자신만의 것
                            itemJson[CHANCE_KEY] = item.chance
                        }
                        else -> {
                            // TODO: 로그 남기기
                        }
                    }

                    // Json으로 객체 다 직렬화 한 후, 자신의 parentNode의 childNodeList에 자신을 추가
                    categoryChildJsonArray.add(itemJson)
                }
                rankChildJsonArray.add(categoryJson)
            }
            rootJsonArray.add(rankJson)
        }
        val gsonBuilder = GsonBuilder().setPrettyPrinting().create()
        val finalJson = gsonBuilder.toJson(rootJsonArray)

//        val bWriter = BufferedWriter(FileWriter(dataFile))
        val bWriter = BufferedWriter(OutputStreamWriter(FileOutputStream(dataFile), "EUC-KR"))
        try {
            bWriter.write(finalJson)
            bWriter.flush()
        } catch(ignored: IOException) {
        } finally {
            bWriter.close()
        }
    }

    fun loadAll() {
        createFilesIfNotExist()
        try {
            val allLines = FileUtils.readLines(dataFile, "EUC-KR")
            val strBuilder = StringBuilder()
            allLines.forEach { strBuilder.append(it) }
            val simplifiedJsonStr = strBuilder.toString()  // 1줄로 바꾼 형태가 simplified

            val rootJsonArray = JSONParser().parse(simplifiedJsonStr) as JSONArray
            // 사실상 save의 오른쪽 -> 왼쪽 이랑 비슷한데, 이건 객체 만들면서 알아서 parentNode의 childNodeList에 추가해줌
            for (rankJson in rootJsonArray.map { it as JSONObject }) {
                val nodeRankObj = NodeRank(rankJson[NAME_KEY] as String, rankJson[CHANCE_KEY] as Double, arrayListOf())

                for (categoryJson in (rankJson[CHILD_NODE_LIST_KEY] as JSONArray).map { it as JSONObject }) {
                    val nodeCategory = NodeCategory(nodeRankObj, categoryJson[NAME_KEY] as String, categoryJson[CHANCE_KEY] as Double, arrayListOf())

                    for (itemJson in (categoryJson[CHILD_NODE_LIST_KEY] as JSONArray).map { it as JSONObject }) {
                        when (itemJson[NODE_ITEM_CLASS] as String) {
                            NodeItemNormal::class.java.name -> {
                                val item = fromBase64<ItemStack>(itemJson[ITEM_STACK_KEY] as String)
                                NodeItemNormal(nodeCategory, item, itemJson[CHANCE_KEY] as Double)
                            }
                            NodeItemCrackShot::class.java.name -> {
                                val csParentNode = itemJson[CS_PARENT_NODE_KEY] as String
                                NodeItemCrackShot(nodeCategory, csParentNode, itemJson[CHANCE_KEY] as Double)
                            }
                            else -> {
                                // TODO: 로그 남기기
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {  // try에서 if로 체크 생까고 어떠한 문제라도 생기면 걍 catch로 보내서 처리해
            e.printStackTrace()
            backUpData()
        }
    }



    private fun backUpData() {
        if (!backUpFolder.exists()) {
            backUpFolder.mkdir()
        }
        deleteFilesOlderThanNdays(1, backUpFolder)  // 오래된 백업 파일 정리

        // 백업 파일 생성
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd (HHmmss)")
        val backUpFile = File("$backUpFolder/${dateFormat.format(calendar.time)}.json")
        if (!backUpFile.exists()) {  // 혹시 모를 위험 방지
            Files.copy(dataFile.toPath(), backUpFile.toPath())
        }
    }

    // TODO: 작동?
    private fun deleteFilesOlderThanNdays(daysBack: Int, dir: File) {
        val listFiles = dir.listFiles()
        val purgeTime = System.currentTimeMillis() - daysBack * 24 * 60 * 60 * 1000
        if (listFiles != null && listFiles.isNotEmpty()) {
            for (listFile in listFiles) {
                if (listFile.lastModified() < purgeTime && listFiles.size > 10) {
                    listFile.delete()
                }
            }
        }
    }





    private fun createFilesIfNotExist() {
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }
        if (!dataFile.exists()) {
            dataFile.createNewFile()
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


    object DataSaveTaskRegister {
        private const val INTERVAL = 180.toLong() // sec
        private var bukkitTask: BukkitTask? = null

        // 파밍 데이터에 수정이 일어났을 시, INTERVAL 후 저장 task 등록 (1개까지만 활성 가능)
        fun tryToRegisterDataSaveTask() {
            if (bukkitTask != null) return
            bukkitTask = Bukkit.getScheduler().runTaskLater(plugin, {
                saveAll()
                bukkitTask = null
            }, INTERVAL*20)
        }
    }
}