import {Selector} from "testcafe";


export default class CreateDishForm {
  static async fillNameField(t: TestController, value: string) {
    let inputSelector = Selector("label").withText("Name").parent().find("input");
    await t.typeText(inputSelector, value)
  }

  static async fillPriceField(t: TestController, value: string) {
    let inputSelector = Selector("label").withText("Price").parent().find("input");
    await t
      .click(inputSelector)
      .pressKey('ctrl+a delete')
      .typeText(inputSelector, value)
  }

  static async clickCreateDishButton(t: TestController) {
    await t.click(Selector("button").withText("CREATE"))
  }
}
