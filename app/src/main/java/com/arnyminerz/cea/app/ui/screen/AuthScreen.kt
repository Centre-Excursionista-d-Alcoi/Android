package com.arnyminerz.cea.app.ui.screen

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import coil.compose.AsyncImage
import com.arnyminerz.cea.app.BuildConfig
import com.arnyminerz.cea.app.R
import timber.log.Timber
import java.util.Locale

@Composable
fun AuthScreen(onLoginRequest: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        var expanded by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .width(128.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AsyncImage(
                R.mipmap.ic_launcher,
                contentDescription = stringResource(R.string.image_desc_app_icon),
                modifier = Modifier
                    .fillMaxWidth(.7f)
                    .padding(bottom = 32.dp),
            )

            Button(
                onClick = onLoginRequest,
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Image(
                    painter = painterResource(
                        com.google.firebase.firestore.R.drawable.googleg_standard_color_18
                    ),
                    contentDescription = stringResource(R.string.image_desc_google_logo)
                )
                Text(
                    // TODO: Localize message
                    "Login",
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                )
            }
        }

        // Language picker
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .wrapContentSize(Alignment.BottomEnd)
                .padding(8.dp)
        ) {
            val languageTags = BuildConfig.TRANSLATION_ARRAY
            Timber.i("There are ${languageTags.size} locales available.")

            Row(
                modifier = Modifier
                    .clickable { expanded = true },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Rounded.Language,
                    contentDescription = stringResource(R.string.image_desc_language),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(end = 4.dp),
                )
                Text(
                    stringResource(R.string.language_label),
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                languageTags
                    .map { Locale(it) }
                    .forEach { locale ->
                        DropdownMenuItem(
                            onClick = {
                                val appLocale: LocaleListCompat =
                                    LocaleListCompat.forLanguageTags(locale.toLanguageTag())
                                AppCompatDelegate.setApplicationLocales(appLocale)
                            },
                            text = { Text(locale.displayName.replaceFirstChar { it.uppercaseChar() }) },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.onBackground,
                            )
                        )
                    }
            }
        }
    }
}
