package kr.sul.itemfarming.setting.gui

import kr.sul.Main.Companion.plugin
import kr.sul.itemfarming.setting.gui.guimoderator.AnvilGuiModerator
import kr.sul.itemfarming.setting.gui.node.NodeCategory
import kr.sul.itemfarming.setting.gui.node.NodeRank
import kr.sul.itemfarming.setting.gui.node.NodeRankListMgr
import kr.sul.itemfarming.setting.gui.nodecompponent.ChildNodeContainer
import kr.sul.itemfarming.setting.gui.nodecompponent.InternalNode
import kr.sul.servercore.util.ItemBuilder.loreIB
import kr.sul.servercore.util.ItemBuilder.nameIB
import org.apache.logging.log4j.util.TriConsumer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

// < GUI >
// ParentNode - CurrentNode - ChildNode
abstract class InternalNodeGui<T: InternalNode> {
    // abstract start
    // 가장 범용
    abstract val NODE_CLASS: Class<T>  // 아래 3개는 Current Node
    abstract val NODE_TYPE_NAME: String
    abstract val NODE_TYPE_COLOR: String
    abstract val CHILD_NODE_TYPE_NAME: String

    // GUI 관련(per Player 기준!)
    abstract fun getViewingGuiCurrentNodeList(p: Player): ArrayList<T>

    abstract val GUI_NAME_PREFIX: String // GUI가 NodeRank에서 열린 GUI라는 것을 위한 식별용임 (Listener에서 필요)
    abstract val NODE_ITEM_MATERIAL: Material
    abstract val howToCreateCurrentNodeObj: TriConsumer<Player, String, Double>

    abstract val itemForidentificationInGuiBottom: ItemStack
    abstract fun setViewingGuiParentNode(p: Player, parentNode: InternalNode?)
    abstract fun getViewingGuiParentNode(p: Player): InternalNode?
    abstract fun checkViewingGuiParentNodeIsValid(p: Player)  // false일 시 내부에서 throw Exception

    abstract fun goToChildNodeListGui(p: Player, currentNode: T)
    // abstract end

    // GUI에 쓸 CurrentNode 생성하는 버튼
    private val createCurrentNodeButton: ItemStack by lazy {
        ItemStack(Material.BOOK_AND_QUILL)
            .nameIB("${NODE_TYPE_COLOR}${NODE_TYPE_NAME} 추가").loreIB(" §7└ ${NODE_TYPE_NAME}를 새로 생성합니다.", 2)
    }
    // Only NodeCategory용 NodeRank로 돌아가기 버튼
    private val goToRankListGuiButton: ItemStack by lazy {
        ItemStack(Material.CHORUS_FRUIT)
            .nameIB("§2§l이전 GUI로 돌아가기").loreIB(" §7└ NodeRank List를 나열한 GUI로 돌아갑니다.", 2)
    }
    private val listener = ListenUp() // per Player의 GUI Interact용 Listener


    // ParentNode - CurrentNode 에 해당하는 GUI 띄우기
    fun openCurrentNodeListGui(p: Player, parentNode: InternalNode?) {
        setViewingGuiParentNode(p, parentNode)

        val inv = Bukkit.createInventory(null, 72, "$GUI_NAME_PREFIX${TreeUtil.ForCommon.getAppropriateGuiName(parentNode, NODE_TYPE_NAME)}")
        val nodeMaterialItem = ItemStack(NODE_ITEM_MATERIAL)


        getViewingGuiCurrentNodeList(p).forEach {
            // Wool item 색상 변경
            if (nodeMaterialItem.durability < 15) {
                nodeMaterialItem.durability = (nodeMaterialItem.durability + 1).toShort()
            } else {
                nodeMaterialItem.durability = 0
            }

            val made = makeItemForGuiDisplay(nodeMaterialItem, it, NODE_TYPE_NAME, CHILD_NODE_TYPE_NAME)
            inv.addItem(made)
        }

        // Node 추가 버튼
        inv.setItem(54, createCurrentNodeButton)
        // Only NodeCategory용 NodeRank로 돌아가기 버튼
        if (NODE_CLASS == NodeCategory::class.java) {
            inv.setItem(62, goToRankListGuiButton)
        }
        // GUI 식별용 색깔 아이템
        var totalChance = 0.0
        getViewingGuiCurrentNodeList(p).forEach { totalChance += it.chance }
        if (totalChance == 100.0) {
            itemForidentificationInGuiBottom.nameIB("§7확률 합계 : §a&n${totalChance}%§a §2§l[GOOD]")
        } else {
            itemForidentificationInGuiBottom.nameIB("§7확률 합계 : §c&n${totalChance}%§c §4§l[BAD]")
        }
        for (i in 63..71) {
            inv.setItem(i, itemForidentificationInGuiBottom)
        }
        p.openInventory(inv)
    }



    // Node 추가 버튼 눌렀을 때. Anvil[이름] -> Anvil[확률] 띄워서 입력받고, 결과물로 Node Obj(Only InternalNode) 생성시키기
    private fun createCurrentNodeObjWithGui(p: Player) {
        // !: 1차 Anvil GUI [Node 이름 입력]
        AnvilGuiModerator.open(p, "1. $NODE_TYPE_NAME 이름 입력: ", { inputName ->
            // Node 새로 생성

            // 이름 중복 체크
            getViewingGuiCurrentNodeList(p).forEach {
                if (it.name == inputName) {
                    p.sendMessage("§6§lIF: $NODE_TYPE_COLOR[$NODE_TYPE_NAME] §7의 이름이 §c중복§7됩니다. §f'$inputName'")
                    return@open
                }
            }

            // !: 2차 Anvil GUI [Node 확률 입력]
            AnvilGuiModerator.open(p, "2. $NODE_TYPE_NAME 확률 입력: ", { s_inputChance ->
                try {
                    val inputChance = s_inputChance.toDouble()
                    p.sendMessage("§6§lIF: $NODE_TYPE_COLOR[$NODE_TYPE_NAME] §f$inputName : $inputChance% §7를 새로 생성했습니다.")
                    howToCreateCurrentNodeObj.accept(p, inputName, inputChance)
                    TreeDataMgr.DataSaveTaskRegister.tryToRegisterDataSaveTask()  // saveData Task 등록
                    this.openCurrentNodeListGui(p, getViewingGuiParentNode(p))
                } catch (ignored: Exception) {
                    p.sendMessage("§6§lIF: §cDouble §7타입을 입력해야 합니다.")
                }
            }, {
                Bukkit.getScheduler().runTask(plugin) {
                    this.openCurrentNodeListGui(p, getViewingGuiParentNode(p))
                }
            })
        }, {
            // 여긴 Node 새로 생성의 가장 끝부분이 아니기에, ESC를 고려하여 특정 gui로 이동을 넣게되면, 위의 [2] 로 진행이 안됨
        })
    }




    // per Player의 GUI Interact용 Listener
    private inner class ListenUp: Listener {
        init {
            Bukkit.getPluginManager().registerEvents(this, plugin)
        }

        @EventHandler(priority = EventPriority.HIGH)
        fun onClick(e: InventoryClickEvent) {
            if (e.isCancelled || !e.whoClicked.isOp) return
            val p = e.whoClicked as Player
            if (e.inventory?.name?.startsWith(GUI_NAME_PREFIX) == true) {
                e.isCancelled = true
                checkViewingGuiParentNodeIsValid(p) // GUI 2차 검증. false일 시 내부에서 throw Exception
                if (e.inventory == e.clickedInventory && e.currentItem != null) {

                    // 왼 클릭
                    if (e.isLeftClick && !e.isShiftClick) {

                        // Node 선택 버튼 클릭 -> 다음 GUI로 넘어가기
                        if (e.currentItem.type == NODE_ITEM_MATERIAL) {
                            val clickedNode = getObjFromGuiItem(NODE_CLASS, e.currentItem, getViewingGuiCurrentNodeList(p))
                            // !: Child Node List GUI로 넘어가기
                            this@InternalNodeGui.goToChildNodeListGui(p, clickedNode)
                        }


                        // Node 추가 버튼 클릭
                        else if (e.currentItem.isSimilar(createCurrentNodeButton)) {
                            this@InternalNodeGui.createCurrentNodeObjWithGui(p)
                        }

                        // Only NodeCategory용 NodeRank로 돌아가기 버튼
                        else if (e.currentItem.isSimilar(goToRankListGuiButton)) {
                            NodeRankListMgr.openCurrentNodeListGui(p, null)
                        }
                    }


                    // 아이템 우클릭 시 Management GUI (Rename, Edit Chance, Delete)
                    else if (e.isRightClick && e.currentItem.type == NODE_ITEM_MATERIAL) {
                        val clickedNode = getObjFromGuiItem(NODE_CLASS, e.currentItem, getViewingGuiCurrentNodeList(p))
                        NodeManagementGui.Builder().run {
                            setTag(NODE_TYPE_COLOR, NODE_TYPE_NAME)
                            internalNodeObjToEdit = clickedNode
                            backToPreviousGuiMethod =
                                Runnable { this@InternalNodeGui.openCurrentNodeListGui(p, getViewingGuiParentNode(p)) }
                            setDeleteButton((clickedNode as ChildNodeContainer<*>).childNodeList.size) {
                                getViewingGuiCurrentNodeList(p).remove(clickedNode)
                            }
                            this.openManagementGui(p)
                        }
                    }
                }
            }
        }
    }







    // InternalNode 전용 공통 유틸
    companion object {
        // GUI 띄워서 Rank List, Category List, Item List 를 표시할 때 쓰는 아이템 반환
        // materialItem은 그냥 Material에 durability만 포함했다고 보면 됨 (e.g. colored wool)
        fun makeItemForGuiDisplay(materialItem: ItemStack, currentNodeObj: InternalNode, currentClassName: String, childClassName: String): ItemStack {
            val color = run {
                if (currentNodeObj is NodeRank) NodeRank.NOTATION_COLOR
                else NodeCategory.NOTATION_COLOR
            }
            return materialItem.clone().nameIB("$color[${currentClassName.toUpperCase()}] §f${currentNodeObj.name}")
                .loreIB(" §6└ ${currentNodeObj.chance}%")
                .loreIB("")
                .loreIB(" §9§lLeft Click §7: 해당 ${currentClassName}에 종속된 $childClassName List를 GUI를 새로 열어 나열합니다.", 2)
                .loreIB("§9§lRight Click §7: 해당 ${currentClassName}의 세부 설정을 변경합니다.")
        }

        // 위에서 생성한 아이템을 GUI에서 클릭했을 때, 그걸 다시 NodeRank, NodeCategory, NodeItem으로 바꿔주는 역할
        fun <T: InternalNode> getObjFromGuiItem(clazz: Class<T>, item: ItemStack, whereToFind: List<T>): T {
            val whereToCut = item.itemMeta.displayName.indexOf(" ")+1
            val pureName = item.itemMeta.displayName.substring(whereToCut).replaceFirst("§f", "").replace("§", "&")
            return searchObjInGivenListWithName(clazz, pureName, whereToFind)
        }
        private fun <T: InternalNode> searchObjInGivenListWithName(clazz: Class<T>, name: String, whereToFind: List<T>): T {
            whereToFind.forEach {
                if (it.name == name) {
                    return it
                }
            }

            // 로그용 코드
            val whereToFindLog = StringBuilder("")
            whereToFind.forEach {
                whereToFindLog.append("'${it.name}'")
            }
            throw Exception("'$name' [${clazz.name}] 에 해당하는 object를 찾을 수 없습니다.   $whereToFindLog")
        }
    }
}