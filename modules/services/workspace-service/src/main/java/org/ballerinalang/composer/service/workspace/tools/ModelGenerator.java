package org.ballerinalang.composer.service.workspace.tools;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

import com.google.common.base.CaseFormat;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.io.FileUtils;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.NodeKind;

/**
 * Code generator which will convert java models to javascript.
 */
public class ModelGenerator {

    static PrintStream out = System.out;
    static Map<String, String> alias = new HashMap<String, String>();

    static final String TEMPLATE_PATH = "./modules/web/js/ballerina/model/templates/";
    static final String GENERATE_PATH = "./modules/web/js/ballerina/model/gen/";

    public static void main(String args[]) {
        JsonObject nodes = getContext();
        JsonArray kinds = new JsonArray();
        for (Map.Entry<String, JsonElement> entry : nodes.entrySet()) {
            JsonObject node = (JsonObject) entry.getValue();
            String lowerHyphenName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, entry.getKey());
            generateSourceFiles(node, "node.hbs" , "tree/" + lowerHyphenName + ".js");
            generateSourceFiles(node, "abstract-node.hbs" , "tree/abstract-tree/" + lowerHyphenName + ".js");
            generateSourceFiles(node, "dimention-visitor.hbs" , "dimention/" + lowerHyphenName + "-dimention.js");
            generateSourceFiles(node, "position-visitor.hbs" , "position/" + lowerHyphenName + "-position.js");
            kinds.add(node.get("kind"));
        }
        generateSourceFiles(nodes, "abstract-tree-util.hbs" , "abstract-tree-util.js");
    }

    public static JsonObject getContext() {
        // Set alias for the classes
        alias.put("ForkjoinNode", "ForkJoinNode");
        alias.put("ImportNode", "ImportPackageNode");
        alias.put("TryNode", "");
        alias.put("VariableRefNode", "");
        alias.put("XmlnsNode", "");
        alias.put("ArrayLiteralExprNode", "ArrayLiteralNode");
        alias.put("RecodeLiteralExprNode", "");
        alias.put("BinaryExprNode", "BinaryExpressionNode");
        alias.put("UnaryExprNode", "UnaryExpressionNode");
        alias.put("ConnectorInitExprNode", "ConnectorInitExpressionNode");
        alias.put("LambdaNode", "");
        alias.put("XmlQnameNode", "");
        alias.put("XmlElementLiteralNode", "");
        alias.put("XmlTextLiteralNode", "");
        alias.put("XmlCommentLiteralNode", "");
        alias.put("XmlPiLiteralNode", "");
        alias.put("XmlQuotedStringNode", "");
        alias.put("TypeCastExprNode", "");
        alias.put("TypeConversionExprNode", "");
        alias.put("TernaryExprNode", "");

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
                Method[] methods = clazz.getMethods();
                for (Method m : methods) {
                    String methodName = m.getName();
                    if (methodName.startsWith("get")) {
                        JsonObject attribute = new JsonObject();
                        attribute.addProperty("property", toJsonName(m.getName(), 3));
                        attribute.addProperty("methodSuffix", m.getName().substring(3));
                        attribute.addProperty("list", List.class.isAssignableFrom(m.getReturnType()));
                        attribute.addProperty("isNode", Node.class.isAssignableFrom(m.getReturnType()));
                        attr.add(attribute);
                    }
                }
                nodeObj.add("attributes", attr);
                nodes.add(nodeClassName, nodeObj);
            } catch (NoSuchElementException e) {
                out.println("Unable to find proper class for the given kind :" + nodeClassName);
            }
        }
        out.println(nodes);
        return nodes;
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
        for (File file : scannedDir.listFiles()) {
            classes.addAll(find(file, scannedPackage));
        }
        return classes;
    }

    private static List<Class<?>> find(File file, String scannedPackage) {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        String resource = scannedPackage + PKG_SEPARATOR + file.getName();
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                classes.addAll(find(child, resource));
            }
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
