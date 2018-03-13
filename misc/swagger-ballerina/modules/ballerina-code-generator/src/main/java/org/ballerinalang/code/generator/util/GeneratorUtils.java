/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.code.generator.util;

import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.expressions.AnnotationAttachmentAttributeNode;
import org.ballerinalang.model.tree.expressions.AnnotationAttachmentAttributeValueNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilities used by ballerina code generator.
 */
public class GeneratorUtils {

    /**
     * Retrieve a specific annotation by name from a list of annotations.
     *
     * @param name        name of the required annotation
     * @param pkg         package of the required annotation
     * @param annotations list of annotations containing the required annotation
     * @return returns annotation with the name <code>name</code> if found or
     * null if annotation not found in the list
     */
    public static AnnotationAttachmentNode getAnnotationFromList(String name, String pkg,
            List<? extends AnnotationAttachmentNode> annotations) {
        AnnotationAttachmentNode annotation = null;
        if (name == null || pkg == null) {
            return null;
        }

        for (AnnotationAttachmentNode ann : annotations) {
            if (pkg.equals(ann.getPackageAlias().getValue()) && name.equals(ann.getAnnotationName().getValue())) {
                annotation = ann;
            }
        }

        return annotation;
    }

    /**
     * Retrieve {@link Map} representation of a Ballerina Attribute list
     *
     * @param attrs attribute list
     * @return Map of attributes with attribute name as the key. Empty Map will be returned if list is empty.
     */
    public static Map<String, AnnotationAttachmentAttributeNode> getAttributeMap(
            List<? extends AnnotationAttachmentAttributeNode> attrs) {
        Map<String, AnnotationAttachmentAttributeNode> attrMap = new HashMap<>();
        attrs.forEach(attr -> attrMap.put(attr.getName().getValue(), attr));

        return attrMap;
    }

    /**
     * Retrieve String value of {@code attr}.
     *
     * @param attr Attribute with non array type value
     * @return value of the attribute or {@code null} if {@code attr} is null
     */
    public static String getAttributeValue(AnnotationAttachmentAttributeNode attr) {
        if (attr == null) {
            return null;
        }

        return attr.getValue().getValue().toString();
    }

    /**
     * Retrieve value at specific index from array type attribute node.
     *
     * @param attr Attribute with array type values
     * @param index index of the value to be retrieved
     * @return value in the {@code index}<sup>th</sup> location of value list
     * <p>null will be returned in following cases</p>
     * <ul>
     *     <li>{@code attr is null}</li>
     *     <li>{@code attr} doesn't contain array type values</li>
     * </ul>
     */
    public static String getAttributeValue(AnnotationAttachmentAttributeNode attr, int index) {
        String value = null;
        if (attr == null) {
            return null;
        }

        String[] values = getAttributeValueArray(attr);
        if (values != null && values.length >= index) {
            value = values[index];
        }

        return value;
    }

    /**
     * Retrieve attribute value list of a Array type attribute.
     *
     * @param attr Attribute with array type values
     * @return array of Strings containing all values for {@code attr}.
     * <p>null will be returned in following two cases</p>
     * <ul>
     *     <li>{@code attr} is null</li>
     *     <li>{@code attr} doesn't contain array type values</li>
     * </ul>
     */
    public static String[] getAttributeValueArray(AnnotationAttachmentAttributeNode attr) {
        String[] values = null;
        if (attr == null || attr.getValue().getValueArray() == null) {
            return values;
        }

        List<? extends AnnotationAttachmentAttributeValueNode> valueList = attr.getValue().getValueArray();
        values = new String[valueList.size()];

        for (int i = 0; i < valueList.size(); i++) {
            values[i] = valueList.get(i).getValue().toString();
        }

        return values;
    }
}
