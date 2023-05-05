package kr.sul.itemfarming.dynmap

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.dynmap.markers.Marker
import java.util.*
import kotlin.collections.ArrayList

object DisplayMarkerContinuously {
    private val currentMarkerList = hashMapOf<UUID, ArrayList<Marker>>() // sessionId, marker list

    /**
     * @param expireAt Marker 을 표시하는 시간 (null: 무제한)
     */
    fun displayMarker(markerSetName: String, markersParamGetter: ()->(List<MarkerParams>), expireAt: Long?) {
        val sessionId = UUID.randomUUID()  // 함수를 호출할 때마다 새로운 uuid 부여

        val task = Bukkit.getScheduler().runTaskTimer(DynmapHookup.dynmapPlugin as Plugin, { ->
            applyMarkerChanges(sessionId, markerSetName, markersParamGetter.invoke())
        }, 20, 40)

        if (expireAt != null) {
            Bukkit.getScheduler().runTaskLater(DynmapHookup.dynmapPlugin as Plugin, { ->
                task.cancel()
                currentMarkerList[sessionId]!!.forEach { marker ->
                    marker.deleteMarker()
                }
                currentMarkerList.remove(sessionId)
            }, expireAt)
        }
    }



    private fun applyMarkerChanges(sessionId: UUID, markerSetName: String, markersParams: List<MarkerParams>) {
        val markersParams = markersParams.toMutableList()
        val existingMarkers = currentMarkerList[sessionId] ?: run {
            currentMarkerList[sessionId] = arrayListOf()
            currentMarkerList[sessionId]!!
        }

        // compare changes
        val additionList = arrayListOf<MarkerParams>()
        val deletionList = arrayListOf<Marker>()
        existingMarkers.forEach { existingMarker ->
            val find = markersParams.find { it.id == existingMarker.markerID }
            if (find != null) {
                // 변경점이 있는 경우 Marker에 적용
                find.compareAndCorrectDifference(existingMarker)
                markersParams.remove(find)
            } else {
                deletionList.add(existingMarker)
            }
        }
        additionList.addAll(markersParams)


        // apply changes
        additionList.forEach {
            val marker = it.createMarker(markerSetName)
            currentMarkerList[sessionId]!!.add(marker)
        }
        deletionList.forEach {
            currentMarkerList[sessionId]!!.remove(it)
            it.deleteMarker()
        }
    }
}