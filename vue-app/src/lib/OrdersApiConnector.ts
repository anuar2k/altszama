import router from "../router/index";
import {
  AllOrdersResponse,
  EditOrderInitialData,
  OrderControllerApi,
  OrderEntryControllerApi,
  OrderEntrySaveRequest,
  OrderEntryUpdateRequest,
  OrderSaveRequest,
  OrderUpdateRequest,
  OrderViewInitialData,
  SetAsOrderedResponse,
  ShowOrderResponse,
  SideDishData
} from "@/frontend-client";
import {AbstractApiConnector} from "@/lib/AbstractApiConnector";


export interface OrderEntryToModify {
  orderId: string;
  dishId: string;
  dishEntryId: string;
  additionalComments: string;
  newDish: boolean;
  newDishName: string;
  newDishPrice: number;
  chosenSideDishes: SideDishData[];
}

export default class OrdersApiConnector extends AbstractApiConnector {
  private readonly orderApi: OrderControllerApi;
  private readonly orderEntryApi: OrderEntryControllerApi;

  constructor() {
    super()
    const configuration = this.createConfiguration()
    this.orderApi = new OrderControllerApi(configuration);
    this.orderEntryApi = new OrderEntryControllerApi(configuration);
  }

  saveOrderEntry(orderId: string, editedOrderEntry: OrderEntryToModify) {
    const formData: OrderEntrySaveRequest = {
      orderId: orderId,
      dishId: editedOrderEntry.dishId,
      newDish: editedOrderEntry.newDish,
      newDishName: editedOrderEntry.newDishName,
      newDishPrice: editedOrderEntry.newDishPrice,
      additionalComments: editedOrderEntry.additionalComments,
      sideDishes: editedOrderEntry.chosenSideDishes.map(sd =>
        Object.assign(sd, { newSideDishPrice: sd.newSideDishPrice })
      )
    };

    return this.orderEntryApi.save1(formData, this.headersWithToken());
  }

  updateOrderEntry(
    orderId: string,
    orderEntryId: string,
    editedOrderEntry: OrderEntryToModify
  ) {
    const formData: OrderEntryUpdateRequest = {
      id: orderEntryId,
      orderId: orderId,
      dishEntryId: editedOrderEntry.dishEntryId,
      dishId: editedOrderEntry.dishId,
      newDish: editedOrderEntry.newDish,
      newDishName: editedOrderEntry.newDishName,
      newDishPrice: editedOrderEntry.newDishPrice,
      additionalComments: editedOrderEntry.additionalComments,
      sideDishes: editedOrderEntry.chosenSideDishes.map(sd =>
        Object.assign({}, sd, { newSideDishPrice: sd.newSideDishPrice })
      )
    };

    return this.orderEntryApi.update1(formData, this.headersWithToken());
  }

  deleteDishEntry(orderEntryId: string, dishEntryId: string) {
    return this.orderEntryApi.delete1(
      orderEntryId,
      dishEntryId,
      this.headersWithToken()
    );
  }

  fetchOrder(orderId: string): Promise<ShowOrderResponse> {
    return this.orderApi.show(orderId, this.headersWithToken());
  }

  fetchAllOrders(): Promise<AllOrdersResponse> {
    return this.orderApi.allOrders(this.headersWithToken());
  }

  fetchTodaysOrders() {
    return this.orderApi.todayOrders(this.headersWithToken());
  }

  fetchOrderView(orderId: string): Promise<OrderViewInitialData> {
    return this.orderApi.orderViewJson(orderId, this.headersWithToken());
  }

  getOrderCreateData() {
    return this.orderApi.create(this.headersWithToken());
  }

  getOrderEditData(orderId: string): Promise<EditOrderInitialData> {
    return this.orderApi.edit(orderId, this.headersWithToken());
  }

  setOrderAsCreated(orderId: string) {
    return this.orderApi.setAsCreated(orderId, this.headersWithToken());
  }

  setOrderAsOrdered(orderId: string) {
    return this.orderApi.setBackAsOrdered(orderId, this.headersWithToken());
  }

  setOrderAsDelivered(orderId: string) {
    return this.orderApi.setAsDelivered(orderId, this.headersWithToken());
  }

  setOrderAsRejected(orderId: string) {
    return this.orderApi.setAsRejected(orderId, this.headersWithToken());
  }

  deleteOrder(orderId: string) {
    return this.orderApi._delete(orderId, this.headersWithToken());
  }

  markOrderEntryAsPaid(orderEntryId: string) {
    return this.orderEntryApi.setAsMarkedAsPaid(
      orderEntryId,
      this.headersWithToken()
    );
  }

  confirmOrderEntryAsPaid(orderEntryId: string) {
    return this.orderEntryApi.setAsConfirmedAsPaid(
      orderEntryId,
      this.headersWithToken()
    );
  }

  createOrder(order: OrderSaveRequest) {
    return this.orderApi.save(order, this.headersWithToken());
  }

  editOrder(order: OrderUpdateRequest) {
    return this.orderApi.update(order, this.headersWithToken());
  }

  makeAnOrder(orderId: string, formData: SetAsOrderedResponse) {
    return this.orderApi
      .setAsOrdered(formData, orderId, this.headersWithToken())
      .then(() => router.back());
  }
}
