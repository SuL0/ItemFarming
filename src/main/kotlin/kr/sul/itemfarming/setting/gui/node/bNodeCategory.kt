package kr.sul.itemfarming.setting.gui.node

import kr.sul.itemfarming.setting.gui.InternalNodeMgr
import kr.sul.itemfarming.setting.gui.TreeUtil
import kr.sul.itemfarming.setting.gui.nodecompponent.ChildNodeContainer
import kr.sul.itemfarming.setting.gui.nodecompponent.InternalNode
import kr.sul.itemfarming.setting.gui.nodecompponent.ParentNodeContainer
import kr.sul.servercore.util.ItemBuilder.nameIB
import org.apache.logging.log4j.util.TriConsumer
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

// Internal node
class NodeCategory(override val parentNode: NodeRank, override var name: String, chance: Double, override val childNodeList: ArrayList<NodeItemAbstract>): InternalNode, ParentNodeContainer<NodeRank>, ChildNodeContainer<NodeItemAbstract> {
    override var chance: Double = chance
        set(value) {
            field = value
            refreshSort(parentNode.childNodeList)
        }

    // parentNode에 연결(link)
    init {
        parentNode.childNodeList.add(this)
        refreshSort(parentNode.childNodeList)
    }
    companion object {
        const val NOTATION_NAME = "Category"
        const val NOTATION_COLOR = "§2§l"

        // parentNode의 childNodeList (=자신이 포함된 childNodeList)를 it.chance 기준 내림차순으로 정렬 (생성, chance 변경 시)
        fun refreshSort(childNodeList: ArrayList<NodeCategory>) {
            childNodeList.sortByDescending { it.chance }
        }
    }
}





object NodeCategoryListMgr: InternalNodeMgr<NodeCategory>() {
    override val NODE_CLASS = NodeCategory::class.java
    override val NODE_TYPE_NAME = NodeCategory.NOTATION_NAME
    override val NODE_TYPE_COLOR = NodeCategory.NOTATION_COLOR
    override val CHILD_NODE_TYPE_NAME = NodeItemAbstract.NOTATION_NAME

    override fun getViewingGuiCurrentNodeList(p: Player): ArrayList<NodeCategory> {
        return TreeUtil.MetaViewingGuiParentNodeUtil.getViewingGuiParentNode(p, NodeRank::class.java).childNodeList
    }

    override val GUI_NAME_PREFIX = "§I§F§:§C§a§t§e§g§o§r§y"
    override val NODE_ITEM_MATERIAL = Material.WOOL
    override val howToCreateCurrentNodeObj = TriConsumer<Player, String, Double> { p, name, chance ->
        val viewingGuiParentNode = getViewingGuiParentNode(p)
        NodeCategory(viewingGuiParentNode, name, chance, arrayListOf())
    }

    override val itemForidentificationInGuiBottom = ItemStack(Material.GREEN_SHULKER_BOX).nameIB("§f")
    override fun setViewingGuiParentNode(p: Player, parentNode: InternalNode?) {
        TreeUtil.MetaViewingGuiParentNodeUtil.setViewingGuiParentNode(p, parentNode!!)
    }
    override fun getViewingGuiParentNode(p: Player): NodeRank {
        return TreeUtil.MetaViewingGuiParentNodeUtil.getViewingGuiParentNode(p, NodeRank::class.java)
    }
    override fun checkViewingGuiParentNodeIsValid(p: Player) {
        TreeUtil.MetaViewingGuiParentNodeUtil.getViewingGuiParentNode(p, NodeRank::class.java) // Valid하지 않다면, 해당 메소드에서 throw Exception을 해줌
    }

    // Category Node List로 워프
    // guiNode = parentNode 라고 해도 될 것 같기도 하고. 나도 내가 정해둔 단어들이 헷갈린다
    override fun goToChildNodeListGui(p: Player, currentNode: NodeCategory) {
        NodeItemListMgr.openCurrentNodeListGui(p, currentNode, 1)
    }
}










/*
object NodeCategoryListMgr: Listener {
    private const val GUI_NAME = "§f!IF: Rank§c->§fCategory list"
    private val CURRENT_GUI_NODE_CLASS = NodeRank::class.java
    private val CATEGORY_ITEM_MATERIAL = Material.WOOL
    private val createCategoryButton = ItemStack(Material.BOOK_AND_QUILL).nameIB("§2§lCategory 추가").loreIB(" §7└ Category를 새로 생성합니다.", 2)
    private val goBackButton = ItemStack(Material.CHORUS_FRUIT).nameIB("§2§l뒤로가기").loreIB(" §7└ Rank List GUI로 되돌아갑니다.", 2)

    fun openCategoryListGui(p: Player, parentGuiNode: NodeRank) {
        // Metadata에 currentGuiNode 저장
        TreeUtil.MetaViewingGuiParentNodeUtil.setViewingGuiParentNode(p, parentGuiNode)

        val inv = Bukkit.createInventory(null, 72, GUI_NAME)
        val wool = ItemStack(CATEGORY_ITEM_MATERIAL)
        // parentGuiNode.childNodeList = currentNode List (GUI 기준)
        parentGuiNode.childNodeList.forEach {
            // Wool item 색상 변경
            if (wool.durability < 15) {
                wool.durability = (wool.durability + 1).toShort()
            } else {
                wool.durability = 0
            }

            val made = InternalNodeMgr.makeItemForGuiDisplay(wool, it, "Category", "Item")
            inv.addItem(made)
        }

        // Nav Bar 버튼 추가
        inv.setItem(54, createCategoryButton)
        inv.setItem(62, goBackButton)
        // 식별용 색
        for (i in 63..71) {
            inv.setItem(i, ItemStack(Material.GREEN_SHULKER_BOX).nameIB("§f"))
        }
        p.openInventory(inv)
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onClick(e: InventoryClickEvent) {
        if (e.isCancelled) return
        val p = e.whoClicked as Player
        if (e.clickedInventory?.name == GUI_NAME && e.currentItem != null) {
            e.isCancelled = true
            val currentGuiNode = TreeUtil.MetaViewingGuiParentNodeUtil.getViewingGuiParentNode(p, CURRENT_GUI_NODE_CLASS)

            // 왼 클릭
            if (e.isLeftClick) {
                // Category 선택 버튼 클릭 -> Item GUI로 넘어가기
                if (e.currentItem.type == CATEGORY_ITEM_MATERIAL) {
                    val clickedCategory = InternalNodeMgr.getObjFromGuiItem(NodeCategory::class.java, e.currentItem, currentGuiNode.childNodeList)

                    // !: Item GUI로 넘어가기
//                        LeafItemGuiMgr.openItemListGui(p, clickedCategory)
                }

                // Rank->Category 추가 버튼 클릭
                else if (e.currentItem == createCategoryButton) {
                    // !: Anvil GUI로 넘어가기
                    AnvilGuiModerator.open(p, "1. Category 추가", { input1 ->
                        // Category 새로 생성

                        // 중복 체크
                        currentGuiNode.childNodeList.forEach {
                            Bukkit.broadcastMessage("${it.name} ?= $input1")
                            if (it.name == input1) {
                                p.sendMessage("§6§lIF: §7Category의 이름이 §c중복§7됩니다. §f'$input1'")
                                return@open
                            }
                        }

                        // !: 2차 Anvil GUI [Category 확률 입력]
                        AnvilGuiModerator.open(p, "2. Category 확률 입력", { s_input2 ->
                            try {
                                val input2 = s_input2.toDouble()
                                p.sendMessage("§6§lIF: §2§l[Category] §f$input1 : $input2% §7를 새로 생성했습니다.")
                                NodeCategory(input1, currentGuiNode, input2, arrayListOf())
                                openCategoryListGui(p, currentGuiNode)
                            } catch (ignored: Exception) {
                                p.sendMessage("§6§lIF: §cDouble §7타입을 입력해야 합니다.")
                            }
                        }, {
                            openCategoryListGui(p, currentGuiNode)
                        })
                        //
                    }, {
                        openCategoryListGui(p, currentGuiNode)
                    })
                }

                // 뒤로가기
                else if (e.currentItem == goBackButton) {
                    NodeRankListMgr.openCurrentNodeListGui(p)
                }
            }
        }
    }
}*/