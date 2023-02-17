package kr.sul.itemfarming.setting

import kr.sul.itemfarming.FarmingThingConfiguration
import kr.sul.itemfarming.setting.itemchanceviewer.Viewer
import kr.sul.servercore.util.MsgPrefix
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.DefaultFor
import revxrsal.commands.bukkit.BukkitCommandActor

@Command("itemfarming")
object Command {
    @DefaultFor("itemfarming")
    fun execute(p: Player, itemChanceFilePath: String) {
        if (!p.isOp) return
        if (itemChanceFilePath == null) {
            p.sendMessage("     §6§l↓↓ ItemChance Path List ↓↓")
            FarmingThingConfiguration.allItemChances.forEach { (path, _) ->
                val jsonMsg = arrayListOf<TextComponent>().run {
                    add(TextComponent(" §7┕ /itemfarming $path").run {
                        hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent("§e 클릭해서 명령어 실행")))
                        clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/itemfarming $path")
                        this
                    })
                    this
                }
                p.sendMessage(*jsonMsg.toTypedArray())
            }
            return
        }
        val nodeData = FarmingThingConfiguration.allItemChances[itemChanceFilePath]
            ?: run {
                p.sendMessage("${MsgPrefix.get("FARMING")}§4§lINVALID PATH PASSED > '${itemChanceFilePath}'")
                return
            }
        Viewer(p, nodeData)
    }
}