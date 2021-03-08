package kr.sul.itemfarming.setting.gui

import kr.sul.itemfarming.Main
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue

object TempCurrentGuiNodeMgr {
    private const val CURRENT_GUI_NODE_KEY = "IF.currentGuiNode"

    fun checkIfCurrentGuiNodeIsNull(p: Player): Boolean {
        if (!p.hasMetadata(CURRENT_GUI_NODE_KEY)) {
            return true
        }
        throw Exception("currentGuiNode가 null임을 기대받았지만 아니었습니다. ${p.name} | ${p.getMetadata(CURRENT_GUI_NODE_KEY)[0].value()?.javaClass}")
    }
    fun <T> getCurrentGuiNode(p: Player, castClazz: Class<T>): T {
        try {
            return castClazz.cast(p.getMetadata(CURRENT_GUI_NODE_KEY)[0].value())
            // or
            // p.getMetadata(CURRENT_GUI_NODE_KEY)[0].value() as T
        } catch (e: ClassCastException) {
            throw Exception("currentGuiNode가 ${castClazz.name}임을 기대받았지만 아니었습니다. ${p.name} | ${p.getMetadata(CURRENT_GUI_NODE_KEY)[0].value()?.javaClass}")
        }
    }
    // Anvil GUI를 넘나들어야 하기 때문에, onInvClose 때 굳이 removeMetaData를 하지 않음
    fun setCurrentGuiNode(p: Player, currentGuiNode: Any?) {
        if (currentGuiNode == null) {
            p.removeMetadata(CURRENT_GUI_NODE_KEY, Main.plugin)
            return
        }
        p.setMetadata(CURRENT_GUI_NODE_KEY, FixedMetadataValue(Main.plugin, currentGuiNode))
    }
}