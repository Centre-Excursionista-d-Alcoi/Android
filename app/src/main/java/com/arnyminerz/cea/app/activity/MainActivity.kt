package com.arnyminerz.cea.app.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.arnyminerz.cea.app.R
import com.arnyminerz.cea.app.ui.data.NavItem
import com.arnyminerz.cea.app.ui.elements.NavigationBarItems
import com.arnyminerz.cea.app.ui.elements.NewsItem
import com.arnyminerz.cea.app.ui.pages.RentalPage
import com.arnyminerz.cea.app.ui.screen.AuthScreen
import com.arnyminerz.cea.app.ui.theme.CEAAppTheme
import com.arnyminerz.cea.app.viewmodel.NewsViewModel
import com.arnyminerz.cea.app.viewmodel.RentingViewModel
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

class MainActivity : AppCompatActivity() {
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    private val auth: FirebaseAuth = Firebase.auth

    private val firebaseUser = mutableStateOf<FirebaseUser?>(null)

    private val newsViewModel by viewModels<NewsViewModel> { NewsViewModel.Factory(application) }
    private val rentingViewModel by viewModels<RentingViewModel> {
        RentingViewModel.Factory(
            application
        )
    }

    private val returnContract = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        auth.currentUser?.uid?.let { rentingViewModel.loadUserRenting(it, true) }
    }

    private val rentContract = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val resultCode = result.resultCode
        val data = result.data?.extras
        if (resultCode == RentingActivity.RESULT_OK && data != null) {
            val documents = data.getStringArray(RentingActivity.EXTRA_RENTING_DOCUMENTS)?.toList()
            Timber.i("Renting ok, documents: ${documents?.joinToString()}")
        } else if (resultCode == RentingActivity.RESULT_CANCEL)
            Timber.i("User cancelled rent.")
        else
            Timber.e("Unknown result code from rent: $resultCode")
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
                                NavigationBarItems(pages, selectedItem) { index ->
                                    selectedItem = index
                                    scope.launch { pagerState.animateScrollToPage(index) }
                                }
                            }
                        },
                        content = { paddingValues ->
                            HorizontalPager(
                                count = pages.size,
                                state = pagerState,
                                contentPadding = paddingValues,
                            ) { page ->
                                when (page) {
                                    0 -> if (news == null)
                                        Text("loading...")
                                    else
                                        LazyColumn {
                                            items(news ?: emptyList()) { article ->
                                                NewsItem(article)
                                            }
                                        }
                                    1 -> RentalPage(rentingViewModel, rentContract, returnContract)
                                    else -> Text("Page: $page")
                                }
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
