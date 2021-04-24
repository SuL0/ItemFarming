package kr.sul.itemfarming.farmingshulkerbox

import kr.sul.itemfarming.Main.Companion.plugin
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.entity.EntityType
import org.bukkit.entity.Shulker
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerInteractEvent

class ShulkerSpawnPoint(private val spawnPoint: Location): Listener {
    private var spawnedShulkerMob: Shulker? = null
    private var placedShulkerBlock: Block? = null  // 타입은 ShulkerBox 아니고 Block
    init {
        spawnShulker()
    }




    private fun spawnShulker() {
        // TODO: 서버 부팅 시 shulkerBoxLocationsPerWorld에 있는 셜커와 셜커박스 모두 제거
//        // 혹시 이미 스폰된 셜커가 있으면 삭제 (보통 리붓 때문)
//        spawnPoint.getNearbyEntities(0.1, 0.1, 0.1).forEach {
//            it.remove()
//        }
//        if (spawnPoint.block.type != Material.AIR) {
//            if (spawnPoint.block.state is ShulkerBox) {  // 셜커 블럭
//                spawnPoint.block.type = Material.AIR
//            } else {  // 이외
//                throw Exception("${spawnPoint.x}, ${spawnPoint.y}, ${spawnPoint.z} 에 블럭이 설치 돼 있음. - ${spawnPoint.block.type}")
//            }
//        }
        // 셜커 스폰
        spawnedShulkerMob = spawnPoint.world.spawnEntity(spawnPoint, EntityType.SHULKER) as Shulker
        spawnedShulkerMob!!.customName = "§c아이템을 지닌 셜커"  // NOTE : 작동 확인해야 함
    }


    // 죽을 때 스케쥴러 등록해서 나중에 해당 위치에 다시 스폰
    @EventHandler(priority = EventPriority.HIGH)
    fun onShulkerDeath(e: EntityDeathEvent) {
        if (e.isCancelled) return
        if (e.entity == spawnedShulkerMob) {
            // 셜커박스 블럭 설치
            spawnPoint.block.type = Material.PURPLE_SHULKER_BOX
            placedShulkerBlock = spawnPoint.block

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
            e.clickedBlock.world.spawnParticle(Particle.END_ROD, e.clickedBlock.location, 15, 0.5, 0.5, 0.5)

            // ShulkerLootInv 열어주기
            ShulkerLootInv(e.player).open()
        }
    }


    companion object {
        const val RESPAWN_DELAY = 300*20.toLong() // tick
    }
}