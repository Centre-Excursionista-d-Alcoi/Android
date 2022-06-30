package com.arnyminerz.cea.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import com.arnyminerz.cea.app.ui.data.NavItem
import com.arnyminerz.cea.app.ui.screen.AuthScreen
import com.arnyminerz.cea.app.ui.theme.CEAAppTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : ComponentActivity() {
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    private val auth: FirebaseAuth = Firebase.auth

    private val firebaseUser = mutableStateOf<FirebaseUser?>(null)

    private val contract = registerForActivityResult(
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
                                contract.launch(
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
                                Text("Page: $page")
                            }
                        }
                    )
                }
            }
        }
    }
}
