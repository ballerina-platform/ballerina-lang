/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.jvm.values;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BObjectType;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.util.Flags;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import static org.ballerinalang.jvm.util.BLangConstants.OBJECT_LANG_LIB;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;

/**
 * <p>
 * Abstract class to be extended by all the ballerina objects.
 * </p>
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p>
 * 
 * @since 0.995.0
 */
public abstract class AbstractObjectValue implements ObjectValue {

    private BObjectType type;

    private final HashMap<String, Object> nativeData = new HashMap<>();

    public AbstractObjectValue(BObjectType type) {
        this.type = type;
    }

    @Override
    public abstract Object call(Strand strand, String funcName, Object... args);

    @Override
    public abstract Object get(String fieldName);

    @Override
    public abstract Object get(StringValue fieldName);

    @Override
    public abstract void set(String fieldName, Object value);

    @Override
    public abstract void set(StringValue fieldName, Object value);

    @Override
    public void addNativeData(String key, Object data) {
        this.nativeData.put(key, data);
    }

    @Override
    public Object getNativeData(String key) {
        return this.nativeData.get(key);
    }

    @Override
    public HashMap<String, Object> getNativeData() {
        return nativeData;
    }

    @Override
    public long getIntValue(String fieldName) {
        return (long) get(fieldName);
    }

    @Override
    public double getFloatValue(String fieldName) {
        return (double) get(fieldName);
    }

    @Override
    public String getStringValue(String fieldName) {
        return (String) get(fieldName);
    }

    @Override
    public String stringValue() {
        return "object " + type.toString();
    }

    @Override
    public boolean getBooleanValue(String fieldName) {
        return (boolean) get(fieldName);
    }

    @Override
    public MapValueImpl getMapValue(String fieldName) {
        return (MapValueImpl) get(fieldName);
    }

    @Override
    public ObjectValue getObjectValue(String fieldName) {
        return (ObjectValue) get(fieldName);
    }

    @Override
    public ArrayValue getArrayValue(String fieldName) {
        return (ArrayValue) get(fieldName);
    }

    @Override
    public BObjectType getType() {
        return type;
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ", "{", "}");
        for (Map.Entry<String, BField> field : ((BStructureType) this.type).getFields().entrySet()) {
            if (!Flags.isFlagOn(field.getValue().flags, Flags.PUBLIC)) {
                continue;
            }
            String fieldName = field.getKey();
            sj.add(fieldName + ":" + getStringValue(get(fieldName)));
        }

        return sj.toString();
    }

    private String getStringValue(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return "\"" + value.toString() + "\"";
        } else {
            return value.toString();
        }
    }

    protected void checkFieldUpdate(String fieldName, Object value) {
        BType fieldType = type.getFields().get(fieldName).type;
        if (TypeChecker.checkIsType(value, fieldType)) {
            return;
        }

        throw BallerinaErrors.createError(getModulePrefixedReason(OBJECT_LANG_LIB,
                                                                  INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER),
                "invalid value for object field '" + fieldName + "': expected value of type '" + fieldType +
                        "', found '" + TypeChecker.getType(value) + "'");
    }
}
