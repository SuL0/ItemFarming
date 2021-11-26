package kr.sul.itemfarming.farming.shulker

import kr.sul.Main.Companion.plugin
import kr.sul.itemfarming.farming.shulker.data.PlacingShulkerBoxSaver
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.entity.Shulker
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.inventivetalent.glow.GlowAPI


object GlowNearbyShulker: Listener {
    private const val TERM = 3*20L
    private const val DISTANCE = 100

    private val currentShulkerGlowMap = hashMapOf<Player, ArrayList<Shulker>>()

    init {
        // TODO Timings에서 엄청난 렉을 유발하는데?  https://timings.aikar.co/?id=513fb818a29d404d9d5fdf818564379f
        Bukkit.getScheduler().runTaskTimer(plugin, {
            // 거리 <= DISTANCE 조건에 부합하지 않는 글로우된 셜커는 글로우 삭제
            currentShulkerGlowMap.forEach { (p, shulkerList) ->
                shulkerList.filter { !(it.world == p.world
                        && it.location.distance(p.location) <= DISTANCE) }
                        .forEach { shulkerToDeleteGlow ->
                    GlowAPI.setGlowing(shulkerToDeleteGlow, false, p)
                }
            }


            // 정상 셜커 (GOLD)
            for (shulker in PlacingShulkerBoxSaver.shulkerBoxSpawnPoints) {
                for (p in Bukkit.getOnlinePlayers().filter { it.isOp }) {
                    if (shulker.spawnPoint.world == p.world
                            && shulker.spawnPoint.distance(p.location) <= DISTANCE
                    ) {
                        if (shulker.spawnedShulkerMob != null) {  // 셜커 몹 -
                            glowShulker(p, shulker.spawnedShulkerMob!!, GlowAPI.Color.GOLD)
                        }
                        else if (shulker.placedShulkerBlock != null) {  // 셜커 블럭 -
                            // Fake Shulker Entity 소환해서 글로우 (GREEN)
//                            val packet = pm.createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING).run {
//                                uuiDs.write(0, UUID.randomUUID())
//                                integers.write(0, rand.nextInt(5000))
//                                integers.write(1, EntityType.SHULKER.typeId.toInt())
//                                doubles.write(0, shulker.placedShulkerBlock!!.x.toDouble())
//                                doubles.write(1, shulker.placedShulkerBlock!!.y.toDouble())
//                                doubles.write(2, shulker.placedShulkerBlock!!.z.toDouble())
//                                bytes.write(0, 0)
//                                bytes.write(1, 0)
//                                integers.write(2, 0)
//                                integers.write(3, 0)
//                                integers.write(4, 0)
//                                this
//                            }
//                            pm.sendServerPacket(p, packet)
                        }
                    }
                }
            }

            // 야생의 비정상 셜커 (RED)
            for (p in Bukkit.getOnlinePlayers().filter { it.isOp }) {
                for (nearbyShulker in p.getNearbyEntities(DISTANCE -1.0, DISTANCE -1.0, DISTANCE -1.0).filterIsInstance<Shulker>()) {
                    if (currentShulkerGlowMap[p]?.contains(nearbyShulker) == false) {
                        glowShulker(p, nearbyShulker, GlowAPI.Color.DARK_RED)
                    }
                }
            }
        }, TERM, TERM)
    }

    private fun glowShulker(p: Player, shulker: Shulker, color: GlowAPI.Color) {
        GlowAPI.setGlowing(shulker, color, p)
        if (!currentShulkerGlowMap.containsKey(p)) {
            currentShulkerGlowMap[p] = arrayListOf()
        }
        currentShulkerGlowMap[p]!!.add(shulker)
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        currentShulkerGlowMap[e.player]?.forEach {
            if (!it.isDead) {
                GlowAPI.setGlowing(it, false, e.player)
            }
        }
        currentShulkerGlowMap.remove(e.player)
    }
}