package kr.sul.itemfarming.farming.chest_notuse.shulker

/*
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
                e.damage = e.damage/2
            }
        }
    }

    @EventHandler
    fun stopShulkerShooting(e: ProjectileLaunchEvent) {
        if (e.entityType == EntityType.SHULKER_BULLET) {
            e.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onShulkerDeath(e: EntityDeathEvent) {
        if (e.isCancelled) return
        if (e.entity is Shulker) {
            e.drops.clear()
            e.droppedExp = 0
            e.entity.location.world.dropItem(e.entity.location, ItemStack(Material.SHULKER_SHELL).nameIB("&7[ &f상자 껍데기 &7]"))
        }
    }
}*/