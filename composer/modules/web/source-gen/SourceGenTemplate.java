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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Auto generated source generation class for java.
 */
public class SourceGen {
    private static final String TAB = "    ";
    private int l = 0;
    private Map<String, JsonObject> anonTypes = new HashMap<>();

    public SourceGen(int l) {
        this.l = l;
    }

    // auto gen start
    // auto-gen-code
    // auto gen end


    @FindbugsSuppressWarnings
    String getSourceOf(JsonObject node, boolean pretty, boolean replaceLambda) {
        if (node == null) {
            return "";
        }

        SourceGenParams sourceGenParams = new SourceGenParams();
        sourceGenParams.setI(0);

        JsonArray wsArray = node.getAsJsonArray("ws");
        JsonArray ws = new JsonArray();

        if (wsArray != null) {
            for (JsonElement wsObj : wsArray) {
                ws.add(wsObj.getAsJsonObject().get("ws"));
            }
        }

        sourceGenParams.setWs(ws);

        sourceGenParams.setShouldIndent(pretty || !(ws != null && ws.size() > 0));

        if (replaceLambda && node.get("kind").getAsString().equals("Lambda")) {
            return "$ function LAMBDA $";
        }

        switch (node.get("kind").getAsString()) {
            case "CompilationUnit":
                return join(node.getAsJsonArray("topLevelNodes"), pretty, replaceLambda,
                        "", null, false, sourceGenParams) +
                        w("", sourceGenParams);
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
    public String w(String defaultWS, SourceGenParams sourceGenParams) {
        JsonArray ws = sourceGenParams.getWs();
        int i = sourceGenParams.getI();

        if (ws.size() > 0 &&
                (ws.size() >= (i + 1))) {
            String wsI = ws.get(i).getAsString();
            sourceGenParams.setI(i + 1);
            // Check if the whitespace have comments
            boolean hasComment = (wsI != null) && wsI.trim().length() > 0;
            if (hasComment || (!sourceGenParams.isShouldIndent() && wsI != null)) {
                return wsI;
            }
        }
        return defaultWS;
    }

    @FindbugsSuppressWarnings
    public String a(String afterWS, boolean shouldIndent) {
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
    private String outdent(JsonObject node, boolean shouldIndent) {
        --l;
        if (shouldIndent) {
            if (node.has("documentationText")) {
                String[] indent = node.get("documentationText").getAsString().split("\n");
                if (indent != null && indent.length > 0) {
                    if (indent[indent.length - 1].equals(repeat(TAB, l))) {
                        // if documentation text already contains the correct dent
                        return "";
                    }
                }
            }
            return "\r\n" + repeat(TAB, l);
        }
        return "";
    }

    @FindbugsSuppressWarnings
    private String dent(boolean shouldIndent) {
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
                       String defaultWS, String separator, boolean suffixLast, SourceGenParams sourceGenParams) {
        StringBuilder str = new StringBuilder();
        for (int j = 0; j < arr.size(); j++) {
            JsonObject node = arr.get(j).getAsJsonObject();
            if (node.get("kind").getAsString().equals("Identifier")) {
                defaultWS = w(defaultWS, sourceGenParams);
                str.append(defaultWS);
            }
            str.append(getSourceOf(node, pretty, replaceLambda));
            if (separator != null && (suffixLast || j != (arr.size() - 1))) {
                defaultWS = w(defaultWS, sourceGenParams);
                str.append(defaultWS).append(separator);
            }
        }
        return str.toString();
    }

    @FindbugsSuppressWarnings
    private void modifyNode(JsonObject node, String parentKind) {
        String kind = node.get("kind").getAsString();

        if (kind.equals("If")) {
            if (node.getAsJsonObject("elseStatement") != null) {
                node.addProperty("ladderParent", true);
            }

            if (node.has("ws") && node.getAsJsonArray("ws").size() > 1 &&
                    node.getAsJsonArray("ws").get(0).getAsJsonObject().get("text").getAsString().equals("else") &&
                    node.getAsJsonArray("ws").get(1).getAsJsonObject().get("text").getAsString().equals("if")) {
                node.addProperty("isElseIfBlock", true);
            }
        }

        if (kind.equals("Transaction")) {
            if (node.has("condition") && node.getAsJsonObject("condition").has("value")) {
                JsonObject retry = null;
                if (node.has("failedBody") &&
                        node.getAsJsonObject("failedBody").has("statements")) {
                    for (JsonElement statement :
                            node.getAsJsonObject("failedBody").get("statements").getAsJsonArray()) {
                        if (statement.isJsonObject() && statement.getAsJsonObject().has("kind") &&
                                statement.getAsJsonObject().get("kind").getAsString().equals("retry")) {
                            retry = statement.getAsJsonObject();
                        }
                    }
                }
                if (node.has("committedBody") &&
                        node.getAsJsonObject("committedBody").has("statements")) {
                    for (JsonElement statement :
                            node.getAsJsonObject("committedBody").get("statements").getAsJsonArray()) {
                        if (statement.isJsonObject() && statement.getAsJsonObject().has("kind") &&
                                statement.getAsJsonObject().get("kind").getAsString().equals("retry")) {
                            retry = statement.getAsJsonObject();
                        }
                    }
                }

                if (node.has("transactionBody") &&
                        node.getAsJsonObject("transactionBody").has("statements")) {
                    for (JsonElement statement :
                            node.getAsJsonObject("transactionBody").get("statements").getAsJsonArray()) {
                        if (statement.isJsonObject() && statement.getAsJsonObject().has("kind") &&
                                statement.getAsJsonObject().get("kind").getAsString().equals("retry")) {
                            retry = statement.getAsJsonObject();
                        }
                    }
                }

                if (retry != null) {
                    retry.addProperty("count", node.getAsJsonObject("condition").get("value").getAsString());
                }
            }
        }

        if (kind.equals("XmlCommentLiteral") ||
                kind.equals("XmlElementLiteral") ||
                kind.equals("XmlTextLiteral") ||
                kind.equals("XmlPiLiteral") &&
                        node.has("ws") &&
                        node.getAsJsonArray("ws").get(0) != null &&
                        node.getAsJsonArray("ws").get(0).getAsJsonObject().get("text").getAsString().contains("xml")
                        && node.getAsJsonArray("ws").get(0).getAsJsonObject().get("text").getAsString().contains("`")) {
            node.addProperty("root", true);
            node.addProperty("startLiteral", node.getAsJsonArray("ws").get(0).getAsJsonObject().get("text").getAsString());
        }

        if (kind.equals("XmlElementLiteral") ||
                kind.equals("XmlTextLiteral") ||
                kind.equals("XmlPiLiteral")) {
            node.addProperty("inTemplateLiteral", true);
        }

        if (kind.equals("XmlPiLiteral") && node.has("ws")) {
            JsonObject startTagWS = new JsonObject();
            startTagWS.addProperty("text", "<?");
            startTagWS.addProperty("ws", "");

            JsonObject endTagWS = new JsonObject();
            endTagWS.addProperty("text", "?>");
            endTagWS.addProperty("ws", "");

            if (node.has("root") &&
                    node.get("root").getAsBoolean() && node.getAsJsonArray("ws").size() > 1) {
                node.add("ws", addDataToArray(1, startTagWS, node.getAsJsonArray("ws")));
                node.add("ws", addDataToArray(2, endTagWS, node.getAsJsonArray("ws")));
            }

            if (!node.has("root") || !(node.has("root") && node.get("root").getAsBoolean())) {
                node.add("ws", addDataToArray(0, startTagWS, node.getAsJsonArray("ws")));
                node.add("ws",
                        addDataToArray(node.getAsJsonArray("ws").size(), endTagWS,
                                node.getAsJsonArray("ws")));
            }

            if (node.has("target") &&
                    node.getAsJsonObject("target").has("unescapedValue")) {
                JsonObject target = node.getAsJsonObject("target");
                for (int i = 0; i < target.getAsJsonArray("ws").size(); i++) {
                    if (target.getAsJsonArray("ws").get(i).getAsJsonObject().get("text").getAsString().contains("<?")
                            && target.getAsJsonArray("ws").get(i).getAsJsonObject().get("text").getAsString().contains(target.get("unescapedValue").getAsString())) {
                        target.addProperty("unescapedValue", target.getAsJsonArray("ws").get(i).getAsJsonObject().get("text").getAsString().replace("<?", ""));
                    }
                }
            }
        }

        if (kind.equals("AnnotationAttachment") &&
                node.getAsJsonObject("packageAlias").get("value").getAsString().equals("builtin")) {
            node.addProperty("builtin", true);
        }

        if (kind.equals("Identifier")) {
            if (node.has("literal") && node.get("literal").getAsBoolean()) {
                node.addProperty("valueWithBar", "^\"" + node.get("value").getAsString() + "\"");
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

            if ((node.getAsJsonArray("packageName") != null && node.getAsJsonArray("packageName").size() == 2 &&
                    node.getAsJsonArray("packageName").get(0).getAsJsonObject().get("value").getAsString()
                            .equals("transactions") && node.getAsJsonArray("packageName").get(1).getAsJsonObject()
                    .get("value").getAsString().equals("coordinator")) || (node.getAsJsonObject("alias") != null &&
                    node.getAsJsonObject("alias").get("value") != null &&
                    node.getAsJsonObject("alias").get("value").getAsString().startsWith("."))) {
                node.addProperty("isInternal", true);
            }
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

        if (kind.equals("Variable")) {
            if (node.has("typeNode")
                    && node.getAsJsonObject("typeNode").has("isAnonType")
                    && node.getAsJsonObject("typeNode").get("isAnonType").getAsBoolean()) {
                node.addProperty("isAnonType", true);
            }

            if (node.has("initialExpression") &&
                    node.getAsJsonObject("initialExpression").has("async") &&
                    node.getAsJsonObject("initialExpression").get("async").getAsBoolean()) {
                if (node.has("ws")) {
                    JsonArray ws = node.getAsJsonArray("ws");
                    for (int i = 0; i < ws.size(); i++) {
                        if (ws.get(i).getAsJsonObject().get("text").getAsString().equals("start")) {
                            if (node.getAsJsonObject("initialExpression").has("ws")) {
                                node.getAsJsonObject("initialExpression").add("ws",
                                        addDataToArray(0, node.getAsJsonArray("ws").get(i),
                                                node.getAsJsonObject("initialExpression").getAsJsonArray("ws")));
                                node.getAsJsonArray("ws").remove(i);
                            }
                        }
                    }
                }
            }

            if (node.has("typeNode") && node.getAsJsonObject("typeNode").has("nullable") &&
                    node.getAsJsonObject("typeNode").get("nullable").getAsBoolean() &&
                    node.getAsJsonObject("typeNode").has("ws")) {
                JsonArray ws = node.getAsJsonObject("typeNode").get("ws").getAsJsonArray();
                for (int i = 0; i < ws.size(); i++) {
                    if (ws.get(i).getAsJsonObject().get("text").getAsString().equals("?")) {
                        node.getAsJsonObject("typeNode").addProperty("nullableOperatorAvailable", true);
                        break;
                    }
                }
            }

            if (node.has("typeNode") &&
                    node.getAsJsonObject("typeNode").has("ws") &&
                    !node.has("ws")) {
                node.addProperty("noVisibleName", true);
            }

            if (node.has("ws")) {
                JsonArray ws = node.getAsJsonArray("ws");
                for (int i = 0; i < ws.size(); i++) {
                    if (ws.get(i).getAsJsonObject().get("text").getAsString().equals(";")) {
                        node.addProperty("endWithSemicolon", true);
                    }

                    if (ws.get(i).getAsJsonObject().get("text").getAsString().equals(",")) {
                        node.addProperty("endWithComma", true);
                    }
                }
            }
        }

        if (kind.equals("Service")) {
            if (!node.has("serviceTypeStruct")) {
                node.addProperty("isServiceTypeUnavailable", true);
            }

            if (!node.has("anonymousEndpointBind") &&
                    node.has("boundEndpoints") &&
                    node.getAsJsonArray("boundEndpoints").size() <= 0) {
                boolean bindAvailable = false;
                for (JsonElement ws : node.getAsJsonArray("ws")) {
                    if (ws.getAsJsonObject().get("text").getAsString().equals("bind")) {
                        bindAvailable = true;
                        break;
                    }
                }

                if (!bindAvailable) {
                    node.addProperty("bindNotAvailable", true);
                }
            }
        }

        if (kind.equals("Resource") && node.has("parameters") && node.getAsJsonArray("parameters").get(0) != null) {
            if (node.getAsJsonArray("parameters").get(0).getAsJsonObject().has("ws")) {
                for (JsonElement ws : node.getAsJsonArray("parameters").get(0).getAsJsonObject().getAsJsonArray("ws")) {
                    if (ws.getAsJsonObject().get("text").getAsString().equals("endpoint")) {
                        JsonObject endpointParam = node.getAsJsonArray("parameters").get(0).getAsJsonObject();
                        String valueWithBar = endpointParam.get("name").getAsJsonObject().has("valueWithBar")
                                ? endpointParam.get("name").getAsJsonObject().get("valueWithBar").getAsString()
                                : endpointParam.get("name").getAsJsonObject().get("value").getAsString();

                        endpointParam.addProperty("serviceEndpoint", true);
                        endpointParam.get("name").getAsJsonObject().addProperty("value",
                                endpointParam.get("name").getAsJsonObject().get("value").getAsString().replace("$", ""));
                        endpointParam.get("name").getAsJsonObject().addProperty("valueWithBar",
                                valueWithBar.replace("$", ""));
                        break;
                    }
                }
            }
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

        // Check if sorrounded by curlies
        if (kind.equals("MatchPatternClause") || kind.equals("MatchExpressionPatternClause")) {
            if (node.has("ws") && node.getAsJsonArray("ws").size() > 2) {
                node.addProperty("withCurlies", true);
            }
        }

        // Check if sorrounded by parantheses
        if (kind.equals("ValueType")) {
            if (node.has("ws") && node.getAsJsonArray("ws").size() > 2) {
                node.addProperty("withParantheses", true);
            }

            if (node.has("typeKind") && node.get("typeKind").getAsString().equals("nil") && node.has("ws")) {
                node.addProperty("emptyParantheses", true);
            }

            if (node.has("nullable") && node.get("nullable").getAsBoolean() && node.has("Ws")) {
                for (int i = 0; i < node.get("ws").getAsJsonArray().size(); i++) {
                    if (node.get("ws").getAsJsonArray().get(i).getAsJsonObject().get("text").getAsString().equals("?")) {
                        node.addProperty("nullableOperatorAvailable", true);
                        break;
                    }
                }
            }
        }

        if (kind.equals("UnionTypeNode") && node.has("ws")) {
            if (node.getAsJsonArray("ws").size() > 2) {
                for (JsonElement ws : node.getAsJsonArray("ws")) {
                    if (ws.getAsJsonObject().get("text").getAsString().equals("(")) {
                        node.addProperty("withParantheses", true);
                        break;
                    }
                }
            }

            JsonArray memberTypeNodes = node.get("memberTypeNodes").getAsJsonArray();
            for (int i = 0; i < memberTypeNodes.size(); i++) {
                if (memberTypeNodes.get(i).getAsJsonObject().has("nullable")
                        && memberTypeNodes.get(i).getAsJsonObject().get("nullable").getAsBoolean()) {
                    for (JsonElement ws : node.getAsJsonArray("ws")) {
                        if (ws.getAsJsonObject().get("text").getAsString().equals("?")) {
                            memberTypeNodes.get(i).getAsJsonObject()
                                    .addProperty("nullableOperatorAvailable", true);
                            break;
                        }
                    }
                }
            }
        }

        if (kind.equals("Function")) {
            if (node.has("returnTypeNode") &&
                    node.getAsJsonObject("returnTypeNode").has("ws") &&
                    node.getAsJsonObject("returnTypeNode").getAsJsonArray("ws").size() > 0) {
                node.addProperty("hasReturns", true);
            }

            if (node.has("defaultableParameters")) {
                JsonArray defaultableParameters = node.getAsJsonArray("defaultableParameters");
                for (int i = 0; i < defaultableParameters.size(); i++) {
                    defaultableParameters.get(i).getAsJsonObject().addProperty("defaultable", true);
                    defaultableParameters.get(i).getAsJsonObject().getAsJsonObject("variable")
                            .addProperty("defaultable", true);
                }
            }

            // Sort and add all the parameters.
            JsonArray allParamsTemp = node.getAsJsonArray("parameters");
            allParamsTemp.addAll(node.getAsJsonArray("defaultableParameters"));
            List<JsonElement> allParamElements = new ArrayList<>();
            allParamsTemp.forEach(jsonElement -> {
                allParamElements.add(jsonElement);
            });

            Collections.sort(allParamElements, (a, b) -> {
                int comparator = 0;
                comparator = (((a.getAsJsonObject().getAsJsonObject("position").get("endColumn").getAsInt() >
                        b.getAsJsonObject().getAsJsonObject("position").get("startColumn").getAsInt())
                        && (a.getAsJsonObject().getAsJsonObject("position").get("endLine").getAsInt() ==
                        b.getAsJsonObject().getAsJsonObject("position").get("endLine").getAsInt())) ||
                        (a.getAsJsonObject().getAsJsonObject("position").get("endLine").getAsInt() >
                                b.getAsJsonObject().getAsJsonObject("position").get("endLine").getAsInt())) ? 1 : -1;
                return comparator;
            });

            JsonArray allParams = new JsonArray();

            allParamElements.forEach(jsonElement -> {
                allParams.add(jsonElement);
            });

            node.add("allParams", allParams);

            if (node.has("receiver") &&
                    !node.getAsJsonObject("receiver").has("ws")) {
                JsonArray wss = node.getAsJsonObject("receiver").getAsJsonArray("ws");
                if (node.getAsJsonObject("receiver").has("typeNode")
                        && node.getAsJsonObject("receiver").getAsJsonObject("typeNode").has("ws")
                        && node.getAsJsonObject("receiver")
                        .getAsJsonObject("typeNode").getAsJsonArray("ws").size() > 0) {
                    for (JsonElement ws : wss) {
                        if (ws.getAsJsonObject().get("text").getAsString().equals("::")) {
                            node.addProperty("objectOuterFunction", true);
                            if (node.getAsJsonObject("receiver")
                                    .getAsJsonObject("typeNode").getAsJsonArray("ws").get(0).getAsJsonObject().get("text").getAsString().equals("function")) {
                                node.getAsJsonObject("receiver")
                                        .getAsJsonObject("typeNode").getAsJsonArray("ws").remove(0);
                            }
                            node.addProperty("objectOuterFunctionTypeName", node.getAsJsonObject("receiver")
                                    .getAsJsonObject("typeNode").get("typeName").getAsString());
                            break;
                        }
                    }
                } else {
                    node.addProperty("noVisibleReceiver", true);
                }
            }

            if (node.has("restParameters") &&
                    node.has("parameters") && node.getAsJsonArray("parameters").size() > 0) {
                node.addProperty("hasRestParams", true);
            }

            if (node.has("restParameters") &&
                    node.getAsJsonObject("restParameters").has("typeNode")) {
                node.getAsJsonObject("restParameters").getAsJsonObject("typeNode").addProperty("isRestParam", true);
            }
        }

        if (kind.equals("TypeDefinition") && node.has("typeNode")) {
            if (!node.has("ws")) {
                node.addProperty("notVisible", true);
            }

            if (node.has("name") &&
                    node.getAsJsonObject("name").get("value").getAsString().startsWith("$anonType$")) {
                this.anonTypes.put(node.getAsJsonObject("name").get("value").getAsString(),
                        node.getAsJsonObject("typeNode"));
            }

            if (node.getAsJsonObject("typeNode").get("kind").getAsString().equals("ObjectType")) {
                node.addProperty("isObjectType", true);
            }

            if (node.getAsJsonObject("typeNode").get("kind").getAsString().equals("RecordType")) {
                node.addProperty("isRecordType", true);
                if (node.has("ws")) {
                    for (int i = 0; i < node.getAsJsonArray("ws").size(); i++) {
                        if (node.getAsJsonArray("ws").get(i)
                                .getAsJsonObject().get("text").getAsString().equals("record")) {
                            node.addProperty("isRecordKeywordAvailable", true);
                        }
                    }
                }
            }
        }

        if (kind.equals("ObjectType")) {
            if (node.has("initFunction")) {
                if (!node.getAsJsonObject("initFunction").has("ws")) {
                    node.getAsJsonObject("initFunction").addProperty("defaultConstructor", true);
                } else {
                    node.getAsJsonObject("initFunction").addProperty("isConstructor", true);
                }
            }
        }

        if (kind.equals("RecordType")) {
            if (node.has("restFieldType")) {
                node.addProperty("isRestFieldAvailable", true);
            }
        }

        if (kind.equals("TypeInitExpr")) {
            if (node.getAsJsonArray("expressions").size() <= 0) {
                node.addProperty("noExpressionAvailable", true);
            }

            if (node.has("ws")) {
                for (int i = 0; i < node.getAsJsonArray("ws").size(); i++) {
                    if (node.getAsJsonArray("ws").get(i).getAsJsonObject().get("text").getAsString()
                            .equals("(")) {
                        node.addProperty("hasParantheses", true);
                        break;
                    }
                }
            }

            if (!node.has("type")) {
                node.addProperty("noTypeAttached", true);
            } else {
                node.add("typeName", node.getAsJsonObject("type").get("typeName"));
            }
        }

        if (kind.equals("Return")) {
            if (node.has("expression") &&
                    node.getAsJsonObject("expression").get("kind").getAsString().equals("Literal")) {
                if (node.getAsJsonObject("expression").get("value").getAsString().equals("()")) {
                    node.addProperty("noExpressionAvailable", true);
                }

                if (node.getAsJsonObject("expression").get("value").getAsString().equals("null")) {
                    node.getAsJsonObject("expression").addProperty("emptyParantheses", true);
                }
            }
        }

        if (kind.equals("Documentation")) {
            if (node.has("ws") && node.getAsJsonArray("ws").size() > 1) {
                node.addProperty("startDoc",
                        node.getAsJsonArray("ws").get(0).getAsJsonObject().get("text").getAsString());
            }

            for (int j = 0; j < node.getAsJsonArray("attributes").size(); j++) {
                JsonObject attribute = node.getAsJsonArray("attributes").get(j).getAsJsonObject();
                if (attribute.has("ws")) {
                    for (int i = 0; i < attribute.getAsJsonArray("ws").size(); i++) {
                        String text = attribute.getAsJsonArray("ws").get(i).getAsJsonObject()
                                .get("text").getAsString();
                        if (text.contains("{{") && !attribute.has("paramType")) {
                            int lastIndex = text.lastIndexOf("{{");
                            String paramType = text.substring(0, lastIndex);
                            String startCurl = text.substring(lastIndex, text.length());
                            attribute.getAsJsonArray("ws").get(i).getAsJsonObject()
                                    .addProperty("text", paramType);
                            attribute.addProperty("paramType", paramType);

                            JsonObject ws = new JsonObject();
                            ws.addProperty("text", startCurl);
                            ws.addProperty("ws", "");
                            ws.addProperty("static", false);
                            attribute.add("ws", addDataToArray(++i, ws, attribute.getAsJsonArray("ws")));
                        }
                    }
                }
            }
        }

        // Tag rest variable nodes
        if (kind.equals("Function") || kind.equals("Resource")) {
            if (node.has("restParameters")) {
                node.getAsJsonObject("restParameters").addProperty("rest", true);
            }
        }

        if (kind.equals("PostIncrement")) {
            node.addProperty("operator",
                    (node.get("operatorKind").getAsString() + node.get("operatorKind").getAsString()));
        }

        if (kind.equals("SelectExpression") && node.has("identifier")) {
            node.addProperty("identifierAvailable", true);
        }

        if (kind.equals("StreamAction") && node.has("invokableBody")) {
            if (node.getAsJsonObject("invokableBody").has("functionNode")) {
                node.getAsJsonObject("invokableBody").getAsJsonObject("functionNode")
                        .addProperty("isStreamAction", true);
            }
        }

        if (kind.equals("StreamingInput") && node.has("alias")) {
            node.addProperty("aliasAvailable", true);
        }

        if (kind.equals("IntRangeExpr")) {
            if (node.has("ws") && node.getAsJsonArray("ws").size() > 0) {
                if (node.getAsJsonArray("ws").get(0).getAsJsonObject().get("text")
                        .getAsString().equals("[")) {
                    node.addProperty("isWrappedWithBracket", true);
                } else if (node.getAsJsonArray("ws").get(0).getAsJsonObject().get("text")
                        .getAsString().equals("(")) {
                    node.addProperty("isWrappedWithParenthesis", true);
                }
            }
        }

        if (kind.equals("FunctionType")) {
            if (node.has("returnTypeNode")
                    && node.getAsJsonObject("returnTypeNode").has("ws")) {
                node.addProperty("hasReturn", true);
            }

            if (node.has("ws")
                    && node.getAsJsonArray("ws").size() > 0
                    && node.getAsJsonArray("ws").get(0).getAsJsonObject().get("text")
                    .getAsString().equals("(")) {
                node.addProperty("withParantheses", true);
            }
        }

        if (kind.equals("Literal") && parentKind.equals("StringTemplateLiteral")) {
            if (node.has("ws")
                    && node.getAsJsonArray("ws").size() == 1
                    && node.getAsJsonArray("ws").get(0).getAsJsonObject().has("text")) {
                node.addProperty("value",
                        node.getAsJsonArray("ws").get(0).getAsJsonObject().get("text").getAsString());
            }

            if ((node.get("value").getAsString().equals("nil")
                    || node.get("value").getAsString().equals("null"))
                    && node.has("ws")
                    && node.getAsJsonArray("ws").size() < 3
                    && node.getAsJsonArray("ws").get(0) != null
                    && node.getAsJsonArray("ws").get(0).getAsJsonObject().get("text")
                    .getAsString().equals("(")) {
                node.addProperty("emptyParantheses", true);
            }
        }

        if (kind.equals("Foreach")) {
            if (node.has("ws")) {
                for (JsonElement ws : node.getAsJsonArray("ws")) {
                    if (ws.getAsJsonObject().get("text").getAsString().equals("(")) {
                        node.addProperty("withParantheses", true);
                        break;
                    }
                }
            }
        }

        if (kind.equals("Endpoint")) {
            if (node.has("ws")) {
                for (JsonElement ws : node.getAsJsonArray("ws")) {
                    if (ws.getAsJsonObject().get("text").getAsString().equals("=")) {
                        node.addProperty("isConfigAssignment", true);
                        break;
                    }
                }
            }
        }

        if (kind.equals("UserDefinedType")) {
            if (node.has("ws") && node.has("nullable") && node.get("nullable").getAsBoolean()) {
                for (JsonElement ws : node.getAsJsonArray("ws")) {
                    if (ws.getAsJsonObject().get("text").getAsString().equals("?")) {
                        node.addProperty("nullableOperatorAvailable", true);
                        break;
                    }
                }
            }

            if (node.has("typeName") && node.getAsJsonObject("typeName").has("value")
                    && anonTypes.containsKey(node.getAsJsonObject("typeName").get("value").getAsString())) {
                node.addProperty("isAnonType", true);
                node.add("anonType",
                        anonTypes.get(node.getAsJsonObject("typeName").get("value").getAsString()));
                anonTypes.remove(node.getAsJsonObject("typeName").get("value").getAsString());
            }
        }

        if (kind.equals("ArrayType")) {
            if (node.getAsJsonArray("dimensions").size() > 0 && node.has("ws")) {
                String dimensionAsString = "";
                JsonObject startingBracket = null;
                JsonObject endingBracket = null;
                StringBuilder content = new StringBuilder();
                JsonArray ws = node.getAsJsonArray("ws");
                for (int j = 0; j < ws.size(); j++) {
                    if (ws.get(j).getAsJsonObject().get("text").getAsString().equals("[")) {
                        startingBracket = ws.get(j).getAsJsonObject();
                    } else if (ws.get(j).getAsJsonObject().get("text").getAsString().equals("]")) {
                        endingBracket = ws.get(j).getAsJsonObject();

                        dimensionAsString += startingBracket.get("text").getAsString() + content.toString()
                                + endingBracket.get("ws").getAsString()
                                + endingBracket.get("text").getAsString();

                        startingBracket = null;
                        endingBracket = null;
                        content = new StringBuilder();
                    } else if (startingBracket != null) {
                        content.append(ws.get(j).getAsJsonObject().get("ws").getAsString())
                                .append(ws.get(j).getAsJsonObject().get("text").getAsString());
                    }
                }

                node.addProperty("dimensionAsString", dimensionAsString);
            }
        }

        if (kind.equals("Block")
                && node.has("ws")
                && node.getAsJsonArray("ws").size() > 0
                && node.getAsJsonArray("ws").get(0)
                .getAsJsonObject().get("text").getAsString().equals("else")) {
            node.addProperty("isElseBlock", true);
        }

        if (kind.equals("FieldBasedAccessExpr")
                && node.has("ws")
                && node.getAsJsonArray("ws").size() > 0
                && node.getAsJsonArray("ws").get(0)
                .getAsJsonObject().get("text").getAsString().equals("!")) {
            node.addProperty("errorLifting", true);
        }

        if (kind.equals("StringTemplateLiteral")) {
            if (node.has("ws")
                    && node.getAsJsonArray("ws").size() > 0
                    && node.getAsJsonArray("ws").get(0).getAsJsonObject()
                    .get("text").getAsString().contains("string")
                    && node.getAsJsonArray("ws").get(0).getAsJsonObject()
                    .get("text").getAsString().contains("`")) {

                node.addProperty("startTemplate",
                        node.getAsJsonArray("ws").get(0).getAsJsonObject().get("text").getAsString());
                literalWSAssignForTemplates(1, 2, node.getAsJsonArray("expressions"),
                        node.getAsJsonArray("ws"), 2);
            }
        }

        if (kind.equals("XmlCommentLiteral") && node.has("ws")) {
            int length = node.getAsJsonArray("ws").size();
            for (int i = 0; i < length; i++) {
                if (node.getAsJsonArray("ws").get(i).getAsJsonObject()
                        .get("text").getAsString().contains("-->")
                        && node.getAsJsonArray("ws").get(i).getAsJsonObject()
                        .get("text").getAsString().length() > 3) {
                    JsonObject ws = new JsonObject();
                    ws.addProperty("text", "-->");
                    ws.addProperty("ws", "");

                    node.getAsJsonArray("ws").get(i).getAsJsonObject().addProperty("text",
                            node.getAsJsonArray("ws").get(i).getAsJsonObject()
                                    .get("text").getAsString().replace("-->", ""));
                    node.add("ws", addDataToArray(i + 1, ws, node.getAsJsonArray("ws")));
                    break;
                }
            }

            if (node.has("root") && node.get("root").getAsBoolean()) {
                literalWSAssignForTemplates(2, 3, node.getAsJsonArray("textFragments"),
                        node.getAsJsonArray("ws"), 4);
            } else {
                literalWSAssignForTemplates(1, 2, node.getAsJsonArray("textFragments"),
                        node.getAsJsonArray("ws"), 2);
            }
        }
    }

    @FindbugsSuppressWarnings
    void literalWSAssignForTemplates(int currentWs, int nextWs,
                                     JsonArray literals, JsonArray ws, int wsStartLocation) {
        if (literals.size() == (ws.size() - wsStartLocation)) {
            for (int i = 0; i < literals.size(); i++) {
                if (literals.get(i).getAsJsonObject().get("kind").getAsString().equals("Literal")) {
                    if (!literals.get(i).getAsJsonObject().has("ws")) {
                        literals.get(i).getAsJsonObject().add("ws", new JsonArray());
                    }

                    if (ws.get(currentWs).getAsJsonObject().get("text").getAsString().contains("{{")) {
                        literals.get(i).getAsJsonObject().get("ws").getAsJsonArray().add(ws.get(currentWs));
                        literals.get(i).getAsJsonObject().addProperty("value",
                                ws.get(currentWs).getAsJsonObject().get("text").getAsString());
                        // TODO: use splice
                        ws.remove(currentWs);
                        literals.get(i).getAsJsonObject().addProperty("startTemplateLiteral", true);

                    } else if (ws.get(currentWs).getAsJsonObject().get("text").getAsString().contains("}}")) {
                        literals.get(i).getAsJsonObject().get("ws").getAsJsonArray().add(ws.get(currentWs));
                        if (ws.get(nextWs).getAsJsonObject().get("text").getAsString().contains("{{")) {
                            literals.get(i).getAsJsonObject().get("ws").getAsJsonArray().add(ws.get(nextWs));
                            literals.get(i).getAsJsonObject().addProperty("value",
                                    ws.get(nextWs).getAsJsonObject().get("text").getAsString());
                            literals.get(i).getAsJsonObject().addProperty("startTemplateLiteral", true);
                            // TODO: use splice
                            ws.remove(nextWs);
                        }
                        // TODO: use splice
                        ws.remove(currentWs);
                        literals.get(i).getAsJsonObject().addProperty("endTemplateLiteral", true);
                    }

                    if (i == (literals.size() - 1)) {
                        literals.get(i).getAsJsonObject().get("ws").getAsJsonArray().add(ws.get(currentWs));
                        literals.get(i).getAsJsonObject().addProperty("value",
                                ws.get(currentWs).getAsJsonObject().get("text").getAsString());
                        literals.get(i).getAsJsonObject().addProperty("lastNodeValue", true);
                        // TODO: use splice.
                        ws.remove(currentWs);
                    }
                }
            }
        } else if ((literals.size() - 1) == (ws.size() - wsStartLocation)) {
            for (int i = 0; i < literals.size(); i++) {
                if (literals.get(i).getAsJsonObject().get("kind").getAsString().equals("Literal")) {
                    if (!literals.get(i).getAsJsonObject().has("ws")) {
                        literals.get(i).getAsJsonObject().add("ws", new JsonArray());
                    }

                    if (ws.get(currentWs).getAsJsonObject().get("text").getAsString().contains("{{")) {
                        literals.get(i).getAsJsonObject().get("ws").getAsJsonArray().add(ws.get(currentWs));
                        literals.get(i).getAsJsonObject().addProperty("value",
                                ws.get(currentWs).getAsJsonObject().get("text").getAsString());
                        //TODO: use splice.
                        ws.remove(currentWs);
                        literals.get(i).getAsJsonObject().addProperty("startTemplateLiteral", true);
                    } else if (ws.get(currentWs).getAsJsonObject().get("text").getAsString().contains("}}")) {
                        literals.get(i).getAsJsonObject().get("ws").getAsJsonArray().add(ws.get(currentWs));
                        if (ws.get(nextWs).getAsJsonObject().get("text").getAsString().contains("{{")) {
                            literals.get(i).getAsJsonObject().get("ws").getAsJsonArray().add(ws.get(nextWs));
                            literals.get(i).getAsJsonObject().addProperty("value",
                                    ws.get(nextWs).getAsJsonObject().get("text").getAsString());
                            literals.get(i).getAsJsonObject().addProperty("startTemplateLiteral", true);
                            //TODO: use splice.
                            ws.remove(nextWs);
                        }
                        //TODO: use splice.
                        ws.remove(currentWs);
                        literals.get(i).getAsJsonObject().addProperty("endTemplateLiteral", true);
                    }
                }
            }
        }
    }

    @FindbugsSuppressWarnings
    JsonArray addDataToArray(int index, JsonElement element, JsonArray ws) {
        int length = ws.size() + 1;
        JsonArray array = new JsonArray();
        boolean added = false;

        for (int i = 0; i < length; i++) {
            if (i == index) {
                array.add(element);
                added = true;
            } else if (added) {
                array.add(ws.get(i - 1));
            } else {
                array.add(ws.get(i));
            }
        }

        return array;
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
                                childItem.getAsJsonObject().has("lambda") &&
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

    @FindbugsSuppressWarnings
    class SourceGenParams {
        private boolean shouldIndent = false;
        private int i;
        private JsonArray ws;

        public SourceGenParams() {
            this.ws = new JsonArray();
            this.i = 0;
            this.shouldIndent = false;
        }

        public boolean isShouldIndent() {
            return shouldIndent;
        }

        public int getI() {
            return i;
        }

        public JsonArray getWs() {
            return ws;
        }

        public void setI(int i) {
            this.i = i;
        }

        public void setShouldIndent(boolean shouldIndent) {
            this.shouldIndent = shouldIndent;
        }

        public void setWs(JsonArray ws) {
            this.ws = ws;
        }
    }
}
