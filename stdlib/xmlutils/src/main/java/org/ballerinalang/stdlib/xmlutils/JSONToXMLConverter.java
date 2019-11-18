/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.stdlib.xmlutils;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.XMLValidator;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.jvm.values.XMLItem;
import org.ballerinalang.jvm.values.XMLSequence;
import org.ballerinalang.jvm.values.XMLValue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * Common utility methods used for JSON manipulation.
 *
 * @since 1.0
 */
@SuppressWarnings("unchecked")
public class JSONToXMLConverter {

    private static final OMFactory OM_FACTORY = OMAbstractFactory.getOMFactory();
    private static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String XSI_PREFIX = "xsi";
    private static final String NIL = "nil";

    /**
     * Converts given JSON object to the corresponding XML.
     *
     * @param json            JSON object to get the corresponding XML
     * @param attributePrefix String prefix used for attributes
     * @param arrayEntryTag   String used as the tag in the arrays
     * @return XMLValue XML representation of the given JSON object
     */
    @SuppressWarnings("rawtypes")
    public static XMLValue convertToXML(Object json, String attributePrefix, String arrayEntryTag) {
        if (json == null) {
            return new XMLSequence();
        }

        List<XMLValue> omElementArrayList = traverseTree(json, attributePrefix, arrayEntryTag);
        if (omElementArrayList.size() == 1) {
            return omElementArrayList.get(0);
        } else {
            // There is a multi rooted node and create xml sequence from it
            ArrayValue elementsSeq = new ArrayValue(new BArrayType(BTypes.typeXML));
            int count = omElementArrayList.size();
            for (int i = 0; i < count; i++) {
                elementsSeq.add(i, omElementArrayList.get(i));
            }
            return new XMLSequence(elementsSeq);
        }
    }

    // Private methods

    /**
     * Traverse a JSON root node and produces the corresponding XML items.
     *
     * @param json            to be traversed
     * @param attributePrefix String prefix used for attributes
     * @param arrayEntryTag   String used as the tag in the arrays
     * @return List of XML items generated during the traversal.
     */
    @SuppressWarnings("rawtypes")
    private static List<XMLValue> traverseTree(Object json, String attributePrefix, String arrayEntryTag) {
        List<XMLValue> xmlArray = new ArrayList<>();
        if (!(json instanceof RefValue)) {
            XMLValue xml = XMLFactory.parse(json.toString());
            xmlArray.add(xml);
        } else {
            traverseJsonNode(json, null, null, xmlArray, attributePrefix, arrayEntryTag);
        }
        return xmlArray;
    }

    /**
     * Traverse a JSON node ad produces the corresponding xml items.
     *
     * @param json               to be traversed
     * @param nodeName           name of the current traversing node
     * @param parentElement      parent element of the current node
     * @param omElementArrayList List of XML items generated
     * @param attributePrefix    String prefix used for attributes
     * @param arrayEntryTag      String used as the tag in the arrays
     * @return List of XML items generated during the traversal.
     */
    @SuppressWarnings("rawtypes")
    private static OMElement traverseJsonNode(Object json, String nodeName, OMElement parentElement,
                                              List<XMLValue> omElementArrayList, String attributePrefix,
                                              String arrayEntryTag) {
        OMElement currentRoot = null;
        if (nodeName != null) {
            // Extract attributes and set to the immediate parent.
            if (nodeName.startsWith(attributePrefix)) {
                if (json instanceof RefValue) {
                    throw BallerinaErrors.createError("attribute cannot be an object or array");
                }
                if (parentElement != null) {
                    String attributeKey = nodeName.substring(1);
                    // Validate whether the attribute name is an XML supported qualified name, according to the XML
                    // recommendation.
                    XMLValidator.validateXMLName(attributeKey);

                    parentElement.addAttribute(attributeKey, json.toString(), null);
                }
                return parentElement;
            }

            // Validate whether the tag name is an XML supported qualified name, according to the XML recommendation.
            XMLValidator.validateXMLName(nodeName);

            currentRoot = OM_FACTORY.createOMElement(nodeName, null);
        }

        if (json == null) {
            OMNamespace xsiNameSpace = OM_FACTORY.createOMNamespace(XSI_NAMESPACE, XSI_PREFIX);
            currentRoot.addAttribute(NIL, "true", xsiNameSpace);
        } else {
            LinkedHashMap<String, Object> map;

            BType type = TypeChecker.getType(json);
            switch (type.getTag()) {

                case TypeTags.MAP_TAG:
                    if (((BMapType) type).getConstrainedType().getTag() != TypeTags.JSON_TAG) {
                        throw BallerinaErrors.createError("error in converting map<non-json> to xml");
                    }
                    map = (MapValueImpl) json;
                    for (Entry<String, Object> entry : map.entrySet()) {
                        currentRoot = traverseJsonNode(entry.getValue(), entry.getKey(), currentRoot,
                                omElementArrayList, attributePrefix, arrayEntryTag);
                        if (nodeName == null) { // Outermost object
                            // todo: fix this
                            //omElementArrayList.add(new XMLItem(currentRoot));
                            currentRoot = null;
                        }
                    }
                    break;
                case TypeTags.JSON_TAG:
                    map = (MapValueImpl) json;
                    for (Entry<String, Object> entry : map.entrySet()) {
                        currentRoot = traverseJsonNode(entry.getValue(), entry.getKey(), currentRoot,
                                omElementArrayList, attributePrefix, arrayEntryTag);
                        if (nodeName == null) { // Outermost object
                            //omElementArrayList.add(new XMLItem(currentRoot));
                            currentRoot = null;
                        }
                    }
                    break;
                case TypeTags.ARRAY_TAG:
                    ArrayValue array = (ArrayValue) json;
                    for (int i = 0; i < array.size(); i++) {
                        currentRoot = traverseJsonNode(array.getRefValue(i), arrayEntryTag, currentRoot,
                                omElementArrayList, attributePrefix, arrayEntryTag);
                        if (nodeName == null) { // Outermost array
                            //omElementArrayList.add(new XMLItem(currentRoot));
                            currentRoot = null;
                        }
                    }
                    break;
                case TypeTags.INT_TAG:
                case TypeTags.FLOAT_TAG:
                case TypeTags.DECIMAL_TAG:
                case TypeTags.STRING_TAG:
                case TypeTags.BOOLEAN_TAG:
                    if (currentRoot == null) {
                        throw BallerinaErrors.createError("error in converting json to xml");
                    }

                    OMText txt1 = OM_FACTORY.createOMText(currentRoot, json.toString());
                    currentRoot.addChild(txt1);
                    break;
                default:
                    throw BallerinaErrors.createError("error in converting json to xml");
            }
        }

        // Set the current constructed root the parent element
        if (parentElement != null) {
            parentElement.addChild(currentRoot);
            currentRoot = parentElement;
        }
        return currentRoot;
    }
}
