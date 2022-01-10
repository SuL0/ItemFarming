package kr.sul.itemfarming.farming.chest_notuse.shulker

//import kr.sul.itemfarming.ConfigLoader

/*
class ShulkerSpawnPoint(val spawnPoint: Location): Listener {
    private val enabled = ConfigLoader.configDataList.contains(spawnPoint.world)  // Config에서 활성화한 월드에 해당하는가
    var spawnedShulkerMob: Shulker? = null
    private var spawnedShulkerMobHealth: Int? = null

    var placedShulkerBlock: Block? = null  // 타입은 ShulkerBox 아니고 Block. 셜커당 해당 Class 한 개를 가짐

    companion object {
        const val RESPAWN_DELAY = (2*60)*20.toLong() // tick
        val SHULKER_HP = ConfigLoader.shulkerHP
    }

    init {
        if (enabled) {
            val chunkLoaded = spawnPoint.chunk.load()

            // TODO 단순히 청크 언로드 방지로 청크 문제를 해결하는 것은 좋지 않아보임.
            if (!chunkLoaded) {
                SimplyLog.log(LogLevel.ERROR_NORMAL, plugin, "${spawnPoint.x}, ${spawnPoint.y}, ${spawnPoint.z} 가 위치한 청크 로드에 실패함")
            } else {
                // 이전 서버에서 설치됐던(리붓 때문) 셜커가 있으면 삭제
                spawnPoint.getNearbyEntities(0.1, 0.1, 0.1).forEach {
                    it.remove()
                }

                // Event Register 후 셜커 스폰
                Bukkit.getPluginManager().registerEvents(this, plugin)
                spawnShulker()
            }
        }
    }



    // 셜커 스폰
    private fun spawnShulker() {
        if (spawnPoint.block.type != Material.AIR) {
            if (spawnPoint.block.state is ShulkerBox) {  // 셜커 블럭
                spawnPoint.block.type = Material.AIR
            }
            else if (spawnPoint.block.type == Material.GRASS || spawnPoint.block.type == Material.LONG_GRASS) {  // 잔디
                spawnPoint.block.type = Material.AIR
            }
            else {  // 이외
                SimplyLog.log(LogLevel.ERROR_LOW, plugin, "${spawnPoint.x}, ${spawnPoint.y}, ${spawnPoint.z} 에 이상한 블럭이 설치 돼 있음. - ${spawnPoint.block.type}")
                throw Exception("${spawnPoint.x}, ${spawnPoint.y}, ${spawnPoint.z} 에 이상한 블럭이 설치 돼 있음. - ${spawnPoint.block.type}")
            }
        }

        spawnedShulkerMob = spawnPoint.world.spawnEntity(spawnPoint, EntityType.SHULKER) as Shulker
        spawnedShulkerMobHealth = SHULKER_HP
        updateShulkerName()
    }
    private fun updateShulkerName() {
        if (spawnedShulkerMob != null) {
            spawnedShulkerMob!!.customName = "§f가디언 HP : §c${spawnedShulkerMobHealth!!.toDouble()}"
        }
    }



    /*
        데미지 상관없이 n대 때리면 Shulker 죽이기
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onDamage(e: EntityDamageEvent) {
        if (e.isCancelled) return
        if (e.entity == spawnedShulkerMob) {
            if (e.cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK ||
                    e.cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK ||
                    e.cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                onDamageOfShulker()
                e.damage = 0.0
            } else {
                e.damage = 0.0  // 투사체던 뭐던 간에 일단 '데미지'는 취소시킴
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGH)
    fun onProjectileHit(e: ProjectileHitEvent) {  // 껍질 안이든 밖이든, 투사체 맞으면 무조건 실행됨
        if (e.hitEntity != null && e.hitEntity == spawnedShulkerMob) {
            val entDamager = e.entity
            if (entDamager is Arrow || entDamager is Egg || entDamager is Snowball) {
                entDamager.remove()  // 셜커 껍질에 화살로 된 총알이 맞으면 튕겨나면서 공중에서 끼여버리고, 이렇게 될 시 onProjectileHit 이벤트를 엄청나게 call하는 문제가 발생하기 때문
                onDamageOfShulker()
            }
        }
    }
    private fun onDamageOfShulker() {
        spawnedShulkerMobHealth = spawnedShulkerMobHealth!!.minus(1)
        updateShulkerName()
        if (spawnedShulkerMobHealth!! <= 0) {
            spawnedShulkerMob!!.health = 0.0
            spawnedShulkerMobHealth = null
            spawnedShulkerMob = null
        }
    }
    //




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
            LootGUI(e.player, e.clickedBlock.location, true).open()
        }
    }



    @EventHandler(priority = EventPriority.LOW)
    fun cancelChunkUnloading(e: ChunkUnloadEvent) {
        if (e.isCancelled) return
        if (e.chunk == spawnPoint.chunk) {
            e.isCancelled = true
        }
    }
}*/