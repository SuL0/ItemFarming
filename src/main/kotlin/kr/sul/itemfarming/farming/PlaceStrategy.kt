//package kr.sul.itemfarming.farming
//
//import kr.sul.servercore.util.KeepExceptionAlert
//
//class PlaceStrategy(
//    amountStr: String,
//    private val locationPool: LocationPool
////    private val makeItemContainerDespawnedWhenItOpened: Boolean,
////    private val makeItemContainerMoveWhenItOpened: Boolean
//) {
//    private val amount = run {
//        if (locationPool.locations.isEmpty()) return@run 0
//        try {
//            if (!amountStr.endsWith("%") || amountStr.lowercase() != "auto") {
//                return@run amountStr.toInt()
//            } else {
//                val ratio = (amountStr.replace("%", "").toInt())/100
//                return@run locationPool.locations.size*ratio
//            }
//        } catch (e: Exception) {
//            KeepExceptionAlert.alert(e, "amountStr: '$amountStr'의 양식 불충족", 100L)
//            return@run 0
//        }
//    }
//
//
//
//
//}