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
            
        case "PackageDeclaration":
            return dent() + w("") + "package" + a(" ")
                 + join(node.getAsJsonArray("packageName"), pretty, replaceLambda, w(""), ".", false) + w("")
                 + ";" + a("");
        case "Import":
            if (node.get("userDefinedAlias") != null
                         && node.get("userDefinedAlias").getAsBoolean() && node.get("packageName") != null
                         && node.getAsJsonObject("alias").get("valueWithBar") != null
                         && !node.getAsJsonObject("alias").get("valueWithBar").getAsString().isEmpty()) {
                return dent() + w("") + "import" + a(" ")
                 + join(node.getAsJsonArray("packageName"), pretty, replaceLambda, w(""), ".", false) + w(" ")
                 + "as" + a("") + w(" ")
                 + node.getAsJsonObject("alias").get("valueWithBar").getAsString() + a("") + w("") + ";" + a("");
            } else {
                return dent() + w("") + "import" + a(" ")
                 + join(node.getAsJsonArray("packageName"), pretty, replaceLambda, w(""), ".", false) + w("")
                 + ";" + a("");
            }
        case "Identifier":
            return w("") + node.get("valueWithBar").getAsString() + a("");
        case "Abort":
            return dent() + w("") + "abort" + a("") + w("") + ";" + a("");
        case "Action":
            if (node.get("annotationAttachments") != null
                    && node.get("documentationAttachments") != null && node.get("deprecatedAttachments") != null
                    && node.getAsJsonObject("name").get("valueWithBar") != null
                    && !node.getAsJsonObject("name").get("valueWithBar").getAsString().isEmpty()
                    && node.get("parameters") != null && node.get("returnParameters") != null
                    && node.getAsJsonArray("returnParameters").size() > 0 && node.get("body") != null
                    && node.get("workers") != null) {
                return join(node.getAsJsonArray("annotationAttachments"), pretty, replaceLambda, w(""), null, false)
                        + dent() + join(node.getAsJsonArray("documentationAttachments"), pretty, replaceLambda, w(""),
                        null, false) + join(node.getAsJsonArray("deprecatedAttachments"), pretty, replaceLambda, w(""),
                        null, false) + w("") + "action" + a("") + w(" ") + node.getAsJsonObject("name")
                        .get("valueWithBar").getAsString() + a("") + w(" ") + "(" + a("") + join(
                        node.getAsJsonArray("parameters"), pretty, replaceLambda, w(""), ",", false) + w("") + ")" + a(
                        "") + w(" ") + "(" + a("") + join(node.getAsJsonArray("returnParameters"), pretty,
                        replaceLambda, w(""), ",", false) + w("") + ")" + a("") + w(" ") + "{" + a("") + indent() + a(
                        "") + getSourceOf(node.getAsJsonObject("body"), pretty, replaceLambda) + join(
                        node.getAsJsonArray("workers"), pretty, replaceLambda, w(""), null, false) + outdent() + w("")
                        + "}" + a("");
            } else {
                return join(node.getAsJsonArray("annotationAttachments"), pretty, replaceLambda, w(""), null, false)
                        + dent() + join(node.getAsJsonArray("documentationAttachments"), pretty, replaceLambda, w(""),
                        null, false) + join(node.getAsJsonArray("deprecatedAttachments"), pretty, replaceLambda, w(""),
                        null, false) + w("") + "action" + a("") + w(" ") + node.getAsJsonObject("name")
                        .get("valueWithBar").getAsString() + a("") + w(" ") + "(" + a("") + join(
                        node.getAsJsonArray("parameters"), pretty, replaceLambda, w(""), ",", false) + w("") + ")" + a(
                        "") + w(" ") + "{" + a("") + indent() + a("") + getSourceOf(node.getAsJsonObject("body"),
                        pretty, replaceLambda) + join(node.getAsJsonArray("workers"), pretty, replaceLambda, w(""),
                        null, false) + outdent() + w("") + "}" + a("");
            }
        case "Annotation":
            return dent() + join(node.getAsJsonArray("annotationAttachments"), pretty, replaceLambda, w(""), null,
                    false) + w("") + "annotation" + a("") + w(" ") + "<" + a("") + join(
                    node.getAsJsonArray("attachmentPoints"), pretty, replaceLambda, w(""), ",", false) + w("") + ">"
                    + a("") + w(" ") + node.getAsJsonObject("name").get("valueWithBar").getAsString() + a(" ") + a(" ")
                    + getSourceOf(node.getAsJsonObject("typeNode"), pretty, replaceLambda) + w("") + ";" + a("");
        case "AnnotationAttachment":
            if (node.get("builtin") != null && node.get("builtin").getAsBoolean()
                    && node.getAsJsonObject("annotationName").get("valueWithBar") != null && !node
                    .getAsJsonObject("annotationName").get("valueWithBar").getAsString().isEmpty()
                    && node.get("expression") != null) {
                return w("") + "@" + a("") + w("") + node.getAsJsonObject("annotationName").get("valueWithBar")
                        .getAsString() + a("") + a(" ") + getSourceOf(node.getAsJsonObject("expression"), pretty,
                        replaceLambda);
            } else if (node.getAsJsonObject("packageAlias").get("valueWithBar") != null && !node
                    .getAsJsonObject("packageAlias").get("valueWithBar").getAsString().isEmpty()
                    && node.getAsJsonObject("annotationName").get("valueWithBar") != null && !node
                    .getAsJsonObject("annotationName").get("valueWithBar").getAsString().isEmpty()
                    && node.get("expression") != null) {
                return w("") + "@" + a("") + w("") + node.getAsJsonObject("packageAlias").get("valueWithBar")
                        .getAsString() + a("") + w("") + ":" + a("") + w("") + node.getAsJsonObject("annotationName")
                        .get("valueWithBar").getAsString() + a("") + a(" ") + getSourceOf(
                        node.getAsJsonObject("expression"), pretty, replaceLambda);
            } else {
                return w("") + "@" + a("") + w("")
                 + node.getAsJsonObject("annotationName").get("valueWithBar").getAsString() + a("") + a(" ")
                 + getSourceOf(node.getAsJsonObject("expression"), pretty, replaceLambda);
            }
        case "ArrayLiteralExpr":
            return w("") + "[" + a("") + join(node.getAsJsonArray("expressions"), pretty, replaceLambda, w(""), ",",
                    false) + w("") + "]" + a("");
        case "Assignment":
            return dent() + (node.get("declaredWithVar").getAsBoolean() ? w("") + "var" + a(" ") : "") + join(
                    node.getAsJsonArray("variables"), pretty, replaceLambda, w(""), ",", false) + w(" ") + "=" + a(" ")
                    + a("") + getSourceOf(node.getAsJsonObject("expression"), pretty, replaceLambda) + w("") + ";" + a(
                    "");
        case "BinaryExpr":
            if (node.get("inTemplateLiteral") != null
                         && node.get("inTemplateLiteral").getAsBoolean() && node.get("leftExpression") != null
                         && node.get("operatorKind") != null
                         && node.get("rightExpression") != null) {
                return w("") + "{{" + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("leftExpression"), pretty, replaceLambda) + w(" ")
                 + node.get("operatorKind").getAsString() + a(" ") + a("")
                 + getSourceOf(node.getAsJsonObject("rightExpression"), pretty, replaceLambda) + w("")
                 + "}}" + a("");
            } else {
                return a("")
                 + getSourceOf(node.getAsJsonObject("leftExpression"), pretty, replaceLambda) + w(" ")
                 + node.get("operatorKind").getAsString() + a(" ") + a("")
                 + getSourceOf(node.getAsJsonObject("rightExpression"), pretty, replaceLambda);
            }
        case "Bind":
            return dent() + w("") + "bind" + a("") + a(" ")
                 + getSourceOf(node.getAsJsonObject("expression"), pretty, replaceLambda) + w(" ")
                 + "with" + a("") + a(" ")
                 + getSourceOf(node.getAsJsonObject("variable"), pretty, replaceLambda) + w("") + ";" + a("");
        case "Block":
            return join(node.getAsJsonArray("statements"), pretty, replaceLambda, w(""), null, false);
        case "Break":
            return dent() + w("") + "break" + a("") + w("") + ";" + a("");
        case "BuiltInRefType":
            return w("") + node.get("typeKind").getAsString() + a("");
        case "Catch":
            return dent() + w("") + "catch" + a("") + w("") + "(" + a("") + a("") + getSourceOf(
                    node.getAsJsonObject("parameter"), pretty, replaceLambda) + w("") + ")" + a("") + w(" ") + "{" + a(
                    "") + indent() + a("") + getSourceOf(node.getAsJsonObject("body"), pretty, replaceLambda)
                    + outdent() + w("") + "}" + a("");
        case "Comment":
            return dent() + w("") + node.get("comment").getAsString() + a("");
        case "Connector":
            if (node.get("annotationAttachments") != null && node.get("documentationAttachments") != null
                    && node.get("deprecatedAttachments") != null
                    && node.getAsJsonObject("name").get("valueWithBar") != null && !node.getAsJsonObject("name")
                    .get("valueWithBar").getAsString().isEmpty() && node.get("parameters") != null
                    && node.get("variableDefs") != null && node.get("actions") != null) {
                return join(node.getAsJsonArray("annotationAttachments"), pretty, replaceLambda, w(""), null, false)
                        + dent() + join(node.getAsJsonArray("documentationAttachments"), pretty, replaceLambda, w(""),
                        null, false) + join(node.getAsJsonArray("deprecatedAttachments"), pretty, replaceLambda, w(""),
                        null, false) + (node.get("public").getAsBoolean() ? w("") + "public" + a(" ") : "") + w("")
                        + "connector" + a("") + w(" ") + node.getAsJsonObject("name").get("valueWithBar").getAsString()
                        + a("") + w(" ") + "(" + a("") + join(node.getAsJsonArray("parameters"), pretty, replaceLambda,
                        w(""), ",", false) + w("") + ")" + a("") + w(" ") + "{" + a("") + indent() + join(
                        node.getAsJsonArray("variableDefs"), pretty, replaceLambda, w(""), null, false) + join(
                        node.getAsJsonArray("actions"), pretty, replaceLambda, w(""), null, false) + outdent() + w("")
                        + "}" + a("");
            } else {
                return join(node.getAsJsonArray("annotationAttachments"), pretty, replaceLambda, w(""), null, false)
                        + dent() + join(node.getAsJsonArray("documentationAttachments"), pretty, replaceLambda, w(""),
                        null, false) + join(node.getAsJsonArray("deprecatedAttachments"), pretty, replaceLambda, w(""),
                        null, false) + (node.get("public").getAsBoolean() ? w("") + "public" + a(" ") : "") + w("")
                        + "connector" + a("") + w(" ") + node.getAsJsonObject("name").get("valueWithBar").getAsString()
                        + a("") + w(" ") + "(" + a("") + join(node.getAsJsonArray("parameters"), pretty, replaceLambda,
                        w(""), ",", false) + w("") + ")" + a("") + w(" ") + "{" + a("") + indent() + join(
                        node.getAsJsonArray("actions"), pretty, replaceLambda, w(""), null, false) + outdent() + w("")
                        + "}" + a("");
            }
        case "ConnectorInitExpr":
            if (node.get("connectorType") != null && node.get("expressions") != null) {
                return dent() + w("") + "create" + a("") + a(" ") + getSourceOf(node.getAsJsonObject("connectorType"),
                        pretty, replaceLambda) + w("") + "(" + a("") + join(node.getAsJsonArray("expressions"), pretty,
                        replaceLambda, w(""), ",", false) + w("") + ")" + a("");
            } else {
                return dent() + w("") + "create" + a("") + a(" ") + getSourceOf(node.getAsJsonObject("connectorType"),
                        pretty, replaceLambda) + w("") + "(" + a("") + w("") + ")" + a("");
            }
        case "ConstrainedType":
            return a("")
                 + getSourceOf(node.getAsJsonObject("type"), pretty, replaceLambda) + w("") + "<" + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("constraint"), pretty, replaceLambda) + w("") + ">"
                 + a("");
        case "Documentation":
            return dent() + w("") + "documentation" + a(" ") + w("") + "{" + a("")
                 + indent() + w("") + node.get("documentationText").getAsString()
                 + a("") + outdent() + w("") + "}" + a("");
        case "Deprecated":
            return dent() + w("") + "deprecated" + a(" ") + w("") + "{" + a("")
                 + indent() + w("") + node.get("documentationText").getAsString()
                 + a("") + outdent() + w("") + "}" + a("");
        case "EndpointType":
            return w("") + "<" + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("constraint"), pretty, replaceLambda) + w("") + ">" + a("");
        case "ExpressionStatement":
            return dent() + a("")
                 + getSourceOf(node.getAsJsonObject("expression"), pretty, replaceLambda) + w("") + ";" + a("");
        case "Enum":
            return dent() + w("") + "enum\u0020" + a("") + a("") + getSourceOf(node.getAsJsonObject("name"), pretty,
                    replaceLambda) + w("") + "{" + a("") + indent() + join(node.getAsJsonArray("enumerators"), pretty,
                    replaceLambda, w(""), ",", false) + outdent() + w("") + "}" + a("");
        case "Enumerator":
            return a("")
                 + getSourceOf(node.getAsJsonObject("name"), pretty, replaceLambda);
        case "FieldBasedAccessExpr":
            return a("")
                 + getSourceOf(node.getAsJsonObject("expression"), pretty, replaceLambda) + w("") + "." + a("") + w("")
                 + node.getAsJsonObject("fieldName").get("valueWithBar").getAsString() + a("");
        case "Foreach":
            return dent() + w("") + "foreach" + a(" ") + join(node.getAsJsonArray("variables"), pretty, replaceLambda,
                    w(" "), ",", false) + w(" ") + "in" + a("") + a(" ") + getSourceOf(
                    node.getAsJsonObject("collection"), pretty, replaceLambda) + w(" ") + "{" + a("") + indent() + a("")
                    + getSourceOf(node.getAsJsonObject("body"), pretty, replaceLambda) + outdent() + w("") + "}" + a(
                    "");
        case "ForkJoin":
            if (node.get("workers") != null && node.get("joinType") != null
                         && node.get("joinCount") != null
                         && node.get("joinedWorkerIdentifiers") != null && node.get("joinResultVar") != null
                         && node.get("joinBody") != null && node.get("timeOutExpression") != null
                         && node.get("timeOutVariable") != null && node.get("timeoutBody") != null) {
                return dent() + dent() + dent() + w("") + "fork" + a("") + w(" ") + "{" + a("") + indent() + join(
                        node.getAsJsonArray("workers"), pretty, replaceLambda, w(""), null, false) + outdent() + w("")
                        + "}" + a("") + w("") + "join" + a("") + w("") + "(" + a("") + w("") + node.get("joinType")
                        .getAsString() + a("") + w(" ") + node.get("joinCount").getAsString() + a("") + join(
                        node.getAsJsonArray("joinedWorkerIdentifiers"), pretty, replaceLambda, w(" "), ",", false) + w(
                        "") + ")" + a("") + w("") + "(" + a("") + a("") + getSourceOf(
                        node.getAsJsonObject("joinResultVar"), pretty, replaceLambda) + w("") + ")" + a("") + w(" ")
                        + "{" + a("") + indent() + a("") + getSourceOf(node.getAsJsonObject("joinBody"), pretty,
                        replaceLambda) + outdent() + w("") + "}" + a("") + w("") + "timeout" + a("") + w("") + "(" + a(
                        "") + a("") + getSourceOf(node.getAsJsonObject("timeOutExpression"), pretty, replaceLambda) + w(
                        "") + ")" + a("") + w("") + "(" + a("") + a("") + getSourceOf(
                        node.getAsJsonObject("timeOutVariable"), pretty, replaceLambda) + w("") + ")" + a("") + w(" ")
                        + "{" + a("") + indent() + a("") + getSourceOf(node.getAsJsonObject("timeoutBody"), pretty,
                        replaceLambda) + outdent() + w("") + "}" + a("");
            } else if (node.get("workers") != null && node.get("joinType") != null
                         && node.get("joinCount") != null
                         && node.get("joinedWorkerIdentifiers") != null && node.get("joinResultVar") != null
                         && node.get("joinBody") != null) {
                return dent() + dent() + w("") + "fork" + a("") + w(" ") + "{" + a("") + indent() + join(
                        node.getAsJsonArray("workers"), pretty, replaceLambda, w(""), null, false) + outdent() + w("")
                        + "}" + a("") + w("") + "join" + a("") + w("") + "(" + a("") + w("") + node.get("joinType")
                        .getAsString() + a("") + w(" ") + node.get("joinCount").getAsString() + a("") + join(
                        node.getAsJsonArray("joinedWorkerIdentifiers"), pretty, replaceLambda, w(" "), ",", false) + w(
                        "") + ")" + a("") + w("") + "(" + a("") + a("") + getSourceOf(
                        node.getAsJsonObject("joinResultVar"), pretty, replaceLambda) + w("") + ")" + a("") + w(" ")
                        + "{" + a("") + indent() + a("") + getSourceOf(node.getAsJsonObject("joinBody"), pretty,
                        replaceLambda) + outdent() + w("") + "}" + a("");
            } else if (node.get("workers") != null && node.get("joinType") != null && node.get("joinCount") != null
                    && node.get("joinedWorkerIdentifiers") != null && node.get("joinResultVar") != null) {
                return dent() + dent() + w("") + "fork" + a("") + w(" ") + "{" + a("") + indent() + join(
                        node.getAsJsonArray("workers"), pretty, replaceLambda, w(""), null, false) + outdent() + w("")
                        + "}" + a("") + w("") + "join" + a("") + w("") + "(" + a("") + w("") + node.get("joinType")
                        .getAsString() + a("") + w(" ") + node.get("joinCount").getAsString() + a("") + join(
                        node.getAsJsonArray("joinedWorkerIdentifiers"), pretty, replaceLambda, w(" "), ",", false) + w(
                        "") + ")" + a("") + w("") + "(" + a("") + a("") + getSourceOf(
                        node.getAsJsonObject("joinResultVar"), pretty, replaceLambda) + w("") + ")" + a("") + w(" ")
                        + "{" + a("") + indent() + outdent() + w("") + "}" + a("");
            } else if (node.get("workers") != null && node.get("joinType") != null
                         && node.get("joinedWorkerIdentifiers") != null
                         && node.get("joinResultVar") != null && node.get("joinBody") != null
                         && node.get("timeOutExpression") != null && node.get("timeOutVariable") != null
                         && node.get("timeoutBody") != null) {
                return dent() + dent() + dent() + w("") + "fork" + a("") + w(" ") + "{" + a("") + indent() + join(
                        node.getAsJsonArray("workers"), pretty, replaceLambda, w(""), null, false) + outdent() + w("")
                        + "}" + a("") + w("") + "join" + a("") + w("") + "(" + a("") + w("") + node.get("joinType")
                        .getAsString() + a("") + join(node.getAsJsonArray("joinedWorkerIdentifiers"), pretty,
                        replaceLambda, w(" "), ",", false) + w("") + ")" + a("") + w("") + "(" + a("") + a("")
                        + getSourceOf(node.getAsJsonObject("joinResultVar"), pretty, replaceLambda) + w("") + ")" + a(
                        "") + w(" ") + "{" + a("") + indent() + a("") + getSourceOf(node.getAsJsonObject("joinBody"),
                        pretty, replaceLambda) + outdent() + w("") + "}" + a("") + w("") + "timeout" + a("") + w("")
                        + "(" + a("") + a("") + getSourceOf(node.getAsJsonObject("timeOutExpression"), pretty,
                        replaceLambda) + w("") + ")" + a("") + w("") + "(" + a("") + a("") + getSourceOf(
                        node.getAsJsonObject("timeOutVariable"), pretty, replaceLambda) + w("") + ")" + a("") + w(" ")
                        + "{" + a("") + indent() + a("") + getSourceOf(node.getAsJsonObject("timeoutBody"), pretty,
                        replaceLambda) + outdent() + w("") + "}" + a("");
            } else if (node.get("workers") != null && node.get("joinType") != null
                         && node.get("joinedWorkerIdentifiers") != null
                         && node.get("joinResultVar") != null && node.get("joinBody") != null) {
                return dent() + dent() + w("") + "fork" + a("") + w(" ") + "{" + a("") + indent() + join(
                        node.getAsJsonArray("workers"), pretty, replaceLambda, w(""), null, false) + outdent() + w("")
                        + "}" + a("") + w("") + "join" + a("") + w("") + "(" + a("") + w("") + node.get("joinType")
                        .getAsString() + a("") + join(node.getAsJsonArray("joinedWorkerIdentifiers"), pretty,
                        replaceLambda, w(" "), ",", false) + w("") + ")" + a("") + w("") + "(" + a("") + a("")
                        + getSourceOf(node.getAsJsonObject("joinResultVar"), pretty, replaceLambda) + w("") + ")" + a(
                        "") + w(" ") + "{" + a("") + indent() + a("") + getSourceOf(node.getAsJsonObject("joinBody"),
                        pretty, replaceLambda) + outdent() + w("") + "}" + a("");
            } else {
                return dent() + dent() + w("") + "fork" + a("") + w(" ") + "{" + a("") + indent() + join(
                        node.getAsJsonArray("workers"), pretty, replaceLambda, w(""), null, false) + outdent() + w("")
                        + "}" + a("") + w("") + "join" + a("") + w("") + "(" + a("") + w("") + node.get("joinType")
                        .getAsString() + a("") + join(node.getAsJsonArray("joinedWorkerIdentifiers"), pretty,
                        replaceLambda, w(" "), ",", false) + w("") + ")" + a("") + w("") + "(" + a("") + a("")
                        + getSourceOf(node.getAsJsonObject("joinResultVar"), pretty, replaceLambda) + w("") + ")" + a(
                        "") + w(" ") + "{" + a("") + indent() + outdent() + w("") + "}" + a("");
            }
        case "Function":
            if (node.get("lambda") != null && node.get("lambda").getAsBoolean()
                         && node.get("annotationAttachments") != null
                         && node.get("documentationAttachments") != null
                         && node.get("deprecatedAttachments") != null && node.get("parameters") != null
                         && node.get("returnParameters") != null
                         && node.getAsJsonArray("returnParameters").size() > 0 && node.get("body") != null
                         && node.get("workers") != null) {
                return dent()
                 + join(node.getAsJsonArray("annotationAttachments"), pretty, replaceLambda, w(""), null, false)
                 + join(node.getAsJsonArray("documentationAttachments"), pretty, replaceLambda, w(""), null, false)
                 + join(node.getAsJsonArray("deprecatedAttachments"), pretty, replaceLambda, w(""), null, false)
                 + w("") + "function" + a("") + w("") + "(" + a("")
                 + join(node.getAsJsonArray("parameters"), pretty, replaceLambda, w(""), ",", false) + w("") + ")"
                 + a(" ") + w("") + "(" + a("")
                 + join(node.getAsJsonArray("returnParameters"), pretty, replaceLambda, w(""), ",", false) + w("")
                 + ")" + a(" ") + w("") + "{" + a("") + indent() + a("")
                 + getSourceOf(node.getAsJsonObject("body"), pretty, replaceLambda)
                 + join(node.getAsJsonArray("workers"), pretty, replaceLambda, w(""), null, false)
                 + outdent() + w("") + "}" + a("");
            } else if (node.get("lambda") != null && node.get("lambda").getAsBoolean()
                         && node.get("annotationAttachments") != null
                         && node.get("documentationAttachments") != null
                         && node.get("deprecatedAttachments") != null && node.get("parameters") != null
                         && node.get("body") != null && node.get("workers") != null) {
                return dent()
                 + join(node.getAsJsonArray("annotationAttachments"), pretty, replaceLambda, w(""), null, false)
                 + join(node.getAsJsonArray("documentationAttachments"), pretty, replaceLambda, w(""), null, false)
                 + join(node.getAsJsonArray("deprecatedAttachments"), pretty, replaceLambda, w(""), null, false)
                 + w("") + "function" + a("") + w("") + "(" + a("")
                 + join(node.getAsJsonArray("parameters"), pretty, replaceLambda, w(""), ",", false) + w("") + ")"
                 + a(" ") + w("") + "{" + a("") + indent() + a("")
                 + getSourceOf(node.getAsJsonObject("body"), pretty, replaceLambda)
                 + join(node.getAsJsonArray("workers"), pretty, replaceLambda, w(""), null, false)
                 + outdent() + w("") + "}" + a("");
            } else if (node.get("annotationAttachments") != null
                         && node.get("documentationAttachments") != null
                         && node.get("deprecatedAttachments") != null && node.get("receiver") != null
                         && node.getAsJsonObject("name").get("valueWithBar") != null
                         && !node.getAsJsonObject("name").get("valueWithBar").getAsString().isEmpty()
                         && node.get("parameters") != null && node.get("returnParameters") != null
                         && node.getAsJsonArray("returnParameters").size() > 0
                         && node.get("body") != null && node.get("workers") != null) {
                return join(node.getAsJsonArray("annotationAttachments"), pretty, replaceLambda, w(""), null, false)
                        + dent() + join(node.getAsJsonArray("documentationAttachments"), pretty, replaceLambda, w(""),
                        null, false) + join(node.getAsJsonArray("deprecatedAttachments"), pretty, replaceLambda, w(""),
                        null, false) + (node.get("public").getAsBoolean() ? w("") + "public" + a(" ") : "") + w("")
                        + "function" + a("") + w("") + "<" + a("") + a("") + getSourceOf(
                        node.getAsJsonObject("receiver"), pretty, replaceLambda) + w("") + ">" + a("") + w(" ") + node
                        .getAsJsonObject("name").get("valueWithBar").getAsString() + a("") + w("") + "(" + a("") + join(
                        node.getAsJsonArray("parameters"), pretty, replaceLambda, w(""), ",", false) + w("") + ")" + a(
                        " ") + w("") + "(" + a("") + join(node.getAsJsonArray("returnParameters"), pretty,
                        replaceLambda, w(""), ",", false) + w("") + ")" + a(" ") + w("") + "{" + a("") + indent() + a(
                        "") + getSourceOf(node.getAsJsonObject("body"), pretty, replaceLambda) + join(
                        node.getAsJsonArray("workers"), pretty, replaceLambda, w(""), null, false) + outdent() + w("")
                        + "}" + a("");
            } else if (node.get("annotationAttachments") != null
                    && node.get("documentationAttachments") != null
                    && node.get("deprecatedAttachments") != null
                    && node.getAsJsonObject("name").get("valueWithBar") != null
                    && !node.getAsJsonObject("name").get("valueWithBar").getAsString().isEmpty()
                    && node.get("parameters") != null && node.get("returnParameters") != null
                    && node.getAsJsonArray("returnParameters").size() > 0 && node.get("body") != null
                    && node.get("workers") != null) {
                return join(node.getAsJsonArray("annotationAttachments"), pretty, replaceLambda, w(""), null, false)
                 + dent()
                 + join(node.getAsJsonArray("documentationAttachments"), pretty, replaceLambda, w(""), null, false)
                 + join(node.getAsJsonArray("deprecatedAttachments"), pretty, replaceLambda, w(""), null, false)
                 + (node.get("public").getAsBoolean() ? w("") + "public" + a(" ") : "") + w("") + "function"
                 + a("") + w(" ")
                 + node.getAsJsonObject("name").get("valueWithBar").getAsString() + a("") + w("") + "(" + a("")
                 + join(node.getAsJsonArray("parameters"), pretty, replaceLambda, w(""), ",", false)
                 + w("") + ")" + a(" ") + w("") + "(" + a("")
                 + join(node.getAsJsonArray("returnParameters"), pretty, replaceLambda, w(""), ",", false)
                 + w("") + ")" + a(" ") + w("") + "{" + a("") + indent() + a("")
                 + getSourceOf(node.getAsJsonObject("body"), pretty, replaceLambda)
                 + join(node.getAsJsonArray("workers"), pretty, replaceLambda, w(""), null, false) + outdent()
                 + w("") + "}" + a("");
            } else if (node.get("annotationAttachments") != null
                         && node.get("documentationAttachments") != null
                         && node.get("deprecatedAttachments") != null && node.get("receiver") != null
                         && node.getAsJsonObject("name").get("valueWithBar") != null
                         && !node.getAsJsonObject("name").get("valueWithBar").getAsString().isEmpty()
                         && node.get("parameters") != null && node.get("body") != null
                         && node.get("workers") != null) {
                return join(node.getAsJsonArray("annotationAttachments"), pretty, replaceLambda, w(""), null, false)
                 + dent()
                 + join(node.getAsJsonArray("documentationAttachments"), pretty, replaceLambda, w(""), null, false)
                 + join(node.getAsJsonArray("deprecatedAttachments"), pretty, replaceLambda, w(""), null, false)
                 + (node.get("public").getAsBoolean() ? w("") + "public" + a(" ") : "") + w("") + "function"
                 + a("") + w("") + "<" + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("receiver"), pretty, replaceLambda) + w("") + ">" + a("") + w(" ")
                 + node.getAsJsonObject("name").get("valueWithBar").getAsString() + a("") + w("") + "(" + a("")
                 + join(node.getAsJsonArray("parameters"), pretty, replaceLambda, w(""), ",", false)
                 + w("") + ")" + a(" ") + w("") + "{" + a("") + indent() + a("")
                 + getSourceOf(node.getAsJsonObject("body"), pretty, replaceLambda)
                 + join(node.getAsJsonArray("workers"), pretty, replaceLambda, w(""), null, false) + outdent() + w("")
                 + "}" + a("");
            } else {
                return join(node.getAsJsonArray("annotationAttachments"), pretty, replaceLambda, w(""), null, false)
                 + dent()
                 + join(node.getAsJsonArray("documentationAttachments"), pretty, replaceLambda, w(""), null, false)
                 + join(node.getAsJsonArray("deprecatedAttachments"), pretty, replaceLambda, w(""), null, false)
                 + (node.get("public").getAsBoolean() ? w("") + "public" + a(" ") : "") + w("") + "function"
                 + a("") + w(" ")
                 + node.getAsJsonObject("name").get("valueWithBar").getAsString() + a("") + w("") + "(" + a("")
                 + join(node.getAsJsonArray("parameters"), pretty, replaceLambda, w(""), ",", false)
                 + w("") + ")" + a(" ") + w("") + "{" + a("") + indent() + a("")
                 + getSourceOf(node.getAsJsonObject("body"), pretty, replaceLambda)
                 + join(node.getAsJsonArray("workers"), pretty, replaceLambda, w(""), null, false) + outdent() + w("")
                 + "}" + a("");
            }
        case "FunctionType":
            if (node.get("paramTypeNode") != null
                         && node.get("returnParamTypeNode") != null
                         && node.getAsJsonArray("returnParamTypeNode").size() > 0) {
                return w("") + "function" + a("") + w("") + "(" + a("")
                 + join(node.getAsJsonArray("paramTypeNode"), pretty, replaceLambda, w(""), ",", false) + w("") + ")"
                 + a("") + (node.get("returnKeywordExists").getAsBoolean() ? w("") + "returns" + a("") : "") + w("")
                 + "(" + a("")
                 + join(node.getAsJsonArray("returnParamTypeNode"), pretty, replaceLambda, w(""), null, false)
                 + w("") + ")" + a("");
            } else {
                return w("") + "function" + a("") + w("") + "(" + a("") + join(node.getAsJsonArray("paramTypeNode"),
                 pretty, replaceLambda, w(""), ",", false) + w("") + ")" + a("");
            }
        case "If":
            if (node.get("ladderParent") != null
                         && node.get("ladderParent").getAsBoolean() && node.get("condition") != null
                         && node.get("body") != null && node.get("elseStatement") != null) {
                return (node.getAsJsonObject("parent").get("kind").getAsString().equals("If") ? "" : dent()) + w("")
                        + "if" + a("") + w(" ") + "(" + a("") + a("") + getSourceOf(node.getAsJsonObject("condition"),
                        pretty, replaceLambda) + w("") + ")" + a(" ") + w("") + "{" + a("") + indent() + a("")
                        + getSourceOf(node.getAsJsonObject("body"), pretty, replaceLambda) + outdent() + w("") + "}"
                        + a("") + w(" ") + "else" + a(" ") + a("") + getSourceOf(node.getAsJsonObject("elseStatement"),
                        pretty, replaceLambda);
            } else if (node.get("condition") != null && node.get("body") != null
                         && node.get("elseStatement") != null) {
                return (node.getAsJsonObject("parent").get("kind").getAsString().equals("If") ? "" : dent())
                 + (node.getAsJsonObject("parent").get("kind").getAsString().equals("If") ? "" : dent()) + w("") + "if"
                 + a("") + w(" ") + "(" + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("condition"), pretty, replaceLambda) + w("") + ")" + a(" ")
                 + w("") + "{" + a("") + indent() + a("")
                 + getSourceOf(node.getAsJsonObject("body"), pretty, replaceLambda) + outdent() + w("")
                 + "}" + a("") + w(" ") + "else" + a(" ") + w("") + "{" + a("") + indent() + a("") + getSourceOf(
                        node.getAsJsonObject("elseStatement"), pretty, replaceLambda) + outdent() + w("") + "}" + a("");
            } else {
                return (node.getAsJsonObject("parent").get("kind").getAsString().equals("If") ? "" : dent()) + w("")
                        + "if" + a("") + w(" ") + "(" + a("") + a("") + getSourceOf(node.getAsJsonObject("condition"),
                        pretty, replaceLambda) + w("") + ")" + a(" ") + w("") + "{" + a("")
                 + indent() + a("")
                 + getSourceOf(node.getAsJsonObject("body"), pretty, replaceLambda) + outdent() + w("") + "}" + a("");
            }
        case "IndexBasedAccessExpr":
            return a("")
                 + getSourceOf(node.getAsJsonObject("expression"), pretty, replaceLambda) + w("") + "[" + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("index"), pretty, replaceLambda) + w("") + "]"
                 + a("");
        case "Invocation":
            if (node.get("actionInvocation") != null
                         && node.get("actionInvocation").getAsBoolean() && node.get("expression") != null
                         && node.getAsJsonObject("name").get("valueWithBar") != null
                         && !node.getAsJsonObject("name").get("valueWithBar").getAsString().isEmpty()
                         && node.get("argumentExpressions") != null) {
                return a("")
                 + getSourceOf(node.getAsJsonObject("expression"), pretty, replaceLambda) + w("") + "->" + a("") + w("")
                 + node.getAsJsonObject("name").get("valueWithBar").getAsString() + a("") + w("")
                 + "(" + a("") + join(node.getAsJsonArray("argumentExpressions"), pretty, replaceLambda, w(""), ",",
                        false) + w("") + ")" + a("");
            } else if (node.get("expression") != null
                         && node.getAsJsonObject("name").get("valueWithBar") != null
                         && !node.getAsJsonObject("name").get("valueWithBar").getAsString().isEmpty()
                         && node.get("argumentExpressions") != null) {
                return a("")
                 + getSourceOf(node.getAsJsonObject("expression"), pretty, replaceLambda) + w("") + "." + a("") + w("")
                 + node.getAsJsonObject("name").get("valueWithBar").getAsString() + a("") + w("") + "("
                 + a("")
                 + join(node.getAsJsonArray("argumentExpressions"), pretty, replaceLambda, w(""), ",", false) + w("")
                 + ")" + a("");
            } else if (node.getAsJsonObject("packageAlias").get("valueWithBar") != null
                         && !node.getAsJsonObject("packageAlias").get("valueWithBar").getAsString().isEmpty()
                         && node.getAsJsonObject("name").get("valueWithBar") != null
                         && !node.getAsJsonObject("name").get("valueWithBar").getAsString().isEmpty()
                         && node.get("argumentExpressions") != null) {
                return w("") + node.getAsJsonObject("packageAlias").get("valueWithBar").getAsString() + a("") + w("")
                        + ":" + a("") + w("") + node.getAsJsonObject("name").get("valueWithBar").getAsString() + a("")
                        + w("") + "(" + a("") + join(node.getAsJsonArray("argumentExpressions"), pretty, replaceLambda,
                        w(""), ",", false) + w("") + ")" + a("");
            } else {
                return w("")
                 + node.getAsJsonObject("name").get("valueWithBar").getAsString() + a("") + w("") + "(" + a("")
                 + join(node.getAsJsonArray("argumentExpressions"), pretty, replaceLambda, w(""), ",", false)
                 + w("") + ")" + a("");
            }
        case "Lambda":
            return a("")
                 + getSourceOf(node.getAsJsonObject("functionNode"), pretty, replaceLambda);
        case "Literal":
            if (node.get("inTemplateLiteral") != null
                         && node.get("inTemplateLiteral").getAsBoolean() && node.get("unescapedValue") != null) {
                return w("") + node.get("unescapedValue").getAsString() + a("");
            } else if (node.get("inTemplateLiteral") != null
                         && node.get("inTemplateLiteral").getAsBoolean()) {
                return "";
            } else {
                return w("") + node.get("value").getAsString() + a("");
            }
        case "Next":
            return dent() + w("") + "next" + a("") + w("") + ";" + a("");
        case "RecordLiteralExpr":
            if (node.get("keyValuePairs") != null) {
                return w("") + "{" + a("") + join(node.getAsJsonArray("keyValuePairs"), pretty, replaceLambda, w(""),
                        ",", false) + w("") + "}" + a("");
            } else {
                return w("") + "{" + a("") + w("") + "}" + a("");
            }
        case "RecordLiteralKeyValue":
            return a("")
                 + getSourceOf(node.getAsJsonObject("key"), pretty, replaceLambda) + w("") + ":" + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("value"), pretty, replaceLambda);
        case "Resource":
            return join(node.getAsJsonArray("annotationAttachments"), pretty, replaceLambda, w(""), null, false)
                 + dent()
                 + join(node.getAsJsonArray("documentationAttachments"), pretty, replaceLambda, w(""), null, false)
                 + join(node.getAsJsonArray("deprecatedAttachments"), pretty, replaceLambda, w(""), null, false)
                 + w("") + "resource" + a("") + w(" ")
                 + node.getAsJsonObject("name").get("valueWithBar").getAsString() + a("") + w(" ") + "(" + a("")
                 + join(node.getAsJsonArray("parameters"), pretty, replaceLambda, w(""), ",", false)
                 + w("") + ")" + a("") + w(" ") + "{" + a("") + indent() + a("")
                 + getSourceOf(node.getAsJsonObject("body"), pretty, replaceLambda)
                 + join(node.getAsJsonArray("workers"), pretty, replaceLambda, w(""), null, false) + outdent()
                 + w("") + "}" + a("");
        case "Return":
            return dent() + w("") + "return" + a(" ")
                 + join(node.getAsJsonArray("expressions"), pretty, replaceLambda, w(" "), ",", false) + w("")
                 + ";" + a("");
        case "Service":
            return join(node.getAsJsonArray("annotationAttachments"), pretty, replaceLambda, w(""), null, false)
                 + dent()
                 + join(node.getAsJsonArray("documentationAttachments"), pretty, replaceLambda, w(""), null, false)
                 + join(node.getAsJsonArray("deprecatedAttachments"), pretty, replaceLambda, w(""), null, false)
                 + w("") + "service" + a("") + w("") + "<" + a("") + w("")
                 + node.getAsJsonObject("protocolPackageIdentifier").get("valueWithBar").getAsString() + a("")
                 + w("") + ">" + a("") + w(" ")
                 + node.getAsJsonObject("name").get("valueWithBar").getAsString() + a("") + w(" ") + "{" + a("")
                 + indent()
                 + join(node.getAsJsonArray("variables"), pretty, replaceLambda, w(""), null, false)
                 + join(node.getAsJsonArray("resources"), pretty, replaceLambda, w(""), null, false) + outdent() + w("")
                 + "}" + a("");
        case "SimpleVariableRef":
            if (node.get("inTemplateLiteral") != null
                         && node.get("inTemplateLiteral").getAsBoolean()
                         && node.getAsJsonObject("packageAlias").get("valueWithBar") != null
                         && !node.getAsJsonObject("packageAlias").get("valueWithBar").getAsString().isEmpty()
                         && node.getAsJsonObject("variableName").get("valueWithBar") != null
                         && !node.getAsJsonObject("variableName").get("valueWithBar").getAsString().isEmpty()) {
                return w("") + "{{" + a("") + w("")
                 + node.getAsJsonObject("packageAlias").get("valueWithBar").getAsString() + a("") + w("") + ":"
                 + a("") + w("") + node.getAsJsonObject("variableName").get("valueWithBar").getAsString()
                 + a("") + w("") + "}}" + a("");
            } else if (node.get("inTemplateLiteral") != null
                         && node.get("inTemplateLiteral").getAsBoolean()
                         && node.getAsJsonObject("variableName").get("valueWithBar") != null
                         && !node.getAsJsonObject("variableName").get("valueWithBar").getAsString().isEmpty()) {
                return w("") + "{{" + a("") + w("")
                 + node.getAsJsonObject("variableName").get("valueWithBar").getAsString() + a("") + w("") + "}}"
                 + a("");
            } else if (node.getAsJsonObject("packageAlias").get("valueWithBar") != null
                         && !node.getAsJsonObject("packageAlias").get("valueWithBar").getAsString().isEmpty()
                         && node.getAsJsonObject("variableName").get("valueWithBar") != null
                         && !node.getAsJsonObject("variableName").get("valueWithBar").getAsString().isEmpty()) {
                return w("")
                 + node.getAsJsonObject("packageAlias").get("valueWithBar").getAsString() + a("") + w("") + ":" + a("")
                 + w("") + node.getAsJsonObject("variableName").get("valueWithBar").getAsString() + a("");
            } else {
                return w("")
                 + node.getAsJsonObject("variableName").get("valueWithBar").getAsString() + a("");
            }
        case "StringTemplateLiteral":
            return w("") + "string\u0020`" + a("")
                 + join(node.getAsJsonArray("expressions"), pretty, replaceLambda, w(""), null, false) + w("")
                 + "`" + a("");
        case "Struct":
            if (node.get("anonStruct") != null
                         && node.get("anonStruct").getAsBoolean() && node.get("fields") != null) {
                return dent() + (node.get("public").getAsBoolean() ? w("") + "public"
                 + a(" ") : "") + w("") + "struct" + a("") + w(" ") + "{" + a("") + indent()
                 + join(node.getAsJsonArray("fields"), pretty, replaceLambda, w(""), ";", true) + outdent() + w("")
                 + "}" + a("");
            } else {
                return join(node.getAsJsonArray("annotationAttachments"), pretty, replaceLambda, w(""), null, false)
                 + dent()
                 + join(node.getAsJsonArray("documentationAttachments"), pretty, replaceLambda, w(""), null, false)
                 + join(node.getAsJsonArray("deprecatedAttachments"), pretty, replaceLambda, w(""), null, false)
                 + (node.get("public").getAsBoolean() ? w("") + "public" + a(" ") : "") + w("") + "struct"
                 + a("") + w(" ") + node.getAsJsonObject("name").get("valueWithBar").getAsString() + a("")
                 + w(" ") + "{" + a("") + indent()
                 + join(node.getAsJsonArray("fields"), pretty, replaceLambda, w(""), ";", true) + outdent() + w("")
                 + "}" + a("");
            }
        case "TernaryExpr":
            return a("")
                 + getSourceOf(node.getAsJsonObject("condition"), pretty, replaceLambda) + w("") + "?" + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("thenExpression"), pretty, replaceLambda) + w("")
                 + ":" + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("elseExpression"), pretty, replaceLambda);
        case "Throw":
            return dent() + w("") + "throw" + a("") + a(" ")
                 + getSourceOf(node.getAsJsonObject("expressions"), pretty, replaceLambda) + w("") + ";"
                 + a("");
        case "Transaction":
            if (node.get("condition") != null
                         && node.get("transactionBody") != null && node.get("failedBody") != null) {
                return dent() + dent() + w("") + "transaction" + a("") + w(" ") + "with"
                 + a("") + w(" ") + "retries" + a("") + w(" ") + "(" + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("condition"), pretty, replaceLambda) + w("") + ")" + a("") + w(" ")
                 + "{" + a("") + indent() + a("")
                 + getSourceOf(node.getAsJsonObject("transactionBody"), pretty, replaceLambda) + outdent() + w("")
                 + "}" + a("") + w(" ") + "failed" + a("") + w(" ") + "{" + a("") + indent() + a("")
                 + getSourceOf(node.getAsJsonObject("failedBody"), pretty, replaceLambda) + outdent() + w("") + "}"
                 + a("");
            } else if (node.get("transactionBody") != null
                         && node.get("failedBody") != null) {
                return dent() + dent() + w("") + "transaction" + a("") + w(" ") + "{"
                 + a("") + indent() + a("")
                 + getSourceOf(node.getAsJsonObject("transactionBody"), pretty, replaceLambda) + outdent() + w("") + "}"
                 + a("") + w(" ") + "failed" + a("") + w(" ") + "{" + a("") + indent() + a("")
                 + getSourceOf(node.getAsJsonObject("failedBody"), pretty, replaceLambda) + outdent()
                 + w("") + "}" + a("");
            } else if (node.get("condition") != null
                         && node.get("transactionBody") != null) {
                return dent() + w("") + "transaction" + a("") + w(" ") + "with" + a("")
                 + w(" ") + "retries" + a("") + w(" ") + "(" + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("condition"), pretty, replaceLambda) + w("") + ")" + a("")
                 + w(" ") + "{" + a("") + indent() + a("")
                 + getSourceOf(node.getAsJsonObject("transactionBody"), pretty, replaceLambda) + outdent()
                 + w("") + "}" + a("");
            } else {
                return dent() + w("") + "transaction" + a("") + w(" ") + "{" + a("")
                 + indent() + a("")
                 + getSourceOf(node.getAsJsonObject("transactionBody"), pretty, replaceLambda) + outdent()
                 + w("") + "}" + a("");
            }
        case "Transform":
            return dent() + w("") + "transform" + a("") + w(" ") + "{" + a("")
                 + indent() + a("")
                 + getSourceOf(node.getAsJsonObject("body"), pretty, replaceLambda) + outdent() + w("") + "}" + a("");
        case "Transformer":
            if (node.get("source") != null
                         && node.get("returnParameters") != null && node.getAsJsonArray("returnParameters").size() > 0
                         && node.getAsJsonObject("name").get("valueWithBar") != null
                         && !node.getAsJsonObject("name").get("valueWithBar").getAsString().isEmpty()
                         && node.get("parameters") != null && node.get("body") != null) {
                return dent() + (node.get("public").getAsBoolean() ? w("") + "public"
                 + a(" ") : "") + w("") + "transformer" + a("") + w("") + "<"
                 + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("source"), pretty, replaceLambda) + w("") + "," + a("")
                 + join(node.getAsJsonArray("returnParameters"), pretty, replaceLambda, w(""), ",", false)
                 + w("") + ">" + a("") + w("")
                 + node.getAsJsonObject("name").get("valueWithBar").getAsString() + a("") + w("") + "(" + a("")
                 + join(node.getAsJsonArray("parameters"), pretty, replaceLambda, w(""), ",", false) + w("") + ")"
                 + a("") + w("") + "{" + a("") + indent() + a("")
                 + getSourceOf(node.getAsJsonObject("body"), pretty, replaceLambda) + outdent() + w("") + "}" + a("");
            } else if (node.get("source") != null
                         && node.get("returnParameters") != null && node.getAsJsonArray("returnParameters").size() > 0
                         && node.getAsJsonObject("name").get("valueWithBar") != null
                         && !node.getAsJsonObject("name").get("valueWithBar").getAsString().isEmpty()
                         && node.get("body") != null) {
                return dent() + (node.get("public").getAsBoolean() ? w("") + "public"
                 + a(" ") : "") + w("") + "transformer" + a("") + w("") + "<"
                 + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("source"), pretty, replaceLambda) + w("") + "," + a("")
                 + join(node.getAsJsonArray("returnParameters"), pretty, replaceLambda, w(""), ",", false)
                 + w("") + ">" + a("") + w("")
                 + node.getAsJsonObject("name").get("valueWithBar").getAsString() + a("") + w("") + "{" + a("")
                 + indent() + a("")
                 + getSourceOf(node.getAsJsonObject("body"), pretty, replaceLambda) + outdent() + w("") + "}" + a("");
            } else {
                return dent() + (node.get("public").getAsBoolean() ? w("") + "public"
                 + a(" ") : "") + w("") + "transformer" + a("") + w("") + "<"
                 + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("source"), pretty, replaceLambda) + w("") + "," + a("")
                 + join(node.getAsJsonArray("returnParameters"), pretty, replaceLambda, w(""), ",", false)
                 + w("") + ">" + a("") + w("") + "{" + a("") + indent() + a("")
                 + getSourceOf(node.getAsJsonObject("body"), pretty, replaceLambda)
                 + outdent() + w("") + "}" + a("");
            }
        case "Try":
            if (node.get("body") != null && node.get("catchBlocks") != null
                         && node.get("finallyBody") != null) {
                return dent() + dent() + w("") + "try" + a("") + w(" ") + "{" + a("")
                 + indent() + a("")
                 + getSourceOf(node.getAsJsonObject("body"), pretty, replaceLambda) + outdent() + w("") + "}" + a("")
                 + join(node.getAsJsonArray("catchBlocks"), pretty, replaceLambda, w(""), null, false) + w("")
                 + "finally" + a("") + w(" ") + "{" + a("") + indent() + a("")
                 + getSourceOf(node.getAsJsonObject("finallyBody"), pretty, replaceLambda) + outdent()
                 + w("") + "}" + a("");
            } else {
                return dent() + w("") + "try" + a("") + w(" ") + "{" + a("") + indent()
                 + a("")
                 + getSourceOf(node.getAsJsonObject("body"), pretty, replaceLambda) + outdent() + w("") + "}" + a("")
                 + join(node.getAsJsonArray("catchBlocks"), pretty, replaceLambda, w(""), null, false);
            }
        case "TypeCastExpr":
            return w("") + "(" + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("typeNode"), pretty, replaceLambda) + w("") + ")" + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("expression"), pretty, replaceLambda);
        case "TypeConversionExpr":
            if (node.get("typeNode") != null
                         && node.get("transformerInvocation") != null && node.get("expression") != null) {
                return w("") + "<" + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("typeNode"), pretty, replaceLambda) + w("") + "," + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("transformerInvocation"), pretty, replaceLambda) + w("") + ">"
                 + a("") + a("") + getSourceOf(node.getAsJsonObject("expression"), pretty, replaceLambda);
            } else {
                return w("") + "<" + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("typeNode"), pretty, replaceLambda) + w("") + ">" + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("expression"), pretty, replaceLambda);
            }
        case "TypeofExpression":
            return w("") + "typeof" + a("") + a(" ")
                 + getSourceOf(node.getAsJsonObject("typeNode"), pretty, replaceLambda);
        case "UnaryExpr":
            return w("") + node.get("operatorKind").getAsString() + a("") + a(" ")
                 + getSourceOf(node.getAsJsonObject("expression"), pretty, replaceLambda);
        case "UserDefinedType":
            if (node.get("anonStruct") != null
                         && node.get("anonStruct").getAsBoolean()) {
                return a("")
                 + getSourceOf(node.getAsJsonObject("anonStruct"), pretty, replaceLambda);
            } else if (node.getAsJsonObject("packageAlias").get("valueWithBar") != null
                         && !node.getAsJsonObject("packageAlias").get("valueWithBar").getAsString().isEmpty()
                         && node.getAsJsonObject("typeName").get("valueWithBar") != null
                         && !node.getAsJsonObject("typeName").get("valueWithBar").getAsString().isEmpty()) {
                return w("")
                 + node.getAsJsonObject("packageAlias").get("valueWithBar").getAsString() + a("") + w("") + ":"
                 + a("") + w("") + node.getAsJsonObject("typeName").get("valueWithBar").getAsString() + a("");
            } else {
                return w("")
                 + node.getAsJsonObject("typeName").get("valueWithBar").getAsString() + a("");
            }
        case "ValueType":
            return w("") + node.get("typeKind").getAsString() + a("");
        case "Variable":
            if (node.get("endpoint") != null
                         && node.get("endpoint").getAsBoolean() && node.get("typeNode") != null
                         && node.getAsJsonObject("name").get("valueWithBar") != null
                         && !node.getAsJsonObject("name").get("valueWithBar").getAsString().isEmpty()
                         && node.get("initialExpression") != null) {
                return dent() + dent() + w("") + "endpoint" + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("typeNode"), pretty, replaceLambda)
                 + w(" ")
                 + node.getAsJsonObject("name").get("valueWithBar").getAsString() + a("") + w("") + "{" + a("")
                 + indent() + a("")
                 + getSourceOf(node.getAsJsonObject("initialExpression"), pretty, replaceLambda) + w("") + ";" + a("")
                 + outdent() + w("") + "}" + a("");
            } else if (node.get("endpoint") != null
                         && node.get("endpoint").getAsBoolean() && node.get("typeNode") != null
                         && node.getAsJsonObject("name").get("valueWithBar") != null
                         && !node.getAsJsonObject("name").get("valueWithBar").getAsString().isEmpty()) {
                return dent() + w("") + "endpoint" + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("typeNode"), pretty, replaceLambda) + w(" ")
                 + node.getAsJsonObject("name").get("valueWithBar").getAsString()
                 + a("") + w("") + "{" + a("") + indent() + outdent() + w("") + "}"
                 + a("");
            } else if (node.get("global") != null && node.get("global").getAsBoolean()
                         && node.get("annotationAttachments") != null
                         && node.get("documentationAttachments") != null
                         && node.get("deprecatedAttachments") != null && node.get("typeNode") != null
                         && node.getAsJsonObject("name").get("valueWithBar") != null
                         && !node.getAsJsonObject("name").get("valueWithBar").getAsString().isEmpty()
                         && node.get("initialExpression") != null) {
                return dent()
                 + join(node.getAsJsonArray("annotationAttachments"), pretty, replaceLambda, w(""), null, false)
                 + join(node.getAsJsonArray("documentationAttachments"), pretty, replaceLambda, w(""), null, false)
                 + join(node.getAsJsonArray("deprecatedAttachments"), pretty, replaceLambda, w(""), null, false)
                 + (node.get("public").getAsBoolean() ? w("") + "public" + a(" ") : "")
                 + (node.get("const").getAsBoolean() ? w("") + "const" + a(" ") : "") + a("")
                 + getSourceOf(node.getAsJsonObject("typeNode"), pretty, replaceLambda)
                 + w(" ")
                 + node.getAsJsonObject("name").get("valueWithBar").getAsString() + a("") + w(" ") + "="
                 + a(" ") + a("")
                 + getSourceOf(node.getAsJsonObject("initialExpression"), pretty, replaceLambda) + w("") + ";" + a("");
            } else if (node.get("global") != null && node.get("global").getAsBoolean()
                         && node.get("annotationAttachments") != null
                         && node.get("documentationAttachments") != null
                         && node.get("deprecatedAttachments") != null && node.get("typeNode") != null
                         && node.getAsJsonObject("name").get("valueWithBar") != null
                         && !node.getAsJsonObject("name").get("valueWithBar").getAsString().isEmpty()) {
                return dent()
                 + join(node.getAsJsonArray("annotationAttachments"), pretty, replaceLambda, w(""), null, false)
                 + join(node.getAsJsonArray("documentationAttachments"), pretty, replaceLambda, w(""), null, false)
                 + join(node.getAsJsonArray("deprecatedAttachments"), pretty, replaceLambda, w(""), null, false) + a("")
                 + getSourceOf(node.getAsJsonObject("typeNode"), pretty, replaceLambda) + w(" ")
                 + node.getAsJsonObject("name").get("valueWithBar").getAsString()
                 + a("") + w("") + ";" + a("");
            } else if (node.get("typeNode") != null
                         && node.getAsJsonObject("name").get("valueWithBar") != null
                         && !node.getAsJsonObject("name").get("valueWithBar").getAsString().isEmpty()
                         && node.get("initialExpression") != null) {
                return a("")
                 + getSourceOf(node.getAsJsonObject("typeNode"), pretty, replaceLambda) + w(" ")
                 + node.getAsJsonObject("name").get("valueWithBar").getAsString() + a("") + w(" ")
                 + "=" + a(" ") + a("")
                 + getSourceOf(node.getAsJsonObject("initialExpression"), pretty, replaceLambda);
            } else if (node.get("annotationAttachments") != null
                         && node.get("documentationAttachments") != null
                         && node.get("deprecatedAttachments") != null && node.get("typeNode") != null
                         && node.getAsJsonObject("name").get("valueWithBar") != null
                         && !node.getAsJsonObject("name").get("valueWithBar").getAsString().isEmpty()) {
                return join(node.getAsJsonArray("annotationAttachments"), pretty, replaceLambda, w(""), null, false)
                 + join(node.getAsJsonArray("documentationAttachments"), pretty, replaceLambda, w(""), null, false)
                 + join(node.getAsJsonArray("deprecatedAttachments"), pretty, replaceLambda, w(""), null, false) + a("")
                 + getSourceOf(node.getAsJsonObject("typeNode"), pretty, replaceLambda) + w(" ")
                 + node.getAsJsonObject("name").get("valueWithBar").getAsString() + a("");
            } else {
                return a("")
                 + getSourceOf(node.getAsJsonObject("typeNode"), pretty, replaceLambda);
            }
        case "VariableDef":
            if (node.get("endpoint") != null
                         && node.get("endpoint").getAsBoolean() && node.get("variable") != null) {
                return a("")
                 + getSourceOf(node.getAsJsonObject("variable"), pretty, replaceLambda);
            } else {
                return dent() + a("")
                 + getSourceOf(node.getAsJsonObject("variable"), pretty, replaceLambda) + w("") + ";" + a("");
            }
        case "While":
            return dent() + w("") + "while" + a("") + w(" ") + "(" + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("condition"), pretty, replaceLambda) + w("") + ")" + a("") + w(" ")
                 + "{" + a("") + indent() + a("")
                 + getSourceOf(node.getAsJsonObject("body"), pretty, replaceLambda) + outdent() + w("") + "}" + a("");
        case "Worker":
            return dent() + w("") + "worker" + a("") + w(" ")
                 + node.getAsJsonObject("name").get("valueWithBar").getAsString() + a("") + w(" ") + "{"
                 + a("") + indent() + a("")
                 + getSourceOf(node.getAsJsonObject("body"), pretty, replaceLambda) + outdent() + w("") + "}" + a("");
        case "WorkerReceive":
            return dent()
                 + join(node.getAsJsonArray("expressions"), pretty, replaceLambda, w(""), ",", false) + w("") + "<-"
                 + a("") + w("")
                 + node.getAsJsonObject("workerName").get("valueWithBar").getAsString() + a("") + w("") + ";" + a("");
        case "WorkerSend":
            if (node.get("forkJoinedSend") != null
                         && node.get("forkJoinedSend").getAsBoolean() && node.get("expressions") != null) {
                return dent()
                 + join(node.getAsJsonArray("expressions"), pretty, replaceLambda, w(""), ",", false) + w("") + "->"
                 + a("") + w("") + "fork" + a("") + w("") + ";" + a("");
            } else {
                return dent()
                 + join(node.getAsJsonArray("expressions"), pretty, replaceLambda, w(""), ",", false) + w("")
                 + "->" + a("") + w("") + node.getAsJsonObject("workerName").get("valueWithBar").getAsString()
                 + a("") + w("") + ";" + a("");
            }
        case "XmlAttribute":
            return a("")
                 + getSourceOf(node.getAsJsonObject("name"), pretty, replaceLambda) + w("") + "=" + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("value"), pretty, replaceLambda);
        case "XmlAttributeAccessExpr":
            if (node.get("expression") != null && node.get("index") != null) {
                return a("")
                 + getSourceOf(node.getAsJsonObject("expression"), pretty, replaceLambda) + w("") + "@" + a("")
                 + w("") + "[" + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("index"), pretty, replaceLambda) + w("") + "]" + a("");
            } else {
                return a("")
                 + getSourceOf(node.getAsJsonObject("expression"), pretty, replaceLambda) + w("") + "@" + a("");
            }
        case "XmlCommentLiteral":
            if (node.get("root") != null && node.get("root").getAsBoolean()
                         && node.get("textFragments") != null) {
                return w("") + "xml`" + a("") + w("") + "<!--" + a("")
                 + join(node.getAsJsonArray("textFragments"), pretty, replaceLambda, w(""), null, false) + w("")
                 + "-->" + a("") + w("") + "`" + a("");
            } else {
                return w("") + "<!--" + a("")
                 + join(node.getAsJsonArray("textFragments"), pretty, replaceLambda, w(""), null, false) + w("") + "-->"
                 + a("");
            }
        case "XmlElementLiteral":
            if (node.get("root") != null && node.get("root").getAsBoolean()
                         && node.get("startTagName") != null && node.get("attributes") != null
                         && node.get("content") != null
                         && node.get("endTagName") != null) {
                return w("") + "xml`" + a("") + w("") + "<" + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("startTagName"), pretty, replaceLambda)
                 + join(node.getAsJsonArray("attributes"), pretty, replaceLambda, w(""), null, false) + w("") + ">"
                 + a("") + join(node.getAsJsonArray("content"), pretty, replaceLambda, w(""), null, false) + w("")
                 + "</" + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("endTagName"), pretty, replaceLambda) + w("") + ">" + a("") + w("")
                 + "`" + a("");
            } else if (node.get("startTagName") != null
                         && node.get("attributes") != null && node.get("content") != null
                         && node.get("endTagName") != null) {
                return w("") + "<" + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("startTagName"), pretty, replaceLambda)
                 + join(node.getAsJsonArray("attributes"), pretty, replaceLambda, w(""), null, false) + w("")
                 + ">" + a("")
                 + join(node.getAsJsonArray("content"), pretty, replaceLambda, w(""), null, false) + w("") + "</"
                 + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("endTagName"), pretty, replaceLambda) + w("") + ">" + a("");
            } else if (node.get("root") != null && node.get("root").getAsBoolean()
                         && node.get("startTagName") != null
                         && node.get("attributes") != null) {
                return w("") + "xml`" + a("") + w("") + "<" + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("startTagName"), pretty, replaceLambda)
                 + join(node.getAsJsonArray("attributes"), pretty, replaceLambda, w(""), null, false) + w("") + "/>`"
                 + a("");
            } else {
                return w("") + "<" + a("") + a("")
                 + getSourceOf(node.getAsJsonObject("startTagName"), pretty, replaceLambda)
                 + join(node.getAsJsonArray("attributes"), pretty, replaceLambda, w(""), null, false) + w("")
                 + "/>" + a("");
            }
        case "XmlPiLiteral":
            if (node.get("target") != null
                         && node.get("dataTextFragments") != null) {
                return a("")
                 + getSourceOf(node.getAsJsonObject("target"), pretty, replaceLambda)
                 + join(node.getAsJsonArray("dataTextFragments"), pretty, replaceLambda, w(""), null, false);
            } else if (node.get("dataTextFragments") != null) {
                return join(node.getAsJsonArray("dataTextFragments"), pretty, replaceLambda, w(""), null, false);
            } else {
                return a("")
                 + getSourceOf(node.getAsJsonObject("target"), pretty, replaceLambda);
            }
        case "XmlQname":
            if (node.getAsJsonObject("prefix").get("valueWithBar") != null
                         && !node.getAsJsonObject("prefix").get("valueWithBar").getAsString().isEmpty()
                         && node.getAsJsonObject("localname").get("valueWithBar") != null
                         && !node.getAsJsonObject("localname").get("valueWithBar").getAsString().isEmpty()) {
                return w("")
                 + node.getAsJsonObject("prefix").get("valueWithBar").getAsString() + a("") + w("") + ":" + a("")
                 + w("")
                 + node.getAsJsonObject("localname").get("valueWithBar").getAsString() + a("");
            } else {
                return w("")
                 + node.getAsJsonObject("localname").get("valueWithBar").getAsString() + a("");
            }
        case "XmlQuotedString":
            return join(node.getAsJsonArray("textFragments"), pretty, replaceLambda, w(""), null, false);
        case "XmlTextLiteral":
            return join(node.getAsJsonArray("textFragments"), pretty, replaceLambda, w(""), null, false);
        case "Xmlns":
            if (node.get("namespaceURI") != null
                         && node.getAsJsonObject("prefix").get("valueWithBar") != null
                         && !node.getAsJsonObject("prefix").get("valueWithBar").getAsString().isEmpty()) {
                return dent() + w("") + "xmlns" + a("") + a(" ")
                 + getSourceOf(node.getAsJsonObject("namespaceURI"), pretty, replaceLambda) + w(" ")
                 + "as" + a("") + w(" ")
                 + node.getAsJsonObject("prefix").get("valueWithBar").getAsString() + a("") + w("") + ";" + a("");
            } else if (node.get("namespaceURI") != null) {
                return dent() + w("") + "xmlns" + a("") + a(" ")
                 + getSourceOf(node.getAsJsonObject("namespaceURI"), pretty, replaceLambda) + w("") + ";"
                 + a("");
            } else {
                return a("")
                 + getSourceOf(node.getAsJsonObject("namespaceDeclaration"), pretty, replaceLambda);
            }

            // auto gen end
            /* eslint-enable max-len */
            default:
                return "";
        }
    }

    @FindbugsSuppressWarnings
    public String w(String defaultWS) {
        if (ws.size() > 0 && (ws.size() >= (i + 1))) {
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

