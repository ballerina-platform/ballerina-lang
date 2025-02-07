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
package io.ballerina.runtime.internal.types;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypeTags;
import io.ballerina.runtime.api.types.XmlType;
import io.ballerina.runtime.internal.values.ReadOnlyUtils;
import io.ballerina.runtime.internal.values.XmlSequence;
import io.ballerina.runtime.internal.values.XmlValue;

import java.util.Optional;

/**
 * {@code BXMLType} represents an XML Element.
 *
 * @since 0.995.0
 */
@SuppressWarnings("unchecked")
public class BXmlType extends BType implements XmlType {

    private final int tag;
    public Type constraint;
    private final boolean readonly;
    private IntersectionType immutableType;
    private IntersectionType intersectionType = null;

    /**
     * Create a {@code BXMLType} which represents the boolean type.
     *
     * @param typeName string name of the type
     * @param constraint constraint of the xml sequence
     */
    public BXmlType(String typeName, Type constraint, Module pkg) {
        super(typeName, pkg, XmlValue.class);
        this.constraint = constraint;
        this.tag = TypeTags.XML_TAG;
        this.readonly = false;
    }

    public BXmlType(String typeName, Module pkg, int tag, boolean readonly) {
        super(typeName, pkg, XmlValue.class);
        this.tag = tag;
        this.readonly = readonly;
        this.constraint = null;
    }

    public BXmlType(String typeName, Type constraint, Module pkg, boolean readonly) {
        super(typeName, pkg, XmlValue.class);
        this.tag = TypeTags.XML_TAG;
        this.readonly = readonly;
        this.constraint = constraint;
    }

    public BXmlType(Type constraint, boolean readonly) {
        super(TypeConstants.XML_TNAME, null, XmlValue.class);
        this.tag = TypeTags.XML_TAG;
        this.constraint = readonly ? ReadOnlyUtils.getReadOnlyType(constraint) : constraint;
        this.readonly = readonly;
    }

    @Override
    public <V extends Object> V getZeroValue() {
        return (V) new XmlSequence();
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        return (V) new XmlSequence();
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
        if (!(this == obj && obj instanceof BXmlType other)) {
            return false;
        }

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
    public IntersectionType getImmutableType() {
        return this.immutableType;
    }

    @Override
    public void setImmutableType(IntersectionType immutableType) {
        this.immutableType = immutableType;
    }

    @Override
    public Optional<IntersectionType> getIntersectionType() {
        return this.intersectionType ==  null ? Optional.empty() : Optional.of(this.intersectionType);
    }

    @Override
    public SemType createSemType(Context cx) {
        SemType semType;
        if (constraint == null) {
            semType = pickTopType();
        } else {
            SemType contraintSemtype;
            if (constraint instanceof ParameterizedType parameterizedType) {
                contraintSemtype = tryInto(cx, parameterizedType.getParamValueType());
            } else {
                contraintSemtype = tryInto(cx, constraint);
            }
            semType = XmlUtils.xmlSequence(contraintSemtype);
        }
        return isReadOnly() ? Core.intersect(Builder.getReadonlyType(), semType) : semType;
    }

    private SemType pickTopType() {
        return switch (tag) {
            case TypeTags.XML_TAG -> Builder.getXmlType();
            case TypeTags.XML_ELEMENT_TAG -> Builder.getXmlElementType();
            case TypeTags.XML_COMMENT_TAG -> Builder.getXmlCommentType();
            case TypeTags.XML_PI_TAG -> Builder.getXmlPIType();
            case TypeTags.XML_TEXT_TAG -> Builder.getXmlTextType();
            default -> throw new IllegalStateException("Unexpected value: " + tag);
        };
    }

    @Override
    public void setIntersectionType(IntersectionType intersectionType) {
        this.intersectionType = intersectionType;
    }

    @Override
    public Optional<SemType> inherentTypeOf(Context cx, ShapeSupplier shapeSupplier, Object object) {
        XmlValue xmlValue = (XmlValue) object;
        if (!isReadOnly(xmlValue)) {
            return Optional.of(getSemType(cx));
        }
        return readonlyShapeOf(object);
    }

    @Override
    public boolean couldInherentTypeBeDifferent() {
        return true;
    }

    @Override
    public Optional<SemType> shapeOf(Context cx, ShapeSupplier shapeSupplierFn, Object object) {
        return readonlyShapeOf(object).map(semType -> Core.intersect(semType, Builder.getReadonlyType()));
    }

    @Override
    public Optional<SemType> acceptedTypeOf(Context cx) {
        return Optional.of(getSemType(cx));
    }

    private Optional<SemType> readonlyShapeOf(Object object) {
        if (object instanceof XmlSequence xmlSequence) {
            // We represent xml<never> as an empty sequence
            var children = xmlSequence.getChildrenList();
            if (children.isEmpty()) {
                return Optional.of(XmlUtils.xmlSingleton(XmlUtils.XML_PRIMITIVE_NEVER));
            } else if (children.size() == 1) {
                // Not entirely sure if this is correct, but needed for passing tests
                return readonlyShapeOf(children.get(0));
            }
            return children.stream()
                    .map(this::readonlyShapeOf)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .reduce(Core::union)
                    .map(XmlUtils::xmlSequence);
        } else if (object instanceof XmlText) {
            // Text is inherently readonly
            return Optional.of(Builder.getXmlTextType());
        } else if (object instanceof XmlItem xml) {
            return getSemType(xml, Builder.getXmlElementType());
        } else if (object instanceof XmlComment xml) {
            return getSemType(xml, Builder.getXmlCommentType());
        } else if (object instanceof XmlPi xml) {
            return getSemType(xml, Builder.getXmlPIType());
        }
        throw new IllegalArgumentException("Unexpected xml value: " + object);
    }

    private static Optional<SemType> getSemType(XmlValue xml, SemType baseType) {
        if (isReadOnly(xml)) {
            return Optional.of(Core.intersect(baseType, Builder.getReadonlyType()));
        }
        return Optional.of(baseType);
    }

    private static boolean isReadOnly(XmlValue xmlValue) {
        if (xmlValue instanceof XmlSequence || xmlValue instanceof XmlText) {
            return true;
        }
        return xmlValue.getType().isReadOnly();
    }
}
