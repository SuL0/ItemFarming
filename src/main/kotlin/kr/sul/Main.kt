package kr.sul

//import kr.sul.itemfarming.farming.chest.ChestClickListener
//import kr.sul.itemfarming.farming.chest.PlaceChestAutomatically
import kr.sul.itemfarming.ConfigLoader
import kr.sul.itemfarming.farming.shulker.GlowNearbyShulker
import kr.sul.itemfarming.farming.shulker.ModifyShulkerForFarming
import kr.sul.itemfarming.farming.shulker.data.PlacingShulkerBoxSaver
import kr.sul.itemfarming.setting.Command
import kr.sul.itemfarming.setting.gui.TreeDataMgr
import kr.sul.servercore.file.simplylog.LogLevel
import kr.sul.servercore.file.simplylog.SimplyLog
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level


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

        Bukkit.getScheduler().runTask(plugin) {
            if (Bukkit.getPluginManager().isPluginEnabled("CrackShot")
                    || Bukkit.getPluginManager().isPluginEnabled("CrackShotAddition")) {
                Bukkit.getLogger().log(Level.WARNING, "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n현재 ItemFarming은 CrackShot2가 아닌 구버전 CrackShot(+CrackShotAddition)은 지원하지 않습니다.\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n")
                SimplyLog.log(LogLevel.ERROR_CRITICAL, plugin, "현재 ItemFarming은 CrackShot2가 아닌 구버전 CrackShot(+CrackShotAddition)은 지원하지 않습니다.")
                Bukkit.shutdown()
            }
        }
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
        Bukkit.getPluginManager().registerEvents(GlowNearbyShulker, this)
//        Bukkit.getPluginManager().registerEvents(ChestClickListener, this)
//        ObjectInitializer.forceInit(PlaceChestAutomatically::class.java)
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