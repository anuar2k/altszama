package altszama.validation

import altszama.order.OrderRepository
import altszama.order.OrderState
import org.springframework.beans.factory.annotation.Autowired
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass


@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = arrayOf(OrderNotOrderedYetValidator::class))
annotation class OrderNotOrderedYet(
    val message: String = "Order is already ordered.",
    val groups: Array<KClass<Any>> = arrayOf(),
    val payload: Array<KClass<out Payload>> = arrayOf()
)


class OrderNotOrderedYetValidator : ConstraintValidator<OrderNotOrderedYet, String?> {

  @Autowired
  private lateinit var orderRepository: OrderRepository

  override fun initialize(constraintAnnotation: OrderNotOrderedYet) {}

  override fun isValid(orderId: String?, context: ConstraintValidatorContext): Boolean {
    val order = orderRepository.findOne(orderId)

    return order.orderState == OrderState.CREATED
  }
}