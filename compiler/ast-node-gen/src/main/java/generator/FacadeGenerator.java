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

public class FacadeGenerator {

    private static List<Field> currentFieldList;
    private static List<AttributeFunction> attributeFunctionList;
    private static SwitchFunction switchFunction;

    public static class SwitchFunction {
        List<Case> caseList;
        SwitchFunction(List<Case> caseList) {
            this.caseList = caseList;
        }
    }

    public static class Case {
        String name;
        int index;
        Case(String name, int index){
            this.name = name;
            this.index = index;
        }
    }

    public static class CreateFacadeCall {
        int index;
        CreateFacadeCall(int index) {
            this.index = index;
        }
    }

    public static class AttributeFunction {
        String returnObject;
        String parentMethod;
        String name;
        int index;
        CreateFacadeCall createFacadeCall;
        AttributeFunction(String returnObject, String parentMethod, String name, int index) {
            this.returnObject = returnObject;
            this.parentMethod = parentMethod;
            this.name = name;
            this.index = index;
        }
        AttributeFunction(String returnObject, String parentMethod, String name, int index, CreateFacadeCall createFacadeCall) {
            this.returnObject = returnObject;
            this.parentMethod = parentMethod;
            this.name = name;
            this.index = index;
            this.createFacadeCall = createFacadeCall;
        }
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
     * attributeFunction is used to represent the attributeFunction block in handlebar template.
     * The attributeFunction block will be replaced by the returned attributeFunction list list.
     * @return returns the attributeFunctionList in a node
     */
    public List<AttributeFunction> attributeFunction() {
        return attributeFunctionList;
    }

    /**
     * switchFunction is to represent the switchFunction block in handlebar template. The switchFunction block will be
     * replaced by the returned switchFunction.
     * @return returns the switchFunction to generated switch case statement
     */
    public SwitchFunction switchFunction(){
        return switchFunction;
    }

    /**
     * generateFacade is used to generate facade classes in the AST
     * @param facadeTemplatePath path of the templated file
     * @param classPath path of the to which the classes to be generated
     * @param nodeJsonPath path of the JSON file which contain the syntax
     * @throws IOException throws as {@link IOException} if these in an error in IO operation
     * @throws GeneratorException throws an {@link GeneratorException} if the tree could not be parsed to java classes
     */
    public static void generateFacade(String facadeTemplatePath, String classPath,
                                      String nodeJsonPath) throws IOException, GeneratorException {
        List<Node> nodes = Common.getTreeNodes(nodeJsonPath);
        if (nodes != null) {
            List<Node> nodeList = restructureNodes(nodes);
            for (Node node : nodeList) {
                MustacheFactory mf = new DefaultMustacheFactory();
                Mustache mustache = mf.compile(facadeTemplatePath);
                Map<String, String> mapScope = new HashMap<>();
                mapScope.put(Constants.PACKAGE_PLACEHOLDER, Constants.FACADE_PACKAGE);
                mapScope.put(Constants.IMPORTS_PLACEHOLDER, Constants.FACADE_IMPORTS);
                mapScope.put(Constants.CLASSNAME_PLACEHOLDER, node.getName());
                mapScope.putAll(Common.buildClassName(node));
                currentFieldList = node.getFields();
                populateAttributeFunction(node.getFields());
                populateSwitchFunction(node.getFields());
                Writer writer = new StringWriter();
                Object[] scopes = new Object[2];
                scopes[0] = mapScope;
                scopes[1] = new FacadeGenerator();
                mustache.execute(writer, scopes);
                writer.flush();
                String facadePackage = "facade";
                Common.writeToFile( writer.toString().replace("&lt;", "<").
                        replace("&gt;", ">"), classPath + "/" + facadePackage + "/"
                        + node.getName() + Constants.DOT + Constants.JAVA_EXT);
                cleanupValues();
            }
        } else {
            throw new GeneratorException("Received an empty node list.");
        }
    }

    /**
     * cleanup values is used to reinitialize the attributes in the class
     */
    private static void cleanupValues() {
        currentFieldList = null;
        attributeFunctionList = null;
        switchFunction = null;
    }

    /**
     * restructureNodes is used to restructure the nodes
     * @param nodeList original list of nodes
     * @return returns the restructured nodes
     */
    private static List<Node> restructureNodes(List<Node> nodeList) {
        List<Node> modifiedNodeList = new ArrayList<>();
        for (Node node : nodeList) {
            if (!(node.getBase().equals(Constants.TOKEN)) || node.getName().equals(Constants.TOKEN)) {
                Node newNode = new Node();
                newNode.setBase(Constants.NON_TERMINAL_NODE);
                newNode.setName(node.getName());
                newNode.setType(node.getType());
                if (node.getFields() != null) {
                    List<Field> fields = new ArrayList<>();
                    for (Field field : node.getFields()) {
                        Field newField = new Field();
                        newField.setName(field.getName());
                        Node immediateParent = Common.getImmediateParentNode(field.getType(), nodeList);
                        if (immediateParent != null && immediateParent.getName().equals(Constants.ST_TOKEN)) {
                            newField.setType(Constants.TOKEN);
                        } else if (field.getType().equals(Constants.SYNTAX_LIST)) {
                            newField.setType(Constants.NODE_LIST);
                        } else {
                            newField.setType(field.getType());
                        }
                        fields.add(newField);
                    }
                    newNode.setFields(fields);
                }
                modifiedNodeList.add(newNode);
            }
        }
        return modifiedNodeList;
    }

    /**
     * populateAttributeFunction is used to populate the values that should go into the attributeFunction block in the
     * handlebar template
     * @param fieldList list of attributes
     */
    private static void populateAttributeFunction(List<Field> fieldList) {
        if (fieldList != null) {
            int i = 0;
            attributeFunctionList = new ArrayList<>();
            for (Field field : fieldList) {
                if (field.getType().equals(Constants.NODE)) {
                    attributeFunctionList.add(new AttributeFunction(Constants.NODE, "node.childInBucket",
                            field.getName(), i, new CreateFacadeCall(i)));
                } else if (field.getType().equals("NodeList<Node>")) {
                    attributeFunctionList.add(new AttributeFunction(field.getType(), "createListNode",
                            field.getName(), i));
                } else {
                    attributeFunctionList.add(new AttributeFunction(Constants.TOKEN, "createToken",
                            field.getName(), i));
                }
                i++;
            }
        }
    }

    /**
     * populateSwitchFunction is used to populate with values that should go into the switch case statements of the
     * generated classes
     * @param fieldList list of attributes
     */
    private static void populateSwitchFunction(List<Field> fieldList) {
        if (fieldList != null) {
            int i = 0;
            List<Case> caseList = new ArrayList<>();
            for (Field field : fieldList) {
                caseList.add(new Case(field.getName(), i));
                i++;
            }
            switchFunction = new SwitchFunction(caseList);
        }
    }
}
