package kr.sul.itemfarming

import kr.sul.itemfarming.Main.Companion.plugin
import kr.sul.itemfarming.farmingshulker.FarmingShulker
import org.bukkit.Bukkit

object ConfigLoader {
    init {
        plugin.saveDefaultConfig()
    }

    // config 불러오기
    fun loadConfig() {
        val config = plugin.config
        Bukkit.getScheduler().runTask(plugin) {
            val farmingShulkerSection = config.getConfigurationSection("파밍셜커")
            farmingShulkerSection.getStringList("적용할월드_목록").forEach {
                if (Bukkit.getWorld(it) != null) {
                    FarmingShulker.activeWorlds.add(Bukkit.getWorld(it))
                }
            }

            val dropNumRangeSection = farmingShulkerSection.getConfigurationSection("아이템_개수_범위")
            dropNumRangeSection.getString("카테고리").run {
                val split = this.split("~")
                try {
                    val min = split[0].toInt()
                    val max = split[1].toInt()
                    FarmingShulker.categoryDropNumRange = FarmingShulker.DropNumRange(min, max)
                } catch (ignored: NumberFormatException) {
                }
            }
            dropNumRangeSection.getString("아이템").run {
                val split = this.split("~")
                try {
                    val min = split[0].toInt()
                    val max = split[1].toInt()
                    FarmingShulker.itemDropNumRange = FarmingShulker.DropNumRange(min, max)
                } catch (ignored: NumberFormatException) {
                }
            }
        }
    }
}