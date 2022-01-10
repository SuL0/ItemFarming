package kr.sul.itemfarming.farming.chest_notuse

/*
object PlaceChestAutomatically {
    private const val TERM = 1*20L
    const val SKULL_META = "ItemFarming: isFarmingSkull"
    private val allBlockFaces = BlockFace.values().filter { it.modY == 0 && it != BlockFace.SELF }  // UP, DOWN, SELF는 걸러냄
//    private const val CHEST_HEAD_BASE64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzU4MzA1ZTUyN2ZjZjA4NTI1ZmRmZjA3ZTRjZDZlNzk3NzRhY2RkNDI2Y2MyNzMyN2Q0NWRmOGE1ZmY4NjQxOSJ9fX0="
    // Base64로 바꿔서 적용해봤는데, 안되길래 그냥 닉네임으로 Skull 데이터 가져옴


    init {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, {
            for (hardWorld in ClassifyWorlds.hardWorlds) {
                // p 근처에 적당한 근처 찾으면 창고 놓기
                for (p in hardWorld.players) {
                    val find = AppropriateLocFinder.find(p) ?: continue

                    // 블럭 설치를 위해 Async -> Sync
                    Bukkit.getScheduler().runTask(plugin) {
                        placeChestSkull(find)
                    }
                }
            }
        }, TERM, TERM)
    }

    private fun placeChestSkull(loc: Location) {
        loc.block.type = Material.SKULL
        val skull = loc.block.state as Skull
        skull.rawData = 1 // 머리가 땅에서 떠있지 않고 바닥에 딱 붙여있게 함
        val rand = Random.nextInt(allBlockFaces.size)
        skull.rotation = allBlockFaces[rand]
        skull.setMetadata(SKULL_META, FixedMetadataValue(plugin, true))
        skull.update(true)

        SkullCreator.blockWithName(loc.block, "MHF_Chest")
    }
}*/