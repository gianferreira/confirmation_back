package br.pucpr.authserver.users

import br.pucpr.authserver.confirmation.requests.ConfirmationRequest
import br.pucpr.authserver.users.requests.LoginRequest
import br.pucpr.authserver.users.requests.UserRequest
import br.pucpr.authserver.users.responses.UserResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.net.URI

@RestController
@RequestMapping("/users")
class UserController(
    val userService: UserService
) {
    @GetMapping
    fun findAll(
        @RequestParam sortDir: String? = null,
        @RequestParam role: String? = null
    ) =
        SortDir.entries.firstOrNull { it.name == (sortDir ?: "ASC").uppercase() }
            ?.let { userService.findAll(it, role) }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.badRequest().build()

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long) =
        userService.findByIdOrNull(id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name="WebToken")
    fun deleteById(@PathVariable id: Long): ResponseEntity<Void> =
        userService.delete(id)
            ?.let { ResponseEntity.ok().build() }
            ?: ResponseEntity.notFound().build()

    @PutMapping("/{id}/roles/{role}")
    fun grant(
        @PathVariable id: Long,
        @PathVariable role: String
    ): ResponseEntity<Void> =
        if (userService.addRole(id, role)) ResponseEntity.ok().build()
        else ResponseEntity.noContent().build()

    @SecurityRequirement(name="WebToken")
    @PreAuthorize("permitAll()")
    @PutMapping("/{id}/avatar", consumes = ["multipart/form-data"])
    fun uploadAvatar(
        @PathVariable id: Long,
        @RequestParam avatar: MultipartFile
    ) = userService.saveAvatar(id, avatar).let {
        ResponseEntity.created(URI(it)).build<Void>()
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody login: LoginRequest) =
        userService.login(login.phone!!, login.device!!)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.status(HttpStatus.ACCEPTED).build()

    @PostMapping("/confirm")
    fun confirm(@Valid @RequestBody confirmation: ConfirmationRequest) =
        userService.confirm(confirmation.phone!!, confirmation.device!!, confirmation.code!!)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.status(HttpStatus.NOT_FOUND).build()

    @PutMapping("/{id}")
    @SecurityRequirement(name="WebToken")
    fun update(
        @PathVariable id: Long,
        @RequestBody @Valid user: UserRequest,
    ) = userService.update(id, user.toUser())
        ?.let { ResponseEntity.ok(it) }
        ?: ResponseEntity.status(HttpStatus.NOT_FOUND).build()
}