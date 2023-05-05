package kr.sul.itemfarming.location

import kr.sul.itemfarming.dynmap.DisplayMarkerContinuously
import kr.sul.itemfarming.dynmap.MarkerParams
import org.bukkit.Bukkit
import org.bukkit.Location
import org.dynmap.markers.AreaMarker

class LocationPool(
    val name: String,
    locations: List<Location>,
    val dynmapArea: AreaMarker?
) {
    val locations = locations.map { it.toBlockLocation() }.toMutableList()
    private val canBeBorrowed = ArrayList(locations)

    fun borrowLocation(): Location? {
        return try {
            val loc = canBeBorrowed.random()
            canBeBorrowed.remove(loc)
            loc
        } catch (e: NoSuchElementException) {
            // 중도에 location이 삭제되면 loc 개수가 모자랄 수 있음
            null
        }
    }
    fun returnLocation(loc: Location) {
        canBeBorrowed.add(loc)
    }

    // 중도에 Command에 의해 추가된 location
    fun addLocation(loc: Location) {
        locations.add(loc)
        canBeBorrowed.add(loc)
    }
    // 중도에 Command에 의해 삭제된 location
    fun deleteLocationPermanently(loc: Location): Boolean {
        if (!locations.contains(loc)) return false
        locations.remove(loc)
        // 이미 어떠한 FarmingThing이 사용중인 위치일 때
        if (!canBeBorrowed.contains(loc)) {
            // 위치 삭제 이벤트 발생시켜서 ItemFarming 객체들이 확인하게끔 함
            Bukkit.getPluginManager().callEvent(LocationDeletionEvent(this, loc))
        }
        canBeBorrowed.remove(loc)
        return true
    }

    fun displayAllLocationWithMarkerForAWhile() {
        DisplayMarkerContinuously.displayMarker("Debug", {
            return@displayMarker locations.map {
                val markerIconId = if (canBeBorrowed.contains(it)) { "redflag" } else { "greenflag" }
                MarkerParams(it, markerIconId, it.hashCode().toString(), "I'm in '${dynmapArea?.markerID ?: "NO-WHERE(X)"}'")
            }
        }, 60*20)
    }




    enum class DeleteResult {
        DELETED,
        DELETED_BUT_IN_USE,
        NOT_EXIST;
    }
}