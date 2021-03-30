package kr.sul.itemfarming.setting.gui

import kr.sul.itemfarming.Main
import kr.sul.itemfarming.setting.gui.node.NodeCategory
import kr.sul.itemfarming.setting.gui.node.NodeItem
import kr.sul.itemfarming.setting.gui.node.NodeRank
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

        // Null -> Rank -> Category(parentNode에 들어갈 수 있는 최하위 노드) -> Item List
        fun getAppropriateGuiName(parentNode: Any?, currentNodeName: String): String {
            when (parentNode) {
                null -> {
                    return "§8§lN " +
                            "§c-> §n$currentNodeName List"
                }
                is NodeRank -> {
                    return "§8§lN " +
                            "§f-> ${NodeRank.NOTATION_COLOR}${parentNode.name}[${parentNode.chance}%] " +
                            "§c-> §n$currentNodeName List"
                }
                is NodeCategory -> {
                    return "§8§lN " +
                            "§f-> ${NodeRank.NOTATION_COLOR}${parentNode.parentNode.name}[${parentNode.parentNode.chance}%] " +
                            "§f-> ${NodeCategory.NOTATION_COLOR}${parentNode.name}[${parentNode.chance}%] " +
                            "§c-> §n$currentNodeName List"
                }
                else -> throw Exception("${parentNode::class.java} | $currentNodeName")
            }
        }

        // 인자가 NodeRank | NodeCategory | NodeItem 중 하나라면 그것의 name을 반환해줌
        fun getNodeName(node: Any): String {
            return when (node) {
                is NodeRank -> node.name
                is NodeCategory -> node.name
                is NodeItem -> node.displayName
                else -> throw Exception("${node::class.java}")
            }
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
        fun <T> getViewingGuiParentNode(p: Player, parentNodeClass: Class<T>): T {
            try {
                return parentNodeClass.cast(p.getMetadata(VIEWING_GUI_PARENTNODE_KEY)[0].value())
                // or
                // p.getMetadata(CURRENT_GUI_NODE_KEY)[0].value() as T
            } catch (e: ClassCastException) {
                throw Exception("viewingGuiParentNode가 ${parentNodeClass.name}임을 기대받았지만 아니었습니다. ${p.name} | ${p.getMetadata(VIEWING_GUI_PARENTNODE_KEY)[0].value()?.javaClass}")
            }
        }
        // Anvil GUI를 넘나들어야 하기 때문에, onInvClose 때 굳이 removeMetaData를 하지 않음
        fun setViewingGuiParentNode(p: Player, parentNode: Any?) {
            if (parentNode == null) {
                p.removeMetadata(VIEWING_GUI_PARENTNODE_KEY, Main.plugin)
                return
            }
            p.setMetadata(VIEWING_GUI_PARENTNODE_KEY, FixedMetadataValue(Main.plugin, parentNode))
        }
    }
}