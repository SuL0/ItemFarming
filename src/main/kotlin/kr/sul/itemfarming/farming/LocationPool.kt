package kr.sul.itemfarming.farming

import org.bukkit.Location

class LocationPool(
    locations: List<Location>
) {
    val locations = locations.map { it.toBlockLocation() }
    private val canBeBorrowed = ArrayList(locations)

    fun borrowLocation(): Location {
        val loc = canBeBorrowed.random()
        canBeBorrowed.remove(loc)
        return loc
    }
    fun returnLocation(loc: Location) {
        canBeBorrowed.add(loc)
    }
}