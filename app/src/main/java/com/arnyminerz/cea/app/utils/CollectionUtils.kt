package com.arnyminerz.cea.app.utils

import com.arnyminerz.cea.app.data.InventoryItem
import com.arnyminerz.cea.app.data.InventoryItem.PricingPeriod

fun <T, L : MutableCollection<T>> L.append(item: T): L = apply {
    add(item)
}

/**
 * From a collection of prices, gets the sum of prices, and the period in which they will get paid.
 *
 * For example, if you have a list of two items, one for 2€ weekly, and another one for 5€ monthly,
 * the method will return `13.0 to [PricingPeriod.MONTHLY]`, since a month has 4 weeks (4*2€=8€),
 * plus 5€ of the other element, it's 8€+5€=13€, which would have to get paid monthly.
 * @author Arnau Mora
 * @since 20220709
 */
fun <L : Collection<InventoryItem.Price>> L.sumPrice(): Pair<Float, PricingPeriod> {
    val pricingPeriods = PricingPeriod.values()
    var greatestPricing: PricingPeriod? = null
    for (price in this) {
        val period = price.period
        val periodIndex = pricingPeriods.indexOf(period)
        if (greatestPricing == null || periodIndex > pricingPeriods.indexOf(greatestPricing))
            greatestPricing = period
    }
    return if (greatestPricing != null)
        map { it.amount * (greatestPricing.hoursPerPeriod / it.period.hoursPerPeriod) }
            .sum()
            .let { it to greatestPricing }
    else 0f to PricingPeriod.NONE
}
