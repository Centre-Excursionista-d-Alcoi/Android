package com.arnyminerz.cea.app.viewmodel

import android.app.Application
import androidx.annotation.UiThread
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.arnyminerz.cea.app.data.ConstrainedInventoryItem
import com.arnyminerz.cea.app.data.InventoryItem
import com.arnyminerz.cea.app.data.RentingData
import com.arnyminerz.cea.app.data.Section
import com.arnyminerz.cea.app.data.divideInSections
import com.arnyminerz.cea.app.provider.RentingDataProvider
import com.arnyminerz.cea.app.utils.io
import com.arnyminerz.cea.app.utils.ui
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.launch

/**
 * Used for loading, displaying and requesting inventory-related things.
 * @author Arnau Mora
 * @since 202200719
 * @param application The application used for initializing the ViewModel.
 */
class RentingViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * A reference to the [RentingDataProvider] used for making all the requests.
     * @author Arnau Mora
     * @since 202200719
     */
    private val renting: RentingDataProvider = RentingDataProvider.getInstance(application)

    /**
     * A mutable state of the list of sections and items. Gets updated by [loadSectionsWithItems]
     * with the loaded items.
     * @author Arnau Mora
     * @since 202200719
     * @see loadSectionsWithItems
     */
    val sectionsWithItems = mutableStateOf<Map<Section, List<InventoryItem>>?>(null)

    val availableItems = mutableStateOf<Map<Section, List<ConstrainedInventoryItem>>?>(null)

    val userRentingData = mutableStateOf<List<RentingData>?>(null)

    /**
     * Uses the ViewModel's scope to load all the inventory items from the server or the local
     * storage if available.
     * @author Arnau Mora
     * @since 20220719
     */
    fun loadSectionsWithItems() =
        viewModelScope.launch {
            val inventory = io { renting.fetchInventory() }
            ui { sectionsWithItems.value = inventory }
        }

    fun loadAvailableItems() =
        viewModelScope.launch {
            val items = io { renting.fetchRentingData() }
            ui { availableItems.value = items.divideInSections() }
        }

    fun loadUserRenting(uid: String, filterReturned: Boolean = false) =
        viewModelScope.launch {
            val list = io { renting.fetchUserRentingData(uid) }
                .filter { !filterReturned || !it.hasBeenReturned }
            ui { userRentingData.value = list }
        }

    fun rent(
        items: List<RentingData>,
        @UiThread onRentFinished: (documents: List<DocumentReference>) -> Unit
    ) = viewModelScope.launch {
        val documents = io { renting.rent(items) }
        ui { onRentFinished(documents) }
    }

    fun returnRent(rentingData: RentingData, @UiThread onReturned: () -> Unit) =
        viewModelScope.launch {
            io { renting.returnRent(rentingData) }
            ui { onReturned() }
        }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            RentingViewModel(application) as T
    }
}