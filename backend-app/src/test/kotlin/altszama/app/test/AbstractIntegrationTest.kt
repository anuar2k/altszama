package altszama.app.test

import altszama.app.TestInitializer
import altszama.app.auth.User
import altszama.app.auth.UserService
import altszama.app.dish.Dish
import altszama.app.order.Order
import altszama.app.orderEntry.OrderEntry
import altszama.app.orderEntry.OrderEntryService
import altszama.app.orderEntry.dto.OrderEntrySaveRequest
import altszama.app.team.Team
import com.fasterxml.jackson.databind.ObjectMapper
import com.mongodb.BasicDBObject
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.result.MockMvcResultMatchers


@SpringBootTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@AutoConfigureMockMvc
@ContextConfiguration(initializers = [TestInitializer::class])
open class AbstractIntegrationTest() {

  @Autowired
  private lateinit var mongoTemplate: MongoTemplate

  @Autowired
  private lateinit var userService: UserService

  @Autowired
  private lateinit var mockMvc: MockMvc

  @Autowired
  private lateinit var objectMapper: ObjectMapper

  @Autowired
  private lateinit var orderEntryService: OrderEntryService

  protected val fakeOrderId = "111111111111111111111111"

  @BeforeEach
  fun beforeEach() {
    for (collectionName in mongoTemplate.collectionNames) {
      if (!collectionName.startsWith("system.")) {
        mongoTemplate.getCollection(collectionName).deleteMany(BasicDBObject())
      }
    }
  }

  protected fun createUserAndGetToken(username: String, email: String): Pair<String, User> {
    val user = userService.createNewUser(username, email)
    val token = "Bearer ${userService.createJwtTokenFromUserInfo(username, email).token}"

    return Pair(token, user)
  }

  protected fun expectBadRequestWithMessage(request: MockHttpServletRequestBuilder, expectedMessage: String) {
    val response = mockMvc.perform(request)
        .andExpect(MockMvcResultMatchers.status().isBadRequest)
        .andReturn()
        .response.contentAsString

    Assertions.assertThat(objectMapper.readTree(response)["message"].asText()).isEqualTo(expectedMessage)
  }

  protected fun createOrderEntry(order: Order, dish: Dish, user: User, team: Team): OrderEntry {
    val orderEntrySaveRequest = OrderEntrySaveRequest(
      orderId = order.id,
      dishId = dish.id,
      newDish = false,
      newDishName = null,
      newDishPrice = null
    )
    return orderEntryService.saveEntry(user, team, orderEntrySaveRequest)
  }

}
