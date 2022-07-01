package com.arnyminerz.cea.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnyminerz.cea.app.activity.RentingActivity
import com.arnyminerz.cea.app.data.InventoryItem
import com.arnyminerz.cea.app.data.RentingData
import com.arnyminerz.cea.app.ui.data.NavItem
import com.arnyminerz.cea.app.ui.elements.NewsItem
import com.arnyminerz.cea.app.ui.elements.RentItem
import com.arnyminerz.cea.app.ui.screen.AuthScreen
import com.arnyminerz.cea.app.ui.theme.CEAAppTheme
import com.arnyminerz.cea.app.viewmodel.NewsViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore

    private val firebaseUser = mutableStateOf<FirebaseUser?>(null)

    private val newsViewModel by viewModels<NewsViewModel> { NewsViewModel.Factory(application) }

    private val rentContract = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

    }

    private val loginContract = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        try {
            val data = result.data
            val credential = oneTapClient.getSignInCredentialFromIntent(data)
            val idToken = credential.googleIdToken
            when {
                idToken != null -> {
                    // Got an ID token from Google. Use it to authenticate
                    // with Firebase.
                    Timber.i("Got ID token. Logging in with Firebase...")

                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Timber.d("signInWithCredential:success")
                                val user = auth.currentUser
                                firebaseUser.value = user
                            } else {
                                // If sign in fails, display a message to the user.
                                Timber.e(task.exception, "signInWithCredential:failure")
                                firebaseUser.value = null
                            }
                        }
                }
                else -> {
                    // Shouldn't happen.
                    Timber.e("No ID token!")
                }
            }
        } catch (e: ApiException) {
            Timber.e(e, "Could not login.")
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.default_web_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

        firebaseUser.value = auth.currentUser

        setContent {
            CEAAppTheme {
                val user by remember { firebaseUser }

                if (user == null)
                    AuthScreen {
                        oneTapClient.beginSignIn(signInRequest)
                            .addOnSuccessListener { result ->
                                loginContract.launch(
                                    IntentSenderRequest
                                        .Builder(result.pendingIntent.intentSender)
                                        .build()
                                )
                            }
                            .addOnFailureListener { error ->
                                Timber.e(error, "Could not begin sign in.")
                            }
                    }
                else {
                    var selectedItem by remember { mutableStateOf(0) }
                    val news by newsViewModel.news

                    val scope = rememberCoroutineScope()
                    val pagerState = rememberPagerState(selectedItem)

                    LaunchedEffect(pagerState) {
                        // Collect from the pager state a snapshotFlow reading the currentPage
                        snapshotFlow { pagerState.currentPage }.collect { page ->
                            selectedItem = page
                        }
                    }

                    val pages = listOf(
                        NavItem(
                            Icons.Rounded.Newspaper,
                            Icons.Outlined.Newspaper,
                            R.string.main_menu_news,
                        ),
                        NavItem(
                            Icons.Rounded.EditNote,
                            Icons.Outlined.EditNote,
                            R.string.main_menu_rental,
                        ),
                        NavItem(
                            Icons.Rounded.Person,
                            Icons.Outlined.Person,
                            R.string.main_menu_profile,
                        ),
                        NavItem(
                            Icons.Rounded.Settings,
                            Icons.Outlined.Settings,
                            R.string.main_menu_settings,
                        ),
                    )

                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                pages.forEachIndexed { index, item ->
                                    NavigationBarItem(
                                        selected = selectedItem == index,
                                        onClick = {
                                            selectedItem = index
                                            scope.launch { pagerState.animateScrollToPage(index) }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (selectedItem == index)
                                                    item.selectedIcon
                                                else
                                                    item.unselectedIcon,
                                                contentDescription = stringResource(item.textRes),
                                            )
                                        },
                                        label = {
                                            Text(stringResource(item.textRes))
                                        },
                                        alwaysShowLabel = false,
                                    )
                                }
                            }
                        },
                        content = { paddingValues ->
                            HorizontalPager(
                                count = pages.size,
                                state = pagerState,
                                contentPadding = paddingValues,
                            ) { page ->
                                if (page == 0)
                                    if (news == null)
                                        Text("loading...")
                                    else
                                        LazyColumn {
                                            items(news ?: emptyList()) { article ->
                                                NewsItem(article)
                                            }
                                        }
                                else if (page == 1) {
                                    Box(
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        FloatingActionButton(
                                            onClick = {
                                                rentContract.launch(
                                                    Intent(
                                                        this@MainActivity,
                                                        RentingActivity::class.java
                                                    )
                                                )
                                            },
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .align(Alignment.BottomEnd),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Add,
                                                contentDescription = "", // TODO: Localize
                                            )
                                        }

                                        var rentItems by remember {
                                            mutableStateOf<List<RentingData>>(
                                                emptyList()
                                            )
                                        }

                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxSize()
                                        ) {
                                            items(rentItems) { i ->
                                                RentItem(i)
                                            }
                                        }

                                        db.collection("users")
                                            .document(user!!.uid)
                                            .get()
                                            .addOnSuccessListener { userDocument ->
                                                rentItems = emptyList()

                                                val renting =
                                                    userDocument.get("renting") as Map<*, *>
                                                for ((catKey, date) in renting) {
                                                    catKey as String
                                                    date as Timestamp
                                                    val (category, itemIndex) = catKey
                                                        .split("/")
                                                        .let { it[0] to it[1].toInt() }
                                                    db.collection("inventory")
                                                        .document(category)
                                                        .get()
                                                        .addOnSuccessListener { document ->
                                                            val displayName =
                                                                document.getString("displayName")!!
                                                            val items =
                                                                document.get("items") as ArrayList<*>
                                                            val item = items[itemIndex] as Map<*, *>
                                                            val amount = item["amount"] as Long
                                                            val name = item["name"] as String
                                                            val description =
                                                                item["description"] as String
                                                            val type = item["type"] as String

                                                            rentItems = rentItems
                                                                .toMutableList()
                                                                .apply {
                                                                    add(
                                                                        RentingData(
                                                                            displayName,
                                                                            InventoryItem(
                                                                                amount.toUInt(),
                                                                                name,
                                                                                description,
                                                                                type,
                                                                            ),
                                                                            date.toDate(),
                                                                            // TODO: Amount should be computed by awaiting the inventory contents load, so it can be synchronized with
                                                                            // the greater for, and start counting repetitions of the same renting
                                                                            1U,
                                                                        )
                                                                    )
                                                                }
                                                        }
                                                        .addOnFailureListener { e ->
                                                            // TODO: Notify the user
                                                            Timber.e(e, "Could not get inventory.")
                                                        }
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                // TODO: Notify the user
                                                Timber.e(e, "Could not get inventory.")
                                            }
                                    }
                                } else
                                    Text("Page: $page")
                            }
                        }
                    )

                    if (news == null)
                        newsViewModel.loadNews()
                }
            }
        }
    }
}
