/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.langserver.test;

import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

/**
 * This class contains Util methods for the {@link TestGenerator}.
 */
public class TestGeneratorUtil {
    /**
     * Visit annotation.
     *
     * @param annotation {@link BLangAnnotationAttachment}
     * @param consumer   {@link BiConsumer} function
     */
    public static void visitAnnotation(BLangAnnotationAttachment annotation,
                                       BiConsumer<BLangRecordLiteral.BLangRecordKeyValue, BLangSimpleVarRef> consumer) {
        if (annotation.expr instanceof BLangRecordLiteral) {
            BLangRecordLiteral record = (BLangRecordLiteral) annotation.expr;
            for (BLangRecordLiteral.BLangRecordKeyValue keyValue : record.keyValuePairs) {
                BLangRecordLiteral.BLangRecordKey key = keyValue.key;
                if (key.expr instanceof BLangSimpleVarRef) {
                    BLangSimpleVarRef varRef = (BLangSimpleVarRef) key.expr;
                    consumer.accept(keyValue, varRef);
                }
            }
        }
    }

    /**
     * Visit each records.
     *
     * @param records  list of {@link BLangRecordLiteral.BLangRecordKeyValue}
     * @param consumer {@link BiConsumer} function
     */
    public static void visitRecords(List<BLangRecordLiteral.BLangRecordKeyValue> records,
                                    BiConsumer<BLangRecordLiteral.BLangRecordKeyValue, BLangSimpleVarRef> consumer) {
        for (BLangRecordLiteral.BLangRecordKeyValue keyValue : records) {
            BLangRecordLiteral.BLangRecordKey key = keyValue.key;
            if (key.expr instanceof BLangSimpleVarRef) {
                BLangSimpleVarRef varRef = (BLangSimpleVarRef) key.expr;
                consumer.accept(keyValue, varRef);
            }
        }
    }

    /**
     * Returns a value of this field.
     *
     * @param fieldName  field name
     * @param annotation {@link BLangAnnotationAttachment}
     * @return field value
     */
    public static String getAnnotationValueAsString(String fieldName, BLangAnnotationAttachment annotation) {
        final String[] fieldValue = {null};
        TestGeneratorUtil.visitAnnotation(annotation, (keyValue, varRef) -> {
            String variableName = varRef.variableName.value;
            if (fieldName.equalsIgnoreCase(variableName)) {
                if (keyValue.valueExpr instanceof BLangLiteral) {
                    fieldValue[0] = String.valueOf(((BLangLiteral) keyValue.valueExpr).value);
                }
            }
        });
        return fieldValue[0];
    }

    /**
     * Returns a list of values of this field.
     *
     * @param fieldName  field name
     * @param annotation {@link BLangAnnotationAttachment}
     * @return list of field values
     */
    public static List<String> getAnnotationValueAsArray(String fieldName, BLangAnnotationAttachment annotation) {
        List<String> fieldValsList = new ArrayList<>();
        TestGeneratorUtil.visitAnnotation(annotation, (keyValue, varRef) -> {
            String variableName = varRef.variableName.value;
            if (fieldName.equalsIgnoreCase(variableName)) {
                if (keyValue.valueExpr instanceof BLangArrayLiteral) {
                    BLangArrayLiteral values = (BLangArrayLiteral) keyValue.valueExpr;
                    List<BLangExpression> arr = values.exprs;
                    for (BLangExpression exp : arr) {
                        if (exp instanceof BLangLiteral) {
                            fieldValsList.add(String.valueOf(((BLangLiteral) exp).value));
                        }
                    }
                }
            }
        });
        return fieldValsList;
    }

    /**
     * Returns value of this field.
     *
     * @param fieldName field name
     * @param record    {@link BLangRecordLiteral}
     * @return field value as string
     */
    public static String getRecordValueAsString(String fieldName, BLangRecordLiteral record) {
        return String.valueOf(getRecordValue(fieldName, record));
    }

    /**
     * Returns value of this field.
     *
     * @param fieldName field name
     * @param record    {@link BLangRecordLiteral}
     * @return {@link Object} field value
     */
    public static Object getRecordValue(String fieldName, BLangRecordLiteral record) {
        final Object[] fieldValue = {null};
        TestGeneratorUtil.visitRecords(record.keyValuePairs, (keyValue, varRef) -> {
            String variableName = varRef.variableName.value;
            if (fieldName.equalsIgnoreCase(variableName)) {
                fieldValue[0] = keyValue.valueExpr;
            }
        });
        return fieldValue[0];
    }

    /**
     * Returns True if the value of the field exists, False otherwise.
     *
     * @param fieldName field name
     * @param record    {@link BLangRecordLiteral}
     * @return True if the value exists, False otherwise.
     */
    public static boolean isRecordValueExists(String fieldName, BLangRecordLiteral record) {
        final boolean[] fieldValue = {false};
        TestGeneratorUtil.visitRecords(record.keyValuePairs, (keyValue, varRef) -> {
            String variableName = varRef.variableName.value;
            if (fieldName.equalsIgnoreCase(variableName)) {
                fieldValue[0] = true;
            }
        });
        return fieldValue[0];
    }

    /**
     * Uppercase case the first letter of this string.
     *
     * @param name name to be converted
     * @return converted string
     */
    public static String upperCaseFirstLetter(String name) {
        return name.substring(0, 1).toUpperCase(Locale.getDefault()) + name.substring(1);
    }

    /**
     * Lowercase the first letter of this string.
     *
     * @param name name to be converted
     * @return converted string
     */
    public static String lowerCaseFirstLetter(String name) {
        return name.substring(0, 1).toLowerCase(Locale.getDefault()) + name.substring(1);
    }
}
