package kr.sul.itemdb

import kr.sul.Main.Companion.plugin
import org.bukkit.Bukkit

import org.bukkit.scheduler.BukkitRunnable
import java.io.File


// 노트패드로 수정한 사항은 이쪽 object가 모두 감지해서 서버에 로드 해줌
object FileModifyingChecker {
    private val timeSinceLastChanged = hashMapOf<String, Long>()
    private val existingCategoryFiles: List<File>?
        get() = DataManager.getExistingCategoryFiles()

    init {
        logTimes()
        // 주기적으로 수정 또는 새파일 생성 확인
        object : BukkitRunnable() {
            override fun run() {
                checkIfModifiedOrCreated()
            }
        }.runTaskTimerAsynchronously(plugin, 1, 20)
    }


    fun checkIfModifiedOrCreated() {
        if (existingCategoryFiles == null) return
        val filesToMonitor = existingCategoryFiles!!
        for (file in filesToMonitor) {
            val fileName = file.name
            // 새로운 카테고리 파일 생성
            if (!timeSinceLastChanged.containsKey(fileName)) {
                sendMessageToOpPlayers("${Command.PREFIX} §6${file.name} §f파일 생성됨  §c§l(RELOAD)")
                timeSinceLastChanged[fileName] = file.lastModified()
                DataManager.loadData()
                return
            }

            // 파일이 수정됨
            if (timeSinceLastChanged[fileName]!! < file.lastModified()) {
                sendMessageToOpPlayers("${Command.PREFIX} §6${file.name} §f파일 수정됨  §c§l(RELOAD)")
                this.timeSinceLastChanged[fileName] = file.lastModified()
                DataManager.loadData()
            }
        }

        // 이전에 로드됐던 파일이 현재는 존재하지 않을 때
        for (loadedFileName in timeSinceLastChanged.keys) {
            // 파일이 삭제됨
            if (filesToMonitor.none {it.name == loadedFileName }) {
                sendMessageToOpPlayers("${Command.PREFIX} §6${loadedFileName} §f파일 삭제됨  §c§l(RELOAD)")
                this.timeSinceLastChanged.remove(loadedFileName)
                DataManager.loadData()
            }
        }
    }


    private fun logTimes() {
        val listOfFiles = existingCategoryFiles
            ?: return
        for (file in listOfFiles) {
            timeSinceLastChanged[file.name] = file.lastModified()
        }
    }

    private fun sendMessageToOpPlayers(message: String) {
        Bukkit.getOnlinePlayers().filter { it.isOp }.forEach {
            it.sendMessage(message)
        }
    }
}