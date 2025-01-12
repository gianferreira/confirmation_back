package br.pucpr.authserver.users.responses

import br.pucpr.authserver.users.User

data class UserResponse(
    val id: Long,
    val name: String,
    val nick: String,
    val phone: String,
    val device: String,
    val pending: Boolean,
    val avatar: String
) {
    constructor(u: User, avatarUrl: String): this(
        id=u.id!!,
        name=u.name,
        nick=u.nick,
        phone=u.phone,
        device=u.device,
        pending=u.pending,
        avatar= avatarUrl,
    )
}
