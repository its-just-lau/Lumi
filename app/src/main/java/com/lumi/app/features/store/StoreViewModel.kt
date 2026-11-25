package com.lumi.app.features.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lumi.app.core.network.toUserMessage
import com.lumi.app.data.models.store.StoreItemResponse
import com.lumi.app.data.repositories.ProgressRepository
import com.lumi.app.data.repositories.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import com.lumi.app.core.i18n.AppText
import com.lumi.app.R

data class StoreData(
    val items: List<StoreItemResponse> = emptyList(),
    // Cantidad que el usuario tiene de cada artículo (itemId -> cantidad)
    val inventory: Map<String, Int> = emptyMap(),
    val gems: Int = 0,
    val loaded: Boolean = false
)

class StoreViewModel(
    private val storeRepository: StoreRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _data = MutableStateFlow(StoreData())
    val data: StateFlow<StoreData> = _data.asStateFlow()

    private val _buying = MutableStateFlow(false)
    val buying: StateFlow<Boolean> = _buying.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    // Texto de la compra exitosa; al tener valor se muestra la celebración de Lumi.
    private val _purchased = MutableStateFlow<String?>(null)
    val purchased: StateFlow<String?> = _purchased.asStateFlow()

    fun load() {
        viewModelScope.launch {
            storeRepository.getItems()
                .onSuccess { items ->
                    _data.value = _data.value.copy(items = items, loaded = true)
                }
                .onFailure {
                    _data.value = _data.value.copy(loaded = true)
                    _message.value = it.toUserMessage(AppText.get(R.string.no_se_pudo_cargar_la))
                }
        }

        viewModelScope.launch {
            storeRepository.getInventory().onSuccess { list ->
                _data.value = _data.value.copy(
                    inventory = list
                        .filter { it.itemId != null }
                        .associate { it.itemId!! to it.quantity }
                )
            }
        }

        viewModelScope.launch {
            progressRepository.getWallet().onSuccess {
                _data.value = _data.value.copy(gems = it.gems)
            }
        }
    }

    fun purchase(item: StoreItemResponse) {
        if (_buying.value) return

        viewModelScope.launch {
            _buying.value = true

            storeRepository.purchase(
                itemId = item.id,
                idempotencyKey = UUID.randomUUID().toString()
            ).onSuccess { response ->
                val current = _data.value
                _data.value = current.copy(
                    gems = response.wallet?.gems ?: current.gems,
                    inventory = response.inventory?.let {
                        current.inventory + (item.id to it.quantity)
                    } ?: (current.inventory + (item.id to (current.inventory[item.id] ?: 0) + 1))
                )
                _purchased.value = AppText.get(R.string.canjeaste, item.name)
            }.onFailure {
                _message.value = it.toUserMessage(AppText.get(R.string.no_se_pudo_completar_el))
            }

            _buying.value = false
        }
    }

    fun consumeMessage() {
        _message.value = null
    }

    fun consumePurchased() {
        _purchased.value = null
    }
}

class StoreViewModelFactory(
    private val storeRepository: StoreRepository,
    private val progressRepository: ProgressRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoreViewModel::class.java)) {
            return StoreViewModel(storeRepository, progressRepository) as T
        }

        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
