package net.villagerzock.inventory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class Item {

    public static List<Item> itemRegistry =  new ArrayList<>();

    public int ordinal;
    public int getOrdinal() {
        return ordinal;
    }
    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }
}
