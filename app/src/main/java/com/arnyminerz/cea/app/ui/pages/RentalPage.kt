package com.arnyminerz.cea.app.ui.pages

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.arnyminerz.cea.app.activity.RentingActivity
import com.arnyminerz.cea.app.utils.format
import com.arnyminerz.cea.app.viewmodel.RentingViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import timber.log.Timber

@ExperimentalMaterial3Api
@Composable
fun RentalPage(viewModel: RentingViewModel, rentContract: ActivityResultLauncher<Intent>) {
    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    Timber.i("Launching RentingActivity...")
                    rentContract.launch(
                        Intent(context, RentingActivity::class.java)
                    )
                },
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "", // TODO: Localize
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            val userRentingData by viewModel.userRentingData

            LazyColumn {
                items(userRentingData ?: emptyList()) { item ->
                    Text(item.timestamp.format("dd-MM-yyyy hh:mm:ss"))
                    Text(item.item.displayName + " x" + item.amount + " (${item.item.id})")
                }
            }

            Firebase.auth.currentUser?.let { viewModel.loadUserRenting(it.uid, true) }
        }
    }
}
