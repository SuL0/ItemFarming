package kr.sul.itemdb

import kr.sul.Main.Companion.plugin
import kr.sul.servercore.file.CustomFileUtil
import kr.sul.servercore.file.SimplyBackup
import kr.sul.servercore.util.Base64Serialization
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileWriter


// 파일이 notepad상에서 수정됐을 경우 -> load
// 파일이 만약 인게임에서 수정됐을 경우 -> save
// TODO Async save 진행중에는 데이터 load 같은거 save 되기 전까지 딜레이 줘야 함
object DataManager {
    private val dataFolder = File("${plugin.dataFolder}/ITEMDB")
    private val backupFolder = File("${dataFolder.path}/backup")
    val categoryDataList = arrayListOf<CategoryData>()

    private const val KEY_ITEMSTACK = "itemStack"
    private const val KEY_DISPLAYNAME = "displayName"
    private const val KEY_LORE = "lore"

    var dataState = DataState.CAN_MODIFY
//    TODO 이걸 이용해서 load나 save에 딜레이 주기.  load가 끝나기 전에는 save 불가. save 끝나기 전에는 load 불가
    enum class DataState { CAN_MODIFY, MODIFYING }
    init {
        createFoldersIfNotExists()
        loadData()
    }

    fun loadData() {
        if (dataState == DataState.MODIFYING) {
            throw Exception("DataState가 이미 MODIFYING인 상태")
        }
        dataState = DataState.MODIFYING
        Bukkit.getScheduler().runTaskAsynchronously(plugin) {
            try {
                categoryDataList.clear()
                if (getExistingCategoryFiles() == null) return@runTaskAsynchronously
                // 카테고리(파일)
                for (dataFile in getExistingCategoryFiles()!!) {
                    val dataFileYaml = YamlConfiguration.loadConfiguration(dataFile)
                    val categoryData = CategoryData(dataFile.nameWithoutExtension)
                    categoryDataList.add(categoryData)
                    // 아이템키
                    for (itemKeyName in dataFileYaml.getKeys(false)) {
                        val itemStack: String = dataFileYaml.getString("$itemKeyName.$KEY_ITEMSTACK")
                        val displayName: String? = dataFileYaml.getString("$itemKeyName.$KEY_DISPLAYNAME")
                        val lore: List<String>? = dataFileYaml.getStringList("$itemKeyName.$KEY_LORE")

                        val itemData = ItemData(itemKeyName, Base64Serialization.fromBase64(itemStack), displayName, lore)
                        categoryData.itemDataList.add(itemData)
                    }
                }
            } catch (exception: Exception) {
                Bukkit.getServer().shutdown()
                throw Exception("Server OFF")
            }
            dataState = DataState.CAN_MODIFY
        }
    }

    fun saveData(asAsync: Boolean) {  // Command로 새로운 KeyName 추가할 때만 save
        if (dataState == DataState.MODIFYING) {
            throw Exception("DataState가 이미 MODIFYING인 상태")
        }
        dataState = DataState.MODIFYING
        Bukkit.getScheduler().runTaskAsynchronously(plugin) {
            // 코루틴 이용해서 backUp(Async) 끝나기까지 기다려주게 하면 좋을 것 같은데? https://shynixn.github.io/MCCoroutine/wiki/site/newplugin/ 3번 참조 (withContext)
            backup(asAsync)  // async 백업 끝날 때 까지 기다려야 하지 않나?
            if (categoryDataList.isNotEmpty()) {
                Bukkit.broadcastMessage("$categoryDataList")
            }
            // 카테고리(파일)
            for (categoryData in categoryDataList) {
                val dataFile = File("${dataFolder}.${categoryData.name}.yml")
                // 뭔가 잘못되긴 했는데, 그냥 파일 다시 생성해 줌
                if (!dataFile.exists()) {
                    dataFile.createNewFile()
                }
                val dataMap: HashMap<String, Map<String, Any>> = hashMapOf()  // 아이템명, ((itemStack, displayName, lore), value)
                for (itemData in categoryData.itemDataList) {
                    dataMap[itemData.keyName] = hashMapOf(Pair(KEY_ITEMSTACK, itemData.toBase64()))
                    dataMap[itemData.keyName] = hashMapOf(Pair(KEY_DISPLAYNAME, itemData.itemStack.itemMeta.displayName))
                    dataMap[itemData.keyName] = hashMapOf(Pair(KEY_LORE, itemData.itemStack.itemMeta.lore))
                }

                val options = DumperOptions()
                options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK  // BLOCK을 FLOW로 바꾸면 Json 문법으로 나옴
                options.isPrettyFlow = true
                val yaml = Yaml(options)
                val writer = FileWriter(dataFile)
                yaml.dump(dataMap, writer)
            }
            dataState = DataState.CAN_MODIFY
        }
    }

    private fun backup(asAsync: Boolean) {
        createFoldersIfNotExists()
        if (getExistingCategoryFiles() != null) {
            SimplyBackup.backupFiles(null, getExistingCategoryFiles()!!, backupFolder, true, asAsync)
            CustomFileUtil.deleteFilesOlderThanNdays(30, backupFolder, 30, asAsync)  // 오래된 백업 파일 정리
        }
    }

    fun getExistingCategoryFiles(): List<File>? {
        if (dataFolder.listFiles() == null && dataFolder.listFiles()!!.isEmpty()) {
            return null
        }
        return dataFolder.listFiles()!!.filter { it.isFile && it.extension == "yml" }
    }
    private fun createFoldersIfNotExists() {
        if (!dataFolder.exists()) {
            dataFolder.mkdir()
        }
        if (!backupFolder.exists()) {
            backupFolder.mkdir()
        }
    }
}
//
//fun main() {
//    val writer = FileWriter(File("C:/Users/User1/Desktop/백업1/test.txt"))
//    val map = hashMapOf<String, HashMap<String, Any>>()
//    map["ImKEY"] = hashMapOf(Pair("yeah", "nice"))
//    map["KEY"] = hashMapOf(Pair("yeah", listOf("im", "nice", "guy")))
//    val options = DumperOptions()
//    options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
//    options.isPrettyFlow = false
//    val yaml = Yaml(options)
//    yaml.dump(map, writer)
//}

//fun main() = runBlocking { // this: CoroutineScope
//    launch { // launch a new coroutine and continue
//        delay(1000L) // non-blocking delay for 1 second (default time unit is ms)
//        println("World!") // print after delay
//    }
//    println("Hello") // main coroutine continues while a previous one is delayed
//}


//suspend fun main() {
//    a()
//    return
//    println("/thread:${Thread.currentThread().name}")
////    withContext(Dispatchers.IO) {
////    runBlocking {
//    GlobalScope.launch {
//        delay(1000L)
//        println("Hello    /thread:${Thread.currentThread().name}")
//    }
//    println("world    /thread:${Thread.currentThread().name}")
//    Thread.sleep(2000)
//}
//
//suspend fun a() {
//    b()
//    println("a - ${Thread.currentThread().name}")
//}
//
//suspend fun b()  { // this: CoroutineScope
//    withContext(Dispatchers.IO) {
//        delay(1000)
//        println("blocking - ${Thread.currentThread().name}")
//    }
//}
