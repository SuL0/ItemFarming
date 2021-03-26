package kr.sul.itemfarming.setting.gui.node

import kr.sul.itemfarming.setting.gui.InternalNodeMgr
import kr.sul.itemfarming.setting.gui.TreeDataMgr
import kr.sul.itemfarming.setting.gui.TreeUtil
import kr.sul.itemfarming.setting.gui.nodecompponent.ChildNodeContainer
import kr.sul.itemfarming.setting.gui.nodecompponent.InternalNode
import kr.sul.servercore.util.ItemBuilder.nameIB
import org.apache.logging.log4j.util.TriConsumer
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

// Root node

// Rank -> Rarity?
class NodeRank(override var name: String, override var chance: Double, override val childNodeList: ArrayList<NodeCategory>): InternalNode, ChildNodeContainer<NodeCategory> {

    // rootNodeList에 추가
    init {
        TreeDataMgr.rootNodeList.add(this)
    }

    // 외부에서는 반드시 이것으로 써야 함 (수정 용이)
    companion object {
        const val NOTATION_NAME = "Rank"
        const val NOTATION_COLOR = "§4§l"
    }
}













object NodeRankListMgr: InternalNodeMgr<NodeRank>() {
    override val NODE_CLASS = NodeRank::class.java
    override val NODE_TYPE_NAME = "Rank"
    override val NODE_TYPE_COLOR = "§4§l"
    override val CHILD_NODE_TYPE_NAME = "Category"

    override fun getGuiCurrentNodeList(p: Player): ArrayList<NodeRank> {
        return TreeDataMgr.rootNodeList
    }

    override val GUI_NAME = "§f!IF: §c->§fRank list"
    override val NODE_ITEM_MATERIAL = Material.WOOL
    override val howToCreateCurrentNodeObj = TriConsumer<Player, String, Double> { _, name, chance -> NodeRank(name, chance, arrayListOf()) }

    override val itemForidentificationInGui = ItemStack(Material.RED_SHULKER_BOX).nameIB("§f")
    override fun setGuiParentNode(p: Player, parentNode: InternalNode?) {
        TreeUtil.MetaViewingGuiParentNodeUtil.setViewingGuiParentNode(p, null)  // Metadata에 null 저장 (최상위 노드들을 보고 있기 때문)
    }
    override fun getGuiParentNode(p: Player): InternalNode? { // 사실상 무조건 null만 줌
        return null // null -> NodeRank List (CurrentNode)
    }
    override fun checkGuiParentNodeIsValid(p: Player) {
        TreeUtil.MetaViewingGuiParentNodeUtil.checkIfViewingGuiParentNodeIsNull(p)
    }

    // Category Node List로 워프
    // guiNode = parentNode 라고 해도 될 것 같기도 하고. 나도 내가 정해둔 단어들이 헷갈린다
    override fun goToChildNodeListGui(p: Player, currentNode: NodeRank) {
        NodeCategoryListMgr.openCurrentNodeListGui(p, currentNode)
    }
}



/*
object NodeRankListMgr: Listener {
    private const val GUI_NAME = "§f!IF: §c->§fRank list"
    private val RANK_ITEM_MATERIAL = Material.WOOL
    private val createRankButton = ItemStack(Material.BOOK_AND_QUILL).nameIB("§4§lRank 추가").loreIB(" §7└ Rank를 새로 생성합니다.", 2)

    fun openRankListGui(p: Player) {
        // Metadata에 null 저장 (최상위 노드들을 보고 있기 때문)
        TreeMgr.TempCurrentGuiNodeMgr.setCurrentGuiNode(p, null)

        val inv = Bukkit.createInventory(null, 72, GUI_NAME)
        val wool = ItemStack(RANK_ITEM_MATERIAL)
        TreeDataMgr.rootNodeList.forEach {
            // Wool item 색상 변경
            if (wool.durability < 15) {
                wool.durability = (wool.durability + 1).toShort()
            } else {
                wool.durability = 0
            }

            val made = TreeMgr.ForNodeType.makeItemForGuiDisplay(wool, it, "Rank", NodeCategory.NOTATION_NAME)
            inv.addItem(made)
        }

        // Rank 추가 버튼
        inv.setItem(54, createRankButton)
        // 식별용 색
        for (i in 63..71) {
            inv.setItem(i, ItemStack(Material.RED_SHULKER_BOX).nameIB("§f"))
        }
        p.openInventory(inv)
    }





    @EventHandler(priority = EventPriority.HIGH)
    fun onClick(e: InventoryClickEvent) {
        if (e.isCancelled) return
        val p = e.whoClicked as Player
        if (e.clickedInventory?.name == GUI_NAME && e.currentItem != null) {
            e.isCancelled = true
            TreeMgr.TempCurrentGuiNodeMgr.checkIfCurrentGuiNodeIsNull(p) // false일 시 내부에서 throw Exception

            // 왼 클릭
            if (e.isLeftClick && !e.isShiftClick) {

                // Category 선택 버튼 클릭 -> Category GUI로 넘어가기
                if (e.currentItem.type == RANK_ITEM_MATERIAL) {
                    val clickedRank = TreeMgr.ForNodeType.getObjFromGuiItem(NodeRank::class.java, e.currentItem, TreeDataMgr.rootNodeList)
                    // !: Category GUI로 넘어가기
                    NodeCategoryListMgr.openCategoryListGui(p, clickedRank)
                }


                // Rank 추가 버튼 클릭
                else if (e.currentItem.isSimilar(createRankButton)) {
                    // !: 1차 Anvil GUI [Rank 이름 입력]
                    AnvilGuiModerator.open(p, "1. Rank 이름 입력", { input1 ->
                        // Rank 새로 생성

                        // 이름 중복 체크
                        TreeDataMgr.rootNodeList.forEach {
                            if (it.name == input1) {
                                p.sendMessage("§6§lIF: §4§l[Rank] §7의 이름이 §c중복§7됩니다. §f'$input1'")
                                return@open
                            }
                        }

                        // !: 2차 Anvil GUI [Rank 확률 입력]
                        AnvilGuiModerator.open(p, "2. Rank 확률 입력", { s_input2 ->
                            try {
                                val input2 = s_input2.toDouble()
                                p.sendMessage("§6§lIF: §4§l[Rank] §f$input1 : $input2% §7를 새로 생성했습니다.")
                                NodeRank(input1, input2, arrayListOf())
                                this.openRankListGui(p)
                            } catch (ignored: Exception) {
                                p.sendMessage("§6§lIF: §cDouble §7타입을 입력해야 합니다.")
                            }
                        }, {
                            Bukkit.getScheduler().runTask(plugin) {
                                this.openRankListGui(p)
                            }
                        })
                    }, {
                        // 여긴 Rank 새로 생성의 가장 끝부분이 아니기에, ESC를 고려하여 특정 gui로 이동을 넣게되면, 위의 [2] 로 진행이 안됨
                    })
                }
            }



            // 아이템 쉬프트 왼클릭 시 Management GUI (Rename, Edit Chance, Delete)
            else if (e.isShiftClick && e.isLeftClick && e.currentItem.type == RANK_ITEM_MATERIAL) {
                val clickedRank = TreeMgr.ForNodeType.getObjFromGuiItem(NodeRank::class.java, e.currentItem, TreeDataMgr.rootNodeList)
                NodeManagementGui.Builder().run {
                    setTag("§4§l", "Rank")
                    nodeTypeObjToEdit = clickedRank
                    backToPreviousGuiMethod = Runnable { openRankListGui(p) }
                    setDeleteButton(clickedRank.childNodeList.size) {
                        TreeDataMgr.rootNodeList.remove(clickedRank)
                    }
                    openManagementGui(p)
                }
            }
        }
    }
}*/