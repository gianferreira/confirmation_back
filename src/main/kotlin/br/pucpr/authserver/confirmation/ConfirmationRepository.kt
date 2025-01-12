package br.pucpr.authserver.confirmation

import org.springframework.data.jpa.repository.JpaRepository

interface ConfirmationRepository: JpaRepository<Confirmation, Long> {
    fun findByCode(code: String): Confirmation?
}
