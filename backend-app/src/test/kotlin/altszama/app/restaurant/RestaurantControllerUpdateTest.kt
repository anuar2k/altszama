package altszama.app.restaurant

import altszama.app.restaurant.dto.RestaurantSaveRequest
import altszama.app.team.TeamService
import altszama.app.test.AbstractIntegrationTest
import altszama.app.validation.NoAccessToRestaurant
import altszama.app.validation.RestaurantDoesNotExist
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

internal class RestaurantControllerUpdateTest : AbstractIntegrationTest() {

  @Autowired
  private lateinit var mockMvc: MockMvc

  @Autowired
  private lateinit var teamService: TeamService

  @Autowired
  private lateinit var restaurantService: RestaurantService

  @Autowired
  private lateinit var objectMapper: ObjectMapper


  @Test
  fun itShouldUpdateRestaurantIfTheTeamIsCorrect() {
    val user1Token = createUserAndGetToken("James1", "james1@team1.com")

    val team1 = teamService.createTeam("team1.com", "", listOf("james1@team1.com"))

    val restaurant = restaurantService.createRestaurant(team1, RestaurantSaveRequest("Restaurant 1", address = "Address 1"))

    val updateContent = """{
        "id": "${restaurant.id}",
        "name": "Restaurant 11",
        "telephone": "",
        "address": "Address 2",
        "url": ""
    }""".trimIndent()

    val request = MockMvcRequestBuilders.put("/api/restaurants/update")
        .content(updateContent)
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", user1Token)

    mockMvc.perform(request)
        .andExpect(MockMvcResultMatchers.status().isOk)
        .andReturn()
        .response.contentAsString

    val updatedRestaurant = restaurantService.findById(restaurant.id).get()

    assertThat(updatedRestaurant.name).isEqualTo("Restaurant 11")
    assertThat(updatedRestaurant.address).isEqualTo("Address 2")
  }

  @Test
  fun itShouldFailToUpdateRestaurantIfItDoesNotExist() {
    val user1Token = createUserAndGetToken("James1", "james1@team1.com")

    val team1 = teamService.createTeam("team1.com", "", listOf("james1@team1.com"))
    val team2 = teamService.createTeam("team2.com", "", listOf("james2@team2.com"))

    val fakeRestaurantId = "111111111111111111111111"

    val updateContent = """{
        "id": "${fakeRestaurantId}",
        "name": "Restaurant 11",
        "telephone": "",
        "address": "Address 2",
        "url": ""
    }""".trimIndent()

    val request = MockMvcRequestBuilders.put("/api/restaurants/update")
        .content(updateContent)
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", user1Token)

    expectBadRequestWithMessage(request, RestaurantDoesNotExist().message)
  }

  @Test
  fun itShouldFailToUpdateRestaurantIfTheTeamIsWrong() {
    val user1Token = createUserAndGetToken("James1", "james1@team1.com")

    val team1 = teamService.createTeam("team1.com", "", listOf("james1@team1.com"))
    val team2 = teamService.createTeam("team2.com", "", listOf("james2@team2.com"))

    val restaurant = restaurantService.createRestaurant(team2, RestaurantSaveRequest("Restaurant 1", address = "Address 1"))

    val updateContent = """{
        "id": "${restaurant.id}",
        "name": "Restaurant 11",
        "telephone": "",
        "address": "Address 2",
        "url": ""
    }""".trimIndent()

    val request = MockMvcRequestBuilders.put("/api/restaurants/update")
        .content(updateContent)
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", user1Token)

    expectBadRequestWithMessage(request, NoAccessToRestaurant().message)

    val updatedRestaurant = restaurantService.findById(restaurant.id).get()

    assertThat(updatedRestaurant.name).isEqualTo("Restaurant 1")
    assertThat(updatedRestaurant.address).isEqualTo("Address 1")
  }

  @Test
  fun itShouldFailToUpdateRestaurantIfTheNewNameIsBlank() {
    val user1Token = createUserAndGetToken("James1", "james1@team1.com")

    val team1 = teamService.createTeam("team1.com", "", listOf("james1@team1.com"))

    val restaurant = restaurantService.createRestaurant(team1, RestaurantSaveRequest("Restaurant 1", address = "Address 1"))

    val updateContent = """{
        "id": "${restaurant.id}",
        "name": "",
        "telephone": "",
        "address": "Address 2",
        "url": ""
    }""".trimIndent()

    val request = MockMvcRequestBuilders.put("/api/restaurants/update")
        .content(updateContent)
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", user1Token)

    expectBadRequestWithMessage(request, "Restaurant name cannot be blank")

    val updatedRestaurant = restaurantService.findById(restaurant.id).get()

    assertThat(updatedRestaurant.name).isEqualTo("Restaurant 1")
    assertThat(updatedRestaurant.address).isEqualTo("Address 1")
  }

  @Test
  fun itShouldFailToUpdateRestaurantIfTheNewNameIsNotInTheData() {
    val user1Token = createUserAndGetToken("James1", "james1@team1.com")

    val team1 = teamService.createTeam("team1.com", "", listOf("james1@team1.com"))

    val restaurant = restaurantService.createRestaurant(team1, RestaurantSaveRequest("Restaurant 1", address = "Address 1"))

    val updateContent = """{
        "id": "${restaurant.id}",
        "telephone": "",
        "address": "Address 2",
        "url": ""
    }""".trimIndent()

    val request = MockMvcRequestBuilders.put("/api/restaurants/update")
        .content(updateContent)
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", user1Token)

    expectBadRequestWithMessage(request, "Restaurant name cannot be blank")

    val updatedRestaurant = restaurantService.findById(restaurant.id).get()

    assertThat(updatedRestaurant.name).isEqualTo("Restaurant 1")
    assertThat(updatedRestaurant.address).isEqualTo("Address 1")
  }
}
