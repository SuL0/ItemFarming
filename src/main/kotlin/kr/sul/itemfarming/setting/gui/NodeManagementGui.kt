package kr.sul.itemfarming.setting.gui

import kr.sul.itemfarming.Main
import kr.sul.itemfarming.setting.gui.guimoderator.AnvilGuiModerator
import kr.sul.itemfarming.setting.gui.guimoderator.ConfirmGuiModerator
import kr.sul.itemfarming.setting.gui.node.NodeCategory
import kr.sul.itemfarming.setting.gui.node.NodeRank
import kr.sul.itemfarming.setting.gui.nodecompponent.InternalNode
import kr.sul.servercore.usefulgui.GuiWithRunnableButtons
import kr.sul.servercore.util.ItemBuilder.loreIB
import kr.sul.servercore.util.ItemBuilder.nameIB
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

// GuiWithRunnable에 의존하는 클래스
// NOTE: 사실상 onClose를 받아서 인벤을 여는 것은 위험을 야기할 가능성이 있음. 다만 이것은 관리자용 이기에, 편리성을 더 추구하였음.
class NodeManagementGui(private val p: Player,
                        private val internalNodeObjToEdit: InternalNode,
                        private val nodeTag: NodeTag,

                        private val backToPreviousGuiMethod: Runnable,
                        private val childNodeSize: Int, private val deleteMethod: Runnable) {
    fun openManagementGui() {
        GuiWithRunnableButtons.Builder()
            .plugin(Main.plugin)
            .lines(3)
            .title("${nodeTag.getCombined()} ${internalNodeObjToEdit.name} §7세부 설정")
            .addRunnableButton(4, getItemForDisplay(), null)
            .addBackToPreviousGui()
            .addRename()
            .addEditChance()
            .addDelete()
            .openGuiWithRunnable(p)
    }


    private fun getItemForDisplay(): ItemStack {
        val objToEditClassName = run {
            when (internalNodeObjToEdit) {
                is NodeRank -> return@run NodeRank.NOTATION_NAME
                is NodeCategory -> return@run NodeCategory.NOTATION_NAME
                else -> throw Exception(internalNodeObjToEdit.javaClass.name)
            }
        }
        if (internalNodeObjToEdit is NodeRank || internalNodeObjToEdit is NodeCategory) {
            val itemForDisplay = InternalNodeMgr.makeItemForGuiDisplay(ItemStack(Material.WOOL), internalNodeObjToEdit, objToEditClassName, "어차피 지워질거라 필없")
            TreeUtil.ForCommon.deleteAllLoreExceptFirstLine(itemForDisplay)
            return itemForDisplay
        }
        throw Exception(internalNodeObjToEdit.javaClass.name)
    }

    private fun GuiWithRunnableButtons.Builder.addEditChance(): GuiWithRunnableButtons.Builder {
        addRunnableButton(22, ItemStack(Material.ENCHANTED_BOOK)
            .nameIB("§6§l확률 변경")
            .loreIB(" §7└ ${internalNodeObjToEdit.name} 의 확률 §f${internalNodeObjToEdit.chance}% §7을(를) 변경합니다.", 2)) {
            AnvilGuiModerator.open(p, "Edit) ${nodeTag.str} 확률 입력.", { s_input ->
                try {
                    val input = s_input.toDouble()
                    p.sendMessage("§6§lIF: ${nodeTag.getCombined()} §7${internalNodeObjToEdit.name} 의 확률을 §f$s_input% §7로 변경했습니다.")
                    internalNodeObjToEdit.chance = input
                    // 인벤은 아래의 onClose()가 열어줌
                } catch (ignored: Exception) {
                    p.sendMessage("§6§lIF: §cDouble §7타입을 입력해야 합니다.")
                }
            }, {
                Bukkit.getScheduler().runTask(Main.plugin) {
                    openManagementGui()
                }
            })
        }
        return this
    }

    private fun GuiWithRunnableButtons.Builder.addRename(): GuiWithRunnableButtons.Builder {
        addRunnableButton(18, ItemStack(Material.BOOK_AND_QUILL)
            .nameIB("§6§l이름 변경")
            .loreIB(" §7└ 이름 §f${internalNodeObjToEdit.name} §7을(를) 변경합니다.", 2)) {

            AnvilGuiModerator.open(p, "Edit) ${nodeTag.str} 이름 입력.", { input ->
                p.sendMessage("§6§lIF: ${nodeTag.getCombined()} §7의 이름을 §f'${internalNodeObjToEdit.name}' -> '$input' §7로 변경했습니다.")
                internalNodeObjToEdit.name = input
                // 인벤은 아래의 onClose()가 열어줌
            }, {
                Bukkit.getScheduler().runTask(Main.plugin) {
                    openManagementGui()
                }
            })
        }
        return this
    }

    private fun GuiWithRunnableButtons.Builder.addDelete(): GuiWithRunnableButtons.Builder {
        addRunnableButton(26, ItemStack(Material.RECORD_4)
            .nameIB("§4§l[!] §c해당 ${nodeTag.str} 삭제")
            .loreIB(" §7└ ${nodeTag.getCombined()} §f${internalNodeObjToEdit.name} §7을(를) 삭제합니다.", 2)) {

            // 2차 확인 (다만, 삭제가 잦은 LeafItem의 경우는 SHIFT+왼클 으로 바로 삭제할 수 있게끔)
            ConfirmGuiModerator.open(p, "§f정말 ${nodeTag.getCombined()} §f${internalNodeObjToEdit.name} §f을(를) 삭제합니까?",
                listOf(" §7└ 해당 ${nodeTag.getCombined()} §7는 §e${childNodeSize}개§7의 하위 노드를 가지고 있습니다."), {
                    deleteMethod.run()
                    p.sendMessage("§6§lIF: ${nodeTag.getCombined()} §f${internalNodeObjToEdit.name} §7을(를) 삭제했습니다.")
                    // 인벤은 아래의 onClose()가 열어줌
                }, {
                    Bukkit.getScheduler().runTask(Main.plugin) {
                        backToPreviousGuiMethod.run()
                    }
                })
        }
        return this
    }

    private fun GuiWithRunnableButtons.Builder.addBackToPreviousGui(): GuiWithRunnableButtons.Builder {
        addRunnableButton(6, ItemStack(Material.CHORUS_FRUIT)
            .nameIB("§6§l뒤로가기")
            .loreIB(" §7└ ${nodeTag.str} List GUI로 되돌아갑니다.", 2)) {
            backToPreviousGuiMethod.run()
        }
        return this
    }


    class NodeTag(private val color: String, val str: String) {
        fun getCombined(): String {
            return "$color[$str]"
        }
    }

    class Builder {
        var nodeTag: NodeTag? = null
        var internalNodeObjToEdit: InternalNode? = null
        fun setTag(color: String, str: String) {
            nodeTag = NodeTag(color, str)
        }
        var backToPreviousGuiMethod: Runnable? = null
        private var childNodeSize: Int? = null
        private var deleteMethod: Runnable? = null
        fun setDeleteButton(childNodeSize: Int, deleteMethod: Runnable) {
            this.childNodeSize = childNodeSize
            this.deleteMethod = deleteMethod
        }

        fun openManagementGui(p: Player) {
            val gui = NodeManagementGui(p, internalNodeObjToEdit!!, nodeTag!!, backToPreviousGuiMethod!!, childNodeSize!!, deleteMethod!!)
            gui.openManagementGui()
        }
    }
}