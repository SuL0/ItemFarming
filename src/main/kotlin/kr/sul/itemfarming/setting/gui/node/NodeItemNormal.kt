package kr.sul.itemfarming.setting.gui.node

import kr.sul.servercore.util.UniqueIdAPI
import org.bukkit.inventory.ItemStack
import java.util.*

class NodeItemNormal(parentNode: NodeCategory,
                     uuid: UUID,  // 고유한 NodeItem을 가리키는 용도. Item은 Unique하지 않아, 중복 가능성이 있기 때문 (파일에선 저장하지 않음. 그냥 load할때마다 UUID 바뀐다고 보면 됨)
                     private val privateItem: ItemStack,
                     chance: Double)
    : NodeItemAbstract(parentNode, uuid, chance) {

    override val item: ItemStack
        get() {
            if (UniqueIdAPI.hasUniqueID(privateItem)) {  // privateItem의 UID를 갱신한 후 반환
                UniqueIdAPI.wipeAndCarveNewUniqueID(privateItem)
            }
            return privateItem
        }

    // parentNode에 연결(link)
    init {
        parentNode.childNodeList.add(this)
        refreshSort(parentNode.childNodeList)
    }

    // default constructor(UUID 랜덤 생성)
    constructor(parentNode: NodeCategory, item: ItemStack, chance: Double) : this(parentNode, UUID.randomUUID(), item, chance)
}