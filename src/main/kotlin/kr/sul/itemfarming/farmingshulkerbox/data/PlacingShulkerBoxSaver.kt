package kr.sul.itemfarming.farmingshulkerbox.data

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kr.sul.itemfarming.Main.Companion.plugin
import org.apache.commons.io.FileUtils
import org.bukkit.block.ShulkerBox
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

object PlacingShulkerBoxSaver {
    object ListenUp : Listener {
        @EventHandler(priority = EventPriority.HIGH)
        fun onPlaceShulkerBox(e: BlockPlaceEvent) {
            if (e.isCancelled) return
            if (e.player.isOp && e.block.state is ShulkerBox
                        && DataStorage.activeWorlds.contains(e.block.world)) {

                val worldName = e.block.world.name
                val placeSimpleLoc = DataStorage.SimpleLocation.convertFromLoc(e.block.location)

                e.player.sendMessage("§6§lIF: §f셜커 상자 위치를 §a등록§f했습니다.")
                // shulkerBoxLocationList에 placeSimpleLoc 추가
                if (!DataStorage.shulkerBoxLocationsPerWorld.containsKey(worldName)) {
                    DataStorage.shulkerBoxLocationsPerWorld[worldName] = arrayListOf()
                }
                DataStorage.shulkerBoxLocationsPerWorld[worldName]!!.add(placeSimpleLoc)
            }
        }

        @EventHandler(priority = EventPriority.HIGH)
        fun onBreakShulkerBox(e: BlockBreakEvent) {
            if (e.isCancelled) return
            if (e.player.isOp && e.block.state is ShulkerBox
                        && DataStorage.activeWorlds.contains(e.block.world)) {

                val worldName = e.block.world.name
                val brokeSimpleLoc = DataStorage.SimpleLocation.convertFromLoc(e.block.location)

                if (DataStorage.shulkerBoxLocationsPerWorld[worldName]?.contains(brokeSimpleLoc) == true) {
                    e.player.sendMessage("§6§lIF: §f셜커 상자 위치를 §c삭제§f했습니다.")
                    DataStorage.shulkerBoxLocationsPerWorld[worldName]!!.remove(brokeSimpleLoc)
                    // arrayList 비었을 시, 월드 Key 삭제
                    if (DataStorage.shulkerBoxLocationsPerWorld[worldName]!!.size == 0) {
                        DataStorage.shulkerBoxLocationsPerWorld.remove(worldName)
                    }
                }
            }
        }

        // 등록 안된 셜커 우클릭 될 시, 바로 등록
        // 이것이 있기에, 데이터가 날아가거나 등록이 제대로 안됐다 해도 문제 X
        @EventHandler(priority = EventPriority.HIGH)
        fun onOpenShulkerBox(e: PlayerInteractEvent) {
            if (e.isCancelled) return
            if (DataStorage.activeWorlds.contains(e.player.world)
                        && e.action == Action.RIGHT_CLICK_BLOCK && e.clickedBlock.state is ShulkerBox) {

                val worldName = e.clickedBlock.world.name
                val shulkerSimpleLoc = DataStorage.SimpleLocation.convertFromLoc(e.clickedBlock.location)
                if (DataStorage.shulkerBoxLocationsPerWorld[worldName]?.contains(shulkerSimpleLoc) != true) {
                    DataStorage.shulkerBoxLocationsPerWorld[worldName]!!.add(shulkerSimpleLoc)
                    // TODO: LogToFile 이용해서 로그 찍기
                }
            }
        }
    }





    object DataMgr {
        private val dataFile = File("${plugin.dataFolder}/shulkerbox_location_list.json")

        fun saveAll() {
            createFilesIfNotExists()
            val gson = GsonBuilder().setPrettyPrinting().create()
            val finalJson = gson.toJson(DataStorage.shulkerBoxLocationsPerWorld)

            val bWriter = BufferedWriter(FileWriter(dataFile))
            try {
                bWriter.write(finalJson)
                bWriter.flush()
            } catch (ignored: IOException) {
            } finally {
                bWriter.close()
            }
        }

        fun loadAll() {
            createFilesIfNotExists()
            try {
                val allLines = FileUtils.readLines(dataFile, "EUC-KR")
                val strBuilder = StringBuilder()
                allLines.forEach { strBuilder.append(it) }
                val simplifiedJsonStr = strBuilder.toString()  // 1줄로 바꾼 형태가 simplified

                val gson = GsonBuilder().setPrettyPrinting().create()
                val myType = object : TypeToken<HashMap<String, ArrayList<DataStorage.SimpleLocation>>>() {}.type  // 대충 뭔가를 우회한다고 이런 방식 쓴다고 함. 이건 일반적인 방법으론 객체 생성이 막혀있어서 익명객체 쓴 듯?  자세한건 https://namocom.tistory.com/671
                val result = gson.fromJson<HashMap<String, ArrayList<DataStorage.SimpleLocation>>>(simplifiedJsonStr, myType)
                DataStorage.shulkerBoxLocationsPerWorld.putAll(result)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        private fun createFilesIfNotExists() {
            if (!plugin.dataFolder.exists()) {
                plugin.dataFolder.mkdirs()
            }
            if (!dataFile.exists()) {
                dataFile.createNewFile()
            }
        }
    }
}