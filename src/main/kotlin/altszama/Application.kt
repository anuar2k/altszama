package altszama;

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.data.web.config.EnableSpringDataWebSupport
import java.util.*

@SpringBootApplication
@EnableSpringDataWebSupport
open class Application {

  @EventListener(ApplicationReadyEvent::class)
  fun doSomethingAfterStartup() {
    Locale.setDefault(Locale.forLanguageTag("pl_PL"))
  }

}

fun main(args: Array<String>) {
  SpringApplication.run(Application::class.java, *args)
}
