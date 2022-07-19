package com.arnyminerz.cea.app.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.cea.app.R
import com.arnyminerz.cea.app.data.RentingData
import com.arnyminerz.cea.app.ui.elements.CardWithHeader
import com.arnyminerz.cea.app.utils.getParcelableCompat
import com.arnyminerz.cea.app.viewmodel.RentingViewModel

class ReturnActivity : AppCompatActivity() {
    companion object {
        /**
         * When the item got returned correctly.
         * @author Arnau Mora
         * @since 20220719
         */
        const val RESULT_RETURNED = 0

        /**
         * When the Activity is launched with a missing parameter.
         * @author Arnau Mora
         * @since 20220719
         */
        const val RESULT_WRONG_REQUEST = 1

        /**
         * When the user cancels the return, by pressing either the back or the close buttons.
         * @author Arnau Mora
         * @since 20220719
         */
        const val RESULT_USER_CANCELLED = 2

        /**
         * Specifies what key was missing when making the request, and thrown [RESULT_WRONG_REQUEST].
         * @author Arnau Mora
         * @since 20220719
         */
        const val RESULT_EXTRA_MISSING_KEY = "missing_key"

        private const val EXTRA_RENTING = "renting"

        fun intent(context: Context, rentingData: RentingData) =
            Intent(context, ReturnActivity::class.java).apply {
                putExtra(EXTRA_RENTING, rentingData)
            }
    }

    private val rentingViewModel by viewModels<RentingViewModel> {
        RentingViewModel.Factory(
            application
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent.extras
        if (extras?.containsKey(EXTRA_RENTING) != true) {
            setResult(
                RESULT_WRONG_REQUEST,
                Intent().putExtra(RESULT_EXTRA_MISSING_KEY, EXTRA_RENTING)
            )
            finish()
            return
        }
        val rentingData = extras.getParcelableCompat(EXTRA_RENTING, RentingData::class)!!

        setContent {
            BackHandler(onBack = ::backHandler)

            var confirmed by remember { mutableStateOf(false) }
            var loading by remember { mutableStateOf(false) }

            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(stringResource(R.string.return_renting_title))
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = ::backHandler,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = stringResource(R.string.image_desc_close),
                                )
                            }
                        },
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier.padding(paddingValues)
                ) {
                    CardWithHeader(
                        icon = Icons.Rounded.Receipt,
                        contentDescription = stringResource(R.string.image_desc_return),
                        title = stringResource(R.string.return_renting_card_title),
                        modifier = Modifier.padding(horizontal = 8.dp),
                    ) {
                        Text(
                            stringResource(R.string.return_renting_card_message),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                        ) {
                            Text(
                                "x${rentingData.amount}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(8.dp),
                            )
                            Text(
                                rentingData.item.displayName,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(8.dp),
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable { confirmed = !confirmed },
                            verticalAlignment = Alignment.Top,
                        ) {
                            Checkbox(
                                checked = confirmed,
                                onCheckedChange = { confirmed = it },
                            )
                            Text(
                                text = stringResource(R.string.return_renting_confirm),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                        OutlinedButton(
                            enabled = confirmed && !loading,
                            onClick = {
                                loading = true
                                rentingViewModel.returnRent(rentingData) {
                                    loading = false
                                    setResult(RESULT_OK)
                                    finish()
                                }
                            },
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 12.dp)
                                .fillMaxWidth(),
                        ) {
                            Text(text = stringResource(R.string.action_return))
                        }
                        AnimatedVisibility(loading) {
                            LinearProgressIndicator()
                        }
                    }
                }
            }
        }
    }

    private fun backHandler() {
        setResult(RESULT_USER_CANCELLED)
        finish()
    }
}