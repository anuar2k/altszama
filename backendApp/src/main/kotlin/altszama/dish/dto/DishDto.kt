package altszama.dish.dto

import altszama.dish.Dish
import altszama.dish.SideDish
import java.time.Instant

data class DishDto(
    val id: String,
    val name: String,
    val price: Int,
    val sideDishes: List<SideDish>,
    val category: String,
    val lastCrawled: Instant?
) {

  companion object {
    fun fromDish(dish: Dish): DishDto {
      return DishDto(
          dish.id,
          dish.name,
          dish.price,
          dish.sideDishes,
          dish.category,
          dish.lastCrawled
      )
    }
  }

}