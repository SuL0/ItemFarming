package kr.sul.itemdb

import kr.sul.Main.Companion.plugin
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory

object Command: CommandExecutor {
    const val PREFIX = "§f§lITEMDB §7::"

    // itemdb 명령어
    // itemdb <category> : 카테고리 GUI를 연다 (Read & 아이템 등록안된 것 등록)
    // itemdb add <category> <keyname>: 현재 들고있는 아이템을 (category).yml의 keyName으로 등록
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player || !sender.isOp) return true
        // 카테고리 리스트 채팅으로 띄워줌
        if (args.isEmpty()) {
            sender.sendMessage("         §c§lCategory 목록     §7/itemdb help: 도움말")
            DataManager.categoryDataList.forEach {
                val jsonMsg = arrayListOf<TextComponent>().run {
                    add(TextComponent("$PREFIX §6/itemdb ${it.name}").run {
                        hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent("§e 클릭해서 명령어 실행")))
                        clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/itemdb ${it.name}")
                        this
                    })
                    this
                }
                sender.sendMessage(*jsonMsg.toTypedArray())
            }
            return true
        }
        else if (args[0] == "help") {
            sender.sendMessage("")
            sender.sendMessage("$PREFIX §6itemdb <category> §f: 카테고리 GUI를 연다 (Read & 아이템 등록안된 것 등록)")
            sender.sendMessage("$PREFIX §6/itemdb add <category> <keyname> §f: 현재 들고있는 아이템을 (category).yml의 keyName으로 등록 ")
            return true
        }
        else if (args[0] == "add") {
            if (args.size != 3) {
                sender.sendMessage("$PREFIX §6/itemdb add <category> <keyname> §f: 현재 들고있는 아이템을 (category).yml의 keyName으로 등록 ")
                return true
            }
            val itemInMainHand = sender.inventory.itemInMainHand
            val categoryData = DataManager.categoryDataList.firstOrNull { it.name == args[1] }
            if (itemInMainHand != null && itemInMainHand.type == Material.AIR) {
                sender.sendMessage("$PREFIX §c손에 들고 있는 아이템이 없습니다.")
                return true
            }
            if (categoryData == null) {
                sender.sendMessage("$PREFIX §e${categoryData} §f라는 카테고리 파일이 존재하지 않습니다.")
                return true
            }
            if (categoryData.itemDataList.any { it.keyName == args[2] }) {
                sender.sendMessage("$PREFIX §e${args[0]} §f이라는 키는 §e${categoryData}§f에 이미 존재합니다.")
                return true
            }
            val itemName = sender.inventory.itemInMainHand?.itemMeta?.displayName ?: sender.inventory.itemInMainHand.type
            sender.sendMessage("$PREFIX §f아이템 §c$itemName §f을 §e${args[1]}.yml ${args[2]} §f에 등록하였습니다.")

            val itemData = ItemData(args[2], itemInMainHand)
            categoryData.itemDataList.add(itemData)
            DataManager.saveData(true)
            return true
        }
        // 카테고리에 맞는 GUI 열기
        else if (args.size == 1) {
            if (DataManager.categoryDataList.none { it.name == args[0] }) {
                sender.sendMessage("$PREFIX §c${args[0]} 은 없는 카테고리 입니다.")
                return true
            }
            if (ItemDbGui.isAvailable) {
                val category = (DataManager.categoryDataList.first { it.name == args[0] })
                ItemDbGui(sender, category)
            } else {
                sender.sendMessage("$PREFIX §c다른 유저가 이용 중이라, 현재 이용 불가능합니다.")
            }
            return true
        }
        else {
            sender.sendMessage("$PREFIX 없는 명령어 입니다.")
        }
        return true
    }




    private class ItemDbGui(private val p: Player, private val categoryData: CategoryData): Listener {
        companion object {
            var isAvailable = true  // ItemDbGui는 플레이어 최대 한 명 까지 이용할 수 있음
        }

        private lateinit var gui: Inventory
        init {
            isAvailable = false
            Bukkit.getPluginManager().registerEvents(this, plugin)
            loadGui()
        }

        private fun loadGui() {
            gui = Bukkit.createInventory(null, 6*9, "ITEMDB")
            categoryData.itemDataList.forEachIndexed { cnt, item ->
                gui.setItem(cnt, item.itemStack)
            }
            p.openInventory(gui)
        }

        /* 커맨드로 대체
        @EventHandler
        fun onClick(e: InventoryClickEvent) {
            if (e.clickedInventory != gui || e.whoClicked != p) return
            // ITEM_NOT_SET인 칸을 e.cursor으로 아이템 설정
            if (e.currentItem == ItemData.ITEM_NOT_SET && categoryData.itemDataList.size-1 >= e.slot && categoryData.itemDataList[e.slot].itemStack == ItemData.ITEM_NOT_SET
                    && e.cursor != null && e.cursor.type != Material.AIR) {
                categoryData.itemDataList[e.slot].itemStack = e.cursor
                p.sendMessage("$PREFIX 설정되었습니다.")
            }
            e.isCancelled = true
        }*/

        @EventHandler
        fun onClose(e: InventoryCloseEvent) {
            destroy()
        }

        private fun destroy() {
            isAvailable = true
            HandlerList.unregisterAll(this)
        }
    }
}