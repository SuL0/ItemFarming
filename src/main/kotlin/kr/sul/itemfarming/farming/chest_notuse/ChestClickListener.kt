package kr.sul.itemfarming.farming.chest_notuse

/*
object ChestClickListener: Listener {
    @EventHandler(priority = EventPriority.HIGH)
    fun onOpenShulkerLootBlock(e: PlayerInteractEvent) {
        if (e.isCancelled) return
        // 전리품 셜커박스 열었을 때
        if (e.action == Action.RIGHT_CLICK_BLOCK && e.clickedBlock.type == Material.SKULL) {
            val skull = e.clickedBlock.state as Skull
            if (skull.hasMetadata(PlaceChestAutomatically.SKULL_META)) {
                // 상자 Skull 블럭 없애기
                e.clickedBlock.type = Material.AIR
                // 사라지는 파티클 효과
                e.clickedBlock.world.spawnParticle(Particle.CLOUD, e.clickedBlock.location, 5, 0.0, 0.0, 0.0, 0.1)

                // LootInv 열어주기
                LootGUI(e.player, e.clickedBlock.location, false).open()
            }
        }
    }
}*/