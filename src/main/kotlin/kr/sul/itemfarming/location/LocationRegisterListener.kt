package kr.sul.itemfarming.location

import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent

object LocationRegisterListener: Listener {
    val whereToRegister = hashMapOf<Player, LocationPool>()

    @EventHandler
    fun onChestPlace(e: BlockPlaceEvent) {
        if (!whereToRegister.containsKey(e.player) || e.block.type != Material.CHEST) {
            return
        }
        val locationPool = whereToRegister[e.player]!!
        e.isCancelled = true
        if (locationPool.locations.find { it == e.block.location } != null) {
            e.player.sendMessage("§e${locationPool.name} Pool§f에 이미 존재하는 위치입니다.")
            return
        }
        e.player.sendMessage("§e${locationPool.name} Pool§f에 등록됨")
        locationPool.locations.add(e.block.location)
        e.player.world.spawnParticle(Particle.GLOW, e.block.location.toCenterLocation(), 10, 0.1, 0.5, 0.1, 0.0)
    }
}