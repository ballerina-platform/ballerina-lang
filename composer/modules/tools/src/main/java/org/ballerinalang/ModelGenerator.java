/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.composer.tools;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

import com.google.common.base.CaseFormat;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.FileUtils;

import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.NodeKind;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Code generator which will convert java models to javascript.
 */
public class ModelGenerator {

    static PrintStream out = System.out;
    static Map<String, String> alias = new HashMap<String, String>();

    static final String TEMPLATE_PATH = "./composer/modules/web/src/plugins/ballerina/model/templates/";
    static final String GENERATE_PATH = "./composer/modules/web/src/plugins/ballerina/model/gen/";

    public static void main(String args[]) {
        JsonObject nodes = getContext();
        for (Map.Entry<String, JsonElement> entry : nodes.entrySet()) {
            JsonObject node = (JsonObject) entry.getValue();
            String lowerHyphenName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, entry.getKey());
            generateSourceFiles(node, "node.hbs" , "tree/" + lowerHyphenName + ".js");
            generateSourceFiles(node, "abstract-node.hbs" , "tree/abstract-tree/" + lowerHyphenName + ".js");
        }
        generateSourceFiles(nodes, "abstract-tree-util.hbs" , "abstract-tree-util.js");
        generateSourceFiles(nodes, "node-factory.hbs" , "node-factory.js");
        generateSourceFiles(nodes, "positioning-util.hbs" , "positioning-util.js");
        generateSourceFiles(nodes, "sizing-util.hbs" , "sizing-util.js");
        generateSourceFiles(nodes, "error-rendering-util.hbs" , "error-rendering-util.js");
    }

    public static JsonObject getContext() {
        // Set alias for the classes
        //alias.put("EnumeratorNode", "");
        alias.put("ImportNode", "ImportPackageNode");
        alias.put("RecordLiteralKeyValueNode", "");
        alias.put("XmlnsNode", "");
        alias.put("ArrayLiteralExprNode", "ArrayLiteralNode");
        alias.put("BinaryExprNode", "BinaryExpressionNode");
        alias.put("BracedTupleExprNode", "BracedOrTupleExpression");
        alias.put("TypeInitExprNode", "TypeInitNode");
        alias.put("FieldBasedAccessExprNode", "FieldBasedAccessNode");
        alias.put("IndexBasedAccessExprNode", "IndexBasedAccessNode");
        alias.put("IntRangeExprNode", "IntRangeExpression");
        //alias.put("ActionInvocationNode", "");
        alias.put("LambdaNode", "LambdaFunctionNode");
        alias.put("RecordLiteralExprNode", "");
        alias.put("SimpleVariableRefNode", "SimpleVariableReferenceNode");
        alias.put("TernaryExprNode", "TernaryExpressionNode");
        alias.put("AwaitExprNode", "AwaitExpressionNode");
        alias.put("TypeCastExprNode", "TypeCastNode");
        alias.put("TypeConversionExprNode", "TypeConversionNode");
        alias.put("IsAssignableExprNode", "");
        alias.put("UnaryExprNode", "UnaryExpressionNode");
        alias.put("RestArgsExprNode", "RestArgsNode");
        alias.put("NamedArgsExprNode", "NamedArgNode");
        //alias.put("XmlQnameNode", "");
        //alias.put("XmlAttributeNode", "");
        //alias.put("XmlAttributeAccessExprNode", "");
        //alias.put("XmlQuotedStringNode", "");
        //alias.put("XmlElementLiteralNode", "");
        //alias.put("XmlTextLiteralNode", "");
        //alias.put("XmlCommentLiteralNode", "");
        //alias.put("XmlPiLiteralNode", "");
        //alias.put("XmlSequenceLiteralNode", "");
        alias.put("MatchExpressionPatternClauseNode", "MatchExpressionPatternNode");
        alias.put("TableQueryExpressionNode", "");
        alias.put("MatchPatternClauseNode", "MatchStatementPatternNode");
        //alias.put("TransformNode", "");
        alias.put("TryNode", "TryCatchFinallyNode");
        alias.put("VariableDefNode", "VariableDefinitionNode");
        alias.put("StreamNode", "");
        alias.put("UnionTypeNodeNode", "UnionTypeNode");
        alias.put("TupleTypeNodeNode", "TupleTypeNode");
        alias.put("BuiltInRefTypeNode", "");
        alias.put("EndpointTypeNode", "UserDefinedTypeNode");
        alias.put("StreamingInputNode", "");
        alias.put("JoinStreamingInputNode", "");
        alias.put("TableQueryNode", "");
        alias.put("SetAssignmentClauseNode", "");
        alias.put("SetNode", "");
        alias.put("StreamingQueryNode", "StreamingQueryStatementNode");
        //alias.put("QueryNode", "");
        alias.put("StreamingQueryDeclarationNode", "");
        alias.put("WithinNode", "WithinClause");
        alias.put("PatternClauseNode", "PatternClause");
        alias.put("TypeDefinitionNode", "");
        alias.put("TableNode", "");
        alias.put("ElvisExprNode", "ElvisExpressionNode");
        //alias.put("CheckExprNode", "");

        List<Class<?>> list = ModelGenerator.find("org.ballerinalang.model.tree");

        NodeKind[] nodeKinds = NodeKind.class.getEnumConstants();
        JsonObject nodes = new JsonObject();
        for (NodeKind node: nodeKinds) {
            String nodeKind = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, node.toString());
            String nodeClassName = nodeKind + "Node";
            try {
                String actualClassName = (alias.get(nodeClassName) != null) ? alias.get(nodeClassName) : nodeClassName;
                Class<?> clazz = list.stream().filter(nodeClass -> nodeClass.getSimpleName()
                        .equals(actualClassName)).findFirst().get();

                JsonObject nodeObj = new JsonObject();
                nodeObj.addProperty("kind", nodeKind);
                nodeObj.addProperty("name", nodeClassName);
                nodeObj.addProperty("fileName", CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, nodeClassName));
                JsonArray attr = new JsonArray();
                JsonArray bools = new JsonArray();
                JsonArray imports = new JsonArray();
                List<String> parents = Arrays.asList(clazz.getInterfaces()).stream().map(
                        parent -> parent.getSimpleName()
                ).collect(Collectors.toList());

                // tag object with supper type
                if (parents.contains("StatementNode")) {
                    nodeObj.addProperty("isStatement", true);
                    JsonObject imp = new JsonObject();
                    imp.addProperty("returnType", "StatementNode");
                    imp.addProperty("returnTypeFile", "statement-node");
                    imports.add(imp);
                } else {
                    nodeObj.addProperty("isStatement", false);
                }

                if (parents.contains("ExpressionNode")) {
                    nodeObj.addProperty("isExpression", true);
                    JsonObject imp = new JsonObject();
                    imp.addProperty("returnType", "ExpressionNode");
                    imp.addProperty("returnTypeFile", "expression-node");
                    imports.add(imp);
                } else {
                    nodeObj.addProperty("isExpression", false);
                }

                if (!parents.contains("StatementNode") && !parents.contains("ExpressionNode")) {
                    JsonObject imp = new JsonObject();
                    imp.addProperty("returnType", "Node");
                    imp.addProperty("returnTypeFile", "node");
                    imports.add(imp);
                }

                Method[] methods = clazz.getMethods();
                for (Method m : methods) {
                    String methodName = m.getName();
                    if ("getKind".equals(methodName) || "getWS".equals(methodName) ||
                            "getPosition".equals(methodName)) {
                        continue;
                    }
                    if (methodName.startsWith("get")) {
                        JsonObject attribute = new JsonObject();
                        JsonObject imp = new JsonObject();
                        attribute.addProperty("property", toJsonName(m.getName(), 3));
                        attribute.addProperty("methodSuffix", m.getName().substring(3));
                        attribute.addProperty("list", List.class.isAssignableFrom(m.getReturnType()));
                        attribute.addProperty("isNode", Node.class.isAssignableFrom(m.getReturnType()));
                        if (Node.class.isAssignableFrom(m.getReturnType())) {
                            String returnClass = m.getReturnType().getSimpleName();
                            if (alias.containsValue(m.getReturnType().getSimpleName())) {
                                returnClass = getKindForAliasClass(m.getReturnType().getSimpleName());
                            }
                            imp.addProperty("returnType", returnClass);
                            attribute.addProperty("returnType", returnClass);
                            imp.addProperty("returnTypeFile",
                                    CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, returnClass));
                            if (!imports.contains(imp)) {
                                imports.add(imp);
                            }
                        }
                        attr.add(attribute);
                    }
                    if (methodName.startsWith("is")) {
                        JsonObject attribute = new JsonObject();
                        JsonObject imp = new JsonObject();
                        attribute.addProperty("property", toJsonName(m.getName(), 2));
                        attribute.addProperty("methodSuffix", m.getName().substring(2));
                        attribute.addProperty("list", List.class.isAssignableFrom(m.getReturnType()));
                        attribute.addProperty("isNode", Node.class.isAssignableFrom(m.getReturnType()));
                        if (Node.class.isAssignableFrom(m.getReturnType())) {
                            String returnClass = m.getReturnType().getSimpleName();
                            if (alias.containsValue(m.getReturnType().getSimpleName())) {
                                returnClass = getKindForAliasClass(m.getReturnType().getSimpleName());
                            }
                            imp.addProperty("returnType", returnClass);
                            attribute.addProperty("returnType", returnClass);
                            imp.addProperty("returnTypeFile",
                                    CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, returnClass));
                            if (!imports.contains(imp)) {
                                imports.add(imp);
                            }
                        }
                        bools.add(attribute);
                    }
                }
                nodeObj.add("attributes", attr);
                nodeObj.add("bools", bools);
                nodeObj.add("imports", imports);
                nodes.add(nodeClassName, nodeObj);
            } catch (NoSuchElementException e) {
                out.println("alias.put(\"" + nodeClassName + "\", \"\");");
            }
        }
        out.println(nodes);
        return nodes;
    }

    private static String getKindForAliasClass(String simpleName) {
        for (Map.Entry<String, String> entry : alias.entrySet()) {
            if (simpleName.equals(entry.getValue())) {
                return entry.getKey();
            }
        }

        throw new AssertionError("Undefined entry :" + simpleName);
    }

    public static void generateSourceFiles(JsonObject node, String tpl , String name) {
        try {
            String templateString = FileUtils.readFileToString(new File(TEMPLATE_PATH + tpl),
                    "UTF-8");
            Handlebars handlebars = new Handlebars();
            Template template = handlebars.compileInline(templateString);

            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Object>>() { }.getType();
            Map<String, Object> map = gson.fromJson(node, type);

            Context context = Context.newBuilder(map).build();
            String generated = template.apply(context);
            FileUtils.writeStringToFile(new File(GENERATE_PATH + name), generated, "UTF-8");
        } catch (IOException e) {
            throw new AssertionError("Template files should be always defined.");
        }
    }

    private static final char PKG_SEPARATOR = '.';

    private static final char DIR_SEPARATOR = '/';

    private static final String CLASS_FILE_SUFFIX = ".class";

    private static final String BAD_PACKAGE_ERROR = "Unable to get resources from path '%s'. " +
            "Are you sure the package '%s' exists?";

    public static List<Class<?>> find(String scannedPackage) {
        String scannedPath = scannedPackage.replace(PKG_SEPARATOR, DIR_SEPARATOR);
        URL scannedUrl = Thread.currentThread().getContextClassLoader().getResource(scannedPath);
        if (scannedUrl == null) {
            throw new IllegalArgumentException(String.format(BAD_PACKAGE_ERROR, scannedPath, scannedPackage));
        }
        File scannedDir = new File(scannedUrl.getFile());
        List<Class<?>> classes = new ArrayList<Class<?>>();
        File[] files = scannedDir.listFiles();
        Stream.of(files).forEach((file -> classes.addAll(find(file, scannedPackage))));
        return classes;
    }

    private static List<Class<?>> find(File file, String scannedPackage) {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        String resource = scannedPackage + PKG_SEPARATOR + file.getName();
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            Stream.of(files).forEach((child -> classes.addAll(find(child, resource))));
        } else if (resource.endsWith(CLASS_FILE_SUFFIX)) {
            int endIndex = resource.length() - CLASS_FILE_SUFFIX.length();
            String className = resource.substring(0, endIndex);
            try {
                classes.add(Class.forName(className));
            } catch (ClassNotFoundException ignore) {
            }
        }
        return classes;
    }

    private static String toJsonName(String name, int prefixLen) {
        return Character.toLowerCase(name.charAt(prefixLen)) + name.substring(prefixLen + 1);
    }

}
