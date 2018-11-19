/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.command.testgen;

import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * This class provides capabilities for processing annotation configurations.
 *
 * @since 0.985.0
 */
public class AnnotationConfigsProcessor {
    /**
     * Visit annotation for the provided annotations consumer.
     *
     * @param annotation {@link BLangAnnotationAttachment}
     * @param consumer   {@link BiConsumer} annotations consumer
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
     * Visit each records for the provided annotations consumer.
     *
     * @param records  list of {@link BLangRecordLiteral.BLangRecordKeyValue}
     * @param consumer {@link BiConsumer} annotations consumer
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
     * Search annotations and returns string field value by field name.
     *
     * @param fieldName  field name
     * @param annotation {@link BLangAnnotationAttachment}
     * @return if found, returns field value string or null otherwise
     */
    public static Optional<String> searchStringField(String fieldName, BLangAnnotationAttachment annotation) {
        final String[] fieldValue = {null};
        visitAnnotation(annotation, (keyValue, varRef) -> {
            String variableName = varRef.variableName.value;
            if (fieldName.equalsIgnoreCase(variableName) && keyValue.valueExpr instanceof BLangLiteral) {
                fieldValue[0] = String.valueOf(((BLangLiteral) keyValue.valueExpr).value);
            }
        });
        return Optional.ofNullable(fieldValue[0]);
    }

    /**
     * Search annotations and returns array field value by field name.
     *
     * @param fieldName  field name
     * @param annotation {@link BLangAnnotationAttachment}
     * @return if found, returns list of field values or empty list otherwise
     */
    public static List<String> searchArrayField(String fieldName, BLangAnnotationAttachment annotation) {
        List<String> fieldValsList = new ArrayList<>();
        visitAnnotation(annotation, (keyValue, varRef) -> {
            String variableName = varRef.variableName.value;
            if (fieldName.equalsIgnoreCase(variableName) && keyValue.valueExpr instanceof BLangArrayLiteral) {
                BLangArrayLiteral values = (BLangArrayLiteral) keyValue.valueExpr;
                List<BLangExpression> arr = values.exprs;
                for (BLangExpression exp : arr) {
                    if (exp instanceof BLangLiteral) {
                        fieldValsList.add(String.valueOf(((BLangLiteral) exp).value));
                    }
                }
            }
        });
        return fieldValsList;
    }

    /**
     * Search records and returns string field value by field name.
     *
     * @param fieldName field name
     * @param record    {@link BLangRecordLiteral}
     * @return if found, returns field value string or null otherwise
     */
    public static Optional<String> searchStringField(String fieldName, BLangRecordLiteral record) {
        Optional<Object> field = searchArrayField(fieldName, record);
        return field.map(String::valueOf);
    }

    /**
     * Search records and returns field value object by field name.
     *
     * @param fieldName field name
     * @param record    {@link BLangRecordLiteral}
     * @return if found, returns field value {@link Object} or null otherwise
     */
    public static Optional<Object> searchArrayField(String fieldName, BLangRecordLiteral record) {
        final Object[] fieldValue = {null};
        visitRecords(record.keyValuePairs, (keyValue, varRef) -> {
            String variableName = varRef.variableName.value;
            if (fieldName.equalsIgnoreCase(variableName)) {
                fieldValue[0] = keyValue.valueExpr;
            }
        });
        return Optional.ofNullable(fieldValue[0]);
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
        visitRecords(record.keyValuePairs, (keyValue, varRef) -> {
            String variableName = varRef.variableName.value;
            if (fieldName.equalsIgnoreCase(variableName)) {
                fieldValue[0] = true;
            }
        });
        return fieldValue[0];
    }
}
