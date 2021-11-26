package kr.sul.itemfarming.setting.gui.node

import com.shampaggon.crackshot2.CSMinion
import kr.sul.Main
import kr.sul.servercore.file.simplylog.LogLevel
import kr.sul.servercore.file.simplylog.SimplyLog
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.logging.Level

class NodeItemCrackShot(parentNode: NodeCategory,
                         uuid: UUID,  // 고유한 NodeItem을 가리키는 용도. Item은 Unique하지 않아, 중복 가능성이 있기 때문 (파일에선 저장하지 않음. 그냥 load할때마다 UUID 바뀐다고 보면 됨)
                         val csParentNode: String,
                         chance: Double)
    : NodeItemAbstract(parentNode, uuid, chance) {

    // parentNode에 연결(link)
    init {
        parentNode.childNodeList.add(this)
        refreshSort(parentNode.childNodeList)

        if (CSMinion.getInstance().vendingMachine(csParentNode) == null) {
            Bukkit.getLogger().log(Level.WARNING, "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\nCrackShot으로 된 아이템(ParentNode: $csParentNode) 가 CrackShot에서 찾을 수 없는 총기입니다.\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n")
            SimplyLog.log(LogLevel.ERROR_CRITICAL, Main.plugin, "CrackShot으로 된 아이템(ParentNode: $csParentNode) 가 CrackShot에서 찾을 수 없는 총기입니다.")
            Bukkit.shutdown()
        }
    }

    override val item: ItemStack
        get() {
            return CSMinion.getInstance().vendingMachine(csParentNode)
        }

    // default constructor(UUID 랜덤 생성)
    constructor(parentNode: NodeCategory, csParentNode: String, chance: Double) : this(parentNode, UUID.randomUUID(), csParentNode, chance)
}