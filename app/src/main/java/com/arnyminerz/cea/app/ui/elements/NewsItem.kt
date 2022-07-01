package com.arnyminerz.cea.app.ui.elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import coil.size.SizeResolver
import com.arnyminerz.cea.app.utils.launchUrl
import com.arnyminerz.cea.app.utils.toPx
import com.prof.rssparser.Article
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
@ExperimentalMaterial3Api
fun NewsItem(
    article: Article
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(enabled = article.link != null) {
                article.link?.let { context.launchUrl(it) }
            },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(article.image)
                    .crossfade(true)
                    .size(
                        SizeResolver(
                            Size(
                                configuration.screenWidthDp.toPx.toInt(),
                                200.dp.toPx.toInt(),
                            )
                        )
                    )
                    .build(),
                contentDescription = "", // TODO: Localize
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.FillWidth,
            )

            Text(
                article.title!!,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .fillMaxWidth()
            )
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .fillMaxWidth()
            ) {
                val pubDate = article.pubDate
                if (pubDate != null) {
                    val date = SimpleDateFormat("E, d MMM yyyy HH:mm:ss Z", Locale.getDefault())
                        .parse(pubDate)
                    if (date != null) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarToday,
                            contentDescription = "Date", // TODO: Localize
                        )
                        Text(
                            SimpleDateFormat.getDateInstance().format(date),
                        )
                    }
                }

                val categories = article.categories
                if (categories.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Rounded.Folder,
                        contentDescription = "Categories", // TODO: Localize
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Text(
                        categories.joinToString(", "),
                        modifier = Modifier.weight(1f),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton(onClick = { /* TODO: Share url */ }) {
                    Icon(
                        Icons.Rounded.Share,
                        "Share", // Localize
                    )
                }
            }
        }
    }
}
