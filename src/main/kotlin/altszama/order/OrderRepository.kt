package altszama.order

import org.springframework.data.mongodb.repository.MongoRepository
import java.time.LocalDate

interface OrderRepository : MongoRepository<Order, String> {
  fun findByOrderDate(orderDate: LocalDate): List<Order>

  fun findByRestaurantId(restaurantId: String): List<Order>
}
