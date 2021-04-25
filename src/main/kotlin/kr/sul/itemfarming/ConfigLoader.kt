package kr.sul.itemfarming

import kr.sul.itemfarming.Main.Companion.plugin
import org.bukkit.Bukkit
import org.bukkit.World
import kotlin.random.Random

object ConfigLoader {
    val activeWorlds = arrayListOf<World>()  // 셜커 블럭 설치 Listen 용, shulkerBoxLocationsPerWorld 에서 활성 비활성 거르는용

    // ConfigLoader에서 덮어씀
    lateinit var categoryDropNumRange: DropNumRange
    lateinit var itemDropNumRange: DropNumRange
    //


    data class DropNumRange(val min: Int, val max:Int) {
        fun random(): Int {
            return Random.nextInt(min, max+1)
        }
    }




    init {
        plugin.saveDefaultConfig()
    }

    // config 불러오기
    fun loadConfig() {
        val config = plugin.config
        Bukkit.getScheduler().runTask(plugin) {
            val itemFarmingSection = config.getConfigurationSection("아이템파밍")
            itemFarmingSection.getStringList("적용할월드_목록").forEach { world ->
                if (Bukkit.getWorld(world) != null) {
                    activeWorlds.add(Bukkit.getWorld(world))  // 셜커 블럭 설치 Listen 용, shulkerBoxLocationsPerWorld 에서 활성 비활성 거르는용
                }
            }

            val dropNumRangeSection = itemFarmingSection.getConfigurationSection("아이템_개수_범위")
            dropNumRangeSection.getString("카테고리").run {
                val split = this.split("~")
                try {
                    val min = split[0].toInt()
                    val max = split[1].toInt()
                    categoryDropNumRange = DropNumRange(min, max)
                } catch (ignored: NumberFormatException) {
                }
            }
            dropNumRangeSection.getString("아이템").run {
                val split = this.split("~")
                try {
                    val min = split[0].toInt()
                    val max = split[1].toInt()
                    itemDropNumRange = DropNumRange(min, max)
                } catch (ignored: NumberFormatException) {
                }
            }
        }
    }
}