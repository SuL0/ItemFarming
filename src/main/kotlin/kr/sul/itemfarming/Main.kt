package kr.sul.itemfarming

import kr.sul.itemfarming.farmingshulkerbox.ModifyShulkerForFarming
import kr.sul.itemfarming.farmingshulkerbox.data.PlacingShulkerBoxSaver
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

        // 데이터 로드
        TreeDataMgr.loadAll() // 확률
        ConfigLoader.loadConfig() // 콘피그
        PlacingShulkerBoxSaver.DataMgr.loadAll() // 셜커 위치
    }

    override fun onDisable() {
        TreeDataMgr.saveAll(false)
        PlacingShulkerBoxSaver.DataMgr.saveAll()
    }

    private fun registerClasses() {
        getCommand("ItemFarming").executor = Command
        Bukkit.getPluginManager().registerEvents(this, this)
        Bukkit.getPluginManager().registerEvents(PlacingShulkerBoxSaver.ListenUp, this)
        Bukkit.getPluginManager().registerEvents(ModifyShulkerForFarming, this)
    }



//    // NOTE: TO TEST
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