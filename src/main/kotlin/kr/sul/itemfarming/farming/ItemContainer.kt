package kr.sul.itemfarming.farming

import kr.sul.Main.Companion.plugin
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer


abstract class ItemContainer {

    class BlockType(
        override var placedLoc: Location?,
        val material: Material
    ): ItemContainer(), Listener {
        override var inventory: Inventory = Bukkit.createInventory(null, 27)
        init {
            Bukkit.getPluginManager().registerEvents(this, plugin)
        }
        override fun place(locToPlace: Location) {
            locToPlace.block.type = material
            this.placedLoc = locToPlace.block.location
            inventory.clear()
        }

        override fun remove() {
            if (placedLoc == null) return
            placedLoc?.block?.type = Material.AIR
            placedLoc = null
            inventory.clear()
        }

        override fun setItemsPretty(items: List<ItemStack>) {
            inventory.clear()
            for ((i, item) in items.withIndex()) {
                inventory.setItem(9+i, item)
            }
        }
        @EventHandler
        fun onInteract(e: PlayerInteractEvent) {
            if (placedLoc != null
                    && e.clickedBlock.location.blockX == placedLoc!!.blockX
                    && e.clickedBlock.location.blockY == placedLoc!!.blockY
                    && e.clickedBlock.location.blockZ == placedLoc!!.blockZ) {
                onInteract?.accept(e)
            }
        }
        @EventHandler
        fun onClose(e: InventoryCloseEvent) {
            if (e.inventory == inventory) {
                onClose?.accept(e)
            }
        }

    }


    abstract var placedLoc: Location?
    abstract var inventory: Inventory
    abstract fun place(locToPlace: Location)
    abstract fun remove()
    abstract fun setItemsPretty(items: List<ItemStack>)
    var onInteract: Consumer<PlayerInteractEvent>? = null
    var onClose: Consumer<InventoryCloseEvent>? = null
}