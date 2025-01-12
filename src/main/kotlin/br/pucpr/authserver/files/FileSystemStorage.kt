package br.pucpr.authserver.files

import br.pucpr.authserver.users.User
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

@Component
class FileSystemStorage: FileStorage {
    override fun save(user: User, path: String, file: MultipartFile) {
        val root = Paths.get(ROOT)
        val destinationFile = root.resolve(path)
            .normalize()
            .toAbsolutePath()

        Files.createDirectories(destinationFile.parent)
        file.inputStream.use {
            Files.copy(it, destinationFile,
                StandardCopyOption.REPLACE_EXISTING)
        }
    }

    override fun load(path: String): Resource? =
        Paths.get(ROOT, path.replace("--", "/"))
            .takeIf { Files.isRegularFile(it) }
            ?.let { UrlResource(it.toUri()) }

    override fun urlFor(name: String): String =
        "http://localhost:8080/api/files/" +
                URLEncoder.encode(
                    name.replace("/", "--"),
                    StandardCharsets.UTF_8
                )

    companion object {
        const val ROOT = "./fs"
    }
}