package kr.sul.itemfarming

import kr.sul.itemfarming.Main.Companion.plugin
import kr.sul.servercore.file.simplylog.LogLevel
import kr.sul.servercore.file.simplylog.SimplyLog
import org.bukkit.Bukkit
import org.bukkit.World

object ConfigLoader {
    // World는 셜커 블럭 설치 Listen 용, shulkerBoxLocationsPerWorld 에서 활성 비활성 거르는용
    val configDataList = hashMapOf<World, ConfigData>()


    init {
        plugin.saveDefaultConfig()
    }

    // config 불러오기
    fun loadConfig() {
        val config = plugin.config
        Bukkit.getScheduler().runTask(plugin) {
            for (worldName in config.getKeys(false)) {
                val world = Bukkit.getWorld(worldName)
                val categoryNumRange = ConfigData.DropNumRange.convert(config.getString("$worldName.카테고리"))
                val itemNumRange = ConfigData.DropNumRange.convert(config.getString("$worldName.아이템"))

                if (Bukkit.getWorld(worldName) == null) {
                    SimplyLog.log(LogLevel.ERROR_NORMAL, plugin, "ItemFarming에 등록된 월드 $worldName 이 서버에 존재하지 않는 월드임")
                    continue
                }
                if (categoryNumRange == null || itemNumRange == null) {
                    SimplyLog.log(LogLevel.ERROR_NORMAL, plugin, "ItemFarming의 $worldName.categoryNumRange 또는 $worldName.itemNumRange 의 값이 양식에 맞지 않음")
                    continue
                }

                configDataList[world] = ConfigData(world, categoryNumRange, itemNumRange)
            }

        }
    }
}