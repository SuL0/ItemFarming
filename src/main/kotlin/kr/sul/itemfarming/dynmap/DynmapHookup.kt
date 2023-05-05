package kr.sul.itemfarming.dynmap

import kr.sul.servercore.util.MsgPrefix
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.dynmap.DynmapAPI
import org.dynmap.markers.AreaMarker
import org.dynmap.markers.Marker

object DynmapHookup {
    // TODO 에어드랍 나타났을 시 지도에 아이콘을 띄워야 함
    val dynmapPlugin = Bukkit.getPluginManager().getPlugin("dynmap") as (DynmapAPI)
    private val areaMarkerSet = dynmapPlugin.markerAPI.getMarkerSet("Farming") ?: run {
        return@run dynmapPlugin.markerAPI.createMarkerSet("Farming", "Farming", null, true).apply {
            this.hideByDefault = false
        }
    }
    fun getArea(areaName: String?): AreaMarker? {
        if (areaName == null || areaName == "") return null
        return areaMarkerSet.findAreaMarker(areaName)
    }

    fun createMarker(loc: Location, markerSetName: String, markerIconId: String, id: String, label: String): Marker {
        val markerSet = dynmapPlugin.markerAPI.getMarkerSet(markerSetName) ?: run {
            return@run dynmapPlugin.markerAPI.createMarkerSet(markerSetName, markerSetName, null, false) // the fourth parameter represents should marker be persistent or not
        }
        markerSet.hideByDefault = true
        val icon = dynmapPlugin.markerAPI.getMarkerIcon(markerIconId)
        /**
         * id - ID of the marker - must be unique within the set: if null, unique ID is generated
         * label - Label for the marker (plain text)
         * world's name, x,y,z, icon - Icon for the marker
         * is_persistent - if true, marker is persistent (saved and reloaded on restart).  If set is not persistent, this must be false.
         */

        return markerSet.createMarker(id, label, loc.world.name, loc.x, loc.y, loc.z, icon, false)
    }
    fun Marker.deleteAfter(tick: Long): Marker {
        Bukkit.getScheduler().runTaskLater(dynmapPlugin as Plugin, { ->
            this.deleteMarker()
        }, tick)
        return this
    }



    private val tempCornerStore = hashMapOf<Player, ArrayList<Location>>()
    fun addCorner(p: Player) {
        if (!tempCornerStore.containsKey(p)) {
            tempCornerStore[p] = arrayListOf()
        }
        val loc = p.location.toBlockLocation()
        if (tempCornerStore[p]!!.isNotEmpty() && tempCornerStore[p]!!.first().world != loc.world) {
            p.sendMessage("${MsgPrefix.get("FARMING")}§f다른 월드의 corner을 추가하려면 먼저 §c/itemfarming clearcorners§f를 진행해 주세요")
            return
        }
        tempCornerStore[p]!!.add(loc)
        p.sendMessage("${MsgPrefix.get("FARMING")}§fAdded corner #${tempCornerStore[p]!!.size} at (${loc.x}, ${loc.y}, ${loc.z}) to list")
    }
    fun clearCorners(p: Player) {
        tempCornerStore[p]?.clear()
        p.sendMessage("${MsgPrefix.get("FARMING")}§fCleared corner list")
    }
    fun addArea(p: Player, id: String, label: String) {
        if (!tempCornerStore.contains(p)) {
            p.sendMessage("${MsgPrefix.get("FARMING")}§f/itemfarming addcorner 을 통해 §c최소 2개의 corner§f을 먼저 추가해 주세요")
            return
        }
        areaMarkerSet.createAreaMarker(id, label, false, tempCornerStore[p]!!.first().world.name,
            tempCornerStore[p]!!.map { it.x }.toDoubleArray(), tempCornerStore[p]!!.map { it.z }.toDoubleArray(), true)
        tempCornerStore[p]!!.clear()
        p.sendMessage("${MsgPrefix.get("FARMING")}§fAdded area id:'${id}' label:'${label}'")
    }
}