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
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextEdit;
import io.ballerina.tools.text.TextRange;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Represents a request for a Ballerina AST Modify.
 *
 * @since 1.3.0
 */
public class BallerinaTreeModifyUtil {

    private static final String DELETE = "delete";

    private BallerinaTreeModifyUtil() {
    }

    private static final Map<String, String> typeMapping = new HashMap<String, String>() {{
        put("DELETE", "");
        put("IMPORT", "import $TYPE;\n");
        put("DECLARATION", "$TYPE $VARIABLE = new ($PARAMS);\n");
        put("REMOTE_SERVICE_CALL_CHECK", "$TYPE $VARIABLE = checkpanic $CALLER->$FUNCTION($PARAMS);\n");
        put("REMOTE_SERVICE_CALL", "$TYPE $VARIABLE = $CALLER->$FUNCTION($PARAMS);\n");
        put("SERVICE_CALL_CHECK", "$TYPE $VARIABLE = checkpanic $CALLER.$FUNCTION($PARAMS);\n");
        put("SERVICE_CALL", "$TYPE $VARIABLE = $CALLER.$FUNCTION($PARAMS);\n");
        put("MAIN_START", "$COMMENTpublic function main() {\n");
        put("MAIN_START_MODIFY", "$COMMENTpublic function main() {");
        put("MAIN_END", "\n}\n");
        put("SERVICE_START", "@http:ServiceConfig {\n\tbasePath: \"/\"\n}\n" +
                "service $SERVICE on new http:Listener($PORT) {\n" +
                "@http:ResourceConfig {\n\tmethods: [$METHODS],\npath: \"/$RES_PATH\"\n}\n" +
                "    resource function $RESOURCE(http:Caller caller, http:Request req) {\n\n");
        put("SERVICE_START_MODIFY", "@http:ServiceConfig {\n\tbasePath: \"/\"\n}\n" +
                "service $SERVICE on new http:Listener($PORT) {\n" +
                "@http:ResourceConfig {\n\tmethods: [$METHODS],\npath: \"/$RES_PATH\"\n}\n" +
                "    resource function $RESOURCE(http:Caller caller, http:Request req) {");
        put("SERVICE_END",
                "    }\n" +
                        "}\n");
        put("IF_STATEMENT", "if ($CONDITION) {\n" +
                "\n} else {\n\n}\n");
        put("FOREACH_STATEMENT", "foreach $TYPE $VARIABLE in $COLLECTION {\n" +
                "\n}\n");
        put("LOG_STATEMENT", "log:print$TYPE($LOG_EXPR);\n");
        put("PROPERTY_STATEMENT", "$PROPERTY\n");
        put("RESPOND", "$TYPE $VARIABLE = $CALLER->respond($EXPRESSION);\n");
        put("TYPE_GUARD_IF", "if($VARIABLE is $TYPE) {\n" +
                "$STATEMENT" +
                "\n}\n");
        put("TYPE_GUARD_ELSE_IF", "else if($VARIABLE is $TYPE) {\n" +
                "\n}\n");
        put("TYPE_GUARD_ELSE", " else {\n" +
                "\n}\n");
        put("RESPOND_WITH_CHECK", "checkpanic $CALLER->respond(<@untainted>$EXPRESSION);\n");
        put("PROPERTY_STATEMENT", "$PROPERTY\n");
        put("RETURN_STATEMENT", "return $RETURN_EXPR;\n");
        put("CHECKED_PAYLOAD_FUNCTION_INVOCATION", "$TYPE $VARIABLE = checkpanic $RESPONSE.$PAYLOAD();\n");
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

    public static List<TextEdit> getUnusedImportRanges(
            Collection<BLangImportPackage> unusedImports, TextDocument textDocument) {
        List<TextEdit> edits = new ArrayList<>();
        for (BLangImportPackage importPackage : unusedImports) {
            LinePosition startLinePos = LinePosition.from(importPackage.getPosition()
                            .lineRange().startLine().line() - 1,
                    importPackage.getPosition().lineRange().startLine().offset() - 1);
            LinePosition endLinePos = LinePosition.from(importPackage.getPosition()
                            .lineRange().endLine().line() - 1,
                    importPackage.getPosition().lineRange().endLine().offset() - 1);
            int startOffset = textDocument.textPositionFrom(startLinePos);
            int endOffset = textDocument.textPositionFrom(endLinePos) + 1;
            edits.add(TextEdit.from(
                    TextRange.from(startOffset,
                            endOffset - startOffset), ""));
        }
        return edits;
    }

    public static TextEdit createTextEdit(TextDocument oldTextDocument,
                                          JsonObject config,
                                          String type,
                                          int startLine,
                                          int startColumn,
                                          int endLine,
                                          int endColumn) {
        String mainStartMapping = BallerinaTreeModifyUtil.resolveMapping(type,
                config == null ? new JsonObject() : config);
        int theStartOffset = oldTextDocument.textPositionFrom(LinePosition.from(
                startLine - 1, startColumn - 1));
        int theEndOffset = oldTextDocument.textPositionFrom(LinePosition.from(
                endLine - 1, endColumn - 1));
        return TextEdit.from(
                TextRange.from(theStartOffset,
                        theEndOffset - theStartOffset), mainStartMapping);
    }
}
