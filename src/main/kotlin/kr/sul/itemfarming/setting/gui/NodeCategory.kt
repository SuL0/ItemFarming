package kr.sul.itemfarming.setting.gui

import kr.sul.itemfarming.setting.gui.nodecompponent.ChildNodeContainer
import kr.sul.itemfarming.setting.gui.nodecompponent.ParentNodeContainer
import kr.sul.itemfarming.setting.gui.guimoderator.AnvilGuiModerator
import kr.sul.servercore.util.ItemBuilder.loreIB
import kr.sul.servercore.util.ItemBuilder.nameIB
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

// Internal node
class NodeCategory(var name: String, override val parentNode: NodeRank, var chance: Double, override val childNodeList: ArrayList<LeafItem>): ParentNodeContainer<NodeRank>, ChildNodeContainer<LeafItem> {

    // parentNode에 연결(link)
    init {
        parentNode.childNodeList.add(this)
    }



    object NodeCategoryListMgr: Listener {
        private const val GUI_NAME = "§f!IF: Rank§c->§fCategory list"
        private val CURRENT_GUI_NODE_CLASS = NodeRank::class.java
        private val CATEGORY_ITEM_MATERIAL = Material.WOOL
        private val createCategoryButton = ItemStack(Material.BOOK_AND_QUILL).nameIB("§2§lCategory 추가").loreIB(" §7└ Category를 새로 생성합니다.", 2)
        private val goBackButton = ItemStack(Material.CHORUS_FRUIT).nameIB("§2§l뒤로가기").loreIB(" §7└ Rank List GUI로 되돌아갑니다.", 2)

        fun openCategoryListGui(p: Player, currentGuiNode: NodeRank) {
            // Metadata에 currentGuiNode 저장
            TempCurrentGuiNodeMgr.setCurrentGuiNode(p, currentGuiNode)

            val inv = Bukkit.createInventory(null, 72, GUI_NAME)
            val wool = ItemStack(CATEGORY_ITEM_MATERIAL)
            currentGuiNode.childNodeList.forEach {
                // Wool item 색상 변경
                if (wool.durability < 15) {
                    wool.durability = (wool.durability + 1).toShort()
                } else {
                    wool.durability = 0
                }

                val clonedWool = wool.clone().nameIB("§2§l[CATEGORY] §f${it.name}")
                    .loreIB(" §6└ ${it.chance}%")
                    .loreIB("")
                    .loreIB("      §9§lClick §7: 해당 Category에 종속된 Item List를 GUI를 새로 열어 나열합니다.", 2)
                    .loreIB("§9§lShift§7+§9§lClick §7: 해당 Category의 세부 설정을 변경합니다.")
                inv.addItem(clonedWool)
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
                val currentGuiNode = TempCurrentGuiNodeMgr.getCurrentGuiNode(p, CURRENT_GUI_NODE_CLASS)

                // 왼 클릭
                if (e.isLeftClick) {
                    // Category 선택 버튼 클릭 -> Item GUI로 넘어가기
                    if (e.currentItem.type == CATEGORY_ITEM_MATERIAL) {
                        val clickedCategoryName = ChatColor.stripColor(e.currentItem.itemMeta.displayName.split(" ")[1])
                        val clickedCategory = getCategoryObjectFromName(clickedCategoryName, currentGuiNode)

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
                                    this.openCategoryListGui(p, currentGuiNode)
                                } catch (ignored: Exception) {
                                    p.sendMessage("§6§lIF: §cDouble §7타입을 입력해야 합니다.")
                                }
                            }, {
                                this.openCategoryListGui(p, currentGuiNode)
                            })
                            //
                        }, {
                            this.openCategoryListGui(p, currentGuiNode)
                        })
                    }

                    // 뒤로가기
                    else if (e.currentItem == goBackButton) {
                        NodeRank.NodeRankListMgr.openRankListGui(p)
                    }
                }

                // 아이템 우클릭 시 Rank 이름 변경
//                else if (e.isRightClick && e.currentItem.type == CATEGORY_ITEM_MATERIAL) {
//                    val clickedRankName = ChatColor.stripColor(e.currentItem.itemMeta.displayName)
//                    val clickedRank = getCategoryObjectFromName(clickedRankName, currentGuiNode)
//                    AnvilGuiModerator.open(p, "Category $clickedRankName 의 이름 변경", { modifiedRankName ->
//                        p.sendMessage("§6§lIF: §7Category의 이름을 §f'$modifiedRankName' §7로 변경했습니다.")
//                        clickedRank.name = modifiedRankName
//                        this.openCategoryListGui(p, currentGuiNode)
//                    }, {
//                        this.openCategoryListGui(p, currentGuiNode)
//                    })
//                }
            }
        }

        private fun getCategoryObjectFromName(name: String, currentGuiNode: NodeRank): NodeCategory {
            val list = currentGuiNode.childNodeList.filter { it.name == name }
            if (list.size != 1) {
                throw Exception("selectedCategoryName 에 해당하는 object가 currentGuiNode.childNodeList에 존재하지 않습니다.  $name | ${TreeDataMgr.rootNodeList}")
            }
            return list.first()
        }
    }
}