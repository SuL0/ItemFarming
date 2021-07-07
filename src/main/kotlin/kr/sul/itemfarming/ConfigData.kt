package kr.sul.itemfarming

import org.bukkit.World
import kotlin.random.Random

class ConfigData(val world: World,
                 val categoryDropNumRage: DropNumRange,
                 val itemDropNumRange: DropNumRange) {


    data class DropNumRange(val min: Int, val max:Int) {
        fun random(): Int {
            return Random.nextInt(min, max+1)
        }

        companion object {
            fun convert(str: String): DropNumRange? {
                return try {
                    val split = str.split("~")
                    val min = split[0].toInt()
                    val max = split[1].toInt()
                    DropNumRange(min, max)
                } catch (ignored: Exception) {
                    null
                }
            }
        }
    }
}