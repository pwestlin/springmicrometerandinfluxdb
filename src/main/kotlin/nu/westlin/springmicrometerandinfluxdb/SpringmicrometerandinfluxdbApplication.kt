package nu.westlin.springmicrometerandinfluxdb

import io.micrometer.core.annotation.Timed
import io.micrometer.core.aop.TimedAspect
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Tags
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.InetAddress
import java.net.UnknownHostException


@SpringBootApplication
class SpringmicrometerandinfluxdbApplication

fun main(args: Array<String>) {
    runApplication<SpringmicrometerandinfluxdbApplication>(*args)
}

@RestController
@RequestMapping("/")
class JobsController {

    @GetMapping("/{name}")
    @Timed(value = "timed")
    suspend fun getEmployeeById(@PathVariable name: String): Int {
        val timer = Metrics.timer("request.timer", "name", name)
        return timer.recordCallable {
            Thread.sleep(100)
            val counter: Counter = Metrics.counter("request.names", "name", name)
            println("counter.count() = ${counter.count()}")
            counter.increment()
            counter.count().toInt()
        }
    }

}

@Configuration
@ConditionalOnProperty(prefix = "management.metrics.export.influx", name = ["enabled"], matchIfMissing = true)
class MetricsConfiguration {

    @Bean
    fun timedAspect(registry: MeterRegistry): TimedAspect {
        return TimedAspect(registry) { pjp ->
            Tags.of("class", pjp.staticPart.signature.declaringTypeName,
                "method", pjp.staticPart.signature.name)
        }
    }

    @Bean
    fun threadMetrics(): JvmThreadMetrics {
        return JvmThreadMetrics()
    }

    @Bean
    fun meterRegistryCustomizer(
        @Value("\${spring.application.name}") applicationName: String,
        @Autowired environment: Environment
    ): MeterRegistryCustomizer<MeterRegistry> {
        return MeterRegistryCustomizer { registry ->
            registry.config().commonTags(listOf(
                Tag.of("hostname", getHostName()),
                Tag.of("application", applicationName),
                Tag.of("profiles", environment.activeProfiles.joinToString(separator = ", "))
            ))
        }
    }

    protected fun getHostName(): String {
        try {
            return InetAddress.getLocalHost().hostName.replace(".lmv.lm.se".toRegex(), "")
        } catch (e: UnknownHostException) {
            throw RuntimeException(e.message, e)
        }
    }
}
