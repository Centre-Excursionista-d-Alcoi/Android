package com.arnyminerz.cea.app.ui.elements

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arnyminerz.cea.app.data.InventoryItem
import com.google.firebase.Timestamp
import timber.log.Timber

@ExperimentalMaterial3Api
@Composable
fun InventoryItem(
    index: Int,
    category: String,
    item: InventoryItem,
    rentingItems: Map<String, Pair<String, Timestamp>>,
) {
    var selectedAmount by remember { mutableStateOf(0) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        val rents = arrayListOf<Pair<String, Timestamp>>().apply {
            for ((userId, rd) in rentingItems) {
                val (rentingItem, date) = rd
                val (ri, rentingCatDisp) = rentingItem.split("/")
                    .takeIf { it.size >= 2 }
                    ?.let { it[0] to it[1].toInt() to it[2] }
                    ?: continue
                val (_, rentingIndex) = ri
                Timber.i("Displaying item for cat $category (vs $rentingCatDisp), at $index (vs $rentingIndex)")
                if (category == rentingCatDisp && rentingIndex == index)
                    add(userId to date)
            }
        }

        /*val rentingLimit = item.amount.toInt() - rents.size

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                category,
                modifier = Modifier.weight(1f),
                fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                if (rents.isEmpty())
                    item.amount.toString()
                else
                    "$rentingLimit (${item.amount})",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge,
            )
        }
        Text(
            text = "${item.name} - ${item.type}",
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            IconButton(
                onClick = { selectedAmount-- },
                enabled = selectedAmount > 0,
            ) {
                Icon(
                    Icons.Rounded.Remove,
                    "", // TODO: Localize
                )
            }
            TextField(
                value = selectedAmount.toString(),
                onValueChange = {},
                readOnly = true,
                enabled = false,
                modifier = Modifier.weight(1f),
                singleLine = true,
                maxLines = 1,
                colors = TextFieldDefaults.outlinedTextFieldColors(),
            )
            IconButton(
                onClick = { selectedAmount++ },
                enabled = selectedAmount < rentingLimit,
            ) {
                Icon(
                    Icons.Rounded.Add,
                    "", // TODO: Localize
                )
            }
        }*/
    }
}
