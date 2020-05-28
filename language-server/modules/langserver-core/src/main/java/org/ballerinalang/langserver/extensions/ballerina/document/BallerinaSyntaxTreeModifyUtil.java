/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.extensions.ballerina.document;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Represents a request for a Ballerina AST Modify.
 *
 * @since 1.3.0
 */
public class BallerinaSyntaxTreeModifyUtil {

    private static final Map<String, String> typeMapping = new HashMap<String, String>() {{
        put("DELETE", "");
        put("IMPORT", "import $TYPE;\n");
        put("DECLARATION", "$TYPE $VARIABLE = new ($PARAMS);\n");
        put("REMOTE_SERVICE_CALL_CHECK", "$TYPE $VARIABLE = check $CALLER->$FUNCTION($PARAMS);\n");
        put("REMOTE_SERVICE_CALL", "$TYPE $VARIABLE = $CALLER->$FUNCTION($PARAMS);\n");
        put("SERVICE_CALL_CHECK", "$TYPE $VARIABLE = check $CALLER.$FUNCTION($PARAMS);\n");
        put("SERVICE_CALL", "$TYPE $VARIABLE = $CALLER.$FUNCTION($PARAMS);\n");
        put("MAIN_START", "public function main() {\n");
        put("MAIN_END", "}\n");
        put("SERVICE_START", "service $SERVICE on new http:Listener($PORT) {\n" +
                "    resource function $RESOURCE(http:Caller caller, http:Request req) {\n");
        put("SERVICE_END",
                "    }\n" +
                        "}\n");
    }};

    public static String resolveMapping(String type, JsonObject config) {
        if (type == null || type.isEmpty()) {
            return null;
        }
        String mapping = typeMapping.get(type.toUpperCase(Locale.getDefault()));
        if (mapping == null) {
            return null;
        }
        for (Map.Entry<String, JsonElement> entry : config.entrySet()) {
            String key = entry.getKey().toUpperCase(Locale.getDefault());
            String value;
            if (key.equals("PARAMS")) {
                JsonArray array = entry.getValue().getAsJsonArray();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < array.size(); i++) {
                    sb.append(array.get(i).getAsString());
                    if (i != array.size() - 1) {
                        sb.append(", ");
                    }
                }
                value = sb.toString();
            } else {
                value = entry.getValue().getAsString();
            }
            mapping = mapping.replaceAll("\\$" + key, value);
        }
        return mapping;
    }
}
