package kr.sul.itemfarming.farmingshulkerbox

import kr.sul.itemfarming.ConfigLoader
import kr.sul.itemfarming.Main.Companion.plugin
import kr.sul.servercore.file.simplylog.LogLevel
import kr.sul.servercore.file.simplylog.SimplyLog
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.EntityType
import org.bukkit.entity.Shulker
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerInteractEvent

class ShulkerSpawnPoint(val spawnPoint: Location): Listener {
    private val enabled = ConfigLoader.configDataList.contains(spawnPoint.world)  // Config에서 활성화한 월드에 해당하는가
    private var spawnedShulkerMob: Shulker? = null
    private var placedShulkerBlock: Block? = null  // 타입은 ShulkerBox 아니고 Block. 셜커당 해당 Class 한 개를 가짐
    init {
        if (enabled) {
            // 이전 서버에서 설치됐던(리붓 때문) 셜커가 있으면 삭제
            spawnPoint.getNearbyEntities(0.1, 0.1, 0.1).forEach {
                it.remove()
            }

            // Event Register 후 셜커 스폰
            Bukkit.getPluginManager().registerEvents(this, plugin)
            spawnShulker()
        }
    }



    // 셜커 스폰
    private fun spawnShulker() {
        if (spawnPoint.block.type != Material.AIR) {
            if (spawnPoint.block.state is ShulkerBox) {  // 셜커 블럭
                spawnPoint.block.type = Material.AIR
            } else {  // 이외
                SimplyLog.log(LogLevel.ERROR_LOW, plugin, "${spawnPoint.x}, ${spawnPoint.y}, ${spawnPoint.z} 에 이상한 블럭이 설치 돼 있음. - ${spawnPoint.block.type}")
                throw Exception("${spawnPoint.x}, ${spawnPoint.y}, ${spawnPoint.z} 에 이상한 블럭이 설치 돼 있음. - ${spawnPoint.block.type}")
            }
        }

        spawnedShulkerMob = spawnPoint.world.spawnEntity(spawnPoint, EntityType.SHULKER) as Shulker
        spawnedShulkerMob!!.customName = "§c아이템을 지닌 셜커"
    }


    // 죽을 때 스케쥴러 등록해서 나중에 해당 위치에 다시 스폰
    @EventHandler(priority = EventPriority.HIGH)
    fun onShulkerDeath(e: EntityDeathEvent) {
        if (e.isCancelled) return
        if (e.entity == spawnedShulkerMob) {
            // 셜커 죽는 모션 본 후, 셜커박스 블럭(전리품) 설치
            Bukkit.getScheduler().runTaskLater(plugin, {
                spawnPoint.block.type = Material.WHITE_SHULKER_BOX
                placedShulkerBlock = spawnPoint.block
            }, 20L)

            // 셜커 몹 사후 처리
            spawnedShulkerMob = null
            Bukkit.getScheduler().runTaskLater(plugin, {
                spawnShulker()
            }, RESPAWN_DELAY)
        }
    }


    @EventHandler(priority = EventPriority.HIGH)
    fun onOpenShulkerLootBlock(e: PlayerInteractEvent) {
        if (e.isCancelled) return
        // 전리품 셜커박스 열었을 때
        if (e.action == Action.RIGHT_CLICK_BLOCK && e.clickedBlock == placedShulkerBlock) {
            // 셜커박스 블럭 없애기
            placedShulkerBlock = null
            e.clickedBlock.type = Material.AIR
            // 사라지는 파티클 효과
            e.clickedBlock.world.spawnParticle(Particle.CLOUD, e.clickedBlock.location, 5, 0.0, 0.0, 0.0, 0.1)

            // ShulkerLootInv 열어주기
            ShulkerLootInv(e.player, e.clickedBlock.location).open()
        }
    }


    companion object {
        const val RESPAWN_DELAY = (30*60)*20.toLong() // tick
    }
}