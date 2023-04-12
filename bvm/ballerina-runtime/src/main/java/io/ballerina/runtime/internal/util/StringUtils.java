/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal.util;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BValue;
import io.ballerina.runtime.internal.CycleUtils;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.values.AbstractObjectValue;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.DecimalValue;
import io.ballerina.runtime.internal.values.MapValueImpl;
import io.ballerina.runtime.internal.values.RefValue;

/**
 * Common utility methods used for String manipulation.
 *
 * @since 2201.6.0
 */
public class StringUtils {

    public static final String STR_CYCLE = "...";
    public static final String TO_STRING = "toString";

    private StringUtils() {}

    /**
     * Returns the human-readable string value of Ballerina values.
     *
     * @param value     The value on which the function is invoked
     * @param parent    The link to the parent node
     * @return          String value of the value
     */
    public static String getStringVal(Object value, BLink parent) {
        if (value == null) {
            return "";
        }

        Type type = TypeUtils.getReferredType(TypeChecker.getType(value));

        if (type.getTag() == TypeTags.STRING_TAG) {
            return ((BString) value).getValue();
        }

        if (type.getTag() < TypeTags.NULL_TAG) {
            return String.valueOf(value);
        }

        CycleUtils.Node node = new CycleUtils.Node(value, parent);

        if (node.hasCyclesSoFar()) {
            return STR_CYCLE;
        }

        if (type.getTag() == TypeTags.MAP_TAG || type.getTag() == TypeTags.RECORD_TYPE_TAG) {
            MapValueImpl mapValue = (MapValueImpl) value;
            return mapValue.stringValue(parent);
        }

        if (type.getTag() == TypeTags.ARRAY_TAG || type.getTag() == TypeTags.TUPLE_TAG) {
            ArrayValue arrayValue = (ArrayValue) value;
            return arrayValue.stringValue(parent);
        }

        if (type.getTag() == TypeTags.TABLE_TAG) {
            return ((RefValue) value).informalStringValue(parent);
        }

        if (type.getTag() == TypeTags.OBJECT_TYPE_TAG) {
            BObject objectValue = (BObject) value;
            ObjectType objectType = (ObjectType) TypeUtils.getReferredType(objectValue.getType());
            for (MethodType func : objectType.getMethods()) {
                if (func.getName().equals(TO_STRING) && func.getParameters().length == 0 &&
                        func.getType().getReturnType().getTag() == TypeTags.STRING_TAG) {
                    return objectValue.call(Scheduler.getStrand(), TO_STRING).toString();
                }
            }
        }

        BValue bValue = (BValue) value;
        return bValue.stringValue(parent);
    }

    /**
     * Returns the string value of Ballerina values in expression style.
     *
     * @param value The value on which the function is invoked
     * @param parent The link to the parent node
     * @return String value of the value in expression style
     */
    public static String getExpressionStringVal(Object value, BLink parent) {
        if (value == null) {
            return "()";
        }

        Type type = TypeUtils.getReferredType(TypeChecker.getType(value));

        if (type.getTag() == TypeTags.STRING_TAG) {
            return "\"" + ((BString) value).getValue() + "\"";
        }

        if (type.getTag() == TypeTags.DECIMAL_TAG) {
            DecimalValue decimalValue = (DecimalValue) value;
            return decimalValue.expressionStringValue(parent);
        }

        if (type.getTag() == TypeTags.FLOAT_TAG) {
            if (Double.isNaN((Double) value)) {
                return "float:" + value;
            }
            if (Double.isInfinite((Double) value)) {
                return "float:" + value;
            }
        }

        if (type.getTag() < TypeTags.NULL_TAG) {
            return String.valueOf(value);
        }

        CycleUtils.Node node = new CycleUtils.Node(value, parent);

        if (node.hasCyclesSoFar()) {
            return STR_CYCLE + "[" + node.getIndex() + "]";
        }

        if (type.getTag() == TypeTags.MAP_TAG || type.getTag() == TypeTags.RECORD_TYPE_TAG) {
            MapValueImpl mapValue = (MapValueImpl) value;
            return mapValue.expressionStringValue(parent);
        }

        if (type.getTag() == TypeTags.ARRAY_TAG || type.getTag() == TypeTags.TUPLE_TAG) {
            ArrayValue arrayValue = (ArrayValue) value;
            return arrayValue.expressionStringValue(parent);
        }

        if (type.getTag() == TypeTags.TABLE_TAG) {
            return ((RefValue) value).expressionStringValue(parent);
        }

        if (type.getTag() == TypeTags.OBJECT_TYPE_TAG) {
            AbstractObjectValue objectValue = (AbstractObjectValue) value;
            ObjectType objectType = (ObjectType) TypeUtils.getReferredType(objectValue.getType());
            for (MethodType func : objectType.getMethods()) {
                if (func.getName().equals(TO_STRING) && func.getParameters().length == 0 &&
                        func.getType().getReturnType().getTag() == TypeTags.STRING_TAG) {
                    return "object " + objectValue.call(Scheduler.getStrand(), TO_STRING).toString();
                }
            }
        }

        RefValue refValue = (RefValue) value;
        return refValue.expressionStringValue(parent);
    }
}
