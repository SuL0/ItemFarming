package kr.sul.itemfarming.setting.gui

import kr.sul.itemfarming.Main.Companion.plugin
import kr.sul.itemfarming.setting.gui.nodecompponent.ChildNodeContainer
import kr.sul.itemfarming.setting.gui.guimoderator.AnvilGuiModerator
import kr.sul.itemfarming.setting.gui.guimoderator.ConfirmGuiModerator
import kr.sul.servercore.usefulgui.GuiWithRunnableButtons
import kr.sul.servercore.util.ItemBuilder.clearLoreIB
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

// Root node

// Rank -> Rarity?
class NodeRank(var name: String, var chance: Double, override val childNodeList: ArrayList<NodeCategory>): ChildNodeContainer<NodeCategory> {

    // rootNodeList에 추가
    init {
        TreeDataMgr.rootNodeList.add(this)
    }



    object NodeRankListMgr: Listener {
        private const val GUI_NAME = "§f!IF: §c->§fRank list"
        private val RANK_ITEM_MATERIAL = Material.WOOL
        private val createRankButton = ItemStack(Material.BOOK_AND_QUILL).nameIB("§4§lRank 추가").loreIB(" §7└ Rank를 새로 생성합니다.", 2)

        fun openRankListGui(p: Player) {
            // Metadata에 null 저장 (최상위 노드들을 보고 있기 때문)
            TempCurrentGuiNodeMgr.setCurrentGuiNode(p, null)

            val inv = Bukkit.createInventory(null, 72, GUI_NAME)
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
                inv.addItem(clonedWool)
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
                TempCurrentGuiNodeMgr.checkIfCurrentGuiNodeIsNull(p) // false일 시 내부에서 throw Exception

                // 왼 클릭
                if (e.isLeftClick && !e.isShiftClick) {

                    // Category 선택 버튼 클릭 -> Category GUI로 넘어가기
                    if (e.currentItem.type == RANK_ITEM_MATERIAL) {
                        val clickedRank = getRankObjectFromGuiItem(e.currentItem)
                        // !: Category GUI로 넘어가기
                        NodeCategory.NodeCategoryListMgr.openCategoryListGui(p, clickedRank)
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
                                this.openRankListGui(p)
                            })
                            //
                        }, {
                            this.openRankListGui(p)
                        })
                    }
                }



                // 아이템 쉬프트 왼클릭 시 Management GUI (Rename, Edit Chance, Delete)
                else if (e.isShiftClick && e.isLeftClick && e.currentItem.type == RANK_ITEM_MATERIAL) {
                    val clickedRank = getRankObjectFromGuiItem(e.currentItem)
                    val clickedItemModified = e.currentItem.run {
                        val firstLore = this.itemMeta.lore[0]
                        this.clearLoreIB()
                        this.loreIB(firstLore)
                    }

                    GuiWithRunnableButtons.Builder()
                        .plugin(plugin)
                        .lines(3)
                        .title("§4§l[Rank] §f${clickedRank.name} §7세부 설정")
                        .addRunnableButton(4, clickedItemModified, null)
                        .addRunnableButton(18, ItemStack(Material.BOOK_AND_QUILL)
                                .nameIB("§6§l이름 변경")
                                .loreIB(" §7└ 이름 §f${clickedRank.name} §7을(를) 변경합니다.", 2)) {
                            AnvilGuiModerator.open(p, "Edit) Rank 이름 입력.", { input ->
                                p.sendMessage("§6§lIF: §4§l[Rank] §7의 이름을 §f'${clickedRank.name}' -> '$input' §7로 변경했습니다.")
                                clickedRank.name = input
                                this.openRankListGui(p)
                            }, {
                                this.openRankListGui(p)
                            })
                        }
                        .addRunnableButton(22, ItemStack(Material.ENCHANTED_BOOK)
                                .nameIB("§6§l확률 변경")
                                .loreIB(" §7└ ${clickedRank.name} 의 확률 §f${clickedRank.chance}% §7을(를) 변경합니다.", 2)) {
                            AnvilGuiModerator.open(p, "Edit) Rank 확률 입력.", { s_input ->
                                val input = s_input.toDouble()
                                p.sendMessage("§6§lIF: §4§l[Rank] §7${clickedRank.name} 의 확률을 §f$s_input% §7로 변경했습니다.")
                                clickedRank.chance = input
                                this.openRankListGui(p)
                            }, {
                                this.openRankListGui(p)
                            })
                        }
                        .addRunnableButton(26, ItemStack(Material.RECORD_4)
                                .nameIB("§4§l[!] §c해당 Rank 삭제")
                                .loreIB(" §7└ §4§l[Rank] §f${clickedRank.name} §7을(를) 삭제합니다.", 2)) {
                            // 2차 확인 (다만, 삭제가 잦은 LeafItem의 경우는 SHIFT+왼클 으로 바로 삭제할 수 있게끔)
                            ConfirmGuiModerator.open(p, "§f정말 §4§l[Rank] §f${clickedRank.name} §f을(를) 삭제합니까?",
                                listOf(" §7└ 해당 §4§l[Rank] §7는 §e${clickedRank.childNodeList.size}개§7의 하위 노드를 가지고 있습니다."), {
                                    TreeDataMgr.rootNodeList.remove(clickedRank)
                                    p.sendMessage("§6§lIF: §4§l[Rank] §f${clickedRank.name} §7을(를) 삭제했습니다.")
                                    this.openRankListGui(p)
                                }, {
                                    this.openRankListGui(p)
                            })
                        }
                        .addRunnableButton(6, ItemStack(Material.CHORUS_FRUIT)
                                .nameIB("§6§l뒤로가기")
                                .loreIB(" §7└ Rank List GUI로 되돌아갑니다.", 2)) {
                            this.openRankListGui(p)
                        }
                        .open(p)
                }
            }
        }

        private fun getRankObjectFromGuiItem(item: ItemStack): NodeRank {
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
}