package kr.sul.itemfarming.scoreboard

import dev.jcsoftware.jscoreboards.JGlobalScoreboard
import kr.sul.Main.Companion.plugin
import kr.sul.servercore.player.scoreboard.GlobalCustomScoreboard
import org.bukkit.Bukkit
import org.bukkit.entity.Player

// 뽑기가 아니라 그냥 전광판 느낌임. 상자 확률이랑은 사실 별개임ㅋ
object GlobalFarmingScoreboard: GlobalCustomScoreboard {
    // https://github.com/JordanOsterberg/JScoreboards  깜빡거리는 문제는 없어 보이는데?
    // https://www.spigotmc.org/resources/scoreboard-sidebar-api.21042/
    private val playersToUseThisScoreboard = arrayListOf<Player>()
    private var leftChest = 99 // TODO
    private var leftEpicNum = 99 // TODO
    private var leftRareNum = 99 //TODO
    private var leftQuestionareMarkNum = 99 //TODO

    private val scoreboard = JGlobalScoreboard(
        {
            return@JGlobalScoreboard "§a보급상자 현황"
        }, {
            return@JGlobalScoreboard arrayListOf(
                "§7미발견 보급상자        §f: §f§l${leftChest}${kotlin.random.Random.Default.nextInt(1000)}",
                "§e남은 에픽 상자   §f: §f§l${leftEpicNum}${kotlin.random.Random.Default.nextInt(1000)}",
                "§a남은 레어 상자 §f: §f§l${leftRareNum}${kotlin.random.Random.Default.nextInt(1000)}",
                "§a남은 ??? 상자 §f: §f§l${leftQuestionareMarkNum}${kotlin.random.Random.Default.nextInt(1000)}",
                "",
                "§f변동까지 : ",
                "  §f§l${kotlin.random.Random.Default.nextInt(1000)} : ${kotlin.random.Random.Default.nextInt(1000)} : ${kotlin.random.Random.Default.nextInt(1000)}"
            )
        }
    )
    init {
        Bukkit.getScheduler().runTaskTimer(plugin, {
            for (p in playersToUseThisScoreboard) {
                scoreboard.updateScoreboard()
            }
       }, 0L, 10L)
    }

//    fun refreshScoreboard(p: Player) {
//    }

    override fun addPlayer(p: Player) {
        playersToUseThisScoreboard.add(p)
        scoreboard.addPlayer(p)
    }
    override fun removePlayer(p: Player) {
        playersToUseThisScoreboard.remove(p)
        scoreboard.removePlayer(p)
    }
}