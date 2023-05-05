package kr.sul.itemfarming.dynmap

import org.bukkit.Location
import org.dynmap.markers.Marker

class MarkerParams(
    private val loc: Location,
    private val markerIconId: String,
    val id: String,
    private val label: String
) {
    fun createMarker(markerSetName: String): Marker {
        return DynmapHookup.createMarker(loc, markerSetName, markerIconId, id, label)
    }
    
    fun compareAndCorrectDifference(existingMarker: Marker): Marker {
        if (loc.x != existingMarker.x || loc.y != existingMarker.y || loc.z != existingMarker.z) {
            existingMarker.setLocation(loc.world.name, loc.x, loc.y, loc.z)
        }
        if (markerIconId != existingMarker.markerIcon.markerIconID) {
            existingMarker.markerIcon = DynmapHookup.dynmapPlugin.markerAPI.getMarkerIcon(markerIconId)
        }
        if (label != existingMarker.label) {
            existingMarker.label = label
        }
        return existingMarker
    }
}