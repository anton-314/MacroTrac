package dev.antonlammers.trainist.domain.model

sealed class BarcodeException(message: String) : Exception(message) {
    object ServerUnavailable : BarcodeException("server_unavailable")
    object NetworkUnavailable : BarcodeException("network_unavailable")
}
