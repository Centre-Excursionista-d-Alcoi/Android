package com.arnyminerz.cea.app.ui.screen

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.arnyminerz.cea.app.R

@Composable
fun AuthScreen(onLoginRequest: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        var expanded by remember { mutableStateOf(false) }

        Button(
            onClick = onLoginRequest,
            modifier = Modifier
                .align(Alignment.Center),
        ) {
            Text("Login")
        }

        // Language picker
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .wrapContentSize(Alignment.BottomEnd)
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .clickable { expanded = true },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Rounded.Language,
                    contentDescription = stringResource(R.string.image_desc_language),
                )
                Text(
                    stringResource(R.string.language_label),
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    onClick = {
                        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags("en-US")
                        AppCompatDelegate.setApplicationLocales(appLocale)
                    },
                    text = {
                        Text("English")
                    },
                )
            }
        }
    }
}
