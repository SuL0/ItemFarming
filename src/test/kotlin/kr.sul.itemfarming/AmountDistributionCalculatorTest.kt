package kr.sul.itemfarming

import org.junit.Test

class AmountDistributionCalculatorTest {
    @Test
    fun calculate() {
        val calculator = FarmingThingConfiguration.AmountDistributionCalculator(100)
        calculator.registerAmountStrToCalculator("30")
        calculator.registerAmountStrToCalculator("auto")
        calculator.registerAmountStrToCalculator("auto")
        assert(calculator.getCalculatedAmount("auto") == 35)
    }
}