package kr.sul.itemfarming.setting.gui

import fr.minuskube.inv.ClickableItem
import fr.minuskube.inv.SmartInventory
import fr.minuskube.inv.content.InventoryContents
import fr.minuskube.inv.content.InventoryProvider
import kr.sul.itemfarming.Main
import kr.sul.itemfarming.setting.gui.guimoderator.AnvilGuiModerator
import kr.sul.itemfarming.setting.gui.guimoderator.ConfirmGuiModerator
import kr.sul.servercore.usefulgui.GuiWithRunnableButtons
import kr.sul.servercore.util.ItemBuilder.clearLoreIB
import kr.sul.servercore.util.ItemBuilder.loreIB
import kr.sul.servercore.util.ItemBuilder.nameIB
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack


// InventoryProvider에 onClick도 선택적으로 구현할 수 있으면 좋을 듯
//  (Rank List와 같이 중복되는 기능을 하는 아이템이 많으며 클릭했을 때 달려있는 코드 량이 방대하면, Consumer을 아이템 개수만큼 생성하게 돼서 메모리에서 불리하다고 생각됨)

// SmartInv는 간단한 Inv에만 좋은 Framework인 듯
// 전통적인 방식의 Design과 Action을 분리하는 것이 크게 문제가 되어보이진 않음.

// 근데 이거 걍 내가 만든 GuiWithRunnableButtons 이랑 별반 차이 없지 않나?ㅋㅋㅋㅋ
class NodeRankListInv: InventoryProvider {
    companion object {
        private const val GUI_NAME = "§f!IF: §c->§fRank list"
        private val RANK_ITEM_MATERIAL = Material.WOOL
        private val createRankButton = ItemStack(Material.BOOK_AND_QUILL).nameIB("§4§lRank 추가").loreIB(" §7└ Rank를 새로 생성합니다.", 2)

        fun openRankListGui(p: Player) {
            SmartInventory.builder()
                .provider(NodeRankListInv())
                .title(GUI_NAME)
                .size(8, 9)
                .parent(null)
                .build()
                .open(p)
        }

        fun getRankObjectFromGuiItem(item: ItemStack): NodeRank {
            val clickedRankName = ChatColor.stripColor(item.itemMeta.displayName.split(" ")[1])
            return getRankObjectFromName(clickedRankName)
        }
        private fun getRankObjectFromName(name: String): NodeRank {
            val list = TreeDataMgr.rootNodeList.filter { it.name == name }
            if (list.size != 1) {
                throw Exception("selectedRankName 에 해당하는 object가 rootNodeList에 존재하지 않습니다.  $name | ${TreeDataMgr.rootNodeList}")
            }
            return list.first()
        }
    }

    override fun init(p: Player, contents: InventoryContents) {
        // Metadata에 null 저장 (최상위 노드들을 보고 있기 때문)
        TempCurrentGuiNodeMgr.setCurrentGuiNode(p, null)

        // Rank List (Clickablew)
        val wool = ItemStack(RANK_ITEM_MATERIAL)
        TreeDataMgr.rootNodeList.forEach {
            // Wool item 색상 변경
            if (wool.durability < 15) {
                wool.durability = (wool.durability + 1).toShort()
            } else {
                wool.durability = 0
            }

            val clonedWool = wool.clone().nameIB("§4§l[RANK] §f${it.name}")
                .loreIB(" §6└ ${it.chance}%")
                .loreIB("")
                .loreIB("      §9§lClick §7: 해당 Rank에 종속된 Item List를 GUI를 새로 열어 나열합니다.", 2)
                .loreIB("§9§lShift§7+§9§lClick §7: 해당 Rank의 세부 설정을 변경합니다.")

            contents.add(ClickableItem.of(clonedWool) { e ->
                // !: Category GUI로 넘어가기
                if (!e.isShiftClick) {
                    val clickedRank = getRankObjectFromGuiItem(e.currentItem)
                    NodeCategory.NodeCategoryListMgr.openCategoryListGui(p, clickedRank)
                }

                // <: Management GUI (Rename, Edit Chance, Delete)
                else if (e.isShiftClick) {
                    val clickedRank = getRankObjectFromGuiItem(e.currentItem)
                    val clickedItemModified = e.currentItem.run {
                        val firstLore = this.itemMeta.lore[0]
                        this.clearLoreIB()
                        this.loreIB(firstLore)
                    }

                    GuiWithRunnableButtons.Builder()
                        .plugin(Main.plugin)
                        .lines(3)
                        .title("§4§l[Rank] §f${clickedRank.name} §7세부 설정")
                        .addRunnableButton(4, clickedItemModified, null)
                        .addRunnableButton(
                            18, ItemStack(Material.BOOK_AND_QUILL)
                                .nameIB("§6§l이름 변경")
                                .loreIB(" §7└ 이름 §f${clickedRank.name} §7을(를) 변경합니다.", 2)
                        ) {
                            AnvilGuiModerator.open(p, "Edit) Rank 이름 입력.", { input ->
                                p.sendMessage("§6§lIF: §4§l[Rank] §7의 이름을 §f'${clickedRank.name}' -> '$input' §7로 변경했습니다.")
                                clickedRank.name = input
                                openRankListGui(p)
                            }, {
                                openRankListGui(p)
                            })
                        }
                        .addRunnableButton(
                            22, ItemStack(Material.ENCHANTED_BOOK)
                                .nameIB("§6§l확률 변경")
                                .loreIB(" §7└ ${clickedRank.name} 의 확률 §f${clickedRank.chance}% §7을(를) 변경합니다.", 2)
                        ) {
                            AnvilGuiModerator.open(p, "Edit) Rank 확률 입력.", { s_input ->
                                val input = s_input.toDouble()
                                p.sendMessage("§6§lIF: §4§l[Rank] §7${clickedRank.name} 의 확률을 §f$s_input% §7로 변경했습니다.")
                                clickedRank.chance = input
                                openRankListGui(p)
                            }, {
                                openRankListGui(p)
                            })
                        }
                        .addRunnableButton(
                            26, ItemStack(Material.RECORD_4)
                                .nameIB("§4§l[!] §c해당 Rank 삭제")
                                .loreIB(" §7└ §4§l[Rank] §f${clickedRank.name} §7을(를) 삭제합니다.", 2)
                        ) {
                            // 2차 확인 (다만, 삭제가 잦은 LeafItem의 경우는 SHIFT+왼클 으로 바로 삭제할 수 있게끔)
                            ConfirmGuiModerator.open(p, "§f정말 §4§l[Rank] §f${clickedRank.name} §f을(를) 삭제합니까?",
                                listOf(" §7└ 해당 §4§l[Rank] §7는 §e${clickedRank.childNodeList.size}개§7의 하위 노드를 가지고 있습니다."), {
                                    TreeDataMgr.rootNodeList.remove(clickedRank)
                                    p.sendMessage("§6§lIF: §4§l[Rank] §f${clickedRank.name} §7을(를) 삭제했습니다.")
                                    openRankListGui(p)
                                }, {
                                    openRankListGui(p)
                                })
                        }
                        .addRunnableButton(
                            6, ItemStack(Material.CHORUS_FRUIT)
                                .nameIB("§6§l뒤로가기")
                                .loreIB(" §7└ Rank List GUI로 되돌아갑니다.", 2)
                        ) {
                            openRankListGui(p)
                        }
                        .open(p)
                }
            })

        }
        // Rank 추가 버튼
        contents.set(7, 1, ClickableItem.of(createRankButton) {
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
                        openRankListGui(p)
                    } catch (ignored: Exception) {
                        p.sendMessage("§6§lIF: §cDouble §7타입을 입력해야 합니다.")
                    }
                }, {
                    openRankListGui(p)
                })
                //
            }, {
                openRankListGui(p)
            })
        })

        // 식별용 색
        for (i in 1..9) {
            contents.set(8, i, ClickableItem.empty(ItemStack(Material.RED_SHULKER_BOX).nameIB("§f")))
        }
    }

    override fun update(p: Player, contents: InventoryContents) { }
}


//// SmartInv의 ClickableItem으론 onClick 공통 부분을 묶을 수 있는 방법이 딱히 없다.
//object NodeRankListMgr: Listener {
//    fun openRankListGui(p: Player) {
//        // Metadata에 null 저장 (최상위 노드들을 보고 있기 때문)
//        TempCurrentGuiNodeMgr.setCurrentGuiNode(p, null)
//
//
//        p.openInventory(inv)
//    }
//
//
//    @EventHandler(priority = EventPriority.HIGH)
//    fun onClick(e: InventoryClickEvent) {
//        if (e.isCancelled) return
//        val p = e.whoClicked as Player
//        if (e.clickedInventory?.name == GUI_NAME && e.currentItem != null) {
//            e.isCancelled = true
//            TempCurrentGuiNodeMgr.checkIfCurrentGuiNodeIsNull(p) // false일 시 내부에서 throw Exception   // TODO: 시발 ㅋㅋ 공통부분은 어떡하는데
//
//            // 왼 클릭
//            if (e.isLeftClick && !e.isShiftClick) {
//                // Rank 추가 버튼 클릭
//                else if (e.currentItem.isSimilar(createRankButton)) {
//
//                }
//            }
//        }
//    }
//}