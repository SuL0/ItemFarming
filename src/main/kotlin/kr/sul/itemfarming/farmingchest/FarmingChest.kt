package kr.sul.itemfarming.farmingchest

import kr.sul.itemfarming.setting.gui.TreeDataMgr
import kr.sul.itemfarming.setting.gui.TreeUtil
import kr.sul.servercore.util.UptimeBasedOnTick
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.random.Random


// TODO: 아이템 나올 개수, 대상 월드 config에서 설정하게끔
object FarmingChest: Listener {
    // value loaded from config
    val activeWorlds = arrayListOf<World>()

    data class DropNumRange(val min: Int, val max:Int) {
        fun random(): Int {
            return Random.nextInt(min, max+1)
        }
    }
    var categoryDropNumRange = DropNumRange(0, 0)
    var itemDropNumRange = DropNumRange(0, 0)
    //
    private const val REGEN_DELAY = 30 // 초

    private val latestRegenTimeRecorder = HashMap<Chest, Long>()  // 상자 리젠시간 기록용


    @EventHandler
    fun onOpenChest(e: PlayerInteractEvent) {
        val p = e.player
        if (activeWorlds.contains(p.world)
                    && e.action == Action.RIGHT_CLICK_BLOCK && e.clickedBlock.type == Material.CHEST) {
            val chest = e.clickedBlock.state as Chest
            if (!latestRegenTimeRecorder.containsKey(chest) || (UptimeBasedOnTick.uptimeBasedOnTick - latestRegenTimeRecorder[chest]!!)/20 > REGEN_DELAY) {
                regenChest(p, chest)
            } else {
                val leftTime = REGEN_DELAY - (UptimeBasedOnTick.uptimeBasedOnTick - latestRegenTimeRecorder[chest]!!)/20
                p.sendMessage("§6§lIF: §7이미 누군가 열어본 상자입니다. §f[ ${leftTime}초 후 리젠 ]")
            }
        }
    }
    private fun regenChest(p: Player, chest: Chest) {
        latestRegenTimeRecorder[chest] = UptimeBasedOnTick.uptimeBasedOnTick  // 상자 리젠시간 기록
        chest.blockInventory.clear()  // 상자 템 초기화

        if (TreeDataMgr.rootNodeList.size == 0) return
        // 상자 템 리젠
        val rank = pickAtRandom(TreeDataMgr.rootNodeList)!!
        p.sendMessage("§6§lIF: §7당신은 §f${rank.name} §7등급 상자를 발견했습니다.")
        chest.customName = "${rank.name} §0등급 상자"
        for (c in 0 until categoryDropNumRange.random()) {
            val category = pickAtRandom(rank.childNodeList) ?: continue
            for (i in 0 until itemDropNumRange.random()) {
                val item = pickAtRandom(category.childNodeList) ?: continue
                chest.blockInventory.addItem(item.item)
            }
        }
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