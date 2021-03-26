package kr.sul.itemfarming.setting.gui.node

import kr.sul.itemfarming.setting.gui.TreeUtil
import kr.sul.itemfarming.setting.gui.nodecompponent.ParentNodeContainer
import kr.sul.servercore.util.ItemBuilder.loreIB
import kr.sul.servercore.util.ItemBuilder.nameIB
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import java.util.*

// Leaf Node
class NodeItem: ParentNodeContainer<NodeCategory> {
    val uuid: UUID // 파일에서 serializedItem을 가리키는 용도
    val itemDisplayName: String
    val item: ItemStack
    val chance: Double
    override val parentNode: NodeCategory

    // default constructor
    constructor(parentNode: NodeCategory, item: ItemStack, chance: Double) : this(parentNode, UUID.randomUUID(), item, chance)

    // 객체 load 시
    constructor(parentNode: NodeCategory, uuid: UUID, item: ItemStack, chance: Double) {
        this.parentNode = parentNode
        this.uuid = uuid
        this.itemDisplayName = item.itemMeta?.displayName ?: item.type.name
        this.item = item
        this.chance = chance
    }
    companion object {
        const val NOTATION_NAME = "Item"
    }
}


// 쉬프트+왼클: Confirm 후 추가

// 아이템 상호작용>
// 우클릭: 확률 바꾸기 (Rank, Category는 이름 바꾸기)
// 쉬프트+왼클: Confirm 후 삭제

// 페이지 추가
object LeaftItemListMgr: Listener {
    private const val GUI_NAME = "§f!IF: Category§c->§fItem List"
    private val CURRENT_GUI_NODE_CLASS = NodeCategory::class.java
    private val CATEGORY_ITEM_MATERIAL = Material.WOOL
    private val helpItem = ItemStack(Material.WALL_SIGN).nameIB("§e§l도움말")
        .loreIB("§c§l- 윗 인벤 -")
        .loreIB(" §e- §f§lShift+LeftClick §7: 아이템 삭제")
        .loreIB(" §e- §f§l      LeftClick §7: 아이템 확률 설정")
        .loreIB("")
        .loreIB("§c§l- 아래 인벤 -")
        .loreIB(" §e- §f§lShift+LeftClick §7: 아이템 추가")
    private val goBackButton = ItemStack(Material.CHORUS_FRUIT).nameIB("§2뒤로가기").loreIB(" §7└ Rank List GUI로 되돌아갑니다.", 2)

    fun openCategoryListGui(p: Player, currentGuiNode: NodeCategory) {
        // Metadata에 currentGuiNode 저장
        TreeUtil.MetaViewingGuiParentNodeUtil.setViewingGuiParentNode(p, currentGuiNode)

        val inv = Bukkit.createInventory(null, 72, GUI_NAME)
        currentGuiNode.childNodeList.forEach {
            inv.addItem(it.item)
        }

        // Nav Bar 버튼 추가
        inv.setItem(54, helpItem)
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
//                      val clickedCategoryName = ChatColor.stripColor(e.currentItem.itemMeta.displayName)
//                      val clickedCategory = NodeCategory.NodeCategoryListMgr.getCategoryObjectFromName(clickedCategoryName, currentGuiNode)

                    // !: 아이템 확률 설정으로 넘어가기
//                      NodeItemGuiMgr.openItemListGui(p, clickedCategory)
                }

                // 뒤로가기
                else if (e.currentItem == goBackButton) {
                    NodeRankListMgr.openCurrentNodeListGui(p, null)
                }
            }
        }
    }
}