import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class TelegramBotExceptionHandler {

    private val logger = LoggerFactory.getLogger(TelegramBotExceptionHandler::class.java)

    @Pointcut("execution(* io.github.dehuckakpyt.telegrambot..*(..))")
    fun telegramBotMethods() {
    }

    @Around("telegramBotMethods()")
    fun handleTelegramBotExceptions(joinPoint: ProceedingJoinPoint): Any? {
        return try {
            joinPoint.proceed()
        } catch (e: Exception) {
            logger.error("Exception occurred in method: ${joinPoint.signature.name}", e)
            if (e is RuntimeException) {
                logger.error("Runtime exception handled: ${e.message}")
            }
            return null
        }
    }
}
