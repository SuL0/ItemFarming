package kr.sul.itemfarming

import com.google.gson.GsonBuilder
import kr.sul.Main.Companion.plugin
import kr.sul.itemfarming.farming.FarmingThing
import kr.sul.itemfarming.farming.ItemChanceWrapper
import kr.sul.itemfarming.farming.ItemContainer
import kr.sul.itemfarming.farming.LocationPool
import kr.sul.itemfarming.setting.itemchance.NodeData
import kr.sul.itemfarming.setting.itemchance.NodeDataJsonConverter
import kr.sul.servercore.util.KeepExceptionAlert
import org.apache.commons.io.FileUtils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.*


// TODO 백업
object FarmingThingConfiguration: Listener {
    private val farmingThingData = File("${plugin.dataFolder}/farming_things.yml")
    private val locationPoolFolder = File("${plugin.dataFolder}/location_pool")
    private val itemChanceFolder = File("${plugin.dataFolder}/item_chance")

    private val allFarmingThings = arrayListOf<FarmingThing>()

    val allItemChances = hashMapOf<String, NodeData>()  // key: fileName
    private val allLocationPools = hashMapOf<String, LocationPool>()  // key: fileName

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
        arrayOf(locationPoolFolder to "-example_locations.json", itemChanceFolder to "-example_item_chance.json").forEach { ( folder, fileName) ->
            if (!folder.exists()) {
                folder.mkdirs()
                val inputStream = plugin.getResource(fileName)
                FileUtils.copyInputStreamToFile(inputStream, File("${folder.path}/${fileName}"))
            }
        }
    }

    @EventHandler
    fun onDisable(e: PluginDisableEvent) {
        allFarmingThings.forEach { it.destroy() }
        allItemChances.forEach { (fileName, node) ->
            saveItemChance(node, File("${itemChanceFolder}/${fileName}"))
        }
    }
    fun onReloadConfiguration() {
        allFarmingThings.forEach { it.destroy() }
        allItemChances.forEach { (fileName, node) ->
            saveItemChance(node, File("${itemChanceFolder}/${fileName}"))
        }
        allFarmingThings.clear()
        allItemChances.clear()
        allLocationPools.clear()
        initializeFromConfiguration()
    }

    private fun saveItemChance(node: NodeData, file: File) {
        val nodeToJson = node.convertToJson()
        val finalJson = GsonBuilder().setPrettyPrinting().create()
            .toJson(nodeToJson)

        // Writer은 기존의 파일 내용을 모두 무로 만든 후, 파일을 처음부터 새로 쓰기 시작함. 따라서 Writer 생성하고 바로 close()할 시 파일의 모든 내용을 지우게 됨.
        val bWriter = BufferedWriter(OutputStreamWriter(FileOutputStream(file), "UTF-8"))
        try {
            bWriter.write(finalJson)
            bWriter.flush()
        } catch(ignored: IOException) {
        } finally {
            bWriter.close()
        }
    }
    fun initializeFromConfiguration() {
        Bukkit.getScheduler().runTaskLater(plugin, {
            try {
                locationPoolFolder.listFiles(FileFilter { it.isFile && it.name.endsWith("json") && !it.name.startsWith("-") })?.forEach { file ->
                    loadLocationPool(file)
                }
                itemChanceFolder.listFiles(FileFilter { it.isFile && it.name.endsWith("json") && !it.name.startsWith("-") })?.forEach { file ->
                    loadItemChance(file)
                }
                loadFarmingThing(YamlConfiguration.loadConfiguration(farmingThingData))
            } catch (e: Exception) {
                // 데이터 불러오기 실패
                KeepExceptionAlert.alert(e, "[ItemFarming] 데이터를 불러오는 데 실패했습니다 ${e.message} | ${e.cause}", 100L)
            }
        }, 1) // world 때문
    }
    private fun loadLocationPool(file: File) {
        val jsonObject = readFileAndParseAsJsonThing<JSONObject>(file)
        val locations = (jsonObject["locations"] as JSONArray).map { it as JSONObject }.map {
            Location(Bukkit.getWorld(it["world"] as String), it["x"].toString().toDouble(), it["y"].toString().toDouble(), it["z"].toString().toDouble())
        }
        allLocationPools[file.name] = LocationPool(locations)
    }
    private fun loadItemChance(file: File) {
        allItemChances[file.name] = NodeDataJsonConverter.importJson(readJsonFile(file))
    }
    private fun loadFarmingThing(yamlConfiguration: YamlConfiguration) {
        yamlConfiguration.run {
            // calculate the amount (auto때문에 먼저 같은 locationPool을 쓰는 것들끼리 모아서 auto가 값을 몇을 가지게 될지를 계산해야하기 때문)
            val mapForCalcAmount = hashMapOf<LocationPool, AmountDistributionCalculator>()
            getKeys(false).forEach { parentNode ->
                val amountStr = getString("${parentNode}.amount")
                val locationPool = allLocationPools[getString("${parentNode}.locations_file")]!!
                if (!mapForCalcAmount.containsKey(locationPool)) {
                    mapForCalcAmount[locationPool] = AmountDistributionCalculator(locationPool.locations.size)
                }
                mapForCalcAmount[locationPool]!!.register(amountStr)
            }

            // create farming thing instance
            getKeys(false).forEach { parentNode ->
                val amountStr = getString("${parentNode}.amount")
                val locationPool = allLocationPools[getString("${parentNode}.locations_file")]!!
                val amount = mapForCalcAmount[locationPool]!!.getAmount(amountStr)
                for (i in 0 until amount) {

                    val farmingThing = FarmingThing(
                        ItemContainer.BlockType(
                            null,
                            Material.getMaterial(
                                getString("${parentNode}.material")
                            )
                        ),
                        ItemChanceWrapper(
                            allItemChances[getString("${parentNode}.item_chance_file")]!!
                        ),
                        locationPool,
                        getLong("${parentNode}.fill_items_cooldown_term"),
                        getString("${parentNode}.message_when_it_opened"),
                        Sound.valueOf(getString("${parentNode}.sound_when_it_opened")),
                        getBoolean("${parentNode}.make_item_container_despawned_when_it_opened"),
                        getBoolean("${parentNode}.make_item_container_move_when_it_opened"),
                    )
                    allFarmingThings.add(farmingThing)
                }
            }
        }
    }

    private fun<T> readFileAndParseAsJsonThing(file: File): T {
        val simplifiedJsonStr = readJsonFile(file)
        return JSONParser().parse(simplifiedJsonStr) as T
    }
    private fun readJsonFile(file: File): String {
        val allLines = FileUtils.readLines(file, "UTF-8")
        val strBuilder = StringBuilder()
        allLines.forEach { strBuilder.append(it) }
        return strBuilder.toString()
    }

    // 결국 ratio랑 value는 계산기 필요없이 바로 값이 나오는데, auto때문에 이게 필요한거
    // 근데 총량이 100%를 초과하는지는 확인해볼 필요가 있겠는데
    class AmountDistributionCalculator(private val totalAmount: Int) {
        private var accumulatorWithoutAuto = 0
        private var usingAutoCount = 0
        private var exceptionAlerted = false
        fun register(amountStr: String) {
            if (amountStr.lowercase() == "auto") {
                usingAutoCount+=1
            } else {
                val amountParsed = parseStr(amountStr)
                if (amountStr.endsWith("%")) {
                    accumulatorWithoutAuto += (amountParsed/100)*totalAmount
                } else {
                    accumulatorWithoutAuto += amountParsed
                }
            }
        }

        // 배분된 값을 꺼내가는 것
        fun getAmount(amountStr: String): Int {
            // auto를 제외한 값만으로도 100%를 초과하는 상황 (에러)
            if (accumulatorWithoutAuto > totalAmount) {
                if (!exceptionAlerted) {
                    exceptionAlerted = true
                    KeepExceptionAlert.alert(null, "FarmingThing의 amount 설정이 100%를 초과하였음. > accumulatorWithoutAuto: $accumulatorWithoutAuto, usingAuto: $usingAutoCount", 100L)
                }
                return if (amountStr.lowercase() == "auto") {
                    0
                } else if (amountStr.endsWith("%")) {
                    ((parseStr(amountStr)/100.0)*totalAmount * (100.0/accumulatorWithoutAuto)).toInt()   // (100/accumulatorWithoutAuto) 을 통해 100%를 넘는 값을 임시로 보정
                } else {
                    (parseStr(amountStr) * (100.0/accumulatorWithoutAuto)).toInt()
                }
            }
            return if (amountStr.lowercase() == "auto") {
                accumulatorWithoutAuto/usingAutoCount
            } else if (amountStr.endsWith("%")) {
                ((parseStr(amountStr)/100.0)*totalAmount).toInt()
            } else {
                parseStr(amountStr)
            }
        }
        private fun parseStr(amountStr: String): Int {
            return if (amountStr.endsWith("%")) {
                amountStr.replace("%", "").toInt()
            } else {
                amountStr.toInt()
            }
        }
    }
}