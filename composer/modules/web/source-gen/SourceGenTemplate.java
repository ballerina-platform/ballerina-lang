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
package org.ballerinalang.langserver;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.quartz.utils.FindbugsSuppressWarnings;

import java.util.Map;
/**
 * Auto generated source generation class for java.
 */
public class SourceGen {
    private static final String TAB = "    ";
    private int i = 0;
    private int l = 0;
    private boolean shouldIndent = false;
    private JsonArray ws;

    public SourceGen(int l) {
        this.l = l;
        this.ws = null;
    }

    private String times(int n, String f) {
        StringBuilder s = new StringBuilder();
        for (int j = 0; j < n; j++) {
            s.append(f);
        }
        return s.toString();
    }

    @FindbugsSuppressWarnings
    String getSourceOf(JsonObject node, boolean pretty, boolean replaceLambda) {
        if (node == null) {
            return "";
        }

        i = 0;

        JsonArray wsArray = node.getAsJsonArray("ws");
        ws = new JsonArray();

        if (wsArray != null) {
            for (JsonElement wsObj : wsArray) {
                ws.add(wsObj.getAsJsonObject().get("ws"));
            }
        }

        shouldIndent = pretty || ws != null;

        if (replaceLambda && node.get("kind").getAsString().equals("Lambda")) {
            return "$ function LAMBDA $";
        }

        switch (node.get("kind").getAsString()) {
            case "CompilationUnit":
                return join(node.getAsJsonArray("topLevelNodes"), pretty, replaceLambda,
                        w(""), null, false);
            case "ArrayType":
                return getSourceOf(node.getAsJsonObject("elementType"), pretty, replaceLambda) +
                        times(node.get("dimensions").getAsInt(), w("")
                                + "[" + w("") + "]");
            /* eslint-disable max-len */
            // auto gen start
            // auto-gen-code
            // auto gen end
            /* eslint-enable max-len */
            default:
                return "";
        }
    }

    @FindbugsSuppressWarnings
    public String w(String defaultWS) {
        if(ws.size()>0 && (ws.size() >= (i+1))) {
            String wsI = ws.get(i++).getAsString();
            // Check if the whitespace have comments
            boolean hasComment = (wsI != null) && wsI.trim().length() > 0;
            if (hasComment || (!shouldIndent && wsI != null)) {
                return wsI;
            }
        }
        return defaultWS;
    }

    @FindbugsSuppressWarnings
    public String a(String afterWS) {
        if (shouldIndent) {
            return afterWS;
        }
        return "";
    }

    @FindbugsSuppressWarnings
    private String indent() {
        ++l;
        return "";
    }

    @FindbugsSuppressWarnings
    private String outdent() {
        --l;
        if (shouldIndent) {
            return "\n\r" + repeat(TAB, l);
        }
        return "";
    }

    @FindbugsSuppressWarnings
    private String dent() {
        if (shouldIndent) {
            return "\r\n" + repeat(TAB, l);
        }
        return "";
    }

    @FindbugsSuppressWarnings
    private String repeat(String tab, int l) {
        StringBuilder result = new StringBuilder();
        for (int j = 0; j < l; j++) {
            result.append(tab);
        }
        return result.toString();
    }

    @FindbugsSuppressWarnings
    public String join(JsonArray arr, boolean pretty, boolean replaceLambda,
                       String defaultWS, String separator, boolean suffixLast) {
        StringBuilder str = new StringBuilder();
        for (int j = 0; j < arr.size(); j++) {
            JsonObject node = arr.get(j).getAsJsonObject();
            if (node.get("kind").getAsString().equals("Identifier")) {
                str.append(defaultWS);
            }
            str.append(getSourceOf(node, pretty, replaceLambda));
            if (separator != null && (suffixLast || j != (arr.size() - 1))) {
                str.append(defaultWS).append(separator);
            }
        }
        return str.toString();
    }

    @FindbugsSuppressWarnings
    private void modifyNode(JsonObject node, String parentKind) {
        String kind = node.get("kind").getAsString();
        if (kind.equals("If") && node.getAsJsonObject("elseStatement") != null &&
                node.getAsJsonObject("elseStatement").get("kind").getAsString().equals("If")) {
            node.addProperty("ladderParent", true);
        }

        if (kind.equals("XmlElementLiteral") && !parentKind.equals("XmlElementLiteral")) {
            node.addProperty("root", true);
        }

        if (kind.equals("AnnotationAttachment") &&
                node.getAsJsonObject("packageAlias").get("value").getAsString().equals("builtin")) {
            node.addProperty("builtin", true);
        }

        if (kind.equals("Identifier")) {
            if (node.get("literal").getAsBoolean()) {
                node.addProperty("valueWithBar", "|" + node.get("value").getAsString() + "|");
            } else {
                node.addProperty("valueWithBar", node.get("value").getAsString());
            }
        }

        if (kind.equals("Import")) {
            if (node.getAsJsonObject("alias") != null &&
                    node.getAsJsonObject("alias").get("value") != null &&
                    node.getAsJsonArray("packageName") != null && node.getAsJsonArray("packageName").size() != 0) {
                if (!node.getAsJsonObject("alias").get("value").getAsString()
                        .equals(node.getAsJsonArray("packageName").get(node
                                .getAsJsonArray("packageName").size() - 1).getAsJsonObject()
                                .get("value").getAsString())) {
                    node.addProperty("userDefinedAlias", true);
                }
            }
        }

        if (parentKind.equals("XmlElementLiteral") || parentKind.equals("XmlCommentLiteral") ||
                parentKind.equals("StringTemplateLiteral")) {
            node.addProperty("inTemplateLiteral", true);
        }

        if (parentKind.equals("CompilationUnit") && (kind.equals("Variable") || kind.equals("Xmlns"))) {
            node.addProperty("global", true);
        }

        if (kind.equals("VariableDef") && node.getAsJsonObject("variable") != null &&
                node.getAsJsonObject("variable").getAsJsonObject("typeNode") != null &&
                node.getAsJsonObject("variable").getAsJsonObject("typeNode")
                        .get("kind").getAsString().equals("EndpointType")) {
            node.getAsJsonObject("variable").addProperty("endpoint", true);
            node.addProperty("endpoint", true);
        }

        if (kind.equals("ForkJoin")) {
            if (node.getAsJsonObject("joinBody") != null) {
                node.getAsJsonObject("joinBody").add("position",
                        node.getAsJsonObject("joinResultVar").getAsJsonObject("position"));
            }

            if (node.getAsJsonObject("timeoutBody") != null) {
                node.getAsJsonObject("timeoutBody").add("position",
                        node.getAsJsonObject("timeOutExpression").getAsJsonObject("position"));
            }
        }
    }

    @FindbugsSuppressWarnings
    public JsonObject build(JsonObject json, JsonObject parent, String parentKind) {
        String kind = json.get("kind").getAsString();
        for (Map.Entry<String, JsonElement> child : json.entrySet()) {
            if (!child.getKey().equals("position") && !child.getKey().equals("ws")) {
                if (child.getValue().isJsonObject() &&
                        child.getValue().getAsJsonObject().get("kind") != null) {
                    json.add(child.getKey(), build(child.getValue().getAsJsonObject(), json, kind));
                } else if (child.getValue().isJsonArray()) {
                    JsonArray childArray = child.getValue().getAsJsonArray();
                    for (int j = 0; j < childArray.size(); j++) {
                        JsonElement childItem = childArray.get(j);
                        if (kind.equals("CompilationUnit") &&
                                childItem.getAsJsonObject().get("kind").getAsString().equals("Function") &&
                                childItem.getAsJsonObject().get("lambda").getAsBoolean()) {
                            childArray.remove(j);
                            j--;
                        } else if (childItem.isJsonObject() &&
                                childItem.getAsJsonObject().get("kind") != null) {
                            childItem = build(childItem.getAsJsonObject(), json, kind);
                        }
                    }
                }
            }
        }
        modifyNode(json, parentKind);
        json.add("parent", parent);
        return json;
    }
}
