package br.pucpr.authserver.users

import br.pucpr.authserver.errors.UnsupportedMediaTypeException
import br.pucpr.authserver.files.FileStorage
import br.pucpr.authserver.files.S3Storage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class AvatarService(@Qualifier("fileStorage") val storage: FileStorage) {
    fun save(user: User, avatar: MultipartFile): String =
        try {
            val extension = when (avatar.contentType) {
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                "image/jpg" -> "jpg"
                else -> throw UnsupportedMediaTypeException()
            }

            val path = "${user.id}/a_${user.id}.${extension}"
            storage.save(user, "$ROOT/$path", avatar)
            path
        } catch (exception: Error) {
            log.error("Unable to store user ${user.id} avatar. Using default")
            DEFAULT_AVATAR
        }

    fun urlFor(name: String): String = storage.urlFor("$ROOT/$name")

    fun load(path: String): Resource? = storage.load(path)

    companion object {
        const val ROOT = "avatars"
        const val DEFAULT_AVATAR = "default.png"
        private val log = LoggerFactory.getLogger(AvatarService::class.java)
    }
}