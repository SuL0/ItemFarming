package kr.sul.itemfarming.setting.gui.guimoderator

import kr.sul.itemfarming.Main
import kr.sul.servercore.util.ItemBuilder.loreIB
import kr.sul.servercore.util.ItemBuilder.nameIB
import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import java.util.function.Consumer

object AnvilGuiModerator {
    private val itemRight = run {
        val item = ItemStack(Material.ENCHANTED_BOOK)
            .nameIB("§6§lTIP: ")
            .loreIB(" §7└ §f텍스트 창 클릭 §7-> §fCtrl+A §7-> §f입력", 2)
        val meta: EnchantmentStorageMeta = item.itemMeta as EnchantmentStorageMeta
        meta.addStoredEnchant(Enchantment.LOOT_BONUS_BLOCKS, 1, true)
        item.itemMeta = meta
        return@run item
    }

    // 키 입력 받아서 수행할 method를 parameter에 첨부
    fun open(p: Player, text: String, runAfterGettingInput: Consumer<String>, onClose: Runnable) {
        if (p.gameMode != GameMode.CREATIVE) {
            p.gameMode = GameMode.CREATIVE
            p.sendMessage("§6§lIF: §7Anvil 사용을 위해 Creative로 변경했습니다.")
        }
        AnvilGUI.Builder()
            .itemLeft(ItemStack(Material.BOOK_AND_QUILL))
            .itemRight(itemRight)
            .text(text)
            .onComplete { _, string ->
                runAfterGettingInput.accept(string)
                return@onComplete AnvilGUI.Response.close()
            }
            .onClose {
                onClose.run()
            }
            .plugin(Main.plugin)
            .open(p)
    }

}