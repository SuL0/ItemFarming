package kr.sul.itemfarming.farming

import kr.sul.Main.Companion.plugin
import kr.sul.servercore.util.UptimeBasedOnTick
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.Listener
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
        itemContainer.onInteract = Consumer { e ->
            onInteract(e.player)
        }
        itemContainer.onClose = Consumer { _ ->
            onCloseItemContainerGUI()
        }
    }


    fun destroy() {
        itemContainer.remove()
    }

    private fun onInteract(p: Player) {
        if (fillItemsWhenTimePassedEnough()) {
            p.openInventory(itemContainer.inventory)
            if (messageWhenItOpened != null) {
                p.sendMessage(messageWhenItOpened)
            }
            if (soundWhenItOpened != null) {
                p.playSound(p.location, soundWhenItOpened, 1F, 1F)
            }
        }
    }

    private fun onCloseItemContainerGUI() {
        if (makeItemContainerDespawnedWhenItOpened) {
            val placedLoc = itemContainer.placedLoc!!
            if (makeItemContainerMoveWhenItOpened) {
                locationPool.returnLocation(placedLoc)
            }
            itemContainer.remove()
            Bukkit.getScheduler().runTaskLater(plugin, {
                val locToPlace = if (makeItemContainerMoveWhenItOpened) {
                    locationPool.borrowLocation()
                } else {
                    placedLoc
                }
                itemContainer.place(locToPlace)
            }, timeToRefill)
        }
    }


    private fun fillItemsWhenTimePassedEnough(): Boolean {
        if (UptimeBasedOnTick.uptimeBasedOnTick >= timeToRefill) {
            timeToRefill = UptimeBasedOnTick.uptimeBasedOnTick + fillItemsCooldownTerm*20
            itemContainer.setItemsPretty(itemChanceWrapper.getRandomItem())
            return true
        }
        return false
    }
}