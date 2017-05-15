package org.ballerinalang.composer.service.workspace;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.composer.service.workspace.rest.datamodel.BFile;
import org.ballerinalang.composer.service.workspace.rest.datamodel.BLangFileRestService;

import java.io.IOException;
import javax.ws.rs.core.Response;

/**
 * Created by maheeka on 5/12/17.
 */
public class Test {
    public static void main(String[] args) throws IOException {
        String content = "function x(stirng t)(int){}";

        content = "struct Person {\n" +
                  "    string first_name;\n" +
                  "    string last_name;\n" +
                  "    int age;\n" +
                  "    string city;\n" +
                  "}\n" +
                  "\n" +
                  "struct Employee {\n" +
                  "    string name;\n" +
                  "    int age;\n" +
                  "    string address;\n" +
                  "}\n" +
                  "\n" +
                  "function oneToOneTransform() (string, int, string){\n" +
                  "    Person p = {first_name:\"John\", last_name:\"Doe\", age:30, city:\"London\"};\n" +
                  "    Employee e = {};\n" +
                  "    transform {\n" +
                  "       e.address = p.city;\n" +
                  "       e.name = p.first_name;\n" +
                  "       e.age = p.age;\n" +
                  "    };\n" +
                  "    return e.name, e.age, e.address;\n" +
                  "}";

//        content =
//                  "function oneToOneTransform() (string, int, string){\n" +
//                  "    e.name= \"test\"\n"+
//                  "}";



        BFile bFile = new BFile();
        bFile.setContent(content);
        BLangFileRestService bfr = new BLangFileRestService();
        Response response = bfr.getBallerinaJsonDataModelGivenContent(bFile);

        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(response.getEntity().toString()).getAsJsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);
    }
}
