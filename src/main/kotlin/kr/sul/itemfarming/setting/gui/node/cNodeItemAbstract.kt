package kr.sul.itemfarming.setting.gui.node

import com.shampaggon.crackshot2.addition.WeaponNbtParentNodeMgr
import com.shampaggon.crackshot2.addition.util.CrackShotAdditionAPI
import kr.sul.itemfarming.Main
import kr.sul.itemfarming.setting.gui.TreeDataMgr
import kr.sul.itemfarming.setting.gui.TreeUtil
import kr.sul.itemfarming.setting.gui.guimoderator.AnvilGuiModerator
import kr.sul.itemfarming.setting.gui.guimoderator.ConfirmGuiModerator
import kr.sul.itemfarming.setting.gui.nodecompponent.ParentNodeContainer
import kr.sul.servercore.nbtapi.NbtItem
import kr.sul.servercore.util.ItemBuilder.loreIB
import kr.sul.servercore.util.ItemBuilder.nameIB
import org.apache.logging.log4j.util.TriConsumer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import java.util.*


// Leaf Node
// 이것을 구현하는 것들의 차이점은, 오로지 item을 어떻게 반환 해 내는가의 차이
abstract class NodeItemAbstract(override val parentNode: NodeCategory,
                                val uuid: UUID,  // 고유한 NodeItem을 가리키는 용도. Item은 Unique하지 않아, 중복 가능성이 있기 때문 (파일에선 저장하지 않음. 그냥 load할때마다 UUID 바뀐다고 보면 됨)
                                chance: Double): ParentNodeContainer<NodeCategory> {
    abstract val item: ItemStack  // 이것만 abstract

    val displayName: String
        get() = item.itemMeta?.displayName ?: item.type.name
    var chance: Double = chance
        set(value) {
            field = value
            refreshSort(parentNode.childNodeList)
        }


    // 주의: 해당 클래스를 상속하는 클래스는, TreeDataMgr의 Json부분에 직접 몇몇개 수정해줘야 함 (e.g. nodeItemType)
/*  // parentNode에 연결(link)   해당 클래스를 상속하는 클래스는 이 코드를 직접 붙여넣어줘야 함
    init {
        parentNode.childNodeList.add(this)
        refreshSort()
    }
*/


    companion object {
        const val NOTATION_NAME = "Item"
        const val NOTATION_COLOR = "§9§l"

        // parentNode의 childNodeList (=자신이 포함된 childNodeList)를 it.chance 기준 내림차순으로 정렬 (생성, chance 변경 시)
        fun refreshSort(childNodeList: ArrayList<NodeItemAbstract>) {
            childNodeList.sortByDescending { it.chance }
        }
    }
}

// 쉬프트+왼클: Confirm 후 추가
// InternalNodeGui에서 따온 코드 중 InternalNode -> NodeCategory 로 무조건 변경해도 됨

// 페이지 기능
object NodeItemListMgr: Listener {
    val NODE_CLASS = NodeItemAbstract::class.java
    const val NODE_TYPE_NAME = NodeItemAbstract.NOTATION_NAME
    const val NODE_TYPE_COLOR = NodeItemAbstract.NOTATION_COLOR

    fun getViewingGuiCurrentNodeList(p: Player): ArrayList<NodeItemAbstract> {
        return TreeUtil.MetaViewingGuiParentNodeUtil.getViewingGuiParentNode(p, NodeCategory::class.java).childNodeList
    }

    const val GUI_NAME_PREFIX = "§I§F§:§I§t§e§m"
    val howToCreateCurrentNodeObj = TriConsumer<Player, ItemStack, Double> { p, item, chance ->
        val viewingGuiParentNode = getViewingGuiParentNode(p)
        if (CrackShotAdditionAPI.isValidCrackShotWeapon(item)) {
            p.sendMessage(" §7└ 등록된 아이템은 §fCrackShot(2) §7으로 감지되었습니다.")
            val csParentNode = WeaponNbtParentNodeMgr.getWeaponParentNodeFromNbt(item)!!
            NodeItemCrackShot(viewingGuiParentNode, csParentNode, chance)
        } else {
            p.sendMessage(" §7└ 등록된 아이템은 §fNormal Item §7으로 감지되었습니다.")
            NodeItemNormal(viewingGuiParentNode, item, chance)
        }
    }

    private val itemForIdentificationInGuiBottom = ItemStack(Material.BLUE_SHULKER_BOX).nameIB("§f")
    fun setViewingGuiParentNode(p: Player, parentNode: NodeCategory) {
        TreeUtil.MetaViewingGuiParentNodeUtil.setViewingGuiParentNode(p, parentNode)
    }
    fun getViewingGuiParentNode(p: Player): NodeCategory {
        return TreeUtil.MetaViewingGuiParentNodeUtil.getViewingGuiParentNode(p, NodeCategory::class.java)
    }
    fun checkViewingGuiParentNodeIsValid(p: Player) {
        TreeUtil.MetaViewingGuiParentNodeUtil.getViewingGuiParentNode(p, NodeCategory::class.java) // Valid하지 않다면, 해당 메소드에서 throw Exception을 해줌
    }
    // NodeItem 전용 ViewingGuiPage MetaData
    private const val VIEWING_GUI_PAGE_KEY = "IF.ViewingGuiPage"
    private fun setViewingGuiPage(p: Player, page: Int) {
        p.setMetadata(VIEWING_GUI_PAGE_KEY, FixedMetadataValue(Main.plugin, page))
    }
    private fun getViewingGuiPage(p: Player): Int {
        return p.getMetadata(VIEWING_GUI_PAGE_KEY)[0].asInt()
    }
    //
    // NodeItem GUI에 Page를 설정해서 1~6번째 줄 맞는 아이템으로 채우기
    private fun setUpPageToNodeItemGui(p: Player, nodeItemInv: Inventory, page: Int, nodeItemList: List<NodeItemAbstract>) {
        setViewingGuiPage(p, page)
        nodeItemInv.setItem(58, ItemStack(Material.IRON_SWORD).nameIB("§e§l- $page Page -"))  // 현재 페이지 표시용 아이템
        for (guiIndex in 0..53) {
            val itemIndex = ((page-1)*54)+guiIndex
            if (nodeItemList.size >= itemIndex+1) {
                nodeItemInv.setItem(guiIndex, Util.makeItemForGuiDisplay(nodeItemList[itemIndex]))
            }
            // 위에 의해서 덮어씌여지지 않는데 불구하고, 아이템이 배치되어 있는 경우 삭제
            else if (nodeItemInv.getItem(guiIndex) != null) {
                nodeItemInv.setItem(guiIndex, null)
            }
        }
    }

    private val goToPreviousPageBtn = ItemStack(Material.IRON_INGOT).nameIB("§7§l이전 페이지")
        .loreIB(" §7└ Item List 이전 페이지로 넘기기")
    private val goToNextPageBtn = ItemStack(Material.IRON_INGOT).nameIB("§c§l다음 페이지")
        .loreIB(" §7└ Item List 다 페이지로 넘기기")

    private val helpItem = ItemStack(Material.SIGN).nameIB("${NODE_TYPE_COLOR}${NODE_TYPE_NAME} 추가 하는 방법")
        .loreIB(" §7└ 플레이어 인벤토리(아래) 에서", 2)
        .loreIB(" §7└ 추가할 아이템 §9§lShift§7+§9§lLeft Click", 2)
    private val goToCategoryListGuiBtn = ItemStack(Material.CHORUS_FRUIT).nameIB("§2§l이전 GUI로 돌아가기")
        .loreIB(" §7└ Category List를 나열한 GUI로 돌아갑니다.", 2)
    private val listener = ListenUp()



    // NodeCategory - NodeItem 에 해당하는 GUI 띄우기
    fun openCurrentNodeListGui(p: Player, parentNode: NodeCategory, page: Int) {
        TreeUtil.MetaViewingGuiParentNodeUtil.setViewingGuiParentNode(p, parentNode)

        val nodeItemInv = Bukkit.createInventory(null, 72, "$GUI_NAME_PREFIX${TreeUtil.ForCommon.getAppropriateGuiName(parentNode, NODE_TYPE_NAME)}")
        setUpPageToNodeItemGui(p, nodeItemInv, 1, getViewingGuiCurrentNodeList(p))

        nodeItemInv.setItem(54, helpItem)  // Node 추가하는 법 안내 표지판 아이템
        nodeItemInv.setItem(57, goToPreviousPageBtn)  // 이전 페이지
        nodeItemInv.setItem(59, goToNextPageBtn)  // 다음 페이지
        // TODO: Test
        nodeItemInv.setItem(62, goToCategoryListGuiBtn)  // NodeCategory로 돌아가기 버튼
//        goToCategoryListGuiBtn.nameIB("GUI에 과연 변경된 이름이 보일까")
//        Bukkit.broadcastMessage("inv: ${nodeItemInv.getItem(62).hashCode()}")
//        Bukkit.broadcastMessage("original: ${goToCategoryListGuiBtn.hashCode()}")

        // GUI 식별용 색깔 아이템
        var totalChance = 0.0
        getViewingGuiCurrentNodeList(p).forEach { totalChance += it.chance }
        if (totalChance == 100.0) {
            itemForIdentificationInGuiBottom.nameIB("§7확률 합계 : §a&n${totalChance}%§a §2§l[GOOD]")
        } else {
            itemForIdentificationInGuiBottom.nameIB("§7확률 합계 : §c&n${totalChance}%§c §4§l[BAD]")
        }
        for (i in 63..71) {
            nodeItemInv.setItem(i, itemForIdentificationInGuiBottom)  // GUI 식별용 색깔 아이템
        }
        p.openInventory(nodeItemInv)
    }




    // per Player의 GUI Interact용 Listener
    private class ListenUp: Listener {
        init {
            Bukkit.getPluginManager().registerEvents(this, Main.plugin)
        }

        @EventHandler(priority = EventPriority.HIGH)
        fun onClick(e: InventoryClickEvent) {
            if (e.isCancelled || !e.whoClicked.isOp) return
            val p = e.whoClicked as Player
            if (e.inventory?.name?.startsWith(GUI_NAME_PREFIX) == true) {
                e.isCancelled = true
                checkViewingGuiParentNodeIsValid(p) // GUI 2차 검증. false일 시 내부에서 throw Exception

                // 아랫 인벤에서 Shift+Left Click한 아이템을 NodeItem List에 추가하기 (Anvil에서 확률은 추가로 받고)
                if (e.clickedInventory != e.inventory && e.currentItem != null && e.isShiftClick && e.isLeftClick) {
                    val itemToAdd = e.currentItem
                    AnvilGuiModerator.open(p, "1. $NODE_TYPE_NAME 확률 입력", { s_inputChance ->
                        try {
                            val inputChance = s_inputChance.toDouble()
                            p.sendMessage("§6§lIF: $NODE_TYPE_COLOR[$NODE_TYPE_NAME] §f${itemToAdd?.itemMeta?.displayName ?: itemToAdd.type.name} : $inputChance% §7를 새로 생성했습니다.")
                            howToCreateCurrentNodeObj.accept(p, itemToAdd, inputChance)
                            TreeDataMgr.DataSaveTaskRegister.tryToRegisterDataSaveTask()  // saveData Task 등록
                            openCurrentNodeListGui(p, getViewingGuiParentNode(p), getViewingGuiPage(p))
                        } catch (ignored: Exception) {
                            p.sendMessage("§6§lIF: §cDouble §7타입을 입력해야 합니다.")
                        }
                    }, {
                        Bukkit.getScheduler().runTask(Main.plugin) {
                            openCurrentNodeListGui(p, getViewingGuiParentNode(p), getViewingGuiPage(p))
                        }
                    }, itemToAdd.clone())
                }


                // 윗 인벤 클릭
                if (e.clickedInventory == e.inventory && e.currentItem != null) {
                    // 인벤 왼클릭
                    if (e.isLeftClick && !e.isShiftClick) {
                        // 이전 페이지
                        if (e.currentItem.isSimilar(goToPreviousPageBtn)) {
                            if (getViewingGuiPage(p) <= 1) {
                                p.sendMessage("§6§lIF: §7현재 페이지가 §f첫 번째 §7페이지입니다.")
                                return
                            }
                            setUpPageToNodeItemGui(p, e.clickedInventory, getViewingGuiPage(p)-1, getViewingGuiCurrentNodeList(p))
                        }
                        // 다음 페이지
                        else if (e.currentItem.isSimilar(goToNextPageBtn)) {
                            if (((getViewingGuiCurrentNodeList(p).size-1) / 54)+1 < getViewingGuiPage(p)+1) {
                                p.sendMessage("§6§lIF: §7현재 페이지가 §f마지막 §7페이지입니다.")
                                return
                            }
                            setUpPageToNodeItemGui(p, e.clickedInventory, getViewingGuiPage(p)+1, getViewingGuiCurrentNodeList(p))
                        }

                        // NodeCategory로 돌아가기 버튼 왼클릭
                        // TODO: GUI에 ItemStack 둘 때 clone돼서 두어지나, 이거 확인하려면
                        //  val item = 대충 아이템 -> GUI에 item 넣기 -> item을 수정하면 GUI에서도 수정되는지 확인
                        else if (e.currentItem.isSimilar(goToCategoryListGuiBtn)) {
                            NodeCategoryListMgr.openCurrentNodeListGui(p, getViewingGuiParentNode(p).parentNode)
                        }
                    }

                    // NodeItem 아이템 우클릭 시 확률 변경
                    else if (e.isRightClick && NodeItemUuidAPI.hasUniqueID(e.currentItem)) {
                        val clickedNode = Util.getObjFromGuiItem(e.currentItem, getViewingGuiCurrentNodeList(p))
                        AnvilGuiModerator.open(p, "Edit) $NODE_TYPE_NAME 확률 입력.", { s_input ->
                            try {
                                val input = s_input.toDouble()
                                p.sendMessage("§6§lIF: ${NODE_TYPE_COLOR}[${NODE_TYPE_NAME}] §7${clickedNode.displayName} §7의 확률을 §f$s_input% §7로 변경했습니다.")
                                clickedNode.chance = input
                                TreeDataMgr.DataSaveTaskRegister.tryToRegisterDataSaveTask()  // saveData Task 등록
                                // 인벤은 아래의 onClose()가 열어줌
                            } catch (ignored: Exception) {
                                p.sendMessage("§6§lIF: §cDouble §7타입을 입력해야 합니다.")
                            }
                        }, {
                            Bukkit.getScheduler().runTask(Main.plugin) {
                                openCurrentNodeListGui(p, getViewingGuiParentNode(p), getViewingGuiPage(p))
                            }
                        })
                    }

                    // NodeItem 아이템 쉬프트 왼클릭 시 삭제
                    else if (e.isShiftClick && e.isLeftClick && NodeItemUuidAPI.hasUniqueID(e.currentItem)) {
                        val clickedNode = Util.getObjFromGuiItem(e.currentItem, getViewingGuiCurrentNodeList(p))
                        // 2차 확인 (다만, 삭제가 잦은 LeafItem의 경우는 SHIFT+왼클 으로 바로 삭제할 수 있게끔)
                        ConfirmGuiModerator.open(p, "§f정말 ${NODE_TYPE_COLOR}[${NODE_TYPE_NAME}] §f${clickedNode.displayName} §f을(를) 삭제합니까?",
                            listOf(), {
                                getViewingGuiParentNode(p).childNodeList.remove(clickedNode)
                                p.sendMessage("§6§lIF: ${NODE_TYPE_COLOR}[${NODE_TYPE_NAME}] §f${clickedNode.displayName} §7을(를) 삭제했습니다.")
                                TreeDataMgr.DataSaveTaskRegister.tryToRegisterDataSaveTask()  // saveData Task 등록
                                // 인벤은 아래의 onClose()가 열어줌
                            }, {
                                Bukkit.getScheduler().runTask(Main.plugin) {
                                    openCurrentNodeListGui(p, getViewingGuiParentNode(p), getViewingGuiPage(p))
                                }
                            })
                    }
                }
            }
        }
    }




    // Util
    private object Util {
        fun makeItemForGuiDisplay(currentNodeObj: NodeItemAbstract): ItemStack {
            val itemForDisplay = currentNodeObj.item.clone()
            NodeItemUuidAPI.carveSpecificUniqueId(itemForDisplay, currentNodeObj.uuid)

            val tempStoredLore = itemForDisplay.lore
            itemForDisplay.itemMeta = run {
                val meta = itemForDisplay.itemMeta
                meta.lore = listOf()
                meta
            }
            itemForDisplay.nameIB("${NodeItemAbstract.NOTATION_COLOR}[${NodeItemAbstract.NOTATION_NAME}] §f${currentNodeObj.displayName}")
                .loreIB(" §6└ Chance: ${currentNodeObj.chance}%")
                .loreIB(" §7└ UUID: ${currentNodeObj.uuid}")
                .loreIB("")
                .loreIB("      §9§lRight Click §7: 해당 Item의 §6확률§7을 변경합니다.")
                .loreIB("§9§lShift§7+§9§lLeft Click §7: 해당 Item을 §4삭제§7합니다.")
                .loreIB("${NodeItemAbstract.NOTATION_COLOR}§m                                                      ", 2)
                .loreIB("")
            // 원래 아이템 Lore을 밑에 부착
            if (tempStoredLore != null) {
                itemForDisplay.loreIB(tempStoredLore)
            }

            return itemForDisplay
        }

        // 위에서 생성한 아이템을 GUI에서 클릭했을 때, 그걸 다시 NodeRank, NodeCategory, NodeItem으로 바꿔주는 역할
        fun getObjFromGuiItem(item: ItemStack, whereToFind: List<NodeItemAbstract>): NodeItemAbstract {
            val clickedNodeUUID = NodeItemUuidAPI.getUniqueID(item)
            return searchObjInGivenListWithName(clickedNodeUUID, whereToFind)
        }
        fun searchObjInGivenListWithName(nodeUUID: UUID, whereToFind: List<NodeItemAbstract>): NodeItemAbstract {
            whereToFind.forEach {
                if (it.uuid == nodeUUID) {
                    return it
                }
            }

            // 로그용 코드
            val whereToFindLog = StringBuilder("")
            whereToFind.forEach {
                whereToFindLog.append("'${it.uuid}'")
            }
            throw Exception("'$nodeUUID' [NodeItem] 에 해당하는 object를 찾을 수 없습니다.   $whereToFindLog")
        }
    }


    // NodeItem만을 위한 UuidAPI (Item List GUI에서 식별용으로 사용)
    private object NodeItemUuidAPI {
        private const val UUID_KEY = "NodeItem-UUID"

        /**
         * Item List GUI에서 식별용으로 사용
         */
        fun carveSpecificUniqueId(item: ItemStack, uuid: UUID) {
            if (hasUniqueID(item)) throw Exception()
            val nbti = NbtItem(item)
            nbti.tag.setString(UUID_KEY, uuid.toString())
            nbti.applyToOriginal()
        }

        fun hasUniqueID(item: ItemStack): Boolean {
            if (item.type == Material.AIR) return false
            val nbti = NbtItem(item)
            return nbti.tag.hasKey(UUID_KEY)
        }

        fun getUniqueID(item: ItemStack): UUID {
            val nbti = NbtItem(item)
            if (!nbti.tag.hasKey(UUID_KEY)) throw Exception()
            return UUID.fromString(nbti.tag.getString(UUID_KEY))
        }
    }
}