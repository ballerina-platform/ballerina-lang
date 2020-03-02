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

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.XMLValidator;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.jvm.values.XMLItem;
import org.ballerinalang.jvm.values.XMLQName;
import org.ballerinalang.jvm.values.XMLSequence;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.api.BXML;

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
    public static XMLValue convertToXML(Object json, String attributePrefix, String arrayEntryTag) {
        if (json == null) {
            return new XMLSequence();
        }

        List<XMLValue> xmlElemList = traverseTree(json, attributePrefix, arrayEntryTag);
        if (xmlElemList.size() == 1) {
            return xmlElemList.get(0);
        } else {
            ArrayList<BXML> seq = new ArrayList<>(xmlElemList);
            return new XMLSequence(seq);
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
     * @param xmlElemList List of XML items generated
     * @param attributePrefix    String prefix used for attributes
     * @param arrayEntryTag      String used as the tag in the arrays
     * @return List of XML items generated during the traversal.
     */
    @SuppressWarnings("rawtypes")
    private static XMLItem traverseJsonNode(Object json, String nodeName, XMLItem parentElement,
                                              List<XMLValue> xmlElemList, String attributePrefix,
                                              String arrayEntryTag) {
        XMLItem currentRoot = null;
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

                    parentElement.setAttribute(attributeKey, null, null, json.toString());
                }
                return parentElement;
            }

            // Validate whether the tag name is an XML supported qualified name, according to the XML recommendation.
            XMLValidator.validateXMLName(nodeName);

            XMLQName tagName = new XMLQName(nodeName);
            currentRoot = (XMLItem) XMLFactory.createXMLElement(tagName, tagName, (BString) null);
        }

        if (json == null) {
            currentRoot.setAttribute(NIL, XSI_NAMESPACE, XSI_PREFIX, "true");
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
                                xmlElemList, attributePrefix, arrayEntryTag);
                        if (nodeName == null) { // Outermost object
                            xmlElemList.add(currentRoot);
                            currentRoot = null;
                        }
                    }
                    break;
                case TypeTags.JSON_TAG:
                    map = (MapValueImpl) json;
                    for (Entry<String, Object> entry : map.entrySet()) {
                        currentRoot = traverseJsonNode(entry.getValue(), entry.getKey(), currentRoot,
                                xmlElemList, attributePrefix, arrayEntryTag);
                        if (nodeName == null) { // Outermost object
                            xmlElemList.add(currentRoot);
                            currentRoot = null;
                        }
                    }
                    break;
                case TypeTags.ARRAY_TAG:
                    ArrayValue array = (ArrayValue) json;
                    for (int i = 0; i < array.size(); i++) {
                        currentRoot = traverseJsonNode(array.getRefValue(i), arrayEntryTag, currentRoot,
                                xmlElemList, attributePrefix, arrayEntryTag);
                        if (nodeName == null) { // Outermost array
                            xmlElemList.add(currentRoot);
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

                    XMLValue text = XMLFactory.createXMLText(json.toString());
                    addChildElem(currentRoot, text);
                    break;
                default:
                    throw BallerinaErrors.createError("error in converting json to xml");
            }
        }

        // Set the current constructed root the parent element
        if (parentElement != null) {
            addChildElem(parentElement, currentRoot);
            currentRoot = parentElement;
        }
        return currentRoot;
    }

    private static void addChildElem(XMLItem currentRoot, XMLValue child) {
        currentRoot.getChildrenSeq().getChildrenList().add(child);
    }
}
