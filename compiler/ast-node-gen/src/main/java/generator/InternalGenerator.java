package generator;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import exception.GeneratorException;
import generator.util.Common;
import generator.util.Constants;
import model.Field;
import model.Node;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InternalGenerator {
    private static String INTERNAL_PACKAGE = "internal";

    private static List<Field> currentFieldList;
    private static Bucket bucket;
    private static List<ChildNode> addChildNodeList;
    private static ToStringFunction toStringFunction;
    private static FacadeClass facadeFunction;

    public static void generateInternalTree(String internalTemplatePath, String classPath,
                                            String nodeJsonPath) throws IOException, GeneratorException {
        List<Node> originalNodes = Common.getTreeNodes(nodeJsonPath);
        if (originalNodes == null) {
            throw new GeneratorException("Received an empty node list.");
        }
        List<Node> nodes = restructureNodes(originalNodes);
        for (Node node : nodes) {
            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache mustache = mf.compile(internalTemplatePath);
            Map<String, String> mapScope = new HashMap<>();
            mapScope.put(Constants.PACKAGE_PLACEHOLDER, Constants.INTERNAL_PACKAGE);
            mapScope.put(Constants.IMPORTS_PLACEHOLDER, Constants.INTERNAL_IMPORTS);
            mapScope.put(Constants.CLASSNAME_PLACEHOLDER, node.getName());
            mapScope.putAll(Common.buildClassName(node));
            currentFieldList = node.getFields();
            if (currentFieldList != null ) {
                bucket = new Bucket(currentFieldList.size());
                if (!node.getBase().equals(Constants.ST_TOKEN)) {
                    populateChildNode();
                }
            }
            if (node.getName().equals(Constants.ST_TOKEN) || node.getBase().equals(Constants.ST_TOKEN)) {
                if (!node.getName().equals(Constants.MISSING_TOKEN)) {
                    populateToStringFunction(node);
                }
            }
            if (!((node.getType() != null) && (node.getType().equals(Constants.ABSTRACT_KEYWORD)))) {
                if (!node.getBase().equals(Constants.ST_TOKEN) || node.getName().contains(Constants.ST_TOKEN)) {
                    facadeFunction = new FacadeClass(node.getName().substring(2));
                }
            }
            if (node.getBase() != null) {
                Node parentNode = Common.getImmediateParentNode(node.getBase(), nodes);
                if (parentNode != null) {
                    mapScope.put(Constants.IMMEDIATE_PARENT_PLACEHOLDER,
                            superConstructorParentValues(parentNode.getFields()));
                }
            }

            Writer writer = new StringWriter();
            Object[] scopes = new Object[2];
            scopes[0] = mapScope;
            scopes[1] = new InternalGenerator();
            mustache.execute(writer, scopes);
            writer.flush();
            Common.writeToFile(writer.toString(), classPath + "/" + INTERNAL_PACKAGE + "/" + node.getName()
                    + Constants.DOT + Constants.JAVA_EXT);
            cleanupValues();
        }
    }

    private static void cleanupValues() {
        toStringFunction = null;
        facadeFunction = null;
        currentFieldList = null;
        addChildNodeList = null;
        bucket = null;
    }

    private static List<Node> restructureNodes (List<Node> nodeList) {
        List<Node> modifiedNodeList = new ArrayList<>();
        nodeList.forEach(node -> {
            Node modifiedNode = new Node();
            modifiedNode.setType(node.getType());
            modifiedNode.setName("ST" + node.getName());
            modifiedNode.setBase("ST" + node.getBase());
            if (node.getFields() != null) {
                List<Field> modifiedFieldList = new ArrayList<>();
                node.getFields().forEach(field -> {
                    Field modifiedField = new Field();
                    modifiedField.setName(field.getName());
                    modifiedField.setDefaultValue(field.getDefaultValue());
                    if (field.getType().equals("SyntaxList")) {
                        modifiedField.setType(Constants.ST_NODE);
                    } else if (field.getType().equals(Constants.TOKEN) || field.getType().equals(Constants.NODE)) {
                        modifiedField.setType("ST" + field.getType());
                    } else {
                        modifiedField.setType(field.getType());
                    }
                    modifiedFieldList.add(modifiedField);
                });
                modifiedNode.setFields(modifiedFieldList);
            }
            modifiedNodeList.add(modifiedNode);
        });
        return modifiedNodeList;
    }

    private static void populateChildNode() {
        int index = 0;
        addChildNodeList = new ArrayList<>();
        for (Field field : currentFieldList) {
            addChildNodeList.add(new ChildNode(field.getName(), index));
            index++;
        }
    }

    private static void populateToStringFunction(Node node) {
        if (node.getName().equals(Constants.ST_TOKEN)) {
            toStringFunction = new ToStringFunction(Constants.KIND_PROPERTY);
        } else if (node.getBase().equals(Constants.ST_TOKEN)) {
            toStringFunction = new ToStringFunction(Constants.PROPERTY);
        }
    }

    private static String superConstructorParentValues(List<Field> fields) {
        if (fields == null) {
            return "";
        }
        StringBuilder values = new StringBuilder();
        fields.forEach(field -> values.append(Constants.PARAMETER_SEPARATOR).append(Constants.NULL_KEYWORD));
        return values.toString();
    }

    public List<Field> attributes() {
        return currentFieldList;
    }

    public Bucket bucket() {
        return bucket;
    }

    public List<ChildNode> addChildNode() {
        return addChildNodeList;
    }

    public ToStringFunction toStringFunction() {
        return toStringFunction;
    }

    public FacadeClass facadeFunction() {
        return facadeFunction;
    }

    static class Bucket {
        int bucketCount;
        Bucket(int bucketCount) {
            this.bucketCount = bucketCount;
        }
    }

    static class ChildNode {
        String name;
        int index;
        ChildNode(String name, int index) {
            this.name = name;
            this.index = index;
        }
    }

    static class ToStringFunction {
        String kind;
        ToStringFunction(String kind) {
            this.kind = kind;
        }
    }

    static class FacadeClass {
        String facadeClass;
        FacadeClass(String facadeClass) {
            this.facadeClass = facadeClass;
        }
    }
}
