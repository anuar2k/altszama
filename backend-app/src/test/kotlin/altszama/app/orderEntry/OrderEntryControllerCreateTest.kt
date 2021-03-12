package altszama.app.orderEntry

import altszama.app.dish.DishRepository
import altszama.app.dish.DishService
import altszama.app.dish.SideDish
import altszama.app.dish.dto.DishCreateRequest
import altszama.app.order.OrderService
import altszama.app.order.dto.DeliveryData
import altszama.app.order.dto.OrderSaveRequest
import altszama.app.order.dto.PaymentData
import altszama.app.orderEntry.dto.OrderEntrySaveRequest
import altszama.app.restaurant.RestaurantService
import altszama.app.restaurant.dto.RestaurantSaveRequest
import altszama.app.team.TeamService
import altszama.app.test.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.LocalDate
import java.time.LocalTime

class OrderEntryControllerCreateTest : AbstractIntegrationTest() {

  @Autowired
  private lateinit var teamService: TeamService

  @Autowired
  private lateinit var restaurantService: RestaurantService

  @Autowired
  private lateinit var dishService: DishService

  @Autowired
  private lateinit var orderService: OrderService

  @Autowired
  private lateinit var orderEntryService: OrderEntryService

  @Autowired
  private lateinit var orderEntryRepository: OrderEntryRepository

  @Autowired
  private lateinit var dishRepository: DishRepository

  @Autowired
  private lateinit var mockMvc: MockMvc

  @Test()
  fun itShouldAddOrderEntryWithExistingDishAndExistingSidedishSuccessfully() {
    val team1 = teamService.createTeam("team1.com", "team1.com")
    val (user1Token, user1) = createUserAndGetToken("James1", "james1@team1.com")

    val restaurant = restaurantService.createRestaurant(team1, RestaurantSaveRequest("Restaurant 1"))
    val sideDish1 = SideDish(name = "Side dish 1", price = 100)
    val sideDish2 = SideDish(name = "Side dish 2", price = 120)
    val sideDish3 = SideDish(name = "Side dish 3", price = 150)
    val dishCreateRequest = DishCreateRequest(
      "Dish 1",
      100,
      category = "Category 1",
      sideDishes = listOf(
        sideDish1,
        sideDish2,
        sideDish3
      )
    )
    val dish1 = dishService.saveDish(team1, restaurant.id, dishCreateRequest)

    val orderSaveRequest = OrderSaveRequest(restaurantId = restaurant.id, orderDate = LocalDate.now(), timeOfOrder = LocalTime.of(14, 0), deliveryData = DeliveryData(), paymentData = PaymentData())
    val order = orderService.saveOrder(orderSaveRequest, currentUser = user1, currentUserTeam = team1)

    val createContent = """{
        "orderId": "${order.id}",
        "dishId": "${dish1.id}",
        "additionalComments": "Some funny comment",
        "newDish": false,
        "newDishName": "",
        "newDishPrice": 0,
        "sideDishes": [
          {"isNew": false, "id": "${sideDish1.id}"}
        ]
    }""".trimIndent()

    val request = MockMvcRequestBuilders.post("/api/order_entries/save")
      .content(createContent)
      .contentType(MediaType.APPLICATION_JSON)
      .header("Authorization", user1Token)

    mockMvc.perform(request)
      .andExpect(MockMvcResultMatchers.status().isOk)

    val orderEntriesInDb = orderEntryRepository.findByOrderId(order.id)

    assertThat(orderEntriesInDb).hasSize(1)
    assertThat(orderEntriesInDb[0].order.id).isEqualTo(order.id)
    assertThat(orderEntriesInDb[0].dishEntries).hasSize(1)
    assertThat(orderEntriesInDb[0].dishEntries[0].dish.id).isEqualTo(dish1.id)
    assertThat(orderEntriesInDb[0].dishEntries[0].additionalComments).isEqualTo("Some funny comment")
    assertThat(orderEntriesInDb[0].dishEntries[0].chosenSideDishes).isEqualTo(listOf(sideDish1))
  }

  @Test()
  fun itShouldAddOrderEntryWithExistingDishAndExistingSidedishForUserFromTheSameTeam() {
    val team1 = teamService.createTeam("team1.com", "team1.com")
    val (user1Token, user1) = createUserAndGetToken("James1", "james1@team1.com")

    val restaurant = restaurantService.createRestaurant(team1, RestaurantSaveRequest("Restaurant 1"))
    val sideDish1 = SideDish(name = "Side dish 1", price = 100)
    val sideDish2 = SideDish(name = "Side dish 2", price = 120)
    val sideDish3 = SideDish(name = "Side dish 3", price = 150)
    val dishCreateRequest = DishCreateRequest(
      "Dish 1",
      100,
      category = "Category 1",
      sideDishes = listOf(
        sideDish1,
        sideDish2,
        sideDish3
      )
    )
    val dish1 = dishService.saveDish(team1, restaurant.id, dishCreateRequest)

    val orderSaveRequest = OrderSaveRequest(restaurantId = restaurant.id, orderDate = LocalDate.now(), timeOfOrder = LocalTime.of(14, 0), deliveryData = DeliveryData(), paymentData = PaymentData())
    val order = orderService.saveOrder(orderSaveRequest, currentUser = user1, currentUserTeam = team1)

    val (user2Token, user2) = createUserAndGetToken("James2", "james2@team1.com")

    val createContent = """{
        "orderId": "${order.id}",
        "dishId": "${dish1.id}",
        "additionalComments": "Some funny comment",
        "newDish": false,
        "newDishName": "",
        "newDishPrice": 0,
        "sideDishes": [
          {"isNew": false, "id": "${sideDish1.id}"}
        ]
    }""".trimIndent()

    val request = MockMvcRequestBuilders.post("/api/order_entries/save")
      .content(createContent)
      .contentType(MediaType.APPLICATION_JSON)
      .header("Authorization", user2Token)

    mockMvc.perform(request)
      .andExpect(MockMvcResultMatchers.status().isOk)

    val orderEntriesInDb = orderEntryRepository.findByOrderId(order.id)

    assertThat(orderEntriesInDb).hasSize(1)
    assertThat(orderEntriesInDb[0].order.id).isEqualTo(order.id)
    assertThat(orderEntriesInDb[0].dishEntries).hasSize(1)
    assertThat(orderEntriesInDb[0].dishEntries[0].dish.id).isEqualTo(dish1.id)
    assertThat(orderEntriesInDb[0].dishEntries[0].additionalComments).isEqualTo("Some funny comment")
    assertThat(orderEntriesInDb[0].dishEntries[0].chosenSideDishes).isEqualTo(listOf(sideDish1))
  }

  @Test()
  fun itShouldAddOrderEntryWithExistingDishAndNewSideDishSuccessfully() {
    val team1 = teamService.createTeam("team1.com", "team1.com")
    val (user1Token, user1) = createUserAndGetToken("James1", "james1@team1.com")

    val restaurant = restaurantService.createRestaurant(team1, RestaurantSaveRequest("Restaurant 1"))
    val sideDish1 = SideDish(name = "Side dish 1", price = 100)
    val sideDish2 = SideDish(name = "Side dish 2", price = 120)
    val sideDish3 = SideDish(name = "Side dish 3", price = 150)
    val dishCreateRequest = DishCreateRequest(
      "Dish 1",
      100,
      category = "Category 1",
      sideDishes = listOf(
        sideDish1,
        sideDish2,
        sideDish3
      )
    )
    val dish1 = dishService.saveDish(team1, restaurant.id, dishCreateRequest)

    val orderSaveRequest = OrderSaveRequest(restaurantId = restaurant.id, orderDate = LocalDate.now(), timeOfOrder = LocalTime.of(14, 0), deliveryData = DeliveryData(), paymentData = PaymentData())
    val order = orderService.saveOrder(orderSaveRequest, currentUser = user1, currentUserTeam = team1)

    val newSideDishName = "Side dish 4"
    val newSideDishPrice = 200

    val createContent = """{
        "orderId": "${order.id}",
        "dishId": "${dish1.id}",
        "additionalComments": "Some funny comment",
        "newDish": false,
        "newDishName": "",
        "newDishPrice": 0,
        "sideDishes": [
          {"isNew": true, "newSideDishName": "${newSideDishName}", "newSideDishPrice": "${newSideDishPrice}"}
        ]
    }""".trimIndent()

    val request = MockMvcRequestBuilders.post("/api/order_entries/save")
      .content(createContent)
      .contentType(MediaType.APPLICATION_JSON)
      .header("Authorization", user1Token)

    mockMvc.perform(request)
      .andExpect(MockMvcResultMatchers.status().isOk)

    val orderEntriesInDb = orderEntryRepository.findByOrderId(order.id)

    assertThat(orderEntriesInDb).hasSize(1)
    assertThat(orderEntriesInDb[0].order.id).isEqualTo(order.id)
    assertThat(orderEntriesInDb[0].dishEntries).hasSize(1)
    assertThat(orderEntriesInDb[0].dishEntries[0].dish.id).isEqualTo(dish1.id)
    assertThat(orderEntriesInDb[0].dishEntries[0].additionalComments).isEqualTo("Some funny comment")
    assertThat(orderEntriesInDb[0].dishEntries[0].chosenSideDishes).hasSize(1)
    assertThat(orderEntriesInDb[0].dishEntries[0].chosenSideDishes[0].name).isEqualTo(newSideDishName)
    assertThat(orderEntriesInDb[0].dishEntries[0].chosenSideDishes[0].price).isEqualTo(newSideDishPrice)

    val orderedDishFromDb = dishRepository.findById(orderEntriesInDb[0].dishEntries[0].dish.id).get()
    assertThat(orderedDishFromDb.sideDishes).hasSize(4)
    assertThat(orderedDishFromDb.sideDishes.find { sd -> sd.name == newSideDishName }!!.price).isEqualTo(newSideDishPrice)
  }

  @Test()
  fun itShouldAddOrderEntryWithNewDishAndNewSideDishSuccessfully() {
    val team1 = teamService.createTeam("team1.com", "team1.com")
    val (user1Token, user1) = createUserAndGetToken("James1", "james1@team1.com")

    val restaurant = restaurantService.createRestaurant(team1, RestaurantSaveRequest("Restaurant 1"))
    val sideDish1 = SideDish(name = "Side dish 1", price = 100)
    val sideDish2 = SideDish(name = "Side dish 2", price = 120)
    val sideDish3 = SideDish(name = "Side dish 3", price = 150)
    val dishCreateRequest = DishCreateRequest(
      "Dish 1",
      100,
      category = "Category 1",
      sideDishes = listOf(
        sideDish1,
        sideDish2,
        sideDish3
      )
    )
    val dish1 = dishService.saveDish(team1, restaurant.id, dishCreateRequest)

    val orderSaveRequest = OrderSaveRequest(restaurantId = restaurant.id, orderDate = LocalDate.now(), timeOfOrder = LocalTime.of(14, 0), deliveryData = DeliveryData(), paymentData = PaymentData())
    val order = orderService.saveOrder(orderSaveRequest, currentUser = user1, currentUserTeam = team1)

    val newDishName = "New dish"
    val newDishPrice = 2400

    val newSideDishName = "Side dish 4"
    val newSideDishPrice = 200

    val createContent = """{
        "orderId": "${order.id}",
        "dishId": "${dish1.id}",
        "additionalComments": "Some funny comment",
        "newDish": true,
        "newDishName": "${newDishName}",
        "newDishPrice": "${newDishPrice}",
        "sideDishes": [
          {"isNew": true, "newSideDishName": "${newSideDishName}", "newSideDishPrice": "${newSideDishPrice}"}
        ]
    }""".trimIndent()

    val request = MockMvcRequestBuilders.post("/api/order_entries/save")
      .content(createContent)
      .contentType(MediaType.APPLICATION_JSON)
      .header("Authorization", user1Token)

    mockMvc.perform(request)
      .andExpect(MockMvcResultMatchers.status().isOk)

    val orderEntriesInDb = orderEntryRepository.findByOrderId(order.id)

    assertThat(orderEntriesInDb).hasSize(1)
    assertThat(orderEntriesInDb[0].order.id).isEqualTo(order.id)
    assertThat(orderEntriesInDb[0].dishEntries).hasSize(1)
    assertThat(orderEntriesInDb[0].dishEntries[0].dish.name).isEqualTo(newDishName)
    assertThat(orderEntriesInDb[0].dishEntries[0].dish.price).isEqualTo(newDishPrice)
    assertThat(orderEntriesInDb[0].dishEntries[0].additionalComments).isEqualTo("Some funny comment")
    assertThat(orderEntriesInDb[0].dishEntries[0].chosenSideDishes).hasSize(1)
    assertThat(orderEntriesInDb[0].dishEntries[0].chosenSideDishes[0].name).isEqualTo(newSideDishName)
    assertThat(orderEntriesInDb[0].dishEntries[0].chosenSideDishes[0].price).isEqualTo(newSideDishPrice)

    val orderedDishFromDb = dishRepository.findById(orderEntriesInDb[0].dishEntries[0].dish.id).get()
    assertThat(orderedDishFromDb.name).isEqualTo(newDishName)
    assertThat(orderedDishFromDb.price).isEqualTo(newDishPrice)
    assertThat(orderedDishFromDb.sideDishes).hasSize(1)
    assertThat(orderedDishFromDb.sideDishes[0].name).isEqualTo(newSideDishName)
    assertThat(orderedDishFromDb.sideDishes[0].price).isEqualTo(newSideDishPrice)
  }

  @Test()
  fun itShouldAddOrderEntryIfOrderIsAlreadyOrderedButTheUserIsOrderCreator() {
    val team1 = teamService.createTeam("team1.com", "team1.com")
    val (user1Token, user1) = createUserAndGetToken("James1", "james1@team1.com")

    val restaurant = restaurantService.createRestaurant(team1, RestaurantSaveRequest("Restaurant 1"))
    val sideDish1 = SideDish(name = "Side dish 1", price = 100)
    val sideDish2 = SideDish(name = "Side dish 2", price = 120)
    val sideDish3 = SideDish(name = "Side dish 3", price = 150)
    val dishCreateRequest = DishCreateRequest(
      "Dish 1",
      100,
      category = "Category 1",
      sideDishes = listOf(
        sideDish1,
        sideDish2,
        sideDish3
      )
    )
    val dish1 = dishService.saveDish(team1, restaurant.id, dishCreateRequest)

    val orderSaveRequest = OrderSaveRequest(restaurantId = restaurant.id, orderDate = LocalDate.now(), timeOfOrder = LocalTime.of(14, 0), deliveryData = DeliveryData(), paymentData = PaymentData())
    val order = orderService.saveOrder(orderSaveRequest, currentUser = user1, currentUserTeam = team1)

    val orderEntrySaveRequest = OrderEntrySaveRequest(orderId = order.id, dishId = dish1.id, newDish = false, newDishName = null, newDishPrice = null)
    val orderEntry = orderEntryService.saveEntry(user1, orderEntrySaveRequest)

    orderService.setAsOrdered(order.id, null, currentUser = user1)

    val createContent = """{
        "orderId": "${order.id}",
        "dishId": "${dish1.id}",
        "additionalComments": "Some funny comment",
        "newDish": false,
        "newDishName": "",
        "newDishPrice": 0,
        "sideDishes": [
          {"isNew": false, "id": "${sideDish1.id}"}
        ]
    }""".trimIndent()

    val request = MockMvcRequestBuilders.post("/api/order_entries/save")
      .content(createContent)
      .contentType(MediaType.APPLICATION_JSON)
      .header("Authorization", user1Token)

    mockMvc.perform(request)
      .andExpect(MockMvcResultMatchers.status().isOk)

    val orderEntriesInDb = orderEntryRepository.findByOrderId(order.id)

    assertThat(orderEntriesInDb).hasSize(1)
    assertThat(orderEntriesInDb[0].order.id).isEqualTo(order.id)
    assertThat(orderEntriesInDb[0].dishEntries).hasSize(1)
    assertThat(orderEntriesInDb[0].dishEntries[0].dish.id).isEqualTo(dish1.id)
    assertThat(orderEntriesInDb[0].dishEntries[0].additionalComments).isEqualTo("Some funny comment")
    assertThat(orderEntriesInDb[0].dishEntries[0].chosenSideDishes).isEqualTo(listOf(sideDish1))
  }

  @Test()
  fun itShouldNotAddOrderEntryIfOrderIsAlreadyOrdered() {
    val team1 = teamService.createTeam("team1.com", "team1.com")
    val (user1Token, user1) = createUserAndGetToken("James1", "james1@team1.com")

    val restaurant = restaurantService.createRestaurant(team1, RestaurantSaveRequest("Restaurant 1"))
    val sideDish1 = SideDish(name = "Side dish 1", price = 100)
    val sideDish2 = SideDish(name = "Side dish 2", price = 120)
    val sideDish3 = SideDish(name = "Side dish 3", price = 150)
    val dishCreateRequest = DishCreateRequest(
      "Dish 1",
      100,
      category = "Category 1",
      sideDishes = listOf(
        sideDish1,
        sideDish2,
        sideDish3
      )
    )
    val dish1 = dishService.saveDish(team1, restaurant.id, dishCreateRequest)

    val orderSaveRequest = OrderSaveRequest(restaurantId = restaurant.id, orderDate = LocalDate.now(), timeOfOrder = LocalTime.of(14, 0), deliveryData = DeliveryData(), paymentData = PaymentData())
    val order = orderService.saveOrder(orderSaveRequest, currentUser = user1, currentUserTeam = team1)

    val orderEntrySaveRequest = OrderEntrySaveRequest(orderId = order.id, dishId = dish1.id, newDish = false, newDishName = null, newDishPrice = null)
    val orderEntry = orderEntryService.saveEntry(user1, orderEntrySaveRequest)

    orderService.setAsOrdered(order.id, null, currentUser = user1)

    val (user2Token, user2) = createUserAndGetToken("James2", "james2@team1.com")

    val createContent = """{
        "orderId": "${order.id}",
        "dishId": "${dish1.id}",
        "additionalComments": "Some funny comment",
        "newDish": false,
        "newDishName": "",
        "newDishPrice": 0,
        "sideDishes": [
          {"isNew": false, "id": "${sideDish1.id}"}
        ]
    }""".trimIndent()

    val request = MockMvcRequestBuilders.post("/api/order_entries/save")
      .content(createContent)
      .contentType(MediaType.APPLICATION_JSON)
      .header("Authorization", user2Token)

    expectBadRequestWithMessage(request, "Order is already ordered")
  }

  @Test()
  fun itShouldAddOrderEntryIfOrderIsAlreadyDeliveredButTheUserIsOrderCreator() {
    val team1 = teamService.createTeam("team1.com", "team1.com")
    val (user1Token, user1) = createUserAndGetToken("James1", "james1@team1.com")

    val restaurant = restaurantService.createRestaurant(team1, RestaurantSaveRequest("Restaurant 1"))
    val sideDish1 = SideDish(name = "Side dish 1", price = 100)
    val sideDish2 = SideDish(name = "Side dish 2", price = 120)
    val sideDish3 = SideDish(name = "Side dish 3", price = 150)
    val dishCreateRequest = DishCreateRequest(
      "Dish 1",
      100,
      category = "Category 1",
      sideDishes = listOf(
        sideDish1,
        sideDish2,
        sideDish3
      )
    )
    val dish1 = dishService.saveDish(team1, restaurant.id, dishCreateRequest)

    val orderSaveRequest = OrderSaveRequest(restaurantId = restaurant.id, orderDate = LocalDate.now(), timeOfOrder = LocalTime.of(14, 0), deliveryData = DeliveryData(), paymentData = PaymentData())
    val order = orderService.saveOrder(orderSaveRequest, currentUser = user1, currentUserTeam = team1)

    val orderEntrySaveRequest = OrderEntrySaveRequest(orderId = order.id, dishId = dish1.id, newDish = false, newDishName = null, newDishPrice = null)
    val orderEntry = orderEntryService.saveEntry(user1, orderEntrySaveRequest)

    orderService.setAsOrdered(order.id, null, currentUser = user1)
    orderService.setAsDelivered(order.id, currentUser = user1)

    val createContent = """{
        "orderId": "${order.id}",
        "dishId": "${dish1.id}",
        "additionalComments": "Some funny comment",
        "newDish": false,
        "newDishName": "",
        "newDishPrice": 0,
        "sideDishes": [
          {"isNew": false, "id": "${sideDish1.id}"}
        ]
    }""".trimIndent()

    val request = MockMvcRequestBuilders.post("/api/order_entries/save")
      .content(createContent)
      .contentType(MediaType.APPLICATION_JSON)
      .header("Authorization", user1Token)

    mockMvc.perform(request)
      .andExpect(MockMvcResultMatchers.status().isOk)

    val orderEntriesInDb = orderEntryRepository.findByOrderId(order.id)

    assertThat(orderEntriesInDb).hasSize(1)
    assertThat(orderEntriesInDb[0].order.id).isEqualTo(order.id)
    assertThat(orderEntriesInDb[0].dishEntries).hasSize(1)
    assertThat(orderEntriesInDb[0].dishEntries[0].dish.id).isEqualTo(dish1.id)
    assertThat(orderEntriesInDb[0].dishEntries[0].additionalComments).isEqualTo("Some funny comment")
    assertThat(orderEntriesInDb[0].dishEntries[0].chosenSideDishes).isEqualTo(listOf(sideDish1))
  }

  @Test()
  fun itShouldNotAddOrderEntryIfOrderIsAlreadyDelivered() {
    val team1 = teamService.createTeam("team1.com", "team1.com")
    val (user1Token, user1) = createUserAndGetToken("James1", "james1@team1.com")

    val restaurant = restaurantService.createRestaurant(team1, RestaurantSaveRequest("Restaurant 1"))
    val sideDish1 = SideDish(name = "Side dish 1", price = 100)
    val sideDish2 = SideDish(name = "Side dish 2", price = 120)
    val sideDish3 = SideDish(name = "Side dish 3", price = 150)
    val dishCreateRequest = DishCreateRequest(
      "Dish 1",
      100,
      category = "Category 1",
      sideDishes = listOf(
        sideDish1,
        sideDish2,
        sideDish3
      )
    )
    val dish1 = dishService.saveDish(team1, restaurant.id, dishCreateRequest)

    val orderSaveRequest = OrderSaveRequest(restaurantId = restaurant.id, orderDate = LocalDate.now(), timeOfOrder = LocalTime.of(14, 0), deliveryData = DeliveryData(), paymentData = PaymentData())
    val order = orderService.saveOrder(orderSaveRequest, currentUser = user1, currentUserTeam = team1)

    val orderEntrySaveRequest = OrderEntrySaveRequest(orderId = order.id, dishId = dish1.id, newDish = false, newDishName = null, newDishPrice = null)
    val orderEntry = orderEntryService.saveEntry(user1, orderEntrySaveRequest)

    orderService.setAsOrdered(order.id, null, currentUser = user1)
    orderService.setAsDelivered(order.id, currentUser = user1)

    val (user2Token, user2) = createUserAndGetToken("James2", "james2@team1.com")

    val createContent = """{
        "orderId": "${order.id}",
        "dishId": "${dish1.id}",
        "additionalComments": "Some funny comment",
        "newDish": false,
        "newDishName": "",
        "newDishPrice": 0,
        "sideDishes": [
          {"isNew": false, "id": "${sideDish1.id}"}
        ]
    }""".trimIndent()

    val request = MockMvcRequestBuilders.post("/api/order_entries/save")
      .content(createContent)
      .contentType(MediaType.APPLICATION_JSON)
      .header("Authorization", user2Token)

    expectBadRequestWithMessage(request, "Order is already ordered")
  }

  @Test()
  fun itShouldNotAddOrderEntryIfOrderIsRejected() {
    val team1 = teamService.createTeam("team1.com", "team1.com")
    val (user1Token, user1) = createUserAndGetToken("James1", "james1@team1.com")

    val restaurant = restaurantService.createRestaurant(team1, RestaurantSaveRequest("Restaurant 1"))
    val sideDish1 = SideDish(name = "Side dish 1", price = 100)
    val sideDish2 = SideDish(name = "Side dish 2", price = 120)
    val sideDish3 = SideDish(name = "Side dish 3", price = 150)
    val dishCreateRequest = DishCreateRequest(
      "Dish 1",
      100,
      category = "Category 1",
      sideDishes = listOf(
        sideDish1,
        sideDish2,
        sideDish3
      )
    )
    val dish1 = dishService.saveDish(team1, restaurant.id, dishCreateRequest)

    val orderSaveRequest = OrderSaveRequest(restaurantId = restaurant.id, orderDate = LocalDate.now(), timeOfOrder = LocalTime.of(14, 0), deliveryData = DeliveryData(), paymentData = PaymentData())
    val order = orderService.saveOrder(orderSaveRequest, currentUser = user1, currentUserTeam = team1)

    val orderEntrySaveRequest = OrderEntrySaveRequest(orderId = order.id, dishId = dish1.id, newDish = false, newDishName = null, newDishPrice = null)
    val orderEntry = orderEntryService.saveEntry(user1, orderEntrySaveRequest)

    orderService.setAsRejected(order.id, currentUser = user1)

    val createContent = """{
        "orderId": "${order.id}",
        "dishId": "${dish1.id}",
        "additionalComments": "Some funny comment",
        "newDish": false,
        "newDishName": "",
        "newDishPrice": 0,
        "sideDishes": [
          {"isNew": false, "id": "${sideDish1.id}"}
        ]
    }""".trimIndent()

    val request = MockMvcRequestBuilders.post("/api/order_entries/save")
      .content(createContent)
      .contentType(MediaType.APPLICATION_JSON)
      .header("Authorization", user1Token)

    expectBadRequestWithMessage(request, "Order is already ordered")
  }

  @Test()
  fun itShouldNotAddOrderEntryIfExistingSideDishDoesNotExist() {
    val team1 = teamService.createTeam("team1.com", "team1.com")
    val (user1Token, user1) = createUserAndGetToken("James1", "james1@team1.com")

    val restaurant = restaurantService.createRestaurant(team1, RestaurantSaveRequest("Restaurant 1"))
    val sideDish1 = SideDish(name = "Side dish 1", price = 100)
    val sideDish2 = SideDish(name = "Side dish 2", price = 120)
    val sideDish3 = SideDish(name = "Side dish 3", price = 150)
    val dishCreateRequest = DishCreateRequest(
      "Dish 1",
      100,
      category = "Category 1",
      sideDishes = listOf(
        sideDish1,
        sideDish2,
        sideDish3
      )
    )
    val dish1 = dishService.saveDish(team1, restaurant.id, dishCreateRequest)

    val orderSaveRequest = OrderSaveRequest(restaurantId = restaurant.id, orderDate = LocalDate.now(), timeOfOrder = LocalTime.of(14, 0), deliveryData = DeliveryData(), paymentData = PaymentData())
    val order = orderService.saveOrder(orderSaveRequest, currentUser = user1, currentUserTeam = team1)

    val fakeSideDishId = "111111111111"

    val createContent = """{
        "orderId": "${order.id}",
        "dishId": "${dish1.id}",
        "additionalComments": "Some funny comment",
        "newDish": false,
        "newDishName": "",
        "newDishPrice": 0,
        "sideDishes": [
          {"isNew": false, "id": "${fakeSideDishId}"}
        ]
    }""".trimIndent()

    val request = MockMvcRequestBuilders.post("/api/order_entries/save")
      .content(createContent)
      .contentType(MediaType.APPLICATION_JSON)
      .header("Authorization", user1Token)

    expectBadRequestWithMessage(request, "Side dish does not exist")
  }

  @Test()
  fun itShouldNotAddOrderEntryIfNewSideDishPriceIsNegative() {
    val team1 = teamService.createTeam("team1.com", "team1.com")
    val (user1Token, user1) = createUserAndGetToken("James1", "james1@team1.com")

    val restaurant = restaurantService.createRestaurant(team1, RestaurantSaveRequest("Restaurant 1"))
    val sideDish1 = SideDish(name = "Side dish 1", price = 100)
    val sideDish2 = SideDish(name = "Side dish 2", price = 120)
    val sideDish3 = SideDish(name = "Side dish 3", price = 150)
    val dishCreateRequest = DishCreateRequest(
      "Dish 1",
      100,
      category = "Category 1",
      sideDishes = listOf(
        sideDish1,
        sideDish2,
        sideDish3
      )
    )
    val dish1 = dishService.saveDish(team1, restaurant.id, dishCreateRequest)

    val orderSaveRequest = OrderSaveRequest(restaurantId = restaurant.id, orderDate = LocalDate.now(), timeOfOrder = LocalTime.of(14, 0), deliveryData = DeliveryData(), paymentData = PaymentData())
    val order = orderService.saveOrder(orderSaveRequest, currentUser = user1, currentUserTeam = team1)

    val newSideDishName = "Side dish 4"
    val newSideDishPrice = -200

    val createContent = """{
        "orderId": "${order.id}",
        "dishId": "${dish1.id}",
        "additionalComments": "Some funny comment",
        "newDish": false,
        "newDishName": "",
        "newDishPrice": 0,
        "sideDishes": [
          {"isNew": true, "newSideDishName": "${newSideDishName}", "newSideDishPrice": "${newSideDishPrice}"}
        ]
    }""".trimIndent()

    val request = MockMvcRequestBuilders.post("/api/order_entries/save")
      .content(createContent)
      .contentType(MediaType.APPLICATION_JSON)
      .header("Authorization", user1Token)

    expectBadRequestWithMessage(request, "New side dish price cannot be negative")
  }

  @Test()
  fun itShouldNotAddOrderEntryIfNewSideDishNameIsEmpty() {
    val team1 = teamService.createTeam("team1.com", "team1.com")
    val (user1Token, user1) = createUserAndGetToken("James1", "james1@team1.com")

    val restaurant = restaurantService.createRestaurant(team1, RestaurantSaveRequest("Restaurant 1"))
    val sideDish1 = SideDish(name = "Side dish 1", price = 100)
    val sideDish2 = SideDish(name = "Side dish 2", price = 120)
    val sideDish3 = SideDish(name = "Side dish 3", price = 150)
    val dishCreateRequest = DishCreateRequest(
      "Dish 1",
      100,
      category = "Category 1",
      sideDishes = listOf(
        sideDish1,
        sideDish2,
        sideDish3
      )
    )
    val dish1 = dishService.saveDish(team1, restaurant.id, dishCreateRequest)

    val orderSaveRequest = OrderSaveRequest(restaurantId = restaurant.id, orderDate = LocalDate.now(), timeOfOrder = LocalTime.of(14, 0), deliveryData = DeliveryData(), paymentData = PaymentData())
    val order = orderService.saveOrder(orderSaveRequest, currentUser = user1, currentUserTeam = team1)

    val newSideDishName = ""
    val newSideDishPrice = 200

    val createContent = """{
        "orderId": "${order.id}",
        "dishId": "${dish1.id}",
        "additionalComments": "Some funny comment",
        "newDish": false,
        "newDishName": "",
        "newDishPrice": 0,
        "sideDishes": [
          {"isNew": true, "newSideDishName": "${newSideDishName}", "newSideDishPrice": "${newSideDishPrice}"}
        ]
    }""".trimIndent()

    val request = MockMvcRequestBuilders.post("/api/order_entries/save")
      .content(createContent)
      .contentType(MediaType.APPLICATION_JSON)
      .header("Authorization", user1Token)

    expectBadRequestWithMessage(request, "New side dish name cannot be empty")
  }

  @Test()
  fun itShouldNotAddOrderEntryIfExistingDishDoesNotExist() {
    val team1 = teamService.createTeam("team1.com", "team1.com")
    val (user1Token, user1) = createUserAndGetToken("James1", "james1@team1.com")

    val restaurant = restaurantService.createRestaurant(team1, RestaurantSaveRequest("Restaurant 1"))
    val sideDish1 = SideDish(name = "Side dish 1", price = 100)
    val sideDish2 = SideDish(name = "Side dish 2", price = 120)
    val sideDish3 = SideDish(name = "Side dish 3", price = 150)
    val dishCreateRequest = DishCreateRequest(
      "Dish 1",
      100,
      category = "Category 1",
      sideDishes = listOf(
        sideDish1,
        sideDish2,
        sideDish3
      )
    )
    val dish1 = dishService.saveDish(team1, restaurant.id, dishCreateRequest)

    val orderSaveRequest = OrderSaveRequest(restaurantId = restaurant.id, orderDate = LocalDate.now(), timeOfOrder = LocalTime.of(14, 0), deliveryData = DeliveryData(), paymentData = PaymentData())
    val order = orderService.saveOrder(orderSaveRequest, currentUser = user1, currentUserTeam = team1)

    val fakeDishId = "11111111"

    val createContent = """{
        "orderId": "${order.id}",
        "dishId": "${fakeDishId}",
        "additionalComments": "Some funny comment",
        "newDish": false,
        "newDishName": "",
        "newDishPrice": 0,
        "sideDishes": []
    }""".trimIndent()

    val request = MockMvcRequestBuilders.post("/api/order_entries/save")
      .content(createContent)
      .contentType(MediaType.APPLICATION_JSON)
      .header("Authorization", user1Token)

    expectBadRequestWithMessage(request, "Dish does not exist")
  }

  @Test()
  fun itShouldNotAddOrderEntryIfNewDishNameIsEmpty() {
    val team1 = teamService.createTeam("team1.com", "team1.com")
    val (user1Token, user1) = createUserAndGetToken("James1", "james1@team1.com")

    val restaurant = restaurantService.createRestaurant(team1, RestaurantSaveRequest("Restaurant 1"))
    val sideDish1 = SideDish(name = "Side dish 1", price = 100)
    val sideDish2 = SideDish(name = "Side dish 2", price = 120)
    val sideDish3 = SideDish(name = "Side dish 3", price = 150)
    val dishCreateRequest = DishCreateRequest(
      "Dish 1",
      100,
      category = "Category 1",
      sideDishes = listOf(
        sideDish1,
        sideDish2,
        sideDish3
      )
    )
    val dish1 = dishService.saveDish(team1, restaurant.id, dishCreateRequest)

    val orderSaveRequest = OrderSaveRequest(restaurantId = restaurant.id, orderDate = LocalDate.now(), timeOfOrder = LocalTime.of(14, 0), deliveryData = DeliveryData(), paymentData = PaymentData())
    val order = orderService.saveOrder(orderSaveRequest, currentUser = user1, currentUserTeam = team1)

    val newDishName = ""
    val newDishPrice = 2400

    val createContent = """{
        "orderId": "${order.id}",
        "dishId": "${dish1.id}",
        "additionalComments": "Some funny comment",
        "newDish": true,
        "newDishName": "${newDishName}",
        "newDishPrice": "${newDishPrice}",
        "sideDishes": []
    }""".trimIndent()

    val request = MockMvcRequestBuilders.post("/api/order_entries/save")
      .content(createContent)
      .contentType(MediaType.APPLICATION_JSON)
      .header("Authorization", user1Token)

    expectBadRequestWithMessage(request, "Dish name cannot be empty")
  }

  @Test()
  fun itShouldNotAddOrderEntryIfNewDishPriceIsNegative() {
    val team1 = teamService.createTeam("team1.com", "team1.com")
    val (user1Token, user1) = createUserAndGetToken("James1", "james1@team1.com")

    val restaurant = restaurantService.createRestaurant(team1, RestaurantSaveRequest("Restaurant 1"))
    val sideDish1 = SideDish(name = "Side dish 1", price = 100)
    val sideDish2 = SideDish(name = "Side dish 2", price = 120)
    val sideDish3 = SideDish(name = "Side dish 3", price = 150)
    val dishCreateRequest = DishCreateRequest(
      "Dish 1",
      100,
      category = "Category 1",
      sideDishes = listOf(
        sideDish1,
        sideDish2,
        sideDish3
      )
    )
    val dish1 = dishService.saveDish(team1, restaurant.id, dishCreateRequest)

    val orderSaveRequest = OrderSaveRequest(restaurantId = restaurant.id, orderDate = LocalDate.now(), timeOfOrder = LocalTime.of(14, 0), deliveryData = DeliveryData(), paymentData = PaymentData())
    val order = orderService.saveOrder(orderSaveRequest, currentUser = user1, currentUserTeam = team1)

    val newDishName = "New dish"
    val newDishPrice = -2400

    val createContent = """{
        "orderId": "${order.id}",
        "dishId": "${dish1.id}",
        "additionalComments": "Some funny comment",
        "newDish": true,
        "newDishName": "${newDishName}",
        "newDishPrice": "${newDishPrice}",
        "sideDishes": []
    }""".trimIndent()

    val request = MockMvcRequestBuilders.post("/api/order_entries/save")
      .content(createContent)
      .contentType(MediaType.APPLICATION_JSON)
      .header("Authorization", user1Token)

    expectBadRequestWithMessage(request, "Dish price cannot be negative")
  }

  @Test()
  fun itShouldNotAddOrderEntryIfOrderDoesNotExist() {
    val team1 = teamService.createTeam("team1.com", "team1.com")
    val (user1Token, user1) = createUserAndGetToken("James1", "james1@team1.com")

    val restaurant = restaurantService.createRestaurant(team1, RestaurantSaveRequest("Restaurant 1"))
    val sideDish1 = SideDish(name = "Side dish 1", price = 100)
    val sideDish2 = SideDish(name = "Side dish 2", price = 120)
    val sideDish3 = SideDish(name = "Side dish 3", price = 150)
    val dishCreateRequest = DishCreateRequest(
      "Dish 1",
      100,
      category = "Category 1",
      sideDishes = listOf(
        sideDish1,
        sideDish2,
        sideDish3
      )
    )
    val dish1 = dishService.saveDish(team1, restaurant.id, dishCreateRequest)

    val createContent = """{
        "orderId": "${fakeOrderId}",
        "dishId": "${dish1.id}",
        "additionalComments": "Some funny comment",
        "newDish": false,
        "sideDishes": []
    }""".trimIndent()

    val request = MockMvcRequestBuilders.post("/api/order_entries/save")
      .content(createContent)
      .contentType(MediaType.APPLICATION_JSON)
      .header("Authorization", user1Token)

    expectBadRequestWithMessage(request, "Order does not exist")
  }

  @Test()
  fun itShouldNotAddOrderEntryIfUserHasNoAccessToOrder() {
    val team1 = teamService.createTeam("team1.com", "team1.com")
    val (user1Token, user1) = createUserAndGetToken("James1", "james1@team1.com")

    val restaurant = restaurantService.createRestaurant(team1, RestaurantSaveRequest("Restaurant 1"))
    val sideDish1 = SideDish(name = "Side dish 1", price = 100)
    val sideDish2 = SideDish(name = "Side dish 2", price = 120)
    val sideDish3 = SideDish(name = "Side dish 3", price = 150)
    val dishCreateRequest = DishCreateRequest(
      "Dish 1",
      100,
      category = "Category 1",
      sideDishes = listOf(
        sideDish1,
        sideDish2,
        sideDish3
      )
    )
    val dish1 = dishService.saveDish(team1, restaurant.id, dishCreateRequest)

    val orderSaveRequest = OrderSaveRequest(restaurantId = restaurant.id, orderDate = LocalDate.now(), timeOfOrder = LocalTime.of(14, 0), deliveryData = DeliveryData(), paymentData = PaymentData())
    val order = orderService.saveOrder(orderSaveRequest, currentUser = user1, currentUserTeam = team1)

    val team2 = teamService.createTeam("team2.com", "team2.com")
    val (user2Token, user2) = createUserAndGetToken("James2", "james2@team2.com")

    val createContent = """{
        "orderId": "${order.id}",
        "dishId": "${dish1.id}",
        "additionalComments": "Some funny comment",
        "newDish": false,
        "newDishName": "",
        "newDishPrice": 0,
        "sideDishes": []
    }""".trimIndent()

    val request = MockMvcRequestBuilders.post("/api/order_entries/save")
      .content(createContent)
      .contentType(MediaType.APPLICATION_JSON)
      .header("Authorization", user2Token)

    expectBadRequestWithMessage(request, "You have no access to this order")
  }
}