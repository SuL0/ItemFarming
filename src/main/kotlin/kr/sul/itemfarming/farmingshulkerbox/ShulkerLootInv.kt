package kr.sul.itemfarming.farmingshulkerbox

import kr.sul.itemfarming.ConfigLoader
import kr.sul.itemfarming.Main.Companion.plugin
import kr.sul.itemfarming.setting.gui.TreeDataMgr
import kr.sul.itemfarming.setting.gui.TreeUtil
import kr.sul.itemfarming.setting.gui.node.NodeRank
import kr.sul.servercore.file.simplylog.LogLevel
import kr.sul.servercore.file.simplylog.SimplyLog
import kr.sul.servercore.util.ItemBuilder.durabilityIB
import kr.sul.servercore.util.ItemBuilder.nameIB
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

class ShulkerLootInv(private val p: Player): Listener {
    private val lootInv: Inventory

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)

        if (TreeDataMgr.rootNodeList.size >= 1) {
            // GUI에 아이템 채우기
            // TODO: 아이템 중복 드랍 방지
            val rank = pickAtRandom(TreeDataMgr.rootNodeList)!!
            p.sendMessage("§6§lIF: §7당신은 §f${rank.name} §7등급 셜커 상자를 발견했습니다.")
            lootInv = Bukkit.createInventory(null, 27, "${rank.name} §0등급 셜커 소지품")
            decorateShulkerGuiBasedOnRank(rank, lootInv)

            // 카테고리 - 아이템
            for (c in 0 until ConfigLoader.categoryDropNumRange.random()) {
                val category = pickAtRandom(rank.childNodeList) ?: continue
                for (i in 0 until ConfigLoader.itemDropNumRange.random()) {
                    val item = pickAtRandom(category.childNodeList) ?: continue
                    lootInv.addItemRandomly(item.item)
                }
            }
        } else {
            lootInv = Bukkit.createInventory(null, 27, "§4§lError in FarmingShulker")
        }
    }
    fun open() {
        p.openInventory(lootInv)
    }


    @EventHandler(priority = EventPriority.HIGH)
    fun onCloseShulkerLootInv(e: InventoryCloseEvent) {
        if (e.inventory == lootInv) {
            HandlerList.unregisterAll(this)
        }
    }

    @EventHandler
    fun onClickDecoration(e: InventoryClickEvent) {
        if (e.clickedInventory == lootInv && e.currentItem?.itemMeta?.displayName == DECORATE_ITEM_NAME) {
            e.isCancelled = true
        }
    }





    companion object {
        const val DECORATE_ITEM_NAME = "§X§X§X"
        private fun decorateShulkerGuiBasedOnRank(rank: NodeRank, inv: Inventory) {
            val decorateItem = when (rank.name) {
                "고급" -> ItemStack(Material.DIAMOND_PICKAXE).durabilityIB(1527).nameIB(DECORATE_ITEM_NAME)
                "희귀" -> ItemStack(Material.DIAMOND_PICKAXE).durabilityIB(1526).nameIB(DECORATE_ITEM_NAME)
                "영웅" -> ItemStack(Material.DIAMOND_PICKAXE).durabilityIB(1525).nameIB(DECORATE_ITEM_NAME)
                "전설" -> ItemStack(Material.DIAMOND_PICKAXE).durabilityIB(1524).nameIB(DECORATE_ITEM_NAME)
                "신화" -> ItemStack(Material.DIAMOND_PICKAXE).durabilityIB(1523).nameIB(DECORATE_ITEM_NAME)
                "고대" -> ItemStack(Material.DIAMOND_PICKAXE).durabilityIB(1522).nameIB(DECORATE_ITEM_NAME)
                "불가사의" -> ItemStack(Material.DIAMOND_PICKAXE).durabilityIB(1521).nameIB(DECORATE_ITEM_NAME)
                else -> ItemStack(Material.DIAMOND_PICKAXE).durabilityIB(1533).nameIB(DECORATE_ITEM_NAME)  // 일반
            }
            inv.setItem(26, decorateItem)
        }

        private fun <T: Any> pickAtRandom(nodeList: List<T>): T? {
            val random = Random.nextDouble(100.0)+1
            var stackChance = 0.0
            for (node in nodeList) {
                stackChance += TreeUtil.ForCommon.getNodeChance(node)
                if (random <= stackChance) {
                    return node
                }
            }
            return null
        }

        private fun Inventory.addItemRandomly(item: ItemStack) {
            if (this.firstEmpty() == -1) {
                SimplyLog.log(LogLevel.ERROR_LOW, plugin, "파밍 상자에 아이템을 넣으려 했으나, 남는 slot이 부족한 관계로 무시되었음.")
                return
            }

            val emptySlots = arrayListOf<Int>()
            for (i in 0 until this.size) {
                if (this.getItem(i) == null) emptySlots.add(i)
            }

            this.setItem(emptySlots.random(), item)
        }
    }
}