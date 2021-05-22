package kr.sul.itemfarming.setting.gui.node

import kr.sul.itemfarming.Main.Companion.plugin
import org.bukkit.inventory.ItemStack
import java.util.*
import com.shampaggon.crackshot.CSMinion as CSMinion1
import com.shampaggon.crackshot2.CSMinion as CSMinion2

class NodeItemCrackShot(parentNode: NodeCategory,
                         uuid: UUID,  // 고유한 NodeItem을 가리키는 용도. Item은 Unique하지 않아, 중복 가능성이 있기 때문 (파일에선 저장하지 않음. 그냥 load할때마다 UUID 바뀐다고 보면 됨)
                         val csParentNode: String,
                         chance: Double)
    : NodeItemAbstract(parentNode, uuid, chance) {

    // parentNode에 연결(link)
    init {
        parentNode.childNodeList.add(this)
        refreshSort(parentNode.childNodeList)
    }

    override val item: ItemStack
        get() {
            return when {
                // 본섭용 크랙샷
                plugin.server.pluginManager.isPluginEnabled("CrackShot") -> {
                    CSMinion1.getInstance().vendingMachine(csParentNode)
                }
                
                // 부섭용 크랙샷
                plugin.server.pluginManager.isPluginEnabled("CrackShot-2") -> {
                    CSMinion2.getInstance().vendingMachine(csParentNode)
                }
                else -> {
                    throw Exception("")
                }
            }
        }

    // default constructor(UUID 랜덤 생성)
    constructor(parentNode: NodeCategory, csParentNode: String, chance: Double) : this(parentNode, UUID.randomUUID(), csParentNode, chance)
}