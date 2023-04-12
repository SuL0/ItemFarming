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
        private val material: Material
    ): ItemContainer(), Listener {
        override var inventory: Inventory = Bukkit.createInventory(null, 27)
        init {
            Bukkit.getPluginManager().registerEvents(this, plugin)
        }
        override fun place(locToPlace: Location) {
            locToPlace.block.type = material
            this.placedLoc = locToPlace.block.location
            inventory.close()
            inventory.clear()
        }

        override fun remove() {
            if (placedLoc == null) return
            placedLoc?.block?.type = Material.AIR
            placedLoc = null
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
                    && e.clickedBlock?.location?.blockX == placedLoc!!.blockX
                    && e.clickedBlock?.location?.blockY == placedLoc!!.blockY
                    && e.clickedBlock?.location?.blockZ == placedLoc!!.blockZ) {
                e.isCancelled = true
                onInteract?.accept(e)
            }
        }

        // TODO 인벤토리 닫았을 때 남아있는 아이템을 드랍시켜줄까 생각이 들긴 한데, 아직은 없긴 하지만 클릭 불가능한 데코레이션 아이템까지 떨어질 수 있으니 주의해야 함
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