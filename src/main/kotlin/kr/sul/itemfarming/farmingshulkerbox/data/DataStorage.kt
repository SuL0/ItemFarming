package kr.sul.itemfarming.farmingshulkerbox.data

import org.bukkit.Location
import org.bukkit.World
import kotlin.random.Random

object DataStorage {
    val activeWorlds = arrayListOf<World>()  // 셜커 블럭 설치 Listen 용, shulkerBoxLocationsPerWorld 에서 활성 비활성 거르는용
    val shulkerBoxLocationsPerWorld = hashMapOf<String, ArrayList<SimpleLocation>>()  // WorldName, List<SimpleLocation>  - Json으로 바꿨을 때 깔끔한 형태

    // ConfigLoader에서 덮어씀 걱정 ㄴㄴ
    var categoryDropNumRange = DropNumRange(0, 0)
    var itemDropNumRange = DropNumRange(0, 0)
    //


    data class SimpleLocation(val x: Double, val y: Double, val z: Double) {  // 셜커 상자 위치 저장 전용
        companion object {
            // 중앙 위치로 변환 후, SimpleLocation으로 저장
            fun convertFromLoc(loc: Location): SimpleLocation {
                val centerLoc = loc.toCenterLocation()
                return SimpleLocation(centerLoc.x, centerLoc.y, centerLoc.z)
            }
        }
    }

    data class DropNumRange(val min: Int, val max:Int) {
        fun random(): Int {
            return Random.nextInt(min, max+1)
        }
    }
}