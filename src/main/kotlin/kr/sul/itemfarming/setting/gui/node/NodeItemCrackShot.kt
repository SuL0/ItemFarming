package kr.sul.itemfarming.setting.gui.node

import org.bukkit.inventory.ItemStack
import java.util.*
import com.shampaggon.crackshot2.CSMinion

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
        get() { return CSMinion.getInstance().vendingMachine(csParentNode) }

    // default constructor(UUID 랜덤 생성)
    constructor(parentNode: NodeCategory, csParentNode: String, chance: Double) : this(parentNode, UUID.randomUUID(), csParentNode, chance)
}