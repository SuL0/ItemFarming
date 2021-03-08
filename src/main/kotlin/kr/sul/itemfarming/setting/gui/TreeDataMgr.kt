package kr.sul.itemfarming.setting.gui

import kr.sul.itemfarming.Main.Companion.plugin
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException

// Tree구조 Node와 Leaf 데이터를 관리
object TreeDataMgr {
    val rootNodeList = arrayListOf<NodeRank>()  // = RankGuiList
    private val settingFile = File("${plugin.dataFolder}/farmingSetting.yml")
    private val serializedItemFile = File("${plugin.dataFolder}/internal/serializedItem.yml")


    fun saveAll() {
        createFilesIfNotExist()
        val settingConfig = loadAsConfig(settingFile)

        // 개미굴에 물줄기 내려가듯이 저장
        for (rank in rootNodeList) {
            // rank 저장 (등급 이름)
            if (!settingConfig.isConfigurationSection(rank.name)) {
                settingConfig.createSection(rank.name)
            }
            val rankSection = settingConfig.getConfigurationSection(rank.name)
            for (category in rank.childNodeList) {
                // category 저장 (카테고리 이름)
                if (!rankSection.isConfigurationSection(category.name)) {
                    rankSection.createSection(category.name)
                }
                val categorySection = rankSection.getConfigurationSection(category.name)
                for (item in category.childNodeList) {
                    // item 저장 (${uuid},${DisplayName or Type}, 확률(Double)) : SerializedItem은 다른 파일에 uuid를 key로 삼아서 저장

                }
            }
        }

        try {
            settingConfig.save(settingFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    fun loadAll() {
        createFilesIfNotExist()
        val setting = loadAsConfig(settingFile)
        setting.getKeys(true).forEach {

        }
    }

    private fun createFilesIfNotExist() {
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }

        if (!settingFile.exists()) {
            settingFile.createNewFile()
        }
    }

    private fun loadAsConfig(file: File): YamlConfiguration {
        val config = YamlConfiguration()
        config.load(file)
        return config
    }
}