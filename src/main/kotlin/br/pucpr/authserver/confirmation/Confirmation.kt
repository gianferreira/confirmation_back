package br.pucpr.authserver.confirmation

import jakarta.persistence.*
import org.jetbrains.annotations.NotNull

@Entity
@Table(name="tblConfirmation")
class Confirmation(
    @Id @GeneratedValue
    var id: Long? = null,
    @NotNull
    var phone: String = "",
    @NotNull
    var device: String = "",
    @Column(unique = true, nullable = false)
    var code: String = "",
)
