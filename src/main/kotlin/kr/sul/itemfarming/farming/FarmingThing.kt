package kr.sul.itemfarming.farming

import kr.sul.Main.Companion.plugin
import kr.sul.itemfarming.FarmingThingConfiguration
import kr.sul.itemfarming.dynmap.DisplayFarmingThingOnDynmap
import kr.sul.itemfarming.location.LocationDeletionEvent
import kr.sul.itemfarming.location.LocationPool
import kr.sul.servercore.util.MsgPrefix
import kr.sul.servercore.util.UptimeBasedOnTick
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import java.util.function.Consumer

class FarmingThing(
    private val itemContainer: ItemContainer,
    private val itemChanceWrapper: ItemChanceWrapper,
    private val locationPool: LocationPool,
    fillItemsCooldownTerm: Long,
    private val messageWhenItOpened: String?,
    private val soundWhenItOpened: Sound?,
    private val makeItemContainerDespawnedWhenItOpened: Boolean,
    private val makeItemContainerMoveWhenItOpened: Boolean
): Listener {
    private var timeToRefill: Long = 0
    private val fillItemsCooldownTerm = fillItemsCooldownTerm*20 // 초

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
        itemContainer.onInteract = Consumer { e ->
            onInteract(e.player)
        }
        itemContainer.onClose = Consumer { e ->
            onCloseItemContainerGUI(e)
        }
        itemContainer.place(locationPool.borrowLocation()!!)
        regenerateItems()
        onRegen()
    }

    private fun onInteract(p: Player) {
        p.openInventory(itemContainer.inventory)
        if (messageWhenItOpened != null) {
            p.sendMessage("${MsgPrefix.get("FARMING")}${messageWhenItOpened}")
        }
        if (soundWhenItOpened != null) {
            p.playSound(p.location, soundWhenItOpened, 1F, 1F)
        }
    }

    private fun onCloseItemContainerGUI(e: InventoryCloseEvent) {
        if (e.inventory.viewers.size > 1) { // 상자를 두 명이 열었을 수도 있기에
            return
        }
        onStolen()
        timeToRefill = UptimeBasedOnTick.uptimeBasedOnTick + fillItemsCooldownTerm
        if (makeItemContainerDespawnedWhenItOpened) {
            val placedLoc = itemContainer.placedLoc!!
            if (makeItemContainerMoveWhenItOpened) {
                locationPool.returnLocation(placedLoc)
            }
            itemContainer.remove()


            Bukkit.getScheduler().runTaskLater(plugin, { _ ->
                val locToPlace = if (makeItemContainerMoveWhenItOpened) {
                    locationPool.borrowLocation()!!
                } else {
                    placedLoc
                }
                itemContainer.place(locToPlace)
            }, timeToRefill)
        }
        Bukkit.getScheduler().runTaskLater(plugin, { _ ->
            regenerateItems()
            onRegen()
        }, timeToRefill+1)
    }

    private fun regenerateItems(): Boolean {
        if (UptimeBasedOnTick.uptimeBasedOnTick >= timeToRefill) {
            itemContainer.setItemsPretty(itemChanceWrapper.getRandomItem())
            return true
        }
        return false
    }

    // 부수효과 분리
    private fun onRegen() {
        DisplayFarmingThingOnDynmap.showOnMap(locationPool, this, this.itemContainer.placedLoc!!)
    }
    private fun onStolen() {
        DisplayFarmingThingOnDynmap.makeInvisibleOnMap(locationPool, this)
    }

    @EventHandler
    fun onLocationDeletionEvent(e: LocationDeletionEvent) {
        if (e.locationPool == this.locationPool
                && e.location == this.itemContainer.placedLoc) {
            this.destroy() // 위치를 이동시켜줄 수도 있긴 한데 버그 가능성 보이기도 하고, 어차피 실제 서버에서 중도에 삭제할 일 잘 없으니 대충 이리 처리함
        }
    }

    fun destroy() {
        // TODO 누군가 해당 파밍GUI를 열고 있다면 그냥 강제로 닫아도 문제없으려나
        FarmingThingConfiguration.allFarmingThings.remove(this)
        HandlerList.unregisterAll(this)
        itemContainer.destroy()
        DisplayFarmingThingOnDynmap.makeInvisibleOnMap(locationPool, this)
    }
}