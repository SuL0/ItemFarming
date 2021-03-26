package kr.sul.itemfarming.setting.gui

import kr.sul.itemfarming.Main
import kr.sul.servercore.util.ItemBuilder.clearLoreIB
import kr.sul.servercore.util.ItemBuilder.loreIB
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue

object TreeUtil {

    object ForCommon {
        fun deleteAllLoreExceptFirstLine(item: ItemStack) {
            val firstLore = item.itemMeta.lore[0]
            item.clearLoreIB()
            item.loreIB(firstLore)
        }
    }



    // ViewingGuiParentNode를 Meta에 저장하고 있다라는 뜻
    object MetaViewingGuiParentNodeUtil {
        private const val VIEWING_GUI_PARENTNODE_KEY = "IF.viewingGuiParentNode"

        // p가 보고 있는 GUI가 null -> NodeRank List(CurrentNode) 를 보고 있는지 확인
        fun checkIfViewingGuiParentNodeIsNull(p: Player): Boolean {
            if (!p.hasMetadata(VIEWING_GUI_PARENTNODE_KEY)) {
                return true
            }
            throw Exception("viewingGuiParentNode가 null임을 기대받았지만 아니었습니다. ${p.name} | ${p.getMetadata(VIEWING_GUI_PARENTNODE_KEY)[0].value()?.javaClass}")
        }
        // p가 보고있는 GUI의 ParentNode 가져오기 (Metadata에서 가져옴)
        fun <T> getViewingGuiParentNode(p: Player, castClazz: Class<T>): T {
            try {
                return castClazz.cast(p.getMetadata(VIEWING_GUI_PARENTNODE_KEY)[0].value())
                // or
                // p.getMetadata(CURRENT_GUI_NODE_KEY)[0].value() as T
            } catch (e: ClassCastException) {
                throw Exception("viewingGuiParentNode가 ${castClazz.name}임을 기대받았지만 아니었습니다. ${p.name} | ${p.getMetadata(VIEWING_GUI_PARENTNODE_KEY)[0].value()?.javaClass}")
            }
        }
        // Anvil GUI를 넘나들어야 하기 때문에, onInvClose 때 굳이 removeMetaData를 하지 않음
        fun setViewingGuiParentNode(p: Player, currentGuiNode: Any?) {
            if (currentGuiNode == null) {
                p.removeMetadata(VIEWING_GUI_PARENTNODE_KEY, Main.plugin)
                return
            }
            p.setMetadata(VIEWING_GUI_PARENTNODE_KEY, FixedMetadataValue(Main.plugin, currentGuiNode))
        }
    }
}