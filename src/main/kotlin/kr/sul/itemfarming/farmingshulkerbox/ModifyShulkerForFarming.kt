package kr.sul.itemfarming.farmingshulkerbox

import com.shampaggon.crackshot2.CSDirector
import com.shampaggon.crackshot2.events.WeaponDamageEntityEvent
import kr.sul.itemfarming.Main.Companion.plugin
import org.bukkit.Bukkit
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityTeleportEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
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

    // 맞으면 무조건 실행됨
    // 데미지 2/3
    @EventHandler
    fun onProjectileHit(e: ProjectileHitEvent) {
        if (e.hitEntity != null && e.hitEntity.type == EntityType.SHULKER) {
            val entDmger = e.entity
            // Check if it is crackshot bullet
            if ((entDmger is Arrow || entDmger is Egg || entDmger is Snowball) && entDmger.hasMetadata("projParentNode") && e.hitEntity is LivingEntity) {
                val parentNode = (entDmger.getMetadata("projParentNode")[0]).asString()
                var damage = CSDirector.getInstance().getInt("$parentNode.Shooting.Projectile_Damage")
                if (damage < 0) {
                    damage = 0
                }
                (e.hitEntity as Damageable).damage(damage*0.7+0.01)  // .999999 뜨길래 +0.01
            }
        }
    }

    @EventHandler
    // 셜커 껍질 밖에 있으면 실행됨
    // 데미지 1/3
    // 정리해서 껍질 안에 있을 때 : 데미지의 2/3
    // 껍질 밖에 있을 때 : 데미지의 3/3
    fun onWeaponDamage(e: WeaponDamageEntityEvent) {
        if (e.victim.type == EntityType.SHULKER) {
            e.damage = e.damage * 0.3 +0.01
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
}