package br.pucpr.authserver.sms

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.sns.AmazonSNSAsync
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder
import com.amazonaws.services.sns.model.PublishRequest
import com.amazonaws.services.sns.model.SetSMSAttributesRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SmsClient {
    private val sns: AmazonSNSAsync = AmazonSNSAsyncClientBuilder.standard()
        .withRegion(Regions.US_EAST_1)
        .withCredentials(EnvironmentVariableCredentialsProvider())
        .build()

    fun sendSms(phone: String, text: String, important: Boolean = false) {
        try {
            val type = if (important) "Transactional" else "Promotional"
            val attributes = SetSMSAttributesRequest().apply {
                attributes = mapOf("DefaultSMSType" to type)
            }
            sns.setSMSAttributes(attributes)

            sns.publishAsync(
                PublishRequest().apply {
                    phoneNumber = phone
                    message = text
                }
            )
            log.info("Mensagem enviada para o telefone '$phone': $text")
        } catch (error: Exception) {
            log.error("Erro ao tentar enviar a mensagem para o telefone $phone", error)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SmsClient::class.java)
    }
}