package br.pucpr.authserver.roles.responses

import br.pucpr.authserver.roles.Role

class RoleResponse(
    val name: String,
    val description: String
) {
    constructor(role: Role): this(name = role.name, description = role.description)
}
