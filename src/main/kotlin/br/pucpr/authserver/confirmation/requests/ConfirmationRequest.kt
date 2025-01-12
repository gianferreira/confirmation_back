package br.pucpr.authserver.confirmation.requests

data class ConfirmationRequest(
    val phone: String?,
    val device: String?,
    val code: String?
)
