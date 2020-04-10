/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
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
    private static List<Field> currentFieldList;
    private static Bucket bucket;
    private static List<ChildNode> addChildNodeList;
    private static ToStringFunction toStringFunction;
    private static FacadeClass facadeFunction;

    public static class Bucket {
        int bucketCount;
        Bucket(int bucketCount) {
            this.bucketCount = bucketCount;
        }
    }

    public static class ChildNode {
        String name;
        int index;
        ChildNode(String name, int index) {
            this.name = name;
            this.index = index;
        }
    }

    public static class ToStringFunction {
        String kind;
        ToStringFunction(String kind) {
            this.kind = kind;
        }
    }

    public static class FacadeClass {
        String facadeClass;
        FacadeClass(String facadeClass) {
            this.facadeClass = facadeClass;
        }
    }

    /**
     * generateInternalTree is used to generate internal classes in the AST
     * @param internalTemplatePath path of the templated file
     * @param classPath path of the to which the classes to be generated
     * @param nodeJsonPath path of the JSON file which contain the syntax
     * @throws IOException throws as {@link IOException} if these in an error in IO operation
     * @throws GeneratorException throws an {@link GeneratorException} if the tree could not be parsed to java classes
     */
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
            String internalPackage = "internal";
            Common.writeToFile(writer.toString(), classPath + "/" + internalPackage + "/" + node.getName()
                    + Constants.DOT + Constants.JAVA_EXT);
            cleanupValues();
        }
    }

    /**
     * cleanup values is used to reinitialize the attributes in the class
     */
    private static void cleanupValues() {
        toStringFunction = null;
        facadeFunction = null;
        currentFieldList = null;
        addChildNodeList = null;
        bucket = null;
    }

    /**
     * restructureNodes is used to restructure the nodes
     * @param nodeList original list of nodes
     * @return returns the restructured nodes
     */
    private static List<Node> restructureNodes (List<Node> nodeList) {
        List<Node> modifiedNodeList = new ArrayList<>();
        nodeList.forEach(node -> {
            Node modifiedNode = new Node();
            modifiedNode.setType(node.getType());
            modifiedNode.setName(Constants.ST + node.getName());
            modifiedNode.setBase(Constants.ST + node.getBase());
            if (node.getFields() != null) {
                List<Field> modifiedFieldList = new ArrayList<>();
                node.getFields().forEach(field -> {
                    Field modifiedField = new Field();
                    modifiedField.setName(field.getName());
                    modifiedField.setDefaultValue(field.getDefaultValue());
                    if (field.getType().equals(Constants.SYNTAX_LIST)) {
                        modifiedField.setType(Constants.ST_NODE);
                    } else if (field.getType().equals(Constants.TOKEN) || field.getType().equals(Constants.NODE)) {
                        modifiedField.setType(Constants.ST + field.getType());
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

    /**
     * populateChildNode is used to populate with values that should go into the addChildNode block of the
     * generated classes
     */
    private static void populateChildNode() {
        int index = 0;
        addChildNodeList = new ArrayList<>();
        for (Field field : currentFieldList) {
            addChildNodeList.add(new ChildNode(field.getName(), index));
            index++;
        }
    }

    /**
     * populateToStringFunction is used to populate with values that should go into the toString block of the
     * generated classes
     * @param node node that is used to populate toString function
     */
    private static void populateToStringFunction(Node node) {
        if (node.getName().equals(Constants.ST_TOKEN)) {
            toStringFunction = new ToStringFunction(Constants.KIND_PROPERTY);
        } else if (node.getBase().equals(Constants.ST_TOKEN)) {
            toStringFunction = new ToStringFunction(Constants.PROPERTY);
        }
    }

    /**
     * superConstructorParentValues fills the constructor with null values for the number of attributes \
     * in the superclass
     * @param fields list of superclass attributes
     * @return
     */
    private static String superConstructorParentValues(List<Field> fields) {
        if (fields == null) {
            return "";
        }
        StringBuilder values = new StringBuilder();
        fields.forEach(field -> values.append(Constants.PARAMETER_SEPARATOR).append(Constants.NULL_KEYWORD));
        return values.toString();
    }

    /**
     * attributes is used to represent the attributes block in handlebar template. The attributes block will be
     * replaced by the returned attributes list.
     * @return returns the currentFieldList in a node
     */
    public List<Field> attributes() {
        return currentFieldList;
    }

    /**
     * bucket is used to represent the bucket block in handlebar template. The bucket block will be
     * replaced by the returned bucket.
     * @return returns the bucket object
     */
    public Bucket bucket() {
        return bucket;
    }

    /**
     * addChildNode is used to represent the addChildNode block in handlebar template. The attributes block will be
     * replaced by the returned attributes list.
     * @return returns the addChildNodeList
     */
    public List<ChildNode> addChildNode() {
        return addChildNodeList;
    }

    /**
     * toStringFunction is used to represent the toStringFunction block in handlebar template.
     * The toStringFunction block will be replaced by the returned toStringFunction.
     * @return returns the toStringFunction
     */
    public ToStringFunction toStringFunction() {
        return toStringFunction;
    }

    /**
     * facadeFunction is used to represent the facadeFunction block in handlebar template.
     * The facadeFunction block will be replaced by the returned facadeFunction.
     * @return returns the facadeFunction
     */
    public FacadeClass facadeFunction() {
        return facadeFunction;
    }
}
