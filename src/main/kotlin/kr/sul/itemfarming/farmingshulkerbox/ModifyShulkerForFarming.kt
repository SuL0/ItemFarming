package kr.sul.itemfarming.farmingshulkerbox

import kr.sul.itemfarming.Main.Companion.plugin
import kr.sul.servercore.util.ItemBuilder.nameIB
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType

object ModifyShulkerForFarming : Listener {
    @EventHandler(priority = EventPriority.LOW)
    fun onEntityTeleport(e: EntityTeleportEvent) {
        if (e.entityType == EntityType.SHULKER) {
            e.isCancelled = true
        }
    }

    // Levitation 포션효과 제거
    @EventHandler
    fun onDamage(e: EntityDamageByEntityEvent) {
        if (e.damager.type == EntityType.SHULKER_BULLET && e.entity is LivingEntity) {
            Bukkit.getScheduler().runTask(plugin) {
                (e.entity as LivingEntity).removePotionEffect(PotionEffectType.LEVITATION)
            }
        }
    }

    // SHULKER_BULLET의 최대 생존시간은 10초
    @EventHandler
    fun onShoot(e: ProjectileLaunchEvent) {
        if (e.entityType == EntityType.SHULKER_BULLET) {
            val entity = e.entity
            Bukkit.getScheduler().runTaskLater(plugin, {
                if (!entity.isDead) {
                    entity.remove()
                }
            }, 200L)
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onShulkerDeath(e: EntityDeathEvent) {
        if (e.isCancelled) return
        e.entity.location.world.dropItem(e.entity.location, ItemStack(Material.SHULKER_SHELL).nameIB("&7[ &f셜커껍데기 &7]"))
    }
}