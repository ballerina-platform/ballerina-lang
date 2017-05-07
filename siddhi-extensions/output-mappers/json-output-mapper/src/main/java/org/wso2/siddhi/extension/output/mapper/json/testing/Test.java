package org.wso2.siddhi.extension.output.mapper.json.testing;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minidev.json.JSONObject;

/**
 * Created by minudika on 4/5/17.
 */
public class Test {
    public static void main(String args[]){
        Gson gson = new Gson();
        JsonArray jsonArray = new JsonArray();
        for(int i=0;i<4;i++) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("symbol", "WSO2");
            jsonObject.addProperty("price", 55.6);
            jsonObject.addProperty("volume", 100);
            jsonArray.add(jsonObject);
        }

        JsonObject object = new JsonObject();
       object.addProperty("events",gson.toJson(jsonArray));
        System.out.println("event:"+gson.toJson(jsonArray));
    }
}
