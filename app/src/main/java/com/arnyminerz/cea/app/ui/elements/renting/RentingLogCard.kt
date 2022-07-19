package com.arnyminerz.cea.app.ui.elements.renting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AssignmentReturn
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
import com.arnyminerz.cea.app.data.RentingData
import com.arnyminerz.cea.app.utils.LocalizableColor
import com.arnyminerz.cea.app.utils.format

@Composable
@ExperimentalMaterial3Api
fun ItemCard(
    rentingData: RentingData,
) {
    val item = rentingData.item

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    item.localizedDisplayName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    rentingData.timestamp.format("yyyy-MM-dd hh:mm"),
                    modifier = Modifier,
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    item.attributes.forEach { (attribute, value) ->
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
                    "x${rentingData.amount}",
                    modifier = Modifier.align(Alignment.CenterVertically),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 22.sp,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            ) {
                OutlinedButton(
                    onClick = { /*TODO*/ },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        Icons.Rounded.AssignmentReturn,
                        stringResource(R.string.image_desc_return),
                        modifier = Modifier.padding(end = 4.dp),
                    )
                    Text(stringResource(R.string.action_return))
                }
            }
        }
    }
}
