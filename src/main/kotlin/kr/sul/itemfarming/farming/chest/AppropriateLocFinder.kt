package kr.sul.itemfarming.farming.chest

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.random.Random

object AppropriateLocFinder {
    private val random = Random
    private const val AIR_STACK_CUTLINE = 30

    /**
     * @param p 중심이 되는 플레이어
     */
    fun find(p: Player): Location? {
        val centerPoint = p.location

        for (i in 1..20) {
            val tryToFind = tryToFind(centerPoint)
            if (tryToFind != null) {  // tryToFind에서 적절한 위치를 찾았을 때
                return tryToFind
            }
        }

        return null
    }


    // TODO: 지면의 y를 설정하고, 플레이어가 지면보다 아래에 있으면 blockType을 찾을 때 기준 좌표에서 위로만 찾고, 플레이어가 지면보다 위에 있으면 기준 좌표에서 아래만 찾으면 더 좋을 것 같은데?
    private fun tryToFind(centerPoint: Location): Location? {

        // 랜덤하게 10~30칸 만큼 떨어진 좌표
        val randLength = random.nextInt(10, 30+1)  // 10~30
        val randDirVector = Vector(random.nextDouble(-1.0, 1.0), 0.0, random.nextDouble(-1.0, 1.0)).normalize().multiply(randLength)
        val randLoc = centerPoint.clone().add(randDirVector).toBlockLocation()
        randLoc.add(0.0, -10.0, 0.0)  // y -10부터 시작

        val world = centerPoint.world
        // 아래서부터 쓸어올려가며 만약 블럭이 공기라면(+시작하는 지점 바로 아래가 지면이어야 함), airStack 계속 쌓아나가고 만약 airStack이 30이 됐을 시 y좌표-airStack에 블럭 설치.
        val maxI = 50
        var airStack = 0
        for (i in 1..maxI) {
            if ((maxI-i)+airStack < AIR_STACK_CUTLINE) return null  // 남은 i로는 AIR_STACK_CUTLINE을 절대 못 넘길 때
            if (world.getBlockAt(randLoc.blockX, randLoc.blockY+i, randLoc.blockZ).type == Material.AIR) {
                if (airStack == 0) {
                    // airStack을 쌓기 시작하는 장소의 바로 아래가 지면이 아니라면, continue
                    if (!world.getBlockAt(randLoc.blockX, (randLoc.blockY+i)-1, randLoc.blockZ).type.isSolid
                            || world.getBlockAt(randLoc.blockX, (randLoc.blockY+i)-1, randLoc.blockZ).type == Material.LEAVES
                            || world.getBlockAt(randLoc.blockX, (randLoc.blockY+i)-1, randLoc.blockZ).type == Material.LEAVES_2) {
                        continue
                    }
                }
                airStack += 1
            } else {
                airStack = 0
            }

            if (airStack >= AIR_STACK_CUTLINE) {
                return randLoc.toBlockLocation().add(0.0, i.toDouble()-AIR_STACK_CUTLINE+1, 0.0)
            }
        }
        return null
    }
}