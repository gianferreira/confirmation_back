package br.pucpr.authserver.users

import br.pucpr.authserver.confirmation.Confirmation
import br.pucpr.authserver.confirmation.ConfirmationRepository
import br.pucpr.authserver.errors.NotFoundException
import br.pucpr.authserver.sms.SmsClient
import br.pucpr.authserver.roles.RoleRepository
import br.pucpr.authserver.security.Jwt
import br.pucpr.authserver.users.responses.LoginResponse
import br.pucpr.authserver.users.responses.UserResponse
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import kotlin.random.Random

@Service
class UserService(
    val userRepository: UserRepository,
    val roleRepository: RoleRepository,
    val confirmationRepository: ConfirmationRepository,
    val avatarService: AvatarService,
    val smsClient: SmsClient,
    val jwt: Jwt
) {
    fun findAll(dir: SortDir, role: String?) =
        (role?.let { r ->
            when (dir) {
                SortDir.ASC -> userRepository.findByRole(r.uppercase()).sortedBy { it.name }
                SortDir.DESC -> userRepository.findByRole(r.uppercase()).sortedByDescending { it.name }
            }
        } ?: when (dir) {
            SortDir.ASC -> userRepository.findAll(Sort.by("name").ascending())
            SortDir.DESC -> userRepository.findAll(Sort.by("name").descending())
        }).map { it.toResponse() }

    fun findByIdOrNull(id: Long) = userRepository.findByIdOrNull(id)?.toResponse()

    fun delete(id: Long) = userRepository.findByIdOrNull(id)
            .also { userRepository.deleteById(id) }

    fun addRole(id: Long, roleName: String): Boolean {
        val user = userRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("User $id not found!")

        if (user.roles.any { it.name == roleName }) return false

        val role = roleRepository.findByName(roleName)
            ?: throw IllegalArgumentException("Invalid role $roleName!")

        user.roles.add(role)
        userRepository.save(user)
        return true
    }

    fun saveAvatar(id: Long, avatar: MultipartFile): String {
        val user = userRepository
            .findByIdOrNull(id) ?: throw NotFoundException()

        user.avatar = avatarService.save(user, avatar)
        userRepository.save(user)

        return avatarService.urlFor(user.avatar)
    }

    fun login(phone: String, device: String): LoginResponse? {
        val user = userRepository.findByPhone(phone).firstOrNull()

        if (user == null || device != user.device) {
            val code = Random.nextInt(1000, 9999)
            smsClient.sendSms(phone, "Código de verificação: $code", true)

            confirmationRepository.save(
                Confirmation(
                    phone = phone,
                    device = device,
                    code = code.toString(),
                ),
            )

            log.info("Código de verificação criado")
            return null
        } else {
            log.info("Usuário logado: id={}, phone={}", user.id, user.phone)
            return LoginResponse(
                token = jwt.createToken(user),
                user.toResponse()
            )
        }
    }

    fun confirm(phone: String, device: String, code: String): LoginResponse? {
        val confirmation = confirmationRepository.findByCode(code)

        if (confirmation != null
            && confirmation.device == device
            && confirmation.phone == phone
        ) {
            var user = userRepository.findByPhone(phone).firstOrNull()

            if(user == null) {
                user = userRepository.save(User(phone = phone, device = device))
                log.info("Usuário criado: id={}, phone={}", user.id, user.phone)
            } else {
                user.device = device
                userRepository.save(user)
                log.info("Device do usuário atualizado: id={}, phone={}", user.id, user.phone)
            }

            return LoginResponse(
                token = jwt.createToken(user),
                user.toResponse()
            )
        } else {
            log.info("Não há código $code enviado para o telefone $phone")
            return null
        }
    }

    fun update(id: Long, updatedUser: User): UserResponse? {
        val user = userRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("Usuário $id não encontrado!")

        user.name = updatedUser.name
        user.nick = updatedUser.nick
        user.pending = false

        return userRepository.save(user)
            .also { log.info("Cadastro do usuário finalizado: $user") }
            .let { UserResponse(it, avatarService.urlFor(it.avatar)) }
    }

    fun User.toResponse() =
        UserResponse(this, avatarService.urlFor(this.avatar))

    companion object {
        val log = LoggerFactory.getLogger(UserService::class.java)
    }
}
