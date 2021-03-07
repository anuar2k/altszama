package altszama.app.restaurant

import altszama.app.auth.User
import altszama.app.auth.UserService
import altszama.app.dish.DishService
import altszama.app.dish.dto.DishDto
import altszama.app.restaurant.dto.*
import altszama.app.team.TeamService
import altszama.app.validation.UserDoesNotBelongToAnyTeam
import altszama.app.validation.ValidationFailedException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RestaurantControllerDataService {

  @Autowired
  private lateinit var restaurantService: RestaurantService

  @Autowired
  private lateinit var dishService: DishService

  @Autowired
  private lateinit var userService: UserService

  @Autowired
  private lateinit var teamService: TeamService

  fun getIndexData(currentUser: User): IndexResponse {
    val team = teamService.findByUser(currentUser).orElseThrow { UserDoesNotBelongToAnyTeam() }

    val restaurantToCountMap: Map<Restaurant, Long> = restaurantService.restaurantsToDishCountMap(team)

    val restaurantInfoList = restaurantToCountMap.entries
        .map { entry -> RestaurantInfo(entry.key.id, entry.key.name, entry.key.lastCrawled, entry.key.lastEdited, entry.value) }

    return IndexResponse(restaurantInfoList, ImportCredentials(team.importUsername, team.importPassword))
  }

  fun getShowData(restaurantId: String): ShowRestaurantResponse {
    val restaurant = restaurantService.findById(restaurantId).orElseThrow { ValidationFailedException("Restaurant does not exist") }
    val team = teamService.findById(restaurant.team.id).get()
    val dishes = dishService.findAllDishesByRestaurantId(restaurant.id).map { dish -> DishDto.fromDish(dish) }
    val dishesByCategory: Map<String, List<DishDto>> = dishes.groupBy { dish -> dish.category }

    return ShowRestaurantResponse(restaurant, dishes, dishesByCategory)
  }

  fun getEditRestaurantData(currentUser: User, restaurantId: String): EditRestaurantResponse {
    val currentUserTeam = teamService.findByUser(currentUser).get()

    val restaurant = restaurantService.findById(restaurantId).get()

    if (restaurant.team == currentUserTeam) {
      return EditRestaurantResponse(
          restaurant.id,
          restaurant.name,
          restaurant.address,
          restaurant.telephone,
          restaurant.url
      )
    } else {
      throw ValidationFailedException("Not authorized")
    }
  }
}
