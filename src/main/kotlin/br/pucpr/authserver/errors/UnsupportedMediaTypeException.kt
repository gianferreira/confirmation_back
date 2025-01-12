package br.pucpr.authserver.errors
import org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(UNSUPPORTED_MEDIA_TYPE)
class UnsupportedMediaTypeException(
    message: String = "Unsupported Media Type",
    cause: Throwable? = null,
): IllegalArgumentException(message, cause)
{
    constructor(vararg types: String, cause: Throwable?): this(
        "Unsupported Media Type. Supported types: {${types.joinToString()}}", cause
    )
}
