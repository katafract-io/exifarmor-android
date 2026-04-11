package com.katafract.exifarmor.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.katafract.exifarmor.billing.BillingManager
import com.katafract.exifarmor.models.PhotoMetadata
import com.katafract.exifarmor.models.StripOptions
import com.katafract.exifarmor.models.StripResult
import com.katafract.exifarmor.services.MetadataService
import com.katafract.exifarmor.services.StripService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    PREVIEW,
    PROCESSING,
    DONE,
}

class MainViewModel(application: android.app.Application) : AndroidViewModel(application) {

    private val context: Context
        get() = getApplication()

    private val _screen = MutableStateFlow(Screen.HOME)
    val screen: StateFlow<Screen> = _screen

    private val _photoList = MutableStateFlow<List<PhotoMetadata>>(emptyList())
    val photoList: StateFlow<List<PhotoMetadata>> = _photoList

    private val _stripResults = MutableStateFlow<List<StripResult>>(emptyList())
    val stripResults: StateFlow<List<StripResult>> = _stripResults

    private val _stripOptions = MutableStateFlow(StripOptions.PRIVACY_FOCUSED)
    val stripOptions: StateFlow<StripOptions> = _stripOptions

    private val _processingProgress = MutableStateFlow(0f)
    val processingProgress: StateFlow<Float> = _processingProgress

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _showUpgradeSheet = MutableStateFlow(false)
    val showUpgradeSheet: StateFlow<Boolean> = _showUpgradeSheet.asStateFlow()

    val requestBillingLaunch = MutableSharedFlow<Unit>()

    private val billing = BillingManager(
        context = context,
        onPurchaseSuccess = { _, productId ->
            if (productId == BillingManager.PROD_PRO) {
                viewModelScope.launch {
                    _error.emit("Pro unlocked! Enjoy unlimited batch cleaning.")
                    _showUpgradeSheet.emit(false)
                }
            }
        },
        onPurchaseError = { message ->
            viewModelScope.launch {
                _error.emit(message)
            }
        },
    )

    val isPro: StateFlow<Boolean> = billing.isPro

    init {
        billing.connect()
    }

    fun loadPhotos(uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            val isPro = isPro.value
            val toProcess = if (!isPro && uris.size > 5) uris.take(5) else uris

            val metadata = toProcess.mapNotNull { uri ->
                MetadataService.readMetadata(context, uri)
            }

            if (!isPro && uris.size > 5) {
                _error.emit("Free tier limited to 5 photos. Upgrade for unlimited batch processing.")
            }

            _photoList.emit(metadata)
            _screen.emit(Screen.PREVIEW)
        }
    }

    fun updateOptions(options: StripOptions) {
        viewModelScope.launch {
            _stripOptions.emit(options)
        }
    }

    fun startStrip() {
        viewModelScope.launch(Dispatchers.IO) {
            _screen.emit(Screen.PROCESSING)
            _processingProgress.emit(0f)

            val photos = _photoList.value
            val options = _stripOptions.value
            val results = mutableListOf<StripResult>()

            for ((index, metadata) in photos.withIndex()) {
                val result = StripService.strip(context, metadata.uri, metadata, options)
                results.add(result)
                _processingProgress.emit((index + 1f) / photos.size)
            }

            _stripResults.emit(results)
            _screen.emit(Screen.DONE)
        }
    }

    fun shareResults(context: Context) {
        val cleanUris = _stripResults.value.mapNotNull { it.cleanUri }
        if (cleanUris.isEmpty()) {
            viewModelScope.launch {
                _error.emit("No cleaned photos to share")
            }
            return
        }

        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "image/jpeg"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(cleanUris))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share cleaned photos"))
    }

    fun launchBilling(activity: Activity) {
        viewModelScope.launch {
            requestBillingLaunch.emit(Unit)
            billing.launchPurchase(activity, BillingManager.PROD_PRO)
        }
    }

    fun restorePurchases() {
        billing.restorePurchases()
    }

    fun dismissUpgrade() {
        viewModelScope.launch {
            _showUpgradeSheet.emit(false)
        }
    }

    fun showUpgrade() {
        viewModelScope.launch {
            _showUpgradeSheet.emit(true)
        }
    }

    fun clearError() {
        viewModelScope.launch {
            _error.emit(null)
        }
    }

    fun reset() {
        viewModelScope.launch {
            _photoList.emit(emptyList())
            _stripResults.emit(emptyList())
            _screen.emit(Screen.HOME)
            _processingProgress.emit(0f)
            _error.emit(null)
        }
    }

    override fun onCleared() {
        billing.disconnect()
        super.onCleared()
    }
}
