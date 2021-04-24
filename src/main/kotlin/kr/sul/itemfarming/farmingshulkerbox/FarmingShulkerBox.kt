package kr.sul.itemfarming.farmingshulkerbox

import kr.sul.itemfarming.setting.gui.TreeDataMgr
import kr.sul.itemfarming.setting.gui.TreeUtil
import kr.sul.itemfarming.setting.gui.node.NodeRank
import kr.sul.servercore.util.ItemBuilder.durabilityIB
import kr.sul.servercore.util.ItemBuilder.nameIB
import kr.sul.servercore.util.UptimeBasedOnTick
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.random.Random


// TODO: 셜커 몹과의 전투 -> 셜커 상자 구현
object FarmingShulkerBox: Listener {
    // value loaded from config
    val activeWorlds = arrayListOf<World>()   // NOTE: 쓰기(add)가 왜 없냐?

    data class DropNumRange(val min: Int, val max:Int) {
        fun random(): Int {
            return Random.nextInt(min, max+1)
        }
    }
    // ConfigLoader에서 덮어씀 걱정 ㄴㄴ
    var categoryDropNumRange = DropNumRange(0, 0)
    var itemDropNumRange = DropNumRange(0, 0)
    //
    private const val REGEN_DELAY = 30 // 초

    private val latestRegenTimeRecorder = HashMap<ShulkerBox, Long>()  // 셜커 상자 리젠시간 기록용


    @EventHandler(priority = EventPriority.HIGH)
    fun onOpenShulkerBox(e: PlayerInteractEvent) {
        if (e.isCancelled) return
        val p = e.player
        if (activeWorlds.contains(p.world)
                    && e.action == Action.RIGHT_CLICK_BLOCK && e.clickedBlock.state is ShulkerBox) {
            val shulkerBox = e.clickedBlock.state as ShulkerBox
            // 리젠 or 쿨타임 안내
            if (!latestRegenTimeRecorder.containsKey(shulkerBox) || (UptimeBasedOnTick.uptimeBasedOnTick - latestRegenTimeRecorder[shulkerBox]!!)/20 > REGEN_DELAY) {
                regenShulkerBox(p, shulkerBox)
            } else {
                val leftTime = REGEN_DELAY - (UptimeBasedOnTick.uptimeBasedOnTick - latestRegenTimeRecorder[shulkerBox]!!)/20
                p.sendMessage("§6§lIF: §7이미 누군가 열어본 셜커 상자입니다. §f[ ${leftTime}초 후 리젠 ]")
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onInventoryClick(e: InventoryClickEvent) {
        // TODO: DecorateItem 클릭 방지
    }




    private fun decorateShulkerGuiBasedOnRank(rank: NodeRank, shulker: ShulkerBox) {
        val decorateItem = when (rank.name) {
            "고급" -> ItemStack(Material.DIAMOND_PICKAXE).durabilityIB(1527).nameIB("§X§X§X")
            "희귀" -> ItemStack(Material.DIAMOND_PICKAXE).durabilityIB(1526).nameIB("§X§X§X")
            "영웅" -> ItemStack(Material.DIAMOND_PICKAXE).durabilityIB(1525).nameIB("§X§X§X")
            "전설" -> ItemStack(Material.DIAMOND_PICKAXE).durabilityIB(1524).nameIB("§X§X§X")
            "신화" -> ItemStack(Material.DIAMOND_PICKAXE).durabilityIB(1523).nameIB("§X§X§X")
            "고대" -> ItemStack(Material.DIAMOND_PICKAXE).durabilityIB(1522).nameIB("§X§X§X")
            "불가사의" -> ItemStack(Material.DIAMOND_PICKAXE).durabilityIB(1521).nameIB("§X§X§X")
            else -> ItemStack(Material.DIAMOND_PICKAXE).durabilityIB(1533).nameIB("§X§X§X")  // 일반
        }
        shulker.inventory.setItem(26, decorateItem)
    }



    // TODO: 중복 방지
    private fun regenShulkerBox(p: Player, shulker: ShulkerBox) {
        latestRegenTimeRecorder[shulker] = UptimeBasedOnTick.uptimeBasedOnTick  // 셜커 상자 리젠시간 기록
        shulker.inventory.clear()  // 셜커 상자 템 초기화

        if (TreeDataMgr.rootNodeList.size == 0) return
        // 셜커 상자 템 리젠
        val rank = pickAtRandom(TreeDataMgr.rootNodeList)!!
        p.sendMessage("§6§lIF: §7당신은 §f${rank.name} §7등급 셜커 상자를 발견했습니다.")
        shulker.customName = "${rank.name} §0등급 셜커 소지품"  // NOTE: 안되는데? https://www.digminecraft.com/data_tags/shulker.php
        for (c in 0 until categoryDropNumRange.random()) {
            val category = pickAtRandom(rank.childNodeList) ?: continue
            for (i in 0 until itemDropNumRange.random()) {
                val item = pickAtRandom(category.childNodeList) ?: continue
                shulker.inventory.addItem(item.item)
            }
        }

        // Rank에 맞춰서 상자 꾸미기
        decorateShulkerGuiBasedOnRank(rank, shulker)
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
}