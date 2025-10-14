package net.villagerzock;

import com.google.gson.JsonArray;

public class GameMath {
    public static class Vector2i{
        public static final Vector2i ZERO = new Vector2i(0, 0);
        public int x;
        public int y;
        public Vector2i(int x, int y){
            this.x = x;
            this.y = y;
        }

        public JsonArray toJson(){
            JsonArray jsonArray = new JsonArray();
            jsonArray.add(x);
            jsonArray.add(y);
            return jsonArray;
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }

}
