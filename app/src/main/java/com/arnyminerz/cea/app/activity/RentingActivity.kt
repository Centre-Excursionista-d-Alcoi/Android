package com.arnyminerz.cea.app.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnyminerz.cea.app.R
import com.arnyminerz.cea.app.data.InventoryItem
import com.arnyminerz.cea.app.ui.elements.InventoryItem
import com.arnyminerz.cea.app.ui.theme.CEAAppTheme
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class RentingActivity : AppCompatActivity() {
    companion object {
        const val RESULT_CANCEL = 1
    }

    private val db = Firebase.firestore

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CEAAppTheme {
                RentingWindow()
            }
        }
    }

    @Composable
    @ExperimentalMaterial3Api
    fun RentingWindow() {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(stringResource(R.string.renting_title))
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                setResult(
                                    RESULT_CANCEL,
                                    Intent(),
                                )
                                finish()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "", // TODO: Localize
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = {
                        Text(stringResource(R.string.renting_confirm).format(5))
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "", // TODO: Localize
                        )
                    },
                    onClick = { /*TODO*/ },
                )
            },
            floatingActionButtonPosition = FabPosition.End,
            content = { padding ->
                Column(
                    modifier = Modifier.padding(padding)
                ) {
                    var inventory by remember {
                        mutableStateOf<Map<String, List<InventoryItem>>?>(
                            null
                        )
                    }
                    var rentingItems by remember {
                        mutableStateOf<Map<String, Pair<String, Timestamp>>?>(null)
                    }

                    AnimatedVisibility(visible = inventory == null || rentingItems == null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    var activeFilters by remember { mutableStateOf<List<String>>(emptyList()) }

                    if (inventory != null && rentingItems != null) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            items(inventory!!.size) { index ->
                                val key = inventory!!.keys.toList()[index]
                                FilterChip(
                                    selected = activeFilters.contains(key),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                                    onClick = {
                                        activeFilters = activeFilters
                                            .toMutableList()
                                            .apply {
                                                if (contains(key))
                                                    remove(key)
                                                else
                                                    add(key)
                                            }
                                    },
                                    label = {
                                        Text(key)
                                    }
                                )
                            }
                        }
                        LazyColumn {
                            items(inventory!!.size) { index ->
                                val key = inventory!!.keys.toList()[index]
                                val items: List<InventoryItem> = inventory!![key] ?: emptyList()
                                if (activeFilters.contains(key))
                                    for (item in items)
                                        InventoryItem(index, key, item, rentingItems!!)
                            }
                        }
                    }

                    db.collection("inventory")
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val documents = snapshot.documents
                            val list = hashMapOf<String, List<InventoryItem>>()
                            for (document in documents) {
                                val displayName = document.getString("displayName")!!
                                list[displayName] = (document.get("items") as ArrayList<*>)
                                    .map { item ->
                                        item as Map<*, *>
                                        val amount = item["amount"] as Long
                                        val description = item["description"] as String
                                        val name = item["name"] as String
                                        val type = item["type"] as String

                                        InventoryItem(
                                            amount.toUInt(),
                                            name,
                                            description,
                                            type,
                                        )
                                    }
                            }
                            inventory = list

                            db.collection("users")
                                .get()
                                .addOnSuccessListener { usersSnapshot ->
                                    val usersDocuments = usersSnapshot.documents
                                    val rentingItemsBuilder =
                                        hashMapOf<String, Pair<String, Timestamp>>()
                                    for (document in usersDocuments) {
                                        if (!document.contains("renting"))
                                            continue
                                        val renting = document.get("renting") as Map<*, *>
                                        for (rent in renting.keys) {
                                            rent as String
                                            val rentCategory = rent.substring(0, rent.indexOf('/'))
                                            val category = snapshot
                                                .documents
                                                .find { it.id == rentCategory }
                                                ?.getString("displayName")
                                            rentingItemsBuilder[document.id] =
                                                ("$rent/$category") to renting[rent] as Timestamp
                                        }
                                    }
                                    rentingItems = rentingItemsBuilder
                                    Timber.i("Renting items: $rentingItems")
                                }
                                .addOnFailureListener { e ->
                                    // TODO: Notify the user
                                    Timber.e(e, "Could not get inventory.")
                                }
                        }
                        .addOnFailureListener { e ->
                            // TODO: Notify the user
                            Timber.e(e, "Could not get inventory.")
                        }
                }
            }
        )
    }
}
