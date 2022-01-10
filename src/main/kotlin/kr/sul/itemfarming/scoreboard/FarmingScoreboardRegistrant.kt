package kr.sul.itemfarming.scoreboard

import kr.sul.servercore.player.scoreboard.ScoreboardPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent


object FarmingScoreboardRegistrant: Listener {

    // 원래는 전장 들어갈 때 해당 스코어보드 넣고, 나갈 때 빼야할 듯
    /*
    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val scoreboardP = ScoreboardPlayer.Mgr.get(e.player) ?: return
        scoreboardP.applyScoreboard(GlobalFarmingScoreboard)
    }
     */
}