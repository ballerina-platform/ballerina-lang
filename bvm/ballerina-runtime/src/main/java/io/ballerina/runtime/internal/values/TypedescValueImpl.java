/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.internal.values;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BInitialValueEntry;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BListInitialValueEntry;
import io.ballerina.runtime.api.values.BMapInitialValueEntry;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.internal.scheduling.Strand;
import io.ballerina.runtime.internal.types.BAnnotatableType;
import io.ballerina.runtime.internal.types.BTypedescType;
import io.ballerina.runtime.internal.util.exceptions.BallerinaException;

import java.util.Map;

import static io.ballerina.runtime.api.utils.TypeUtils.getReferredType;

/**
 * <p>
 * Ballerina runtime value representation of a *type*. This class will be extended by the generated
 * typedesc classes.
 *
 * {@code typedesc} is used to describe type of a value in Ballerina.
 * For example {@code typedesc} of number 5 is {@code int}, where as {@code typedesc} of a record value is the
 * record type that used to create this particular value instance.
 * </p>
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p>
 *  
 * @since 0.995.0
 */
public class TypedescValueImpl implements  TypedescValue {

    final Type type;
    final Type describingType; // Type of the value describe by this typedesc.
    public MapValue[] closures;
    public MapValue annotations;

    @Deprecated
    public TypedescValueImpl(Type describingType) {
        this.type = new BTypedescType(describingType);
        this.describingType = describingType;
    }

    @Deprecated
    public TypedescValueImpl(Type describingType, MapValue[] closures) {
        this.type = new BTypedescType(describingType);
        this.describingType = describingType;
        this.closures = closures;
    }

    public TypedescValueImpl(Type describingType, MapValue[] closures, MapValue annotations) {
        this(describingType, closures);
        this.annotations = annotations;
        ((BAnnotatableType) describingType).setAnnotations(annotations);
    }

    /**
     * Returns the {@code BType} of the value describe by this type descriptor.
     * @return describing type
     */
    public Type getDescribingType() {
        return describingType;
    }

    @Override
    public Object instantiate(Strand s) {
        if (describingType.getTag() == TypeTags.MAP_TAG || describingType.getTag() == TypeTags.RECORD_TYPE_TAG) {
            return instantiate(s, new MappingInitialValueEntry[0]);
        }

        return instantiate(s, new BInitialValueEntry[0]);
    }

    public MapValue getAnnotations() {
        return annotations;
    }

    @Override
    public Object instantiate(Strand s, BInitialValueEntry[] initialValues) {
        Type referredType = getReferredType(this.describingType);
        if (referredType.getTag() == TypeTags.MAP_TAG) {
            return new MapValueImpl(this.describingType, (BMapInitialValueEntry[]) initialValues);
        } else if (referredType.getTag() == TypeTags.TUPLE_TAG) {
            return new TupleValueImpl(this.describingType, (BListInitialValueEntry[]) initialValues, this);
        }
        // This method will be overridden for user-defined types, therefor this line shouldn't be reached.
        throw new BallerinaException("Given type can't be instantiated at runtime : " + this.describingType);
    }

    @Override
    public String stringValue(BLink parent) {
        return "typedesc " + describingType.toString();
    }

    @Override
    public String expressionStringValue(BLink parent) {
        return stringValue(parent);
    }

    @Override
    public String toString() {
        return stringValue(null);
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
        return;
    }

    @Override
    public BTypedesc getTypedesc() {
        return new TypedescValueImpl(this.type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        return this;
    }
}
