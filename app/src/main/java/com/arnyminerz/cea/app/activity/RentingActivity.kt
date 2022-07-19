package com.arnyminerz.cea.app.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arnyminerz.cea.app.R
import com.arnyminerz.cea.app.data.ConstrainedInventoryItem
import com.arnyminerz.cea.app.data.InventoryItem
import com.arnyminerz.cea.app.data.RentingData
import com.arnyminerz.cea.app.ui.dialog.DatePicker
import com.arnyminerz.cea.app.ui.dialog.DatePickerDisplay
import com.arnyminerz.cea.app.ui.elements.CardWithHeader
import com.arnyminerz.cea.app.ui.elements.renting.ItemCard
import com.arnyminerz.cea.app.ui.elements.renting.SectionCard
import com.arnyminerz.cea.app.ui.theme.CEAAppTheme
import com.arnyminerz.cea.app.utils.format
import com.arnyminerz.cea.app.utils.sumPrice
import com.arnyminerz.cea.app.utils.toast
import com.arnyminerz.cea.app.viewmodel.RentingViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import java.util.Date

class RentingActivity : AppCompatActivity() {
    companion object {
        const val RESULT_OK = 0
        const val RESULT_CANCEL = 1

        const val EXTRA_RENTING_DOCUMENTS = "documents"
    }

    private val viewModel by viewModels<RentingViewModel> { RentingViewModel.Factory(application) }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = Firebase.auth
        val user = auth.currentUser

        setContent {
            CEAAppTheme {
                var selectedAmount by remember { mutableStateOf(0) }
                var currentPage by remember { mutableStateOf(0) }
                val checkoutItems = remember { mutableStateMapOf<InventoryItem, Int>() }
                var renting by remember { mutableStateOf(false) }
                var isCheckoutValid by remember { mutableStateOf(false) }
                var startDate: Date? by remember { mutableStateOf(null) }
                var endDate: Date? by remember { mutableStateOf(null) }
                var paymentMethod: Int? by remember { mutableStateOf(null) }

                val scope = rememberCoroutineScope()
                val pagerState = rememberPagerState()

                fun backHandler() {
                    if (currentPage == 0) {
                        setResult(
                            RESULT_CANCEL,
                            Intent(),
                        )
                        finish()
                    } else
                        scope.launch { pagerState.animateScrollToPage(0) }
                }
                BackHandler(onBack = ::backHandler)

                LaunchedEffect(pagerState) {
                    // Collect from the pager state a snapshotFlow reading the currentPage
                    snapshotFlow { pagerState.currentPage }.collect { page -> currentPage = page }
                }

                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(stringResource(R.string.renting_title))
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = ::backHandler,
                                ) {
                                    Icon(
                                        imageVector = if (currentPage == 0) Icons.Rounded.Close else Icons.Rounded.ChevronLeft,
                                        contentDescription = "", // TODO: Localize
                                    )
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        if (selectedAmount > 0) {
                            ExtendedFloatingActionButton(
                                containerColor = MaterialTheme.colorScheme.secondary
                                    .copy(
                                        alpha = if (isCheckoutValid && !renting)
                                            1f
                                        else
                                            ContentAlpha.disabled,
                                    ),
                                contentColor = MaterialTheme.colorScheme.onSecondary,
                                text = {
                                    Text(
                                        if (currentPage == 0)
                                            stringResource(R.string.renting_confirm)
                                                .format(selectedAmount)
                                        else if (!isCheckoutValid)
                                            stringResource(R.string.renting_rent_missing)
                                        else
                                            stringResource(R.string.renting_rent),
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (currentPage == 0)
                                            Icons.Rounded.ChevronRight
                                        else if (!isCheckoutValid)
                                            Icons.Rounded.Close
                                        else
                                            Icons.Rounded.Check,
                                        contentDescription = stringResource(R.string.image_desc_checkout),
                                    )
                                },
                                onClick = {
                                    if (currentPage == 0)
                                        scope.launch { pagerState.animateScrollToPage(1) }
                                    else if (currentPage == 1)
                                        if (isCheckoutValid) {
                                            checkoutItems
                                                .map { (item, amount) ->
                                                    RentingData(
                                                        String(),
                                                        Calendar.getInstance().time,
                                                        user!!.uid,
                                                        item,
                                                        amount.toLong(),
                                                        startDate,
                                                        endDate,
                                                        null
                                                    )
                                                }
                                                .also {
                                                    renting = true
                                                    viewModel.rent(it) { documents ->
                                                        renting = false

                                                        setResult(
                                                            RESULT_OK,
                                                            Intent()
                                                                .putExtra(
                                                                    EXTRA_RENTING_DOCUMENTS,
                                                                    documents
                                                                        .map { d -> d.id }
                                                                        .toTypedArray(),
                                                                ),
                                                        )
                                                        finish()
                                                    }
                                                }
                                        } else
                                            toast(getString(R.string.toast_error_fill_form))
                                },
                            )
                        }
                    },
                    floatingActionButtonPosition = FabPosition.End,
                    content = { paddingValues ->
                        HorizontalPager(
                            count = 2,
                            modifier = Modifier.padding(paddingValues),
                            userScrollEnabled = false,
                            state = pagerState,
                        ) { page ->
                            when (page) {
                                0 -> RentingSelectionPage(
                                    checkoutItems,
                                    onAmountUpdated = { selectedAmount += it },
                                    onItemUpdated = { item, amount ->
                                        if (amount <= 0)
                                            checkoutItems.remove(item)
                                        else
                                            checkoutItems[item] = amount
                                        Timber.i(
                                            "Item updated. New items: ${
                                                checkoutItems.map { it.key.id to it.value }
                                                    .joinToString()
                                            }"
                                        )
                                    },
                                )
                                1 -> CheckoutPage(
                                    renting,
                                    startDate,
                                    endDate,
                                    paymentMethod,
                                    checkoutItems,
                                    { isCheckoutValid = it },
                                    { startDate = it },
                                    { endDate = it },
                                    { paymentMethod = it },
                                )
                            }
                        }
                    }
                )
            }
        }
    }

    @Composable
    @ExperimentalMaterial3Api
    fun RentingSelectionPage(
        checkoutItems: SnapshotStateMap<InventoryItem, Int>,
        onAmountUpdated: (variance: Int) -> Unit,
        onItemUpdated: (item: InventoryItem, amount: Int) -> Unit,
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            val availableItems by viewModel.availableItems

            AnimatedVisibility(visible = availableItems == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) { CircularProgressIndicator() }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(availableItems?.toList() ?: emptyList()) { (section, items) ->
                    SectionCard(Icons.Rounded.Category, section)
                    items.forEach { item: ConstrainedInventoryItem ->
                        ItemCard(
                            item,
                            checkoutItems.getOrDefault(item.inventoryItem, 0),
                            onAmountUpdated,
                        ) { onItemUpdated(item.inventoryItem, it) }
                    }
                }
            }

            if (availableItems.isNullOrEmpty())
                viewModel.loadAvailableItems()
        }
    }

    @Composable
    @ExperimentalMaterial3Api
    fun CheckoutPage(
        renting: Boolean,
        startDate: Date?,
        endDate: Date?,
        paymentMethod: Int?,
        checkoutItems: SnapshotStateMap<InventoryItem, Int>,
        onCheckoutStateUpdated: (checkoutReady: Boolean) -> Unit,
        onStartDateUpdated: (startDate: Date?) -> Unit,
        onEndDateUpdated: (endDate: Date?) -> Unit,
        onPaymentMethodUpdated: (paymentMethod: Int?) -> Unit,
    ) {
        val focusManager = LocalFocusManager.current

        val (totalPrice, pricePeriod) = checkoutItems
            .mapNotNull { it.key.price }
            .sumPrice()

        fun updateCheckoutState() {
            onCheckoutStateUpdated(
                pricePeriod == InventoryItem.PricingPeriod.NONE ||
                        (startDate != null && endDate != null && paymentMethod != null)
            )
        }
        updateCheckoutState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            CardWithHeader(
                icon = Icons.Rounded.Receipt,
                contentDescription = stringResource(R.string.image_desc_checkout),
                title = stringResource(R.string.renting_checkout_title),
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
            ) {
                val columnWidth = .25f

                Row(Modifier.fillMaxWidth()) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth(.05f)
                            .padding(horizontal = 4.dp),
                    )
                    Text(
                        stringResource(R.string.renting_checkout_name),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        stringResource(R.string.renting_checkout_amount),
                        modifier = Modifier
                            .fillMaxWidth(columnWidth)
                            .padding(horizontal = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        stringResource(R.string.renting_checkout_price),
                        modifier = Modifier
                            .fillMaxWidth(columnWidth)
                            .padding(horizontal = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        stringResource(R.string.renting_checkout_subtotal),
                        modifier = Modifier
                            .fillMaxWidth(columnWidth * 1.2f)
                            .padding(start = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.End,
                    )
                }
                Divider()

                checkoutItems.toList().forEachIndexed { index, (item, amount) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 4.dp, end = 8.dp),
                    ) {
                        Text(
                            (index + 1).toString(),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier
                                .fillMaxWidth(.05f)
                                .padding(horizontal = 4.dp),
                        )
                        Text(
                            item.localizedDisplayName,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            amount.toString(),
                            modifier = Modifier
                                .fillMaxWidth(columnWidth)
                                .padding(horizontal = 4.dp),
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            (item.price?.let { price ->
                                price.amount
                                    .toString()
                                    .plus("€ / ")
                                    .plus(stringResource(price.period.resourceString))
                            }) ?: stringResource(R.string.renting_item_price_free),
                            modifier = Modifier
                                .fillMaxWidth(columnWidth)
                                .padding(horizontal = 4.dp),
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            (item.price?.let { price ->
                                (price.amount * amount)
                                    .toString()
                                    .plus("€ / ")
                                    .plus(stringResource(price.period.resourceString))
                            }) ?: stringResource(R.string.renting_item_price_free),
                            modifier = Modifier
                                .fillMaxWidth(columnWidth * 1.2f)
                                .padding(start = 4.dp),
                            textAlign = TextAlign.End,
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 4.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.renting_checkout_total),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.End,
                    )
                    Text(
                        totalPrice
                            .takeIf { pricePeriod != InventoryItem.PricingPeriod.NONE }
                            ?.toString()
                            ?.plus("€ / ")
                            ?.plus(stringResource(pricePeriod.resourceString))
                            ?: stringResource(R.string.renting_item_price_free),
                        modifier = Modifier
                            .padding(horizontal = 4.dp),
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(Modifier.height(5.dp))
            }

            if (pricePeriod != InventoryItem.PricingPeriod.NONE) {
                var showingDatePicker: DatePickerDisplay? by remember { mutableStateOf(null) }
                if (showingDatePicker != null)
                    DatePicker(
                        showingDatePicker?.minDate,
                        showingDatePicker?.maxDate,
                        onDateSelected = { showingDatePicker?.onDateSelected?.invoke(it); updateCheckoutState() },
                        onDismissRequest = { focusManager.clearFocus(); showingDatePicker = null },
                    )

                CardWithHeader(
                    icon = Icons.Rounded.DateRange,
                    contentDescription = stringResource(R.string.image_desc_dates),
                    title = stringResource(R.string.renting_dates_title),
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = startDate?.format("dd-MM-yyyy") ?: "--/--/----",
                        onValueChange = {},
                        readOnly = true,
                        enabled = !renting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused)
                                    if (!renting)
                                        showingDatePicker = DatePickerDisplay(
                                            Calendar.getInstance().time.time,
                                            endDate?.time,
                                        ) { onStartDateUpdated(it) }
                                    else
                                        focusManager.clearFocus()
                            },
                        label = {
                            Text(stringResource(R.string.renting_dates_start))
                        },
                    )
                    OutlinedTextField(
                        value = endDate?.format("dd-MM-yyyy") ?: "--/--/----",
                        onValueChange = {},
                        readOnly = true,
                        enabled = !renting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused)
                                    if (!renting)
                                        showingDatePicker = DatePickerDisplay(
                                            startDate?.time ?: Calendar.getInstance().time.time,
                                            null,
                                        ) { onEndDateUpdated(it) }
                                    else
                                        focusManager.clearFocus()
                            },
                        label = { Text(stringResource(R.string.renting_dates_end)) },
                    )
                }
            }

            CardWithHeader(
                icon = Icons.Rounded.Payments,
                contentDescription = stringResource(R.string.image_desc_payment_method),
                title = stringResource(R.string.renting_payment_method_title),
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
            ) {
                if (pricePeriod == InventoryItem.PricingPeriod.NONE)
                    Text(
                        stringResource(R.string.renting_payment_not_required),
                        modifier = Modifier.padding(8.dp),
                    )
            }
        }
    }
}
