package net.villagerzock.inventory;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public class ItemStack {
    private Item type;
    private int amount;
    public ItemStack(Item type, int amount) {
        this.type = type;
    }
    public Item getType() {
        return type;
    }
    public void setType(Item type) {
        this.type = type;
    }
    public int getAmount() {
        return amount;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }
    public void writeToJson(JsonElement json) {

    }
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", type.toString());
        jsonObject.addProperty("amount", amount);
        //jsonObject.add("data", type.writeToJson());
        return jsonObject;
    }
    public static ItemStack fromJson(JsonObject json) {
        return new ItemStack(new Item(),0);
    }
}
