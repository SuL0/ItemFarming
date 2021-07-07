package kr.sul.itemfarming.farmingshulkerbox.data

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kr.sul.itemfarming.ConfigLoader
import kr.sul.itemfarming.Main.Companion.plugin
import kr.sul.itemfarming.farmingshulkerbox.ShulkerSpawnPoint
import kr.sul.servercore.file.CustomFileUtil
import kr.sul.servercore.file.SimplyBackUp
import kr.sul.servercore.file.simplylog.LogLevel
import kr.sul.servercore.file.simplylog.SimplyLog
import org.apache.commons.io.FileUtils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

object PlacingShulkerBoxSaver {
    val shulkerBoxSpawnPoints = arrayListOf<ShulkerSpawnPoint>()  // 콘피그 저장 형태랑은 다름


    object ListenUp : Listener {
        @EventHandler(priority = EventPriority.HIGH)
        fun onPlaceShulkerBox(e: BlockPlaceEvent) {
            if (e.isCancelled) return
            if (e.player.isOp && e.block.type == Material.PURPLE_SHULKER_BOX
                        && ConfigLoader.configDataList.contains(e.block.world)) {

                val centeredLoc = e.block.location.toCenterLocation()

                if (shulkerBoxSpawnPoints.any { it.spawnPoint == centeredLoc }) {
                    e.player.sendMessage("§6§lIF: §f이미 해당 위치에 등록된 셜커가 있습니다.")
                    e.isCancelled = true
                    return
                }
                e.player.sendMessage("§6§lIF: §f셜커 상자 위치를 §a등록§f했습니다.")
                e.isCancelled = true

                Bukkit.getScheduler().runTask(plugin) {
                    val shulkerSpawnPoint = ShulkerSpawnPoint(centeredLoc)
                    shulkerBoxSpawnPoints.add(shulkerSpawnPoint)
                }
            }
        }

        @EventHandler(priority = EventPriority.HIGH)
        fun onAttackShulkerMobWithRemover(e: EntityDamageByEntityEvent) {
            if (e.isCancelled) return
            val victim = e.entity
            val damager = e.damager
            if (damager is Player && damager.isOp
                    && damager.inventory.itemInMainHand.type == Material.PURPLE_SHULKER_BOX
                    && victim.type == EntityType.SHULKER
                    && ConfigLoader.configDataList.contains(victim.world)) {

                val centeredLoc = victim.location.toCenterLocation()

                val find = shulkerBoxSpawnPoints.find { it.spawnPoint.x == centeredLoc.x && it.spawnPoint.y == centeredLoc.y && it.spawnPoint.z == centeredLoc.z }  // 그냥 spawnPoint == centeredLoc 하면 다 false로 나옴
                if (find != null) {
                    damager.sendMessage("§6§lIF: §f셜커 상자 위치를 §c삭제§f했습니다.")
                    shulkerBoxSpawnPoints.remove(find)
                    // 셜커 엔티티 죽이기
                    victim.remove()
                }
            }
        }
        @EventHandler(priority = EventPriority.HIGH)
        fun onBreakShulkerBox(e: BlockBreakEvent) {
            if (e.isCancelled) return
            if (e.player.isOp && e.block.state is ShulkerBox
                        && ConfigLoader.configDataList.contains(e.block.world)) {

                val centeredLoc = e.block.location.toCenterLocation()

                val find = shulkerBoxSpawnPoints.find { it.spawnPoint.x == centeredLoc.x && it.spawnPoint.y == centeredLoc.y && it.spawnPoint.z == centeredLoc.z }
                if (find != null) {
                    e.player.sendMessage("§6§lIF: §f셜커 상자 위치를 §c삭제§f했습니다.")
                    shulkerBoxSpawnPoints.remove(find)
                }
            }
        }

        // 등록 안된 셜커 우클릭 될 시, 바로 등록
        // 데이터가 날아간 상황을 대비
        @EventHandler(priority = EventPriority.HIGH)
        fun onOpenShulkerBox(e: PlayerInteractEvent) {
            if (e.isCancelled) return
            if (ConfigLoader.configDataList.contains(e.player.world)
                        && e.action == Action.RIGHT_CLICK_BLOCK && e.clickedBlock.state is ShulkerBox) {

                val centeredLoc = e.clickedBlock.location.toCenterLocation()

                val find = shulkerBoxSpawnPoints.find { it.spawnPoint == centeredLoc }
                if (find == null) {  // 못 찾았을 시
                    val shulkerSpawnPoint = ShulkerSpawnPoint(centeredLoc)
                    shulkerBoxSpawnPoints.add(shulkerSpawnPoint)

                    SimplyLog.log(LogLevel.ERROR_LOW, plugin,
                        "위치 ${centeredLoc.x}, ${centeredLoc.y}, ${centeredLoc.z} 에 등록이 되지 않은 셜커상자를 ${e.player.name} 이 발견")
                }
            }
        }
    }




    // shulkerbox_location_list 데이터 관리
    object DataMgr {
        private val dataFile = File("${plugin.dataFolder}/shulkerbox_location_list.json")
        private val backUpFolder = File("${plugin.dataFolder}/backup")


        fun saveAll() {
            // 데이터를 File 저장용으로 변환
            val shulkerBoxLocationsPerWorld = hashMapOf<String, ArrayList<SimpleLocation>>()  // WorldName, List<SimpleLocation>
            shulkerBoxSpawnPoints.forEach { shulkerSpawnPoint ->
                val worldName = shulkerSpawnPoint.spawnPoint.world.name
                val simpleLoc = SimpleLocation(shulkerSpawnPoint.spawnPoint.x, shulkerSpawnPoint.spawnPoint.y, shulkerSpawnPoint.spawnPoint.z)

                // shulkerBoxLocationList에 placeSimpleLoc 추가
                if (!shulkerBoxLocationsPerWorld.containsKey(worldName)) {
                    shulkerBoxLocationsPerWorld[worldName] = arrayListOf()
                }
                shulkerBoxLocationsPerWorld[worldName]!!.add(simpleLoc)
            }

            // File에 저장
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

            // 백업
            SimplyBackUp.backUpFile(null, dataFile, backUpFolder, false, asAsync = false)
            CustomFileUtil.deleteFilesOlderThanNdays(15, backUpFolder, 10, asAsync = false)
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
                    ?: return

                // File 저장용 데이터에서 실사용 데이터로 변환하기
                Bukkit.getScheduler().runTask(plugin) {
                    result.forEach { (worldName, simpleLocList) ->
                        val world: World? = Bukkit.getWorld(worldName)
                        if (world != null) {
                            simpleLocList.forEach { simpleLoc ->
                                val location = Location(world, simpleLoc.x, simpleLoc.y, simpleLoc.z)
                                // 좌표 중복 검사
                                shulkerBoxSpawnPoints.removeIf { it.spawnPoint == location }
                                // ShulkerBoxSpawnPoints 객체 생성
                                val shulkerSpawnPoint = ShulkerSpawnPoint(location)
                                shulkerBoxSpawnPoints.add(shulkerSpawnPoint)
                            }
                        }
                    }
                }

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


    data class SimpleLocation(val x: Double, val y: Double, val z: Double) {  // 셜커 상자 위치 저장 전용
        companion object {
            // 중앙 위치로 변환 후, SimpleLocation으로 저장
            fun convertFromLoc(loc: Location): SimpleLocation {
                val centerLoc = loc.toCenterLocation()
                return SimpleLocation(centerLoc.x, centerLoc.y, centerLoc.z)
            }
        }
    }
}