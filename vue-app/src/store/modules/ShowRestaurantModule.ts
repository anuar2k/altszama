import ApiConnector from "../../lib/ApiConnector";
import router from "../../router/index";
import {DishDto, Restaurant, ShowRestaurantResponse} from "@/frontend-client";
import {Module} from "vuex";
import {RootState} from "@/store";
import DishesApiConnector from "@/lib/api/DishesApiConnector";

export const INIT_RESTAURANT_DATA = "INIT_RESTAURANT_DATA";
export const FETCH_RESTAURANT_ACTION = "FETCH_RESTAURANT_ACTION";
export const DELETE_RESTAURANT_ACTION = "DELETE_RESTAURANT_ACTION";
export const DELETE_DISH_ACTION = "DELETE_DISH_ACTION";

const connector = new DishesApiConnector();

export interface ShowRestaurantState {
  restaurant?: Restaurant;
  dishes: DishDto[];
  dishesByCategory: { [key: string]: DishDto[] };
}

export const showRestaurantState: ShowRestaurantState = {
  restaurant: {
    id: "",
    name: "",
    url: "",
    telephone: "",
    address: ""
  },
  dishes: [],
  dishesByCategory: {}
};

export const showRestaurantModule: Module<ShowRestaurantState, RootState> = {
  namespaced: true,
  state: showRestaurantState,
  mutations: {
    [INIT_RESTAURANT_DATA](state, payload: ShowRestaurantResponse) {
      state.restaurant = payload.restaurant;
      state.dishes = payload.dishes;
      state.dishesByCategory = payload.dishesByCategory;
    }
  },
  actions: {
    [FETCH_RESTAURANT_ACTION]({ state, rootState }, { restaurantId }) {
      connector
        .getShowRestaurantData(restaurantId)
        .then(response => {
          this.commit(`showRestaurant/${INIT_RESTAURANT_DATA}`, response);
          this.commit("setTitle", `Restaurant ${state.restaurant!.name}`)
          this.commit("setLoadingFalse");
        })
        .catch(errResponse => ApiConnector.handleError(errResponse));
    },
    [DELETE_RESTAURANT_ACTION]({ state, rootState },{ restaurantId, errorsComponent }) {
      connector
        .deleteRestaurant(restaurantId)
        .then(response => router.push({ name: "RestaurantIndex" }))
        .catch(errResponse => errResponse.text().then((errorMessage: string) => ApiConnector.handleError(errorMessage)));
    },
    [DELETE_DISH_ACTION]({ state, rootState }, { restaurantId, dishId }) {
      connector
        .deleteDish(restaurantId, dishId)
        .then(successResponse => {
          this.dispatch(`showRestaurant/${FETCH_RESTAURANT_ACTION}`, { restaurantId: restaurantId });
          this.commit("setLoadingFalse");
        })
        .catch(errResponse => errResponse.text().then((errorMessage: string) => ApiConnector.handleError(errorMessage)));
    }
  }
};
