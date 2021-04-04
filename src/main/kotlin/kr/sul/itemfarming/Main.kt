package kr.sul.itemfarming

import kr.sul.itemfarming.setting.Command
import kr.sul.itemfarming.setting.gui.TreeDataMgr
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin


// TODO: chance 100% 초과하지 않게, 또는 100% 초과하면 관리자가 수정하게끔
class Main : JavaPlugin(), Listener {
    companion object {
        lateinit var plugin: Plugin
    }

    override fun onEnable() {
        plugin = this as Plugin
        registerClasses()
        TreeDataMgr.loadAll()
    }

    override fun onDisable() {
        TreeDataMgr.saveAll()
    }

    private fun registerClasses() {
        getCommand("ItemFarming").executor = Command
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