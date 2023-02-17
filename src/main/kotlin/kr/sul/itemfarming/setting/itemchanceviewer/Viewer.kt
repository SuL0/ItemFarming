package kr.sul.itemfarming.setting.itemchanceviewer

import kr.sul.Main.Companion.plugin
import kr.sul.itemfarming.setting.itemchance.NodeData
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class Viewer(
    private val p: Player,
    initialNode: NodeData
): Listener {
    private var currentParentNode = initialNode
        set(value) {
            field = value
            childNodeViewerObjects = ArrayList(value.childNodes!!.map { ViewerObject(value, it) })
        }
    private var childNodeViewerObjects = arrayListOf<ViewerObject>()
    private val inventory = Bukkit.createInventory(null, 9*6)
    init {
        currentParentNode = initialNode // it's not duplicated with currentParentNode's intial value. cuz it call through custom setter, but intial value not.
        Bukkit.getPluginManager().registerEvents(this, plugin)
        p.openInventory(inventory)
        setPage(initialNode)
    }

    // 페이지 들어가기 전에 childNOdes가 있는 node인지 확인 필요
    private fun setPage(node: NodeData) {
        currentParentNode = node
        refresh()
    }

    private fun refresh() {
        inventory.clear()
        childNodeViewerObjects.forEachIndexed { i, value ->
            inventory.setItem(i, value.render())
        }
    }


    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        if (e.clickedInventory == inventory) {
            e.isCancelled = true
            val find = childNodeViewerObjects.find { it.render() == e.currentItem } ?: return
            if (find.hasChildNode()) {
                setPage(find.node)
            } else if (e.cursor.type != Material.AIR){
                find.putItem(e.cursor)
                e.cursor = ItemStack(Material.AIR)
                refresh()
            }
        }
    }
}