package kr.sul.itemfarming.setting.gui.guimoderator

import kr.sul.itemfarming.Main.Companion.plugin
import kr.sul.servercore.usefulgui.ConfirmGui
import kr.sul.servercore.util.ItemBuilder.loreIB
import kr.sul.servercore.util.ItemBuilder.nameIB
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

object ConfirmGuiModerator: Listener {
    private const val GUI_NAME_PREFIX = "§f!IF:"
    private val ASK_ITEM_MATERIAL = Material.TNT

    fun open(p: Player, askTitle: String, askLoreList: List<String>?, onConfirm: Runnable, onCloseOrDeny: Runnable) {
        val askItem = ItemStack(ASK_ITEM_MATERIAL).nameIB("§6§l[!] §f$askTitle")
        askLoreList?.forEach {
            askItem.loreIB(it, 2)
        }
        askItem.loreIB("").loreIB("§f아래의 버튼을 통해 수락해주십시오.", 2)
        ConfirmGui.Builder()
            .title("$GUI_NAME_PREFIX $askTitle")
            .askItem(askItem)
            .onConfirm(onConfirm)
            .onDeny(onCloseOrDeny)
            .onClose(onCloseOrDeny)
            .plugin(plugin)
            .open(p)
    }
}