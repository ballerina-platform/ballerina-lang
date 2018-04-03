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
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValue;

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
     * Retrieve {@link Map} representation of a Ballerina Record Key Value list.
     *
     * @param list list of key value pairs
     * @return Map of key value pairs. Empty Map will be returned if list is empty.
     */
    public static Map<String, String[]> getKeyValuePairAsMap(List<BLangRecordKeyValue> list) {
        Map<String, String[]> attrMap = new HashMap<>();

        list.forEach(attr -> {
            // We don't accept struct type values for annotation attribute
            if (attr.getValue() instanceof BLangLiteral) {
                attrMap.put(attr.getKey().toString(), new String[] {attr.getValue().toString()});
            } else if (attr.getValue() instanceof BLangArrayLiteral) {
                List<? extends ExpressionNode> exprs = ((BLangArrayLiteral) attr.getValue()).getExpressions();
                String[] values = new String[exprs.size()];

                for (int i = 0; i < exprs.size(); i++) {
                    values[i] = ((BLangLiteral) exprs.get(i)).getValue().toString();
                }

                attrMap.put(attr.getKey().toString(), values);
            }
        });

        return attrMap;
    }

}
