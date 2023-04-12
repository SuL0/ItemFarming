package kr.sul.itemfarming.location

import kr.sul.Main.Companion.plugin
import kr.sul.itemfarming.dynmap.DynmapHookup
import kr.sul.servercore.extensionfunction.BukkitTaskFunction.cancelAfter
import org.bukkit.Location
import org.dynmap.markers.AreaMarker
import org.dynmap.markers.Marker

class LocationPool(
    val name: String,
    locations: List<Location>,
    val dynmapArea: AreaMarker?
) {
    val locations = locations.map { it.toBlockLocation() }.toMutableList()
    private val canBeBorrowed = ArrayList(locations)
    private val previousMarkers = arrayListOf<Marker>()

    fun borrowLocation(): Location {
        val loc = canBeBorrowed.random()
        canBeBorrowed.remove(loc)
        return loc
    }
    fun returnLocation(loc: Location) {
        canBeBorrowed.add(loc)
    }

    fun displayAllLocationWithMarkerForAWhile() {
        DynmapHookup.displayMarker(this, "Debug") {
            // paramGetter[0~2]: (markerIconName, id, label)
            return@displayMarker locations.associateWith {
                val markerIconName = if (canBeBorrowed.contains(it)) {
                    "redflag"
                } else {
                    "greenflag"
                }
                return@associateWith listOf(markerIconName, it.toString(), "is in ${dynmapArea?.markerID ?: "NO-WHERE(X)"}")
            }
        }.cancelAfter(plugin, 60*20)
    }
}