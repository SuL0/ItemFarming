package kr.sul.itemfarming.location

import org.bukkit.Location
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class LocationDeletionEvent(
    val locationPool: LocationPool,
    val location: Location
): Event() {
    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
    override fun getHandlers(): HandlerList {
        return handlerList
    }
}