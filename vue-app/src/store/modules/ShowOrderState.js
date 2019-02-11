import OrdersApiConnector from "../../lib/OrdersApiConnector";
import ApiConnector from "../../lib/ApiConnector";
import router from '../../router/index'

export const NAMESPACE_SHOW_ORDER = "showOrder";

export const LOAD_SHOW_ORDER_DATA = "LOAD_SHOW_ORDER_DATA";
export const FETCH_ORDER_DATA_ACTION = "FETCH_ORDER_DATA_ACTION";
export const UNLOCK_ORDER_ACTION = "UNLOCK_ORDER_ACTION";
export const DELETE_DISH_ENTRY_ACTION = "DELETE_DISH_ENTRY_ACTION";
export const CONFIRM_ORDER_ENTRY_AS_PAID_ACTION = "CONFIRM_ORDER_ENTRY_AS_PAID_ACTION";
export const MARK_ORDER_ENTRY_AS_PAID_ACTION = "MARK_ORDER_ENTRY_AS_PAID_ACTION";
export const SET_ORDER_AS_CREATED_ACTION = "SET_ORDER_AS_CREATED_ACTION";
export const SET_ORDER_AS_ORDERED_ACTION = "SET_ORDER_AS_ORDERED_ACTION";
export const SET_ORDER_AS_DELIVERED_ACTION = "SET_ORDER_AS_DELIVERED_ACTION";
export const SET_ORDER_AS_REJECTED_ACTION = "SET_ORDER_AS_REJECTED_ACTION";
export const DELETE_ORDER_ACTION = "DELETE_ORDER_ACTION";

export default {
  namespaced: true,
  state: {
    order: {
      restaurant: {
        name: ""
      },
      orderState: "",
      orderCreator: {}
    },
    orderEntries: [],
    currentUserId: '',
    allDishesInRestaurant: [],
    allDishesByCategory: [],
    dishIdToSideDishesMap: [],
    totalOrderPrice: 0,
    baseOrderPrice: 0,
  },
  mutations: {
    [LOAD_SHOW_ORDER_DATA] (state, payload) {
      state.order = payload.order;
      state.orderEntries = payload.orderEntries;
      state.currentUserId = payload.currentUserId;
      state.allDishesInRestaurant = payload.allDishesInRestaurant;
      state.allDishesByCategory = payload.allDishesByCategory;
      state.dishIdToSideDishesMap = payload.dishIdToSideDishesMap;
      state.totalOrderPrice = payload.totalOrderPrice;
      state.baseOrderPrice = payload.baseOrderPrice;
    },

  },
  actions: {
    [FETCH_ORDER_DATA_ACTION] ({state}, payload) {
      const orderId = payload.orderId;

      OrdersApiConnector
        .fetchOrder(orderId)
        .then(showOrderData => {
          this.commit(`${NAMESPACE_SHOW_ORDER}/${LOAD_SHOW_ORDER_DATA}`, showOrderData);
          this.commit('setLoadingFalse')
        })
        .catch(errResponse => ApiConnector.handleError(errResponse))
    },

    [UNLOCK_ORDER_ACTION] ({state}, {}) {
      OrdersApiConnector.setOrderAsCreated(state.order.id)
        .then(() => {
          this.commit('setLoadingTrue');
          this.dispatch(`${NAMESPACE_SHOW_ORDER}/${FETCH_ORDER_DATA_ACTION}`, {orderId: state.order.id})
        })
        .catch(errResponse => ApiConnector.handleError(errResponse))
    },
    [DELETE_DISH_ENTRY_ACTION] ({state}, {orderEntryId, dishEntryId}) {
      OrdersApiConnector.deleteDishEntry(orderEntryId, dishEntryId)
        .then(() => {
          this.commit('setLoadingTrue');
          this.dispatch(`${NAMESPACE_SHOW_ORDER}/${FETCH_ORDER_DATA_ACTION}`, {orderId: state.order.id})
        })
        .catch(errResponse => ApiConnector.handleError(errResponse))
    },
    [CONFIRM_ORDER_ENTRY_AS_PAID_ACTION] ({state}, {orderEntryId}) {
      OrdersApiConnector.confirmOrderEntryAsPaid(orderEntryId)
        .then(() => {
          this.commit('setLoadingTrue');
          this.dispatch(`${NAMESPACE_SHOW_ORDER}/${FETCH_ORDER_DATA_ACTION}`, {orderId: state.order.id})
        })
        .catch(errResponse => ApiConnector.handleError(errResponse))
    },
    [MARK_ORDER_ENTRY_AS_PAID_ACTION] ({state}, {orderEntryId}) {
      OrdersApiConnector.markOrderEntryAsPaid(orderEntryId)
          .then(() => {
            this.commit('setLoadingTrue');
            this.dispatch(`${NAMESPACE_SHOW_ORDER}/${FETCH_ORDER_DATA_ACTION}`, {orderId: state.order.id})
          })
          .catch(errResponse => ApiConnector.handleError(errResponse))
    },
    [SET_ORDER_AS_CREATED_ACTION] ({state}, {orderId}) {
      OrdersApiConnector.setOrderAsCreated(orderId)
          .then(() => {
            this.commit('setLoadingTrue');
            this.dispatch(`${NAMESPACE_SHOW_ORDER}/${FETCH_ORDER_DATA_ACTION}`, {orderId: state.order.id})
          })
          .catch(errResponse => ApiConnector.handleError(errResponse))
    },
    [SET_ORDER_AS_ORDERED_ACTION] ({state}, {orderId}) {
      OrdersApiConnector.setOrderAsOrdered(orderId)
          .then(() => {
            this.commit('setLoadingTrue');
            this.dispatch(`${NAMESPACE_SHOW_ORDER}/${FETCH_ORDER_DATA_ACTION}`, {orderId: state.order.id})
          })
          .catch(errResponse => ApiConnector.handleError(errResponse))
    },
    [SET_ORDER_AS_DELIVERED_ACTION] ({state}, {orderId}) {
      OrdersApiConnector.setOrderAsDelivered(orderId)
          .then(() => {
            this.commit('setLoadingTrue');
            this.dispatch(`${NAMESPACE_SHOW_ORDER}/${FETCH_ORDER_DATA_ACTION}`, {orderId: state.order.id})
          })
          .catch(errResponse => ApiConnector.handleError(errResponse))
    },
    [SET_ORDER_AS_REJECTED_ACTION] ({state}, {orderId}) {
      OrdersApiConnector.setOrderAsRejected(orderId)
          .then(() => {
            this.commit('setLoadingTrue');
            this.dispatch(`${NAMESPACE_SHOW_ORDER}/${FETCH_ORDER_DATA_ACTION}`, {orderId: state.order.id})
          })
          .catch(errResponse => ApiConnector.handleError(errResponse))
    },
    [DELETE_ORDER_ACTION] ({state}, {orderId}) {
      OrdersApiConnector.deleteOrder(orderId + '/delete')
        .then(() => router.push('/orders'))
        .catch(errResponse => ApiConnector.handleError(errResponse))
    },
  }
};