package com.arnyminerz.cea.app.ui.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.arnyminerz.cea.app.R
import com.arnyminerz.cea.app.data.RentingData
import java.text.SimpleDateFormat
import java.util.Locale

@ExperimentalMaterial3Api
@Composable
fun RentItem(data: RentingData) {
    Card(
        modifier = Modifier.padding(8.dp)
    ) {
        val formattedDate = SimpleDateFormat("yyyy-mm-dd HH:ss", Locale.getDefault())
            .format(data.date)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 4.dp, bottom = 4.dp, end = 4.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                val item = data.item
                Text(
                    "${item.name} - ${item.type}",
                    fontStyle = FontStyle.Italic,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    data.categoryDisplayName,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    formattedDate,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Column {
                OutlinedButton(onClick = { /*TODO*/ }) {
                    Text(stringResource(R.string.action_return))
                }
            }
        }
    }
}
