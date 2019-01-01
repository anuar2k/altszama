package altszama.order.dto

import altszama.auth.User
import altszama.dish.Dish
import altszama.dish.SideDish
import altszama.dish.dto.DishDto
import altszama.order.Order
import altszama.order.OrderState
import altszama.orderEntry.DishEntry
import altszama.orderEntry.OrderEntry
import altszama.orderEntry.OrderEntryPaymentStatus
import altszama.restaurant.Restaurant
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.format.annotation.DateTimeFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import javax.validation.constraints.NotNull

data class ShowResponse(
    val order: OrderDto,
    val orderEntries: List<ParticipantsOrderEntry>,
    val currentUserId: String,
    val allDishesInRestaurant: List<DishDto>,
    val allDishesByCategory: Map<String, List<DishDto>>,
    val dishIdToSideDishesMap: Map<String, List<SideDish>>,
    val totalOrderPrice: Int
) {

  companion object {

    data class OrderDto(
        val id: String,
        val restaurantId: String,
        val restaurantName: String,
        val restaurantUrl: String,
        val orderCreatorId: String,
        val orderCreatorUsername: String,
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        val orderDate: LocalDate,
        val timeOfOrder: LocalTime?,
        val timeOfDelivery: LocalTime?,
        val orderState: OrderState,
        val decreaseInPercent: Int,
        val deliveryCostPerEverybody: Int,
        val deliveryCostPerDish: Int,
        val paymentByCash: Boolean,
        val paymentByBankTransfer: Boolean,
        val bankTransferNumber: String
    )

    data class ParticipantsOrderEntry(
        val id: String,
        val userId: String,
        val username: String,
        val dishEntries: List<ParticipantsDishEntry>,
        val finalPrice: Int,
        val paymentStatus: OrderEntryPaymentStatus
    )

    data class ParticipantsDishEntry(
        val id: String,
        val dish: DishDto,
        val sideDishes: List<SideDish>,
        val price: Int,
        val comments: String
    )

    fun create(order: Order,
               entries: List<OrderEntry>,
               currentUserId: String,
               allDishesInRestaurant: List<Dish>,
               dishIdToSideDishesMap: Map<String, List<SideDish>>): ShowResponse {
      val orderEntriesByUser: Map<User, List<OrderEntry>> = entries.groupBy { e -> e.user }

      val usersCount = orderEntriesByUser.keys.size

      val participantsUserEntries = orderEntriesByUser
          .flatMap { userToEntries -> userToEntries.value }
          .map { orderEntry -> createParticipantOrderEntry(orderEntry, order, usersCount) }

      val allDishesInRestaurantAsDtos = allDishesInRestaurant.map { dish -> DishDto.fromDish(dish) }

      val allDishesInRestaurantByCategory = allDishesInRestaurantAsDtos
          .groupBy { dish -> dish.category }
          .map { x -> x.key to x.value.sortedBy { dish -> dish.name }}
          .toMap()

      val totalOrderPrice = Order.getTotalPrice(order, entries)

      return ShowResponse(
          fromOrder(order),
          participantsUserEntries,
          currentUserId,
          allDishesInRestaurantAsDtos,
          allDishesInRestaurantByCategory,
          dishIdToSideDishesMap,
          totalOrderPrice
      )
    }

    private fun createParticipantOrderEntry(orderEntry: OrderEntry, order: Order, usersCount: Int): ParticipantsOrderEntry {
      val basePrice = orderEntry.dishEntries.sumBy { dishEntry -> dishEntry.priceWithSidedishes() }

      val decreaseAmount = (basePrice * (order.decreaseInPercent / 100.0)).toInt()
      val deliveryCostPerOrder = (order.deliveryCostPerEverybody / usersCount)
      val deliveryCostPerEntry = order.deliveryCostPerDish * orderEntry.dishEntries.size

      val finalPrice = basePrice - decreaseAmount + deliveryCostPerOrder + deliveryCostPerEntry

      val dishEntries: List<ParticipantsDishEntry> = orderEntry.dishEntries
          .map(this::createParticipantsDishEntry)

      return createParticipantsOrderEntry(orderEntry, dishEntries, finalPrice)
    }

    private fun createParticipantsOrderEntry(orderEntry: OrderEntry, dishEntries: List<ParticipantsDishEntry>, finalPrice: Int): ParticipantsOrderEntry {
      return ParticipantsOrderEntry(
          id = orderEntry.id,
          userId = orderEntry.user.id,
          username = orderEntry.user.username,
          dishEntries = dishEntries,
          finalPrice = finalPrice,
          paymentStatus = orderEntry.paymentStatus
      )
    }

    private fun createParticipantsDishEntry(dishEntry: DishEntry): ParticipantsDishEntry {
      return ParticipantsDishEntry(
          dishEntry.id,
          DishDto.fromDish(dishEntry.dish),
          dishEntry.chosenSideDishes,
          dishEntry.dish.price,
          dishEntry.additionalComments
      )
    }

    private fun fromOrder(order: Order): OrderDto {
      return OrderDto(
          order.id,
          order.restaurant.id,
          order.restaurant.name,
          order.restaurant.url,
          order.orderCreator.id,
          order.orderCreator.username,
          order.orderDate,
          order.timeOfOrder,
          order.timeOfDelivery,
          order.orderState,
          order.decreaseInPercent,
          order.deliveryCostPerEverybody,
          order.deliveryCostPerDish,
          order.paymentByCash,
          order.paymentByBankTransfer,
          order.bankTransferNumber
      )
    }
  }

}