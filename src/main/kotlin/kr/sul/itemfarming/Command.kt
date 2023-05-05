package kr.sul.itemfarming

import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.arguments.*
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import kr.sul.itemfarming.dynmap.DynmapHookup
import kr.sul.itemfarming.location.LocationRegisterListener
import kr.sul.itemfarming.setting.itemchanceviewer.Viewer
import kr.sul.servercore.util.MsgPrefix
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material

object Command {
    fun initialize() {
        CommandTree("itemfarming")
            .withPermission(CommandPermission.OP)
            .executesPlayer (PlayerCommandExecutor { p, args ->
                val jsonMsg1 = arrayListOf<TextComponent>().apply {
                    add(TextComponent(" §a- §f/itemfarming location").apply {
                        hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent("§e 클릭해서 명령어 실행")))
                        clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/itemfarming location")
                    })
                }
                val jsonMsg2 = arrayListOf<TextComponent>().apply {
                    add(TextComponent(" §a- §f/itemfarming chance").apply {
                        hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent("§e 클릭해서 명령어 실행")))
                        clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/itemfarming chance")
                    })
                }
                p.sendMessage("")
                p.sendMessage(*jsonMsg1.toTypedArray())
                p.sendMessage(*jsonMsg2.toTypedArray())
            })

            .then(LiteralArgument("location")
                .executesPlayer(PlayerCommandExecutor { p, args ->
                    // 파일 읽어서 리스트
                    p.sendMessage("")
                    p.sendMessage("     §6§l↓↓ Location Pool List ↓↓")
                    FarmingThingConfiguration.allLocationPools.forEach { (path, locationPool) ->
                        p.sendMessage(" §e┕ §f${path} §a[Dynmap-Area: ${locationPool.dynmapArea?.markerID ?: "§4null"}§a]")
                        val jsonMsg = arrayListOf<TextComponent>().apply {
                            add(TextComponent("   §2§l[ EDIT LOCATION ]").apply {
                                hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent("§f Location Pool의 위치를 수정합니다. §c(재-클릭시 해제)")))
                                clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/itemfarming location registermode $path")
                            })
                            add(TextComponent("    "))
                            add(TextComponent("   §c§l[ SHOW ON DYNMAP ]").apply {
                                hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent("§f 해당 Location Pool에 속한 모든 위치를 Dynmap에 §c60초§f간 표시")))
                                clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/itemfarming location $path")
                            })
                            add(TextComponent("    "))
                            add(TextComponent("   §c§l[ SHOW LOCATION LIST ]").apply {
                                hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent("§f 해당 Location Pool에 속한 모든 위치를 채팅에 표시")))
                                clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/itemfarming location list $path")
                            })
                        }
                        p.sendMessage(*jsonMsg.toTypedArray())
                    }
                })
                .then(LiteralArgument("delete")
                    .then(StringArgument("location pool").replaceSuggestions(ArgumentSuggestions.strings(FarmingThingConfiguration.allLocationPools.keys))
                        .then(LocationArgument("location")
                            .replaceSuggestions(ArgumentSuggestions.strings { record ->
                                val locationPool = FarmingThingConfiguration.allLocationPools[record.previousArgs[0]] ?: return@strings arrayOf("")
                                return@strings locationPool.locations.map { loc -> "${loc.world},${loc.x},${loc.y},${loc.z}"}.toTypedArray()
                            })
                            .executesPlayer(PlayerCommandExecutor { p, args ->
                                val locationPool = FarmingThingConfiguration.allLocationPools[args[0]]
                                    ?: run {
                                        p.sendMessage("${MsgPrefix.get("FARMING")}§4§lINVALID LOCATION_POOL_PATH PASSED > '${args[0]}'")
                                        return@PlayerCommandExecutor
                                    }
                                val world = Bukkit.getWorld((args[1] as String).split(",")[0]) 
                                    ?: run {
                                        p.sendMessage("${MsgPrefix.get("FARMING")}§4§lINVALID LOCATION(WORLD) PASSED > '${args[1]}'")
                                        return@PlayerCommandExecutor
                                    }
                                val x = (args[1] as String).split(",")[1].toDoubleOrNull()
                                val y = (args[1] as String).split(",")[2].toDoubleOrNull()
                                val z = (args[1] as String).split(",")[3].toDoubleOrNull()
                                if (x == null || y == null || z == null) {
                                    p.sendMessage("${MsgPrefix.get("FARMING")}§4§lINVALID LOCATION PASSED > '${args[1]}'")
                                    return@PlayerCommandExecutor
                                }
                                val deleteResult = locationPool.deleteLocationPermanently(Location(world, x,y,z))
                                if (deleteResult) {
                                    p.sendMessage("${MsgPrefix.get("FARMING")}§e'${locationPool.name}' §fLocation Pool 에서 해당 위치를 §c삭제§f했습니다. > ${world.name}, ${x}, ${y}, ${z}")
                                } else {
                                    p.sendMessage("${MsgPrefix.get("FARMING")}§4§l'${locationPool.name}' Location Pool 에 존재하지 않는 위치입니다 > ${world.name}, ${x}, ${y}, ${z}")
                                }
                            })
                        )
                    )
                )
                .then(StringArgument("path")
                    .executesPlayer(PlayerCommandExecutor { p, args ->
                        val locationPool = FarmingThingConfiguration.allLocationPools[args[0]]
                            ?: run {
                                p.sendMessage("${MsgPrefix.get("FARMING")}§4§lINVALID LOCATION_POOL_PATH PASSED > '${args[0]}'")
                                return@PlayerCommandExecutor
                            }
                        p.sendMessage("${MsgPrefix.get("FARMING")}§fdynmap Debug 레이어 확인")
                        locationPool.displayAllLocationWithMarkerForAWhile()
                    })
                )
                .then(LiteralArgument("registermode")
                    .then(StringArgument("path")
                        .executesPlayer(PlayerCommandExecutor { p, args ->
                            val locationPool = FarmingThingConfiguration.allLocationPools[args[0]]
                                ?: run {
                                    p.sendMessage("${MsgPrefix.get("FARMING")}§4§lINVALID LOCATION_POOL_PATH PASSED > '${args[0]}'")
                                    return@PlayerCommandExecutor
                                }
                            if (LocationRegisterListener.whereToRegister[p] == locationPool) {
                                p.sendMessage("${MsgPrefix.get("FARMING")}§f수정 모드가 §c해제§f되었습니다.")
                                LocationRegisterListener.whereToRegister.remove(p)
                                return@PlayerCommandExecutor
                            }
                            p.sendMessage("${MsgPrefix.get("FARMING")}§f상자를 설치하면 §e${args[0]}§f에 등록됩니다.")
                            LocationRegisterListener.whereToRegister[p] = locationPool
                        })
                    )
                )
            )

            .then(LiteralArgument("chance")
                .executesPlayer(PlayerCommandExecutor { p, args ->
                    p.sendMessage("")
                    p.sendMessage("     §6§l↓↓ ItemChance Path List ↓↓")
                    FarmingThingConfiguration.allItemChances.forEach { (path, _) ->
                        val jsonMsg = arrayListOf<TextComponent>().apply {
                            add(TextComponent(" §e┕ §f/itemfarming chance $path").apply {
                                hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent("§e 클릭해서 명령어 실행")))
                                clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/itemfarming chance $path")
                            })
                        }
                        p.sendMessage(*jsonMsg.toTypedArray())
                    }
                })
                .then(StringArgument("path")
                    .executesPlayer(PlayerCommandExecutor { p, args ->
                        val nodeData = FarmingThingConfiguration.allItemChances[args[0]]
                            ?: run {
                                p.sendMessage("${MsgPrefix.get("FARMING")}§4§lINVALID LOCATION_POOL_PATH PASSED > '${args[0]}'")
                                return@PlayerCommandExecutor
                            }
                        Viewer(p, nodeData)
                    })
                )
            )

            .then(LiteralArgument("dynmap")
                .then(LiteralArgument("addcorner")
                    .executesPlayer(PlayerCommandExecutor { p, args ->
                        DynmapHookup.addCorner(p)
                    })
                )
                .then(LiteralArgument("clearcorners")
                    .executesPlayer(PlayerCommandExecutor { p, args ->
                        DynmapHookup.clearCorners(p)
                    })
                )
                .then(LiteralArgument("addarea")
                    .then(StringArgument("id")
                        .then(StringArgument("label")
                            .executesPlayer(PlayerCommandExecutor { p, args ->
                                DynmapHookup.addArea(p, args[0] as String, args[1] as String)
                            })
                        )
                    )
                )
                .then(LiteralArgument("removearea")
                    .then(StringArgument("id")
                        .executesPlayer(PlayerCommandExecutor { p, args ->
                            DynmapHookup.getArea(args[0] as String)?.deleteMarker()
                                ?: run {
                                    p.sendMessage("${MsgPrefix.get("FARMING")}§f${args[0] as String}영역을 찾을 수 없습니다.")
                                    return@PlayerCommandExecutor
                                }
                            p.sendMessage("${MsgPrefix.get("FARMING")}§f${args[0] as String}영역을 §c삭제§f했습니다.")
                        })
                    )
                )
            )
            .then(LiteralArgument("checktype")
                .executesPlayer(PlayerCommandExecutor { p, args ->
                    p.sendMessage("${p.inventory.itemInMainHand.type}")
                })
                .then(StringArgument("item")
                    .executesPlayer(PlayerCommandExecutor { p, args ->
                        p.sendMessage("${Material.getMaterial(args[0] as String)}")
                    })
                )
            )
//            .then(LiteralArgument("reload")
//                .executesPlayer(PlayerCommandExecutor { p, args ->
//                    p.sendMessage("")
//                    p.sendMessage("config가 리로드 되었습니다.")
//                })
//            )
            .register()
    }
}