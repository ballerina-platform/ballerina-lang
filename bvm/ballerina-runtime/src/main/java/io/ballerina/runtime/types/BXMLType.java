/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.types;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.TypeConstants;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.XMLType;
import io.ballerina.runtime.values.XMLSequence;
import io.ballerina.runtime.values.XMLValue;

/**
 * {@code BXMLType} represents an XML Element.
 *
 * @since 0.995.0
 */
@SuppressWarnings("unchecked")
public class BXMLType extends BType implements XMLType {

    private final int tag;
    public Type constraint;
    private final boolean readonly;
    private IntersectionType immutableType;

    /**
     * Create a {@code BXMLType} which represents the boolean type.
     *
     * @param typeName string name of the type
     * @param constraint constraint of the xml sequence
     */
    public BXMLType(String typeName, Type constraint, Module pkg) {
        super(typeName, pkg, XMLValue.class);
        this.constraint = constraint;
        this.tag = TypeTags.XML_TAG;
        this.readonly = false;
    }

    public BXMLType(String typeName, Module pkg, int tag, boolean readonly) {
        super(typeName, pkg, XMLValue.class);
        this.tag = tag;
        this.readonly = readonly;
    }

    public BXMLType(Type constraint, boolean readonly) {
        super(TypeConstants.XML_TNAME, null, XMLValue.class);
        this.tag = TypeTags.XML_TAG;
        this.constraint = constraint;
        this.readonly = readonly;
    }

    @Override
    public <V extends Object> V getZeroValue() {
        return (V) new XMLSequence();
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        return (V) new XMLSequence();
    }

    @Override
    public int getTag() {
        return this.tag;
    }

    @Override
    public boolean isAnydata() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(this == obj && obj instanceof BXMLType)) {
            return false;
        }

        BXMLType other = (BXMLType) obj;
        if (constraint == other.constraint) {
            return true;
        }

        return this.readonly == other.readonly && constraint.equals(other.constraint);
    }

    @Override
    public String toString() {
        if (constraint != null) {
            return TypeConstants.XML_TNAME + "<" + constraint + ">";
        }
        return super.toString();
    }

    @Override
    public boolean isReadOnly() {
        return this.readonly;
    }

    @Override
    public Type getImmutableType() {
        return this.immutableType;
    }

    @Override
    public void setImmutableType(IntersectionType immutableType) {
        this.immutableType = immutableType;
    }
}
