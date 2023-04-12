package kr.sul.itemfarming.dynmap

import kr.sul.itemfarming.farming.FarmingThing
import kr.sul.itemfarming.location.LocationPool
import org.bukkit.Location

// 원래는 Map<FarmingThing, Location> 이었고  순회 1번
// 지금은 순회 2번
object DisplayFarmingThingOnDynmap {
    private val spotToShow = hashMapOf<LocationPool, ArrayList<Pair<FarmingThing, Location>>>()

    init {
        DynmapHookup.displayMarker(this, "Farming") {
            // paramGetter[0~2]: (markerIconName, id, label)
            return@displayMarker spotToShow.map { (t, u) ->
                u.map { it.second }.associateWith { listOf("greenflag", it.toString(), "상자") }
            }.reduce { acc, map -> acc.toMutableMap().apply { putAll(map) } }
        }
    }

    fun showOnMap(locationPool: LocationPool, farmingThing: FarmingThing, loc: Location) {
        if (!spotToShow.contains(locationPool)) {
            spotToShow[locationPool] = arrayListOf()
        }
        spotToShow[locationPool]!!.add(Pair(farmingThing, loc))
    }
    fun makeInvisibleOnMap(locationPool: LocationPool, farmingThing: FarmingThing) {
        if (!spotToShow.contains(locationPool)) return
        spotToShow[locationPool]!!.removeIf { it.first == farmingThing }
    }
}