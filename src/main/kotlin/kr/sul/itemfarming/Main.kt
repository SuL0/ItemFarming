package kr.sul.itemfarming

import kr.sul.itemfarming.setting.Command
import kr.sul.itemfarming.setting.gui.NodeCategory
import kr.sul.itemfarming.setting.gui.NodeRank
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin(), Listener {
    companion object {
        lateinit var plugin: Plugin
    }

    override fun onEnable() {
        plugin = this as Plugin
        registerClasses()
    }

    private fun registerClasses() {
        getCommand("ItemFarming").executor = Command
        Bukkit.getPluginManager().registerEvents(NodeRank.NodeRankListMgr, this)
        Bukkit.getPluginManager().registerEvents(NodeCategory.NodeCategoryListMgr, this)
//        Bukkit.getPluginManager().registerEvents(LeafItemGuiMgr, this)
        Bukkit.getPluginManager().registerEvents(this, this)
    }



//    // FIXME: TO TEST
//    @EventHandler
//    fun onClick(e: InventoryClickEvent) {
//        Bukkit.broadcastMessage("click")
//    }
//    @EventHandler
//    fun onDrag(e: InventoryDragEvent) {
//        e.result = Event.Result.DENY
//        Bukkit.broadcastMessage("drag")
//    }
}