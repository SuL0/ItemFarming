package kr.sul.itemfarming.farmingshulkerbox

import kr.sul.itemfarming.ConfigLoader
import kr.sul.itemfarming.Main.Companion.plugin
import kr.sul.itemfarming.setting.gui.TreeDataMgr
import kr.sul.itemfarming.setting.gui.TreeUtil
import kr.sul.itemfarming.setting.gui.node.NodeCategory
import kr.sul.itemfarming.setting.gui.node.NodeRank
import kr.sul.servercore.file.simplylog.LogLevel
import kr.sul.servercore.file.simplylog.SimplyLog
import kr.sul.servercore.util.ItemBuilder.durabilityIB
import kr.sul.servercore.util.ItemBuilder.nameIB
import org.bukkit.Bukkit
import org.bukkit.Location
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

class ShulkerLootInv(private val p: Player, private val loc: Location): Listener {
    private val lootInv: Inventory

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)

        val thisWorldConfigData = ConfigLoader.configDataList[loc.world]!!   // 해당 셜커의 월드에 해당하는 ConfigData를 불러옴

        if (TreeDataMgr.rootNodeList.size >= 1) {
            // GUI에 아이템 채우기
            val rank = pickAtRandom(TreeDataMgr.rootNodeList)
            p.sendMessage("§6§lIF: §7당신은 §f${rank.name} §7등급 셜커 상자를 발견했습니다.")
            lootInv = Bukkit.createInventory(null, 27, "${rank.name} §0등급 셜커 소지품")
            decorateShulkerGuiBasedOnRank(rank, lootInv)

            val categoryDuplicatePreventer = hashSetOf<NodeCategory>()
            // 카테고리 - 아이템
            for (c in 0 until thisWorldConfigData.categoryDropNumRage.random()) {
                var randCategory: NodeCategory
                var whileCnt = 0
                while(true) {  // 카테고리 중복되게 나오는 것 방지
                    randCategory = pickAtRandom(rank.childNodeList)
                    if (!categoryDuplicatePreventer.contains(randCategory)) {
                        categoryDuplicatePreventer.add(randCategory)
                        break
                    }
                    if (whileCnt++ >= 100) {
                        SimplyLog.log(LogLevel.ERROR_CRITICAL, plugin, "Category 중복 방지 코드가 무한 루프에 빠졌음")
                        throw Exception()
                    }
                }
                for (i in 0 until thisWorldConfigData.itemDropNumRange.random()) {
                    val randItem = pickAtRandom(randCategory.childNodeList)
                    lootInv.addItemRandomly(randItem.item)
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
            for (loopItem in e.inventory.contents.filterNotNull()) {  // GUI에 남은 템 바닥에 드랍
                if (loopItem.itemMeta?.displayName == DECORATE_ITEM_NAME) {  // 장식용 아이템은 제외
                    continue
                }
                loc.world.dropItem(loc, loopItem)
            }
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
//            val decorateItem = when (rank.name) {
//                "고급" -> ItemStack(Material.DIAMOND_PICKAXE).durabilityIB(1527).nameIB(DECORATE_ITEM_NAME)
//                "희귀" -> ItemStack(Material.DIAMOND_PICKAXE).durabilityIB(1526).nameIB(DECORATE_ITEM_NAME)
//                "영웅" -> ItemStack(Material.DIAMOND_PICKAXE).durabilityIB(1525).nameIB(DECORATE_ITEM_NAME)
//                "전설" -> ItemStack(Material.DIAMOND_PICKAXE).durabilityIB(1524).nameIB(DECORATE_ITEM_NAME)
//                "신화" -> ItemStack(Material.DIAMOND_PICKAXE).durabilityIB(1523).nameIB(DECORATE_ITEM_NAME)
//                "고대" -> ItemStack(Material.DIAMOND_PICKAXE).durabilityIB(1522).nameIB(DECORATE_ITEM_NAME)
//                "불가사의" -> ItemStack(Material.DIAMOND_PICKAXE).durabilityIB(1521).nameIB(DECORATE_ITEM_NAME)
//                else -> ItemStack(Material.DIAMOND_PICKAXE).durabilityIB(1533).nameIB(DECORATE_ITEM_NAME)  // 일반
//            }
//            inv.setItem(26, decorateItem)
        }

        // 만약 제비뽑기를 해서 12% 확률에 당첨되는 사람에게 뭔가를 주려고 한다하면, 제비뽑기를 1~100개를 만들어야 함
        // 그리고 만약 12.55% 확률에 당첨을 구현하려면 -> 1~10000개  =  0.01~100
        //
        // 결론 : 0+0.1^소수점~100 인 상황이 아니라(위의 일반적 상황), 0~100-0.1^소수점   인 상황이므로 난수 <= 확률 이 아닌 난수 < 확률 이 맞음.  (헷갈리면 1~10일 때 확률 구현과 0~9일 때 확률 구현을 생각해보면 쉬움)
        private fun <T: Any> pickAtRandom(nodeList: List<T>): T {
            val random = Random.nextDouble(100.0)
            var stackChance = 0.0
            for (node in nodeList) {
                stackChance += TreeUtil.ForCommon.getNodeChance(node)
                if (random < stackChance) {
                    return node
                }
            }
            throw Exception("$random")
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