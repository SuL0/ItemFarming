package kr.sul.itemfarming.setting

import kr.sul.itemfarming.setting.gui.NodeRank
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object Command : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            return true
        }
        NodeRank.NodeRankListMgr.openRankListGui(sender)
        return true
    }
}