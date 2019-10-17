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
package org.ballerinalang.langserver.compiler.sourcegen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.ballerinalang.langserver.compiler.format.FormattingConstants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Temporary source gen class for formatting.
 */
public class FormattingSourceGen {
    private static Map<String, JsonObject> anonTypes = new HashMap<>();

    /**
     * process the given json before source generation.
     *
     * @param json       Current JSON AST node
     * @param parentKind Parent kind
     * @return {@link JsonObject} Processed AST node
     */
    public static JsonObject build(JsonObject json, String parentKind) {
        String kind = json.get("kind").getAsString();
        for (Map.Entry<String, JsonElement> child : json.entrySet()) {
            if (!child.getKey().equals("position") && !child.getKey().equals("ws")) {
                if (child.getValue().isJsonObject() &&
                        child.getValue().getAsJsonObject().get("kind") != null) {
                    json.add(child.getKey(), build(child.getValue().getAsJsonObject(), kind));
                } else if (child.getValue().isJsonArray()) {
                    JsonArray childArray = child.getValue().getAsJsonArray();
                    for (int j = 0; j < childArray.size(); j++) {
                        JsonElement childItem = childArray.get(j);
                        if ("CompilationUnit".equals(kind)
                                && childItem.getAsJsonObject().get("kind").getAsString().equals("Function")
                                && childItem.getAsJsonObject().has("lambda")
                                && childItem.getAsJsonObject().get("lambda").getAsBoolean()) {
                            childArray.remove(j);
                            j--;
                        } else if (childItem.isJsonObject()
                                && childItem.getAsJsonObject().get("kind") != null) {
                            build(childItem.getAsJsonObject(), kind);
                        }
                    }
                }
            }
        }
        modifyNode(json, parentKind);
        return json;
    }

    /**
     * Get the source of the given node.
     *
     * @param node Node to generate the ballerina source for
     * @return {@link String} Generated source string
     */
    public static String getSourceOf(JsonObject node) {
        StringBuilder sourceBuilder = new StringBuilder();
        for (JsonObject wsItem : extractWS(node)) {
            sourceBuilder.append(wsItem.get("ws").getAsString()).append(wsItem.get("text").getAsString());
        }
        return sourceBuilder.toString();
    }

    /**
     * Extract Whitespaces from the given node sorted by the token index.
     *
     * @param node node to extract the white spaces
     * @return {@link List} list of white spaces
     */
    public static List<JsonObject> extractWS(JsonObject node) {
        return unify(getSortedWSList(node));
    }

    /**
     * Reconcile whitespaces for the given node as to the attach point and the start index.
     *
     * @param node        Json Node to be added in to the node tree
     * @param attachPoint Attach point for the node
     * @param tree        whole Json AST tree
     * @param startIndex  Start index where the node to be added
     */
    public static void reconcileWS(JsonObject node, JsonArray attachPoint, JsonObject tree, int startIndex) {
        List<JsonObject> attachWS;
        List<JsonObject> nodeWS = extractWS(node);
        List<JsonObject> astWS = getSortedWSList(tree);

        if (startIndex == -1) {
            if (attachPoint.size() > 0) {
                attachWS = extractWS(attachPoint.get(attachPoint.size() - 1).getAsJsonObject());
                startIndex = attachWS.get(attachWS.size() - 1).get("i").getAsInt() + 1;
            } else {
                startIndex = 0;
            }
        }

        int nodeFirstIndex = nodeWS.get(0).get("i").getAsInt();
        int diff = startIndex - nodeFirstIndex;

        nodeWS.forEach(ws -> ws.addProperty("i", ws.get("i").getAsInt() + diff));

        int lastIndex = nodeWS.get(nodeWS.size() - 1).get("i").getAsInt();
        int treeDiff = (lastIndex - startIndex) + 1;

        for (JsonObject ws : astWS) {
            if (ws.get("i").getAsInt() >= startIndex) {
                ws.addProperty("i", ws.get("i").getAsInt() + treeDiff);
            }
        }
    }

    /**
     * Swap given two nodes' whitespace indexes.
     *
     * @param firstNode  first node to be indexed swapped
     * @param secondNode second node to be indexed swapped
     */
    public static void swapWSIndexes(JsonObject firstNode, JsonObject secondNode) {
        List<JsonObject> firstNodeWS = extractWS(firstNode);
        List<JsonObject> secondNodeWS = extractWS(secondNode);

        int firstNodeIndex = firstNodeWS.get(0).get("i").getAsInt();
        int secondNodeIndex = secondNodeWS.get(0).get("i").getAsInt();
        int diff = firstNodeIndex - secondNodeIndex;

        firstNodeWS.forEach(ws -> ws.addProperty("i", ws.get("i").getAsInt() - diff));
        String whitespace = firstNodeWS.get(0).get(FormattingConstants.WS).getAsString();

        int lastIndexOfFirstNode = firstNodeWS.get(firstNodeWS.size() - 1).get("i").getAsInt() + 1
                + (whitespace.length() > 0 ? 1 : 0);

        int diffToAddToSecondNode = lastIndexOfFirstNode - secondNodeIndex;

        secondNodeWS.forEach(ws -> ws.addProperty("i", ws.get("i").getAsInt() + diffToAddToSecondNode));
    }

    /**
     * Add new whitespace object to given target node on given start index
     * and then update the rest of the tree accordingly.
     *
     * @param targetNode target node where new WS to be added
     * @param tree       AST for the whole source file
     * @param ws         Whitespace
     * @param text       text in the whitespace
     * @param isStatic   set the Static state of the ws
     * @param startIndex start index for the whitespace
     */
    public static void addNewWS(JsonObject targetNode, JsonObject tree, String ws, String text, boolean isStatic,
                                int startIndex) {
        JsonObject newWS = new JsonObject();
        List<JsonObject> astWS = getSortedWSList(tree);
        List<JsonObject> nodeWS = extractWS(targetNode);

        if (startIndex == -1) {
            startIndex = nodeWS.get(nodeWS.size() - 1)
                    .getAsJsonObject().get("i").getAsInt();
        }

        newWS.addProperty("i", startIndex);
        newWS.addProperty("static", isStatic);
        newWS.addProperty(FormattingConstants.WS, ws);
        newWS.addProperty(FormattingConstants.TEXT, text);

        targetNode.getAsJsonArray(FormattingConstants.WS).add(newWS);

        for (JsonObject wsItem : astWS) {
            if (wsItem.get("i").getAsInt() >= startIndex) {
                wsItem.addProperty("i", wsItem.get("i").getAsInt() + 1);
            }
        }
    }

    /**
     * Get the start position suitable in the given attach point where new node to be added.
     *
     * @param node         Parent of the attach point
     * @param attachPoint  Name of the attach point where the new node to be added
     * @param insertBefore start position to add the new node above existing node
     * @return int start position suitable for the new node to be added
     */
    public static int getStartPosition(JsonObject node, String attachPoint, int insertBefore) {
        int startPosition = 0;
        int prevPosition;
        String kind = node.get("kind").getAsString();
        switch (kind) {
            case "CompilationUnit":
                if (node.has("topLevelNodes") && attachPoint.equals("imports")) {
                    JsonArray topLevelNodes = node.getAsJsonArray("topLevelNodes");

                    // Collect all the imports available in top level.
                    List<JsonObject> importsWS = new ArrayList<>();
                    for (int i = 0; i < topLevelNodes.size(); i++) {
                        if (topLevelNodes.get(i).getAsJsonObject().get("kind").getAsString().equals("Import")) {
                            importsWS.add(topLevelNodes.get(i).getAsJsonObject());
                        }
                    }

                    // Find the last position of the imports to add the new import.
                    prevPosition = 0;
                    for (JsonObject ws : importsWS) {
                        List<JsonObject> extractedWS = extractWS(ws);
                        int lastIndex = extractedWS.get(extractedWS.size() - 1).get("i").getAsInt();
                        if (prevPosition < lastIndex) {
                            prevPosition = lastIndex;
                        }
                    }

                    // Start position for the new import.
                    startPosition = prevPosition + 1;
                }
                break;
            case "Service":
            case "Function":
                JsonArray nodeWS = node.getAsJsonArray(FormattingConstants.WS);
                if (node.has("resource")
                        && node.get("resource").getAsBoolean() || kind.equals("Service")) {

                    JsonArray annotationAttachments = node.has("annotationAttachments")
                            ? node.getAsJsonArray("annotationAttachments")
                            : node.getAsJsonArray("annAttachments");
                    if ((node.has("annAttachments") || node.has("annotationAttachments"))
                            && attachPoint.equals("annAttachments")) {
                        startPosition = getCollectionStartPosition(annotationAttachments,
                                nodeWS.get(0).getAsJsonObject().get("i").getAsInt() - 1, insertBefore);
                    }

                    // TODO: handle calculation for the param start position.

                    JsonObject typeDefinition = kind.equals("Service") && node.has("typeDefinition")
                            && node.getAsJsonObject("typeDefinition").get("service").getAsBoolean()
                            ? node.getAsJsonObject("typeDefinition") : null;

                    if (typeDefinition != null && typeDefinition.has("typeNode")
                            && typeDefinition.getAsJsonObject("typeNode").has(FormattingConstants.WS)) {
                        JsonArray typeDefinitionWS = typeDefinition.getAsJsonObject("typeNode")
                                .getAsJsonArray(FormattingConstants.WS);
                        prevPosition = findOpeningBrace(typeDefinitionWS);
                    } else {
                        prevPosition = findOpeningBrace(nodeWS);
                    }

                    if (node.has("endpointNodes")) {
                        // If attachment point endpointNodes, calculate the position to add the new node in to endpoint
                        // Else calculate the position of the last whitespace token.
                        if (attachPoint.equals("endpointNodes")) {
                            startPosition = getCollectionStartPosition(node.getAsJsonArray("endpointNodes"),
                                    prevPosition, insertBefore);
                        } else if (node.getAsJsonArray("endpointNodes").size() > 0) {
                            List<JsonObject> endpointWS = extractWS(node.getAsJsonArray("endpointNodes")
                                    .get(node.getAsJsonArray("endpointNodes").size() - 1).getAsJsonObject());
                            prevPosition = endpointWS.get(endpointWS.size() - 1).get("i").getAsInt();
                        }
                    }

                    if (kind.equals("Service")) {
                        if (node.has("vars")) {
                            JsonArray vars = node.getAsJsonArray("vars");
                            if (attachPoint.equals("vars")) {
                                startPosition = getCollectionStartPosition(vars, prevPosition, insertBefore);
                            } else if (vars.size() > 0) {
                                List<JsonObject> varWS = extractWS(vars.get(vars.size() - 1).getAsJsonObject());
                                prevPosition = varWS.get(varWS.size() - 1).get("i").getAsInt();
                            }
                        }

                        if (node.has("resources")) {
                            JsonArray resources = node.getAsJsonArray("resources");
                            if (attachPoint.equals("resources")) {
                                startPosition = getCollectionStartPosition(resources, prevPosition, insertBefore);
                            } else if (resources.size() > 0) {
                                List<JsonObject> resourceWS = extractWS(resources.get(resources.size() - 1)
                                        .getAsJsonObject());
                                prevPosition = resourceWS.get(resourceWS.size() - 1).get("i").getAsInt();
                            }
                        }
                    }

                    if (kind.equals("Function") && node.has("resource")
                            && node.get("resource").getAsBoolean()) {
                        if (node.has("workers")) {
                            if (attachPoint.equals("workers")) {
                                startPosition = getCollectionStartPosition(node.getAsJsonArray("workers"),
                                        prevPosition, insertBefore);
                            } else if (node.getAsJsonArray("workers").size() > 0) {
                                List<JsonObject> workerWS = extractWS(node.getAsJsonArray("workers")
                                        .get(node.getAsJsonArray("workers").size() - 1).getAsJsonObject());
                                prevPosition = workerWS.get(workerWS.size() - 1).get("i").getAsInt();
                            }
                        }

                        if (node.has(FormattingConstants.BODY) && attachPoint.equals("statements")) {
                            startPosition = getCollectionStartPosition(node.getAsJsonObject(FormattingConstants.BODY)
                                    .getAsJsonArray(attachPoint), prevPosition, insertBefore);
                        }
                    }
                }
                break;
            default:
                if (node.has(FormattingConstants.WS)) {
                    startPosition = findOpeningBrace(node.getAsJsonArray(FormattingConstants.WS)) + 1;
                }
                break;
        }
        return startPosition;
    }

    private static List<JsonObject> getSortedWSList(JsonObject node) {
        List<JsonObject> wsCollection = new ArrayList<>();
        collectWSFromNode(node, wsCollection);
        wsCollection.sort(Comparator.comparingInt(a -> a.get("i").getAsInt()));
        return wsCollection;
    }

    private static List<JsonObject> unify(List<JsonObject> toBeUnified) {
        List<JsonObject> unified = new ArrayList<>();
        JsonObject prevWS = null;
        for (JsonObject wsItem : toBeUnified) {
            if (prevWS == null) {
                prevWS = wsItem;
                unified.add(prevWS);
            } else if (prevWS.get("i").getAsInt() != wsItem.get("i").getAsInt()
                    && !prevWS.equals(wsItem)) {
                unified.add(wsItem);
                prevWS = wsItem;
            }
        }

        return unified;
    }

    private static int getCollectionStartPosition(JsonArray collection, int entryPoint, int insertBefore) {
        int startPosition;
        if (collection.size() > 0) {
            startPosition = getPositionToInsertBefore(collection, insertBefore);
        } else {
            startPosition = entryPoint + 1;
        }
        return startPosition;
    }

    private static int getPositionToInsertBefore(JsonArray collection, int insertBefore) {
        int startPosition = -1;
        if (collection.size() > 0) {
            if (insertBefore == -1) {
                List<JsonObject> statementWS = extractWS(collection.get(collection.size() - 1).getAsJsonObject());
                startPosition = statementWS.get(statementWS.size() - 1).get("i").getAsInt() + 1;
            } else {
                List<JsonObject> statementWS = extractWS(collection.get(insertBefore).getAsJsonObject());
                startPosition = statementWS.get(statementWS.size() - 1).get("i").getAsInt();
            }
        }
        return startPosition;
    }

    private static int findOpeningBrace(JsonArray ws) {
        int index = -1;
        for (int i = 0; i < ws.size(); i++) {
            if (ws.get(i).getAsJsonObject().get(FormattingConstants.TEXT).getAsString().equals("{")) {
                index = ws.get(i).getAsJsonObject().get("i").getAsInt();
                break;
            }
        }

        return index;
    }

    private static void collectWSFromNode(JsonObject node, List<JsonObject> wsCollection) {
        if (!node.has("skip")) {
            for (Map.Entry<String, JsonElement> child : node.entrySet()) {
                String childName = child.getKey();
                if (!"position".equals(childName) && !"parent".equals(childName)) {
                    if (child.getValue().isJsonObject() && child.getValue().getAsJsonObject().has("kind")) {
                        collectWSFromNode(child.getValue().getAsJsonObject(), wsCollection);
                    } else if (child.getValue().isJsonArray()) {
                        if ("ws".equals(childName)) {
                            for (JsonElement wsElement : child.getValue().getAsJsonArray()) {
                                wsCollection.add(wsElement.getAsJsonObject());
                            }
                        } else {
                            for (JsonElement wsElement : child.getValue().getAsJsonArray()) {
                                if (wsElement.isJsonObject() && wsElement.getAsJsonObject().has("kind")) {
                                    collectWSFromNode(wsElement.getAsJsonObject(), wsCollection);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void modifyNode(JsonObject node, String parentKind) {
        String kind = node.get("kind").getAsString();

        if ("CompilationUnit".equals(kind)) {
            if (node.has("ws")) {
                JsonArray compilationUnitWS = node.getAsJsonArray(FormattingConstants.WS);
                if (compilationUnitWS.size() > 0) {
                    compilationUnitWS.get(compilationUnitWS.size() - 1).getAsJsonObject()
                            .addProperty(FormattingConstants.TEXT, "");
                }
            }
        }

        if ("If".equals(kind)) {
            if (node.getAsJsonObject("elseStatement") != null) {
                node.addProperty("ladderParent", true);
            }

            if (node.has("ws") && node.getAsJsonArray("ws").size() > 1
                    && node.getAsJsonArray("ws").get(0).getAsJsonObject().get("text")
                    .getAsString().equals("else")
                    && node.getAsJsonArray("ws").get(1).getAsJsonObject().get("text")
                    .getAsString().equals("if")) {
                node.addProperty("isElseIfBlock", true);
            }
        }

        if ("Transaction".equals(kind)
                && node.has("condition") && node.getAsJsonObject("condition").has("value")) {
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

        if (("XmlCommentLiteral".equals(kind) ||
                "XmlElementLiteral".equals(kind) ||
                "XmlTextLiteral".equals(kind) ||
                "XmlPiLiteral".equals(kind))
                && node.has("ws")
                && node.getAsJsonArray("ws").get(0) != null
                && node.getAsJsonArray("ws").get(0).getAsJsonObject().get("text")
                .getAsString().contains("xml")
                && node.getAsJsonArray("ws").get(0).getAsJsonObject().get("text")
                .getAsString().contains("`")) {
            node.addProperty("root", true);
            node.addProperty("startLiteral", node.getAsJsonArray("ws")
                    .get(0).getAsJsonObject().get("text").getAsString());
        }

        if ("XmlElementLiteral".equals(parentKind)
                || "XmlTextLiteral".equals(parentKind)
                || "XmlPiLiteral".equals(parentKind)) {
            node.addProperty("inTemplateLiteral", true);
        }

        if ("Annotation".equals(kind)
                && node.has("attachmentPoints")
                && node.getAsJsonArray("attachmentPoints").size() <= 0) {
            node.addProperty("noAttachmentPoints", true);
        }

        if ("Identifier".equals(kind)) {
            if (node.has("literal") && node.get("literal").getAsBoolean()) {
                // Re arrange unescaped delimiter identifiers.
                String[] words = node.get("value").getAsString().split(" ");
                StringBuilder valueWithBar = new StringBuilder();
                for (int i = 0; i < words.length; i++) {
                    if (i == 0) {
                        valueWithBar.append("'").append(words[i]);
                    } else {
                        if (words[i].equals("")) {
                            valueWithBar.append("\\").append(" ");
                        } else {
                            valueWithBar.append("\\ ").append(words[i]);
                        }
                    }
                }
                node.addProperty("valueWithBar", valueWithBar.toString());
            } else {
                node.addProperty("valueWithBar", node.get("value").getAsString());
            }
        }

        if ("Import".equals(kind)) {
            if (node.getAsJsonObject("alias") != null
                    && node.getAsJsonObject("alias").get("value") != null
                    && node.getAsJsonArray("packageName") != null
                    && node.getAsJsonArray("packageName").size() != 0
                    && !node.getAsJsonObject("alias").get("value").getAsString()
                    .equals(node.getAsJsonArray("packageName").get(node
                            .getAsJsonArray("packageName").size() - 1).getAsJsonObject()
                            .get("value").getAsString())) {
                node.addProperty("userDefinedAlias", true);
            }

            if ((node.getAsJsonArray("packageName") != null
                    && node.getAsJsonArray("packageName").size() == 2
                    && node.getAsJsonArray("packageName").get(0).getAsJsonObject().get("value")
                    .getAsString().equals("transactions")
                    && node.getAsJsonArray("packageName").get(1).getAsJsonObject()
                    .get("value").getAsString().equals("coordinator"))
                    || (node.getAsJsonObject("alias") != null
                    && node.getAsJsonObject("alias").get("value") != null
                    && node.getAsJsonObject("alias").get("value").getAsString().startsWith("."))) {
                node.addProperty("isInternal", true);
            }
        }

        if ("CompilationUnit".equals(parentKind) && ("Variable".equals(kind) || "Xmlns".equals(kind))) {
            node.addProperty("global", true);
        }

        if ("VariableDef".equals(kind)) {
            // TODO: refactor variable def whitespace.
            // Temporary remove variable def ws and add it to variable whitespaces.
            if (node.has("variable") && node.has(FormattingConstants.WS)
                    && node.getAsJsonObject("variable").has(FormattingConstants.WS)
                    && !(node.getAsJsonObject("variable").get("kind").getAsString().equals("TupleVariable")
                    || (node.getAsJsonObject("variable").has("symbolType")
                    && node.getAsJsonObject("variable").getAsJsonArray("symbolType").size() > 0
                    && node.getAsJsonObject("variable").getAsJsonArray("symbolType").get(0)
                    .getAsString().equals("service")))) {
                node.getAsJsonObject("variable").getAsJsonArray(FormattingConstants.WS)
                        .addAll(node.getAsJsonArray(FormattingConstants.WS));
                node.remove(FormattingConstants.WS);
            }

            if (node.getAsJsonObject("variable") != null
                    && node.getAsJsonObject("variable").getAsJsonObject("typeNode") != null
                    && node.getAsJsonObject("variable").getAsJsonObject("typeNode")
                    .get("kind").getAsString().equals("EndpointType")) {
                node.getAsJsonObject("variable").addProperty("endpoint", true);
                node.addProperty("endpoint", true);
            }

            // If variable def(worker) is in a fork but this is the node added to expand the scope
            // Skip generating the source from this node.
            if (node.has("isInFork") && node.get("isInFork").getAsBoolean()
                    && !parentKind.equals("ForkJoin")) {
                node.addProperty("skip", true);
            }

            // If variable def(worker) contains a invocation skip the generating source form this node.
            if (node.has("isWorker")
                    && node.get("isWorker").getAsBoolean()
                    && node.has("variable")
                    && node.getAsJsonObject("variable").has("initialExpression")
                    && node.getAsJsonObject("variable").getAsJsonObject("initialExpression").has("kind")
                    && node.getAsJsonObject("variable").getAsJsonObject("initialExpression").get("kind").getAsString()
                    .equals("Invocation")) {
                node.addProperty("skip", true);
            }
        }

        if ("Variable".equals(kind)) {
            if ("ObjectType".equals(parentKind)) {
                node.addProperty("inObject", true);
            }

            if (node.has("typeNode")
                    && node.getAsJsonObject("typeNode").has("isAnonType")
                    && node.getAsJsonObject("typeNode").get("isAnonType").getAsBoolean()) {
                node.addProperty("isAnonType", true);
            }

            if (node.has("initialExpression")) {
                node.getAsJsonObject("initialExpression").addProperty("isExpression", true);

                if (node.getAsJsonObject("initialExpression").has("async") &&
                        node.getAsJsonObject("initialExpression").get("async").getAsBoolean()
                        && node.has("ws")) {
                    JsonArray ws = node.getAsJsonArray("ws");
                    for (int i = 0; i < ws.size(); i++) {
                        if (ws.get(i).getAsJsonObject().get("text").getAsString().equals("start")
                                && node.getAsJsonObject("initialExpression").has("ws")) {
                            node.getAsJsonObject("initialExpression").add("ws",
                                    addDataToArray(0, node.getAsJsonArray("ws").get(i),
                                            node.getAsJsonObject("initialExpression")
                                                    .getAsJsonArray("ws")));
                            node.getAsJsonArray("ws").remove(i);
                        }
                    }
                }
            }

            if (node.has("typeNode")
                    && node.getAsJsonObject("typeNode").has("nullable")
                    && node.getAsJsonObject("typeNode").get("nullable").getAsBoolean()
                    && node.getAsJsonObject("typeNode").has("ws")) {
                JsonArray ws = node.getAsJsonObject("typeNode").get("ws").getAsJsonArray();
                for (int i = 0; i < ws.size(); i++) {
                    if (ws.get(i).getAsJsonObject().get("text").getAsString().equals("?")) {
                        node.getAsJsonObject("typeNode").addProperty("nullableOperatorAvailable", true);
                        break;
                    }
                }
            }

            if (node.has("typeNode")
                    && node.getAsJsonObject("typeNode").has("ws")
                    && !node.has("ws")) {
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

            if (node.has("service")
                    && node.get("service").getAsBoolean()
                    && node.has("noVisibleName")
                    && node.get("noVisibleName").getAsBoolean()) {
                node.addProperty("skip", true);
            }

            if (node.has("name")
                    && node.getAsJsonObject("name").get("value").getAsString().startsWith("0")) {
                node.addProperty("worker", true);
            }
        }

        if ("Service".equals(kind)) {
            if (!node.has("serviceTypeStruct")) {
                node.addProperty("isServiceTypeUnavailable", true);
            }

            if (node.has("resources")) {
                if (node.has("typeDefinition")) {
                    JsonObject typeDefinition = node.getAsJsonObject("typeDefinition");
                    JsonObject typeNode = typeDefinition.getAsJsonObject("typeNode");
                    if (typeNode.has("symbolType")
                            && typeNode.getAsJsonArray("symbolType").get(0)
                            .getAsString().equals("service")) {
                        JsonArray functions = typeNode.getAsJsonArray("functions");
                        for (JsonElement func : functions) {
                            JsonObject function = func.getAsJsonObject();
                            if (function.has("resource") && function.get("resource").getAsBoolean()) {
                                function.addProperty("skip", true);
                            }
                        }
                    }
                }
            }

            if (!node.has("anonymousEndpointBind")
                    && node.has("boundEndpoints")
                    && node.getAsJsonArray("boundEndpoints").size() <= 0) {
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

            if (node.has("userDefinedTypeNode")
                    && node.getAsJsonObject("userDefinedTypeNode").has(FormattingConstants.WS)) {
                node.getAsJsonObject("userDefinedTypeNode").remove(FormattingConstants.WS);
            }

            if (node.has("anonymousService")
                    && node.get("anonymousService").getAsBoolean()
                    && parentKind.equals("CompilationUnit")) {
                node.addProperty("skip", true);
            }
        }

        if ("Resource".equals(kind)
                && node.has("parameters")
                && node.getAsJsonArray("parameters").size() > 0
                && node.getAsJsonArray("parameters").get(0).getAsJsonObject().has("ws")) {
            for (JsonElement ws : node.getAsJsonArray("parameters")
                    .get(0).getAsJsonObject().getAsJsonArray("ws")) {
                if (ws.getAsJsonObject().get("text").getAsString().equals("endpoint")) {
                    JsonObject endpointParam = node.getAsJsonArray("parameters").get(0).getAsJsonObject();
                    String valueWithBar = endpointParam.get("name").getAsJsonObject().has("valueWithBar")
                            ? endpointParam.get("name").getAsJsonObject().get("valueWithBar").getAsString()
                            : endpointParam.get("name").getAsJsonObject().get("value").getAsString();

                    endpointParam.addProperty("serviceEndpoint", true);
                    endpointParam.get("name").getAsJsonObject().addProperty("value",
                            endpointParam.get("name").getAsJsonObject().get("value").getAsString()
                                    .replace("$", ""));
                    endpointParam.get("name").getAsJsonObject().addProperty("valueWithBar",
                            valueWithBar.replace("$", ""));
                    break;
                }
            }
        }

        if ("ForkJoin".equals(kind)) {
            if (node.getAsJsonObject("joinBody") != null) {
                node.getAsJsonObject("joinBody").add("position",
                        node.getAsJsonObject("joinResultVar").getAsJsonObject("position"));
            }

            if (node.getAsJsonObject("timeoutBody") != null) {
                node.getAsJsonObject("timeoutBody").add("position",
                        node.getAsJsonObject("timeOutExpression").getAsJsonObject("position"));
            }
        }

        if ("Match".equals(kind)) {
            if (node.has("structuredPatternClauses")) {
                JsonArray structuredPatternClauses = node.getAsJsonArray("structuredPatternClauses");
                for (JsonElement pattern : structuredPatternClauses) {
                    pattern.getAsJsonObject().addProperty("skip", true);
                }
            }

            if (node.has("staticPatternClauses")) {
                JsonArray staticPatternClauses = node.getAsJsonArray("staticPatternClauses");
                for (JsonElement pattern : staticPatternClauses) {
                    pattern.getAsJsonObject().addProperty("skip", true);
                }
            }
        }

        // Check if sorrounded by curlies
        if (("MatchStructuredPatternClause".equals(kind) || "MatchStaticPatternClause".equals(kind)
                || "MatchTypedPatternClause".equals(kind))
                && node.has("ws")) {

            for (JsonElement wsItem : node.getAsJsonArray(FormattingConstants.WS)) {
                JsonObject currentWS = wsItem.getAsJsonObject();
                String text = currentWS.get(FormattingConstants.TEXT).getAsString();
                if (text.equals("{")) {
                    node.addProperty("withCurlies", true);
                    break;
                }
            }
        }

        // Check if sorrounded by parantheses
        if ("ValueType".equals(kind)) {
            if (node.has("ws") && node.getAsJsonArray("ws").size() > 2) {
                node.addProperty("withParantheses", true);
            }

            if (node.has("typeKind")
                    && node.get("typeKind").getAsString().equals("nil")
                    && node.has("ws")) {
                node.addProperty("emptyParantheses", true);
            }

            if (node.has("nullable")
                    && node.get("nullable").getAsBoolean()
                    && node.has("ws")) {
                for (int i = 0; i < node.get("ws").getAsJsonArray().size(); i++) {
                    if (node.get("ws").getAsJsonArray().get(i)
                            .getAsJsonObject().get("text").getAsString().equals("?")) {
                        node.addProperty("nullableOperatorAvailable", true);
                        break;
                    }
                }
            }
        }

        if ("UnionTypeNode".equals(kind) && node.has("ws")) {
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

        if ("Function".equals(kind)) {
            if (node.has("returnTypeNode")
                    && node.getAsJsonObject("returnTypeNode").has("ws")
                    && node.getAsJsonObject("returnTypeNode").getAsJsonArray("ws").size() > 0) {
                node.addProperty("hasReturns", true);
            }

            if (node.has("defaultableParameters")) {
                JsonArray defaultableParameters = node.getAsJsonArray("defaultableParameters");
                for (int i = 0; i < defaultableParameters.size(); i++) {
                    defaultableParameters.get(i).getAsJsonObject().addProperty("defaultable", true);
                    defaultableParameters.get(i).getAsJsonObject().addProperty("param", true);
                    defaultableParameters.get(i).getAsJsonObject().getAsJsonObject("variable")
                            .addProperty("defaultable", true);
                }
            }

            if (node.has("parameters")) {
                JsonArray parameters = node.getAsJsonArray("parameters");
                for (int i = 0; i < parameters.size(); i++) {
                    parameters.get(i).getAsJsonObject().addProperty("param", true);
                }
            }

            if (node.has("parameters")
                    && node.has("defaultableParameters")) {
                // Sort and add all the parameters.
                JsonArray allParamsTemp = node.getAsJsonArray("parameters");
                allParamsTemp.addAll(node.getAsJsonArray("defaultableParameters"));
                List<JsonElement> allParamElements = new ArrayList<>();
                allParamsTemp.forEach(allParamElements::add);

                allParamElements.sort((a, b) -> {
                    int comparator = 0;
                    comparator = (((a.getAsJsonObject().getAsJsonObject("position").get("endColumn").getAsInt() >
                            b.getAsJsonObject().getAsJsonObject("position").get("startColumn").getAsInt())
                            && (a.getAsJsonObject().getAsJsonObject("position").get("endLine").getAsInt() ==
                            b.getAsJsonObject().getAsJsonObject("position").get("endLine").getAsInt())) ||
                            (a.getAsJsonObject().getAsJsonObject("position").get("endLine").getAsInt() >
                                    b.getAsJsonObject().getAsJsonObject("position").get("endLine").getAsInt()))
                            ? 1 : -1;
                    return comparator;
                });

                JsonArray allParams = new JsonArray();
                allParamElements.forEach(allParams::add);
                node.add("allParams", allParams);
            }

            if (node.has("receiver")
                    && !node.getAsJsonObject("receiver").has("ws")) {
                if (node.getAsJsonObject("receiver").has("typeNode")
                        && node.getAsJsonObject("receiver").getAsJsonObject("typeNode").has("ws")
                        && node.getAsJsonObject("receiver")
                        .getAsJsonObject("typeNode").getAsJsonArray("ws").size() > 0) {
                    for (JsonElement ws : node.get("ws").getAsJsonArray()) {
                        if (ws.getAsJsonObject().get("text").getAsString().equals("::")) {
                            node.addProperty("objectOuterFunction", true);
                            if (node.getAsJsonObject("receiver")
                                    .getAsJsonObject("typeNode").getAsJsonArray("ws").get(0)
                                    .getAsJsonObject().get("text").getAsString().equals("function")) {
                                node.getAsJsonObject("receiver")
                                        .getAsJsonObject("typeNode").getAsJsonArray("ws").remove(0);
                            }
                            node.add("objectOuterFunctionTypeName", node.getAsJsonObject("receiver")
                                    .getAsJsonObject("typeNode").getAsJsonObject("typeName"));
                            break;
                        }
                    }
                } else {
                    node.addProperty("noVisibleReceiver", true);
                }
            }

            if (node.has("restParameters")
                    && (node.has("allParams")
                    && node.getAsJsonArray("allParams").size() > 0)) {
                node.addProperty("hasRestParams", true);
            }

            if (node.has("restParameters")) {
                node.getAsJsonObject("restParameters").addProperty("param", true);
                if (node.getAsJsonObject("restParameters").has("typeNode")) {
                    node.getAsJsonObject("restParameters").getAsJsonObject("typeNode")
                            .addProperty("isRestParam", true);
                }
            }
        }

        if ("TypeDefinition".equals(kind) && node.has("typeNode")) {
            if (!node.has("ws")) {
                node.addProperty("notVisible", true);
            }

            if (node.has("name") &&
                    node.getAsJsonObject("name").get("value").getAsString().startsWith("$anonType$")) {
                anonTypes.put(node.getAsJsonObject("name").get("value").getAsString(),
                        node.getAsJsonObject("typeNode"));
            }

            if (node.getAsJsonObject("typeNode").get("kind").getAsString().equals("ObjectType")) {
                node.addProperty("isObjectType", true);
                if (node.has("ws")) {
                    JsonArray typeDefWS = node.getAsJsonArray("ws");
                    for (int i = 0; i < typeDefWS.size(); i++) {
                        if (typeDefWS.get(i).getAsJsonObject().get("text").getAsString().equals("abstract")) {
                            node.addProperty("isAbstractKeywordAvailable", true);
                        }
                    }
                }
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

            if (node.has("service") && node.get("service").getAsBoolean()
                    && parentKind.equals("CompilationUnit")) {
                node.addProperty("skip", true);
            }
        }

        if ("ObjectType".equals(kind) && node.has("initFunction")) {
            if (!node.getAsJsonObject("initFunction").has("ws")) {
                node.getAsJsonObject("initFunction").addProperty("defaultConstructor", true);
            } else {
                node.getAsJsonObject("initFunction").addProperty("isConstructor", true);
            }
        }

        if ("RecordType".equals(kind) && node.has("restFieldType")) {
            node.addProperty("isRestFieldAvailable", true);
        }

        if ("TypeInitExpr".equals(kind)) {
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

        if ("Return".equals(kind) && node.has("expression")) {
            if (node.getAsJsonObject("expression").get("kind").getAsString().equals("Literal")) {
                if (node.getAsJsonObject("expression").get("value").getAsString().equals("()")) {
                    node.addProperty("noExpressionAvailable", true);
                }

                if (node.getAsJsonObject("expression").get("value").getAsString().equals("null")) {
                    node.getAsJsonObject("expression").addProperty("emptyParantheses", true);
                }
            }
            node.getAsJsonObject("expression").addProperty("isExpression", "true");
        }

        if ("Documentation".equals(kind)) {
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
                            attribute.add("ws", addDataToArray(++i, ws,
                                    attribute.getAsJsonArray("ws")));
                        }
                    }
                }
            }
        }

        // Tag rest variable nodes
        if (("Function".equals(kind) || "Resource".equals(kind)) && node.has("restParameters")) {
            node.getAsJsonObject("restParameters").addProperty("rest", true);
        }

        if ("PostIncrement".equals(kind)) {
            node.addProperty("operator",
                    (node.get("operatorKind").getAsString() + node.get("operatorKind").getAsString()));
        }

        if ("SelectExpression".equals(kind) && node.has("identifier")) {
            node.addProperty("identifierAvailable", true);
        }

        if ("StreamAction".equals(kind) && node.has("invokableBody") &&
                node.getAsJsonObject("invokableBody").has("functionNode")) {
            node.getAsJsonObject("invokableBody").getAsJsonObject("functionNode")
                    .addProperty("isStreamAction", true);
        }

        if ("StreamingInput".equals(kind) && node.has("alias")) {
            node.addProperty("aliasAvailable", true);
        }

        if ("IntRangeExpr".equals(kind)
                && node.has("ws")
                && node.getAsJsonArray("ws").size() > 0) {
            if (node.getAsJsonArray("ws").get(0).getAsJsonObject().get("text")
                    .getAsString().equals("[")) {
                node.addProperty("isWrappedWithBracket", true);
            } else if (node.getAsJsonArray("ws").get(0).getAsJsonObject().get("text")
                    .getAsString().equals("(")) {
                node.addProperty("isWrappedWithParenthesis", true);
            }
        }

        if ("FunctionType".equals(kind)) {
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

        if ("Literal".equals(kind) && !"StringTemplateLiteral".equals(parentKind)) {
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

        if ("Foreach".equals(kind) && node.has("ws")) {
            for (JsonElement ws : node.getAsJsonArray("ws")) {
                if (ws.getAsJsonObject().get("text").getAsString().equals("(")) {
                    node.addProperty("withParantheses", true);
                    break;
                }
            }
        }

        if ("Endpoint".equals(kind) && node.has("ws")) {
            for (JsonElement ws : node.getAsJsonArray("ws")) {
                if (ws.getAsJsonObject().get("text").getAsString().equals("=")) {
                    node.addProperty("isConfigAssignment", true);
                    break;
                }
            }
        }

        if ("UserDefinedType".equals(kind)) {
            if (node.has("ws")
                    && node.has("nullable")
                    && node.get("nullable").getAsBoolean()) {
                for (JsonElement ws : node.getAsJsonArray("ws")) {
                    if (ws.getAsJsonObject().get("text").getAsString().equals("?")) {
                        node.addProperty("nullableOperatorAvailable", true);
                        break;
                    }
                }
            }

            if (node.has("typeName")
                    && node.getAsJsonObject("typeName").has("value")
                    && anonTypes.containsKey(node.getAsJsonObject("typeName").get("value").getAsString())) {
                node.addProperty("isAnonType", true);
                JsonObject anonType = anonTypes.get(node.getAsJsonObject("typeName").get("value").getAsString());
                anonType.addProperty("isAnonType", true);
                node.add("anonType", anonType);
                anonTypes.remove(node.getAsJsonObject("typeName").get("value").getAsString());
            }
        }

        if ("ArrayType".equals(kind)
                && node.has("dimensions")
                && node.get("dimensions").getAsInt() > 0
                && node.has("ws")) {
            StringBuilder dimensionAsString = new StringBuilder();
            JsonObject startingBracket = null;
            JsonObject endingBracket;
            StringBuilder content = new StringBuilder();
            JsonArray ws = node.getAsJsonArray("ws");

            for (int j = 0; j < ws.size(); j++) {
                if (ws.get(j).getAsJsonObject().get("text").getAsString().equals("[")) {
                    startingBracket = ws.get(j).getAsJsonObject();
                } else if (ws.get(j).getAsJsonObject().get("text").getAsString().equals("]")) {
                    endingBracket = ws.get(j).getAsJsonObject();

                    dimensionAsString.append(startingBracket.get("text").getAsString()).append(content.toString())
                            .append(endingBracket.get("ws").getAsString()).append(endingBracket.get("text")
                            .getAsString());

                    startingBracket = null;
                    content = new StringBuilder();
                } else if (startingBracket != null) {
                    content.append(ws.get(j).getAsJsonObject().get("ws").getAsString())
                            .append(ws.get(j).getAsJsonObject().get("text").getAsString());
                }
            }

            node.addProperty("dimensionAsString", dimensionAsString.toString());
        }

        if ("Block".equals(kind)
                && node.has("ws")
                && node.getAsJsonArray("ws").size() > 0
                && node.getAsJsonArray("ws").get(0)
                .getAsJsonObject().get("text").getAsString().equals("else")) {
            node.addProperty("isElseBlock", true);
        }

        if ("FieldBasedAccessExpr".equals(kind)
                && node.has("ws")
                && node.getAsJsonArray("ws").size() > 0
                && node.getAsJsonArray("ws").get(0)
                .getAsJsonObject().get("text").getAsString().equals("!")) {
            node.addProperty("errorLifting", true);
        }

        if ("StringTemplateLiteral".equals(kind) && node.has("ws")
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

        if ("ArrowExpr".equals(kind)) {
            if (node.has("ws") && node.getAsJsonArray("ws").size() > 0
                    && node.getAsJsonArray("ws").get(0).getAsJsonObject().get("text")
                    .getAsString().equals("(")) {
                node.addProperty("hasParantheses", true);
            }

            if (node.has("parameters")) {
                JsonArray parameters = node.getAsJsonArray("parameters");
                for (int i = 0; i < parameters.size(); i++) {
                    JsonObject parameter = parameters.get(i).getAsJsonObject();
                    parameter.addProperty("arrowExprParam", true);
                }
            }
        }

        if ("PatternStreamingInput".equals(kind)
                && node.has("ws")
                && node.getAsJsonArray("ws").get(0)
                .getAsJsonObject().get("text").getAsString().equals("(")) {
            node.addProperty("enclosedInParenthesis", true);
        }

        if ("SelectClause".equals(kind) && !node.has("ws")) {
            node.addProperty("notVisible", true);
        }

        if ("OrderByVariable".equals(kind)) {
            if (!node.has("ws")) {
                node.addProperty("noVisibleType", true);
            } else {
                node.addProperty("typeString", node.getAsJsonArray("ws")
                        .get(0).getAsJsonObject().get("text").getAsString());
            }
        }

        if ("Deprecated".equals(kind)
                && node.has("ws")
                && node.getAsJsonArray("ws").size() > 0) {
            node.addProperty("deprecatedStart",
                    node.getAsJsonArray("ws").get(0).getAsJsonObject().get("text").getAsString());
        }

        if ("TypedescExpression".equals(kind)
                && node.has("ws")
                && node.getAsJsonArray("ws").size() > 0) {
            JsonArray typeDescWS = node.getAsJsonArray("ws");
            if (typeDescWS.get(0).getAsJsonObject().get("text").getAsString().equals("object")) {
                node.addProperty("isObject", true);
            } else if (typeDescWS.get(0).getAsJsonObject().get("text").getAsString().equals("record")) {
                node.addProperty("isRecord", true);
            }
        }

        if ("CompoundAssignment".equals(kind)
                && node.has("ws")
                && node.getAsJsonArray("ws").size() > 0) {
            node.addProperty("compoundOperator",
                    node.getAsJsonArray("ws").get(0).getAsJsonObject().get("text").getAsString());
        }

        if ("Assignment".equals(kind) && node.has("expression")) {
            node.getAsJsonObject("expression").addProperty("isExpression", true);
        }
    }

    private static void literalWSAssignForTemplates(int currentWs, int nextWs,
                                                    JsonArray literals, JsonArray ws, int wsStartLocation) {
        if (literals.size() == (ws.size() - wsStartLocation)) {
            for (int i = 0; i < literals.size(); i++) {
                if (literals.get(i).getAsJsonObject().get("kind").getAsString().equals("Literal")) {
                    if (!literals.get(i).getAsJsonObject().has("ws")) {
                        literals.get(i).getAsJsonObject().add("ws", new JsonArray());
                    }

                    stringTemplateSourceFromWS(currentWs, nextWs, literals, ws, i);

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

                    stringTemplateSourceFromWS(currentWs, nextWs, literals, ws, i);
                }
            }
        }
    }

    private static void stringTemplateSourceFromWS(int currentWs, int nextWs, JsonArray literals, JsonArray ws,
                                                   int i) {
        if (ws.get(currentWs).getAsJsonObject().get("text").getAsString().contains("${")) {
            literals.get(i).getAsJsonObject().get("ws").getAsJsonArray().add(ws.get(currentWs));
            literals.get(i).getAsJsonObject().addProperty("value",
                    ws.get(currentWs).getAsJsonObject().get("text").getAsString());
            // TODO: use splice
            ws.remove(currentWs);
            literals.get(i).getAsJsonObject().addProperty("startTemplateLiteral", true);

        } else if (ws.get(currentWs).getAsJsonObject().get("text").getAsString().contains("}")) {
            literals.get(i).getAsJsonObject().get("ws").getAsJsonArray().add(ws.get(currentWs));
            if (ws.get(nextWs).getAsJsonObject().get("text").getAsString().contains("${")) {
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
    }

    private static JsonArray addDataToArray(int index, JsonElement element, JsonArray ws) {
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
}
