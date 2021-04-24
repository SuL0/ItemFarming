package kr.sul.itemfarming.farmingshulkerbox.data

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kr.sul.itemfarming.Main.Companion.plugin
import kr.sul.itemfarming.farmingshulkerbox.FarmingShulkerBox
import org.apache.commons.io.FileUtils
import org.bukkit.Location
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
    private val shulkerBoxLocationsPerWorld = hashMapOf<String, ArrayList<SimpleLocation>>()  // WorldName, List<SimpleLocation>  - Json으로 바꿨을 때 깔끔한 형태

    private data class SimpleLocation(val x: Double, val y: Double, val z: Double) {  // 셜커 상자 위치 저장 전용
        companion object {
            // 중앙 위치로 변환 후, SimpleLocation으로 저장
            fun convertFromLoc(loc: Location): SimpleLocation {
                val centerLoc = loc.toCenterLocation()
                return SimpleLocation(centerLoc.x, centerLoc.y, centerLoc.z)
            }
        }
    }

    object ListenUp : Listener {
        @EventHandler(priority = EventPriority.HIGH)
        fun onPlaceShulkerBox(e: BlockPlaceEvent) {
            if (e.isCancelled) return
            if (e.player.isOp && e.block.state is ShulkerBox
                        && FarmingShulkerBox.activeWorlds.contains(e.block.world)) {

                val worldName = e.block.world.name
                val placeSimpleLoc = SimpleLocation.convertFromLoc(e.block.location)

                e.player.sendMessage("§6§lIF: §f셜커 상자 위치를 §a등록§f했습니다.")
                // shulkerBoxLocationList에 placeSimpleLoc 추가
                if (!shulkerBoxLocationsPerWorld.containsKey(worldName)) {
                    shulkerBoxLocationsPerWorld[worldName] = arrayListOf()
                }
                shulkerBoxLocationsPerWorld[worldName]!!.add(placeSimpleLoc)
            }
        }

        @EventHandler(priority = EventPriority.HIGH)
        fun onBreakShulkerBox(e: BlockBreakEvent) {
            if (e.isCancelled) return
            if (e.player.isOp && e.block.state is ShulkerBox
                        && FarmingShulkerBox.activeWorlds.contains(e.block.world)) {

                val worldName = e.block.world.name
                val brokeSimpleLoc = SimpleLocation.convertFromLoc(e.block.location)

                if (shulkerBoxLocationsPerWorld[worldName]?.contains(brokeSimpleLoc) == true) {
                    e.player.sendMessage("§6§lIF: §f셜커 상자 위치를 §c삭제§f했습니다.")
                    shulkerBoxLocationsPerWorld[worldName]!!.remove(brokeSimpleLoc)
                    // arrayList 비었을 시, 월드 Key 삭제
                    if (shulkerBoxLocationsPerWorld[worldName]!!.size == 0) {
                        shulkerBoxLocationsPerWorld.remove(worldName)
                    }
                }
            }
        }

        // 등록 안된 셜커 우클릭 될 시, 바로 등록
        // 이것이 있기에, 데이터가 날아가거나 등록이 제대로 안됐다 해도 문제 X
        @EventHandler(priority = EventPriority.HIGH)
        fun onOpenShulkerBox(e: PlayerInteractEvent) {
            if (e.isCancelled) return
            if (FarmingShulkerBox.activeWorlds.contains(e.player.world)
                        && e.action == Action.RIGHT_CLICK_BLOCK && e.clickedBlock.state is ShulkerBox) {

                val worldName = e.clickedBlock.world.name
                val shulkerSimpleLoc = SimpleLocation.convertFromLoc(e.clickedBlock.location)
                if (shulkerBoxLocationsPerWorld[worldName]?.contains(shulkerSimpleLoc) != true) {
                    shulkerBoxLocationsPerWorld[worldName]!!.add(shulkerSimpleLoc)
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
            val finalJson = gson.toJson(shulkerBoxLocationsPerWorld)

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
                val myType = object : TypeToken<HashMap<String, ArrayList<SimpleLocation>>>() {}.type  // 대충 뭔가를 우회한다고 이런 방식 쓴다고 함. 이건 일반적인 방법으론 객체 생성이 막혀있어서 익명객체 쓴 듯?  자세한건 https://namocom.tistory.com/671
                val result = gson.fromJson<HashMap<String, ArrayList<SimpleLocation>>>(simplifiedJsonStr, myType)
                shulkerBoxLocationsPerWorld.putAll(result)
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