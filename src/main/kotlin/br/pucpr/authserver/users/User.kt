package br.pucpr.authserver.users

import br.pucpr.authserver.roles.Role
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.jetbrains.annotations.NotNull

@Entity
@Table(name="tblUser")
class User(
    @Id @GeneratedValue
    var id: Long? = null,
    @Column(unique = true, nullable = false)
    var phone: String = "",
    @NotNull
    var device: String = "",
    @NotNull
    var name: String = "",
    @NotNull
    var nick: String = "",
    @NotNull
    var pending: Boolean = true,
    @NotNull
    var avatar: String = AvatarService.DEFAULT_AVATAR,

    @ManyToMany
    @JoinTable(
        name="UserRole",
        joinColumns = [JoinColumn(name = "idUser")],
        inverseJoinColumns = [JoinColumn(name = "idRole")]
    )
    @JsonIgnore
    val roles: MutableSet<Role> = mutableSetOf()
)
