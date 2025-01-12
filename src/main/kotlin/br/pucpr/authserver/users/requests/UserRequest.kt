package br.pucpr.authserver.users.requests

import br.pucpr.authserver.users.User

data class UserRequest(
    val name: String?,
    val nick: String?,
) {
    fun toUser() = User(
        name = name ?: "",
        nick = nick ?: "",
    )
}
