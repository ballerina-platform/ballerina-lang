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
package io.ballerina.runtime.internal.values;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypeId;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.types.BObjectType;
import io.ballerina.runtime.internal.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static io.ballerina.runtime.api.constants.RuntimeConstants.DOT;
import static io.ballerina.runtime.api.constants.RuntimeConstants.OBJECT_LANG_LIB;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.INVALID_UPDATE_ERROR_IDENTIFIER;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;

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
    private final BTypedesc typedesc;
    private final BObjectType objectType;
    private final Type type;

    private final HashMap<String, Object> nativeData = new HashMap<>();

    public AbstractObjectValue(Type type) {
        this.type = type;
        this.objectType = (BObjectType) TypeUtils.getReferredType(type);
        this.typedesc = new TypedescValueImpl(type);
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
        return "object " + objectType.toString();
    }

    @Override
    public String informalStringValue(BLink parent) {
        return stringValue(parent);
    }

    @Override
    public String expressionStringValue(BLink parent) {
        if (objectType.typeIdSet == null) {
            return "object " + this.hashCode();
        }
        StringJoiner sj = new StringJoiner("&");
        List<TypeId> typeIds = objectType.typeIdSet.getIds();
        for (TypeId typeId : typeIds) {
            String pkg = typeId.getPkg().toString();
            if (DOT.equals(pkg)) {
                sj.add(typeId.getName());
            } else {
                sj.add("{" + pkg + "}" + typeId.getName());
            }
        }
        return "object " + sj + " " + this.hashCode();
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
    public Type getType() {
        return type;
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        return this;
    }

    @Override
    public void freezeDirect() {
    }

    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        return this;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ", "{", "}");
        for (Map.Entry<String, Field> field : this.objectType.getFields().entrySet()) {
            if (!SymbolFlags.isFlagOn(field.getValue().getFlags(), SymbolFlags.PUBLIC)) {
                continue;
            }
            String fieldName = field.getKey();
            sj.add(fieldName + ":" + getStringValue(get(StringUtils.fromString(fieldName))));
        }

        return sj.toString();
    }

    @Override
    public BTypedesc getTypedesc() {
        return typedesc;
    }

    private String getStringValue(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return "\"" + value + "\"";
        } else {
            return value.toString();
        }
    }

    protected void checkFieldUpdate(String fieldName, Object value) {
        if (objectType.isReadOnly()) {
            throw ErrorCreator.createError(
                    getModulePrefixedReason(OBJECT_LANG_LIB, INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER),
                    BLangExceptionHelper.getErrorDetails(RuntimeErrors.INVALID_READONLY_VALUE_UPDATE));
        }

        Field field = objectType.getFields().get(fieldName);

        if (SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.FINAL)) {
            throw ErrorCreator.createError(
                    getModulePrefixedReason(OBJECT_LANG_LIB, INVALID_UPDATE_ERROR_IDENTIFIER),
                    BLangExceptionHelper.getErrorDetails(RuntimeErrors.OBJECT_INVALID_FINAL_FIELD_UPDATE,
                                                         fieldName, objectType));
        }
        checkFieldUpdateType(fieldName, value);
    }

    private void checkFieldUpdateType(String fieldName, Object value) {
        Type fieldType = objectType.getFields().get(fieldName).getFieldType();
        if (TypeChecker.checkIsType(value, fieldType)) {
            return;
        }

        throw ErrorCreator.createError(getModulePrefixedReason(OBJECT_LANG_LIB,
                        INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER),
                BLangExceptionHelper.getErrorDetails(RuntimeErrors.INVALID_OBJECT_FIELD_VALUE_ERROR,
                        fieldName, fieldType, TypeChecker.getType(value)));
    }
}
