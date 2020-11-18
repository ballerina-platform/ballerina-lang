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
package io.ballerina.runtime.values;

import io.ballerina.runtime.TypeChecker;
import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.types.BObjectType;
import io.ballerina.runtime.util.Flags;
import io.ballerina.runtime.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.util.exceptions.BallerinaErrorReasons;
import io.ballerina.runtime.util.exceptions.RuntimeErrors;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import static io.ballerina.runtime.util.BLangConstants.OBJECT_LANG_LIB;
import static io.ballerina.runtime.util.exceptions.BallerinaErrorReasons.INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER;
import static io.ballerina.runtime.util.exceptions.BallerinaErrorReasons.INVALID_UPDATE_ERROR_IDENTIFIER;
import static io.ballerina.runtime.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;

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
    public long getIntValue(BString fieldName) {
        return (long) get(fieldName);
    }

    @Override
    public double getFloatValue(BString fieldName) {
        return (double) get(fieldName);
    }

    @Override
    public BString getStringValue(BString fieldName) {
        return (BString) get(fieldName);
    }

    @Override
    public String stringValue(BLink parent) {
        return "object " + type.toString();
    }

    @Override
    public String informalStringValue(BLink parent) {
        return stringValue(parent);
    }

    @Override
    public String expressionStringValue(BLink parent) {
        Module pkg = type.getPackage();
        String moduleLocalName = pkg.getOrg() != null && pkg.getOrg().equals("$anon") ||
                pkg.getName() == null ? type.getName() :
                String.valueOf(BallerinaErrorReasons.getModulePrefixedReason(pkg.getName(), type.getName()));
        return "object " + moduleLocalName + " " + this.hashCode();
    }

    @Override
    public boolean getBooleanValue(BString fieldName) {
        return (boolean) get(fieldName);
    }

    @Override
    public BMap getMapValue(BString fieldName) {
        return (MapValueImpl) get(fieldName);
    }

    @Override
    public BObject getObjectValue(BString fieldName) {
        return (BObject) get(fieldName);
    }

    @Override
    public BArray getArrayValue(BString fieldName) {
        return (ArrayValue) get(fieldName);
    }

    @Override
    public ObjectType getType() {
        return type;
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        return this;
    }

    @Override
    public void freezeDirect() {
        return;
    }

    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        return this;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ", "{", "}");
        for (Map.Entry<String, Field> field : this.type.getFields().entrySet()) {
            if (!Flags.isFlagOn(field.getValue().getFlags(), Flags.PUBLIC)) {
                continue;
            }
            String fieldName = field.getKey();
            sj.add(fieldName + ":" + getStringValue(get(StringUtils.fromString(fieldName))));
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

    protected void checkFieldUpdateOnInitialization(String fieldName, Object value) {
        checkFieldUpdateType(fieldName, value);
    }

    protected void checkFieldUpdate(String fieldName, Object value) {
        if (type.isReadOnly()) {
            throw ErrorCreator.createError(
                    getModulePrefixedReason(OBJECT_LANG_LIB, INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER),
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.INVALID_READONLY_VALUE_UPDATE));
        }

        Field field = type.getFields().get(fieldName);

        if (Flags.isFlagOn(field.getFlags(), Flags.FINAL)) {
            throw ErrorCreator.createError(
                    getModulePrefixedReason(OBJECT_LANG_LIB, INVALID_UPDATE_ERROR_IDENTIFIER),
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.OBJECT_INVALID_FINAL_FIELD_UPDATE,
                                                         fieldName, type));
        }
        checkFieldUpdateType(fieldName, value);
    }

    private void checkFieldUpdateType(String fieldName, Object value) {
        Type fieldType = type.getFields().get(fieldName).getFieldType();
        if (TypeChecker.checkIsType(value, fieldType)) {
            return;
        }

        throw ErrorCreator.createError(getModulePrefixedReason(OBJECT_LANG_LIB,
                                                               INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER),
                                       StringUtils.fromString("invalid value for object field '" + fieldName +
                                                                       "': expected value of type '" + fieldType +
                                                                       "', found '" + TypeChecker.getType(value) +
                                                                       "'"));
    }
}
