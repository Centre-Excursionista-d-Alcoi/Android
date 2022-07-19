package com.arnyminerz.cea.app.ui.elements.renting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.cea.app.R
import com.arnyminerz.cea.app.annotation.ATTR_BRAND
import com.arnyminerz.cea.app.annotation.ATTR_COLOR
import com.arnyminerz.cea.app.annotation.ATTR_LENGTH
import com.arnyminerz.cea.app.data.ConstrainedInventoryItem
import com.arnyminerz.cea.app.utils.LocalizableColor

@Composable
@ExperimentalMaterial3Api
fun ItemCard(
    item: ConstrainedInventoryItem,
    amount: Int,
    onAmountUpdated: (variance: Int) -> Unit,
    onItemQuantityUpdated: (amount: Int) -> Unit,
) {
    val inventoryItem = item.inventoryItem

    androidx.compose.material3.Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Text(
                inventoryItem.displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Row(Modifier.fillMaxWidth()) {
                Column {
                    inventoryItem.attributes.forEach { (attribute, value) ->
                        val attr = when (attribute) {
                            ATTR_BRAND -> stringResource(R.string.renting_item_attr_brand)
                                .format(value)
                            ATTR_COLOR -> stringResource(R.string.renting_item_attr_color)
                                .format(stringResource(LocalizableColor(value).string))
                            ATTR_LENGTH -> stringResource(R.string.renting_item_attr_length)
                                .format(value)
                            else -> return@forEach
                        }
                        Text(
                            attr,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
                Text(
                    stringResource(R.string.renting_item_price).format(
                        inventoryItem.price?.amount
                            ?: stringResource(R.string.renting_item_price_free)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 20.sp,
                    textAlign = TextAlign.End,
                )
            }
            if (item.availableAmount == 0L && inventoryItem.quantity > 0L)
                Text(
                    stringResource(R.string.renting_item_stock_unavailable),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            ) {
                fun updateAmount(variance: Int) {
                    onAmountUpdated(variance)
                    onItemQuantityUpdated(amount + variance)
                }

                OutlinedButton(
                    onClick = { updateAmount(-1) },
                    enabled = amount > 0,
                ) {
                    Icon(
                        Icons.Rounded.Remove,
                        stringResource(R.string.image_desc_decrease),
                    )
                }
                Text(
                    amount.toString(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
                OutlinedButton(
                    onClick = { updateAmount(1) },
                    enabled = amount < item.availableAmount,
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        stringResource(R.string.image_desc_increase),
                    )
                }
            }
        }
    }
}
