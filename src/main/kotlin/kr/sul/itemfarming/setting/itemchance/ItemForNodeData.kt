package kr.sul.itemfarming.setting.itemchance

import com.shampaggon.crackshot2.CSMinion
import kr.sul.servercore.util.Base64Serialization
import kr.sul.servercore.util.ItemBuilder.amountIB
import kr.sul.servercore.util.ItemBuilder.loreIB
import kr.sul.servercore.util.ItemBuilder.nameIB
import kr.sul.servercore.util.KeepExceptionAlert
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

// 노드 아님
interface ItemForNodeData {
    val amount: Int
    fun get(): ItemStack
    fun convertItemForJsonFormat(): String




    data class NormalItem(
        val itemStack: ItemStack,
        override val amount: Int
    ): ItemForNodeData {
        companion object {
            const val identificationCharacter = "(ITEMSTACK)"
        }
        override fun get(): ItemStack {
            return itemStack.amountIB(amount)
        }

        override fun convertItemForJsonFormat(): String {
            return "${identificationCharacter}${Base64Serialization.toBase64(itemStack)}"
        }
    }

    data class CrackShotItem(
        val crackShotParentNode: String,
        override val amount: Int
    ): ItemForNodeData {
        companion object {
            const val identificationCharacter = "(CRACKSHOT)"
        }
        init {
            if (CSMinion.getInstance().vendingMachine(crackShotParentNode) == null) {
                KeepExceptionAlert.alert(null, "[ItemFarming] CrackShot으로 된 아이템(ParentNode: $crackShotParentNode) 가 CrackShot에서 찾을 수 없는 총기입니다.", 100L) {
                    CSMinion.getInstance().vendingMachine(crackShotParentNode) != null
                }
            }
        }

        override fun get(): ItemStack {
            return CSMinion.getInstance().vendingMachine(crackShotParentNode)?.amountIB(amount)
                ?: ItemStack(Material.BARRIER).nameIB("§4§lERROR").loreIB(" §f-> $crackShotParentNode")
        }

        override fun convertItemForJsonFormat(): String {
            return "${identificationCharacter}${crackShotParentNode}"
        }

    }
}