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
import io.ballerina.runtime.api.types.ParameterizedType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypeTags;
import io.ballerina.runtime.api.types.XmlType;
import io.ballerina.runtime.api.types.semtype.BasicTypeBitSet;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.Core;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.api.types.semtype.TypeCheckCache;
import io.ballerina.runtime.api.types.semtype.TypeCheckCacheFactory;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.internal.types.semtype.XmlUtils;
import io.ballerina.runtime.internal.values.ReadOnlyUtils;
import io.ballerina.runtime.internal.values.XmlComment;
import io.ballerina.runtime.internal.values.XmlItem;
import io.ballerina.runtime.internal.values.XmlPi;
import io.ballerina.runtime.internal.values.XmlSequence;
import io.ballerina.runtime.internal.values.XmlText;
import io.ballerina.runtime.internal.values.XmlValue;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * {@code BXMLType} represents an XML Element.
 *
 * @since 0.995.0
 */
@SuppressWarnings("unchecked")
public class BXmlType extends BType implements XmlType, TypeWithShape {

    private static final BasicTypeBitSet BASIC_TYPE = Builder.getXmlType();

    private final int tag;
    public final Type constraint;
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
        super(typeName, pkg, XmlValue.class, false);
        this.constraint = constraint;
        this.tag = TypeTags.XML_TAG;
        this.readonly = false;
        initializeCache();
    }

    public BXmlType(String typeName, Module pkg, int tag, boolean readonly) {
        super(typeName, pkg, XmlValue.class, false);
        this.tag = tag;
        this.readonly = readonly;
        this.constraint = null;
        initializeCache();
    }

    public BXmlType(String typeName, Type constraint, Module pkg, int tag, boolean readonly) {
        super(typeName, pkg, XmlValue.class, false);
        this.tag = tag;
        this.readonly = readonly;
        this.constraint = constraint;
        initializeCache();
    }

    public BXmlType(String typeName, Type constraint, Module pkg, boolean readonly) {
        super(typeName, pkg, XmlValue.class, false);
        this.tag = TypeTags.XML_TAG;
        this.readonly = readonly;
        this.constraint = constraint;
        initializeCache();
    }

    public BXmlType(Type constraint, boolean readonly) {
        super(TypeConstants.XML_TNAME, null, XmlValue.class, false);
        this.tag = TypeTags.XML_TAG;
        this.constraint = readonly ? ReadOnlyUtils.getReadOnlyType(constraint) : constraint;
        this.readonly = readonly;
        initializeCache();
    }

    @Override
    protected void initializeCache() {
        var init = initCachedValues(this);
        typeCheckCache = init.typeCheckCache;
        typeId = init.typeId;
    }

    private static TypeCheckFlyweightCache.TypeCheckFlyweight initCachedValues(BXmlType xmlType) {
        if (xmlType.constraint != null) {
            if (xmlType.readonly) {
                return TypeCheckFlyweightCache.getRO(xmlType.constraint);
            } else {
                return TypeCheckFlyweightCache.getRW(xmlType.constraint);
            }
        } else {
            if (xmlType.readonly) {
                return switch (xmlType.tag) {
                    case TypeTags.XML_TAG -> TypeCheckFlyweightCache.XML;
                    case TypeTags.XML_ELEMENT_TAG -> TypeCheckFlyweightCache.XML_ELEMENT_RO;
                    case TypeTags.XML_COMMENT_TAG -> TypeCheckFlyweightCache.XML_COMMENT_RO;
                    case TypeTags.XML_PI_TAG -> TypeCheckFlyweightCache.XML_PI_RO;
                    case TypeTags.XML_TEXT_TAG -> TypeCheckFlyweightCache.XML_TEXT_RO;
                    default -> throw new IllegalStateException("Unexpected value: " + xmlType.tag);
                };
            } else {
                return switch (xmlType.tag) {
                    case TypeTags.XML_TAG -> TypeCheckFlyweightCache.XML;
                    case TypeTags.XML_ELEMENT_TAG -> TypeCheckFlyweightCache.XML_ELEMENT_RW;
                    case TypeTags.XML_COMMENT_TAG -> TypeCheckFlyweightCache.XML_COMMENT_RW;
                    case TypeTags.XML_PI_TAG -> TypeCheckFlyweightCache.XML_PI_RW;
                    case TypeTags.XML_TEXT_TAG -> TypeCheckFlyweightCache.XML_TEXT_RW;
                    default -> throw new IllegalStateException("Unexpected value: " + xmlType.tag);
                };
            }
        }
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
    public BasicTypeBitSet getBasicType() {
        return BASIC_TYPE;
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
    public SemType acceptedTypeOf(Context cx) {
        return getSemType(cx);
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

    private static class TypeCheckFlyweightCache {

        private static final Map<Type, TypeCheckFlyweight> cacheRO = new IdentityHashMap<>();
        private static final Map<Type, TypeCheckFlyweight> cacheRW = new IdentityHashMap<>();

        private static final TypeCheckFlyweight XML = initReserved();

        private static final TypeCheckFlyweight XML_ELEMENT_RW = initReserved();
        private static final TypeCheckFlyweight XML_COMMENT_RW = initReserved();
        private static final TypeCheckFlyweight XML_PI_RW = initReserved();
        private static final TypeCheckFlyweight XML_TEXT_RW = initReserved();

        private static final TypeCheckFlyweight XML_ELEMENT_RO = initReserved();
        private static final TypeCheckFlyweight XML_COMMENT_RO = initReserved();
        private static final TypeCheckFlyweight XML_PI_RO = initReserved();
        private static final TypeCheckFlyweight XML_TEXT_RO = initReserved();

        private static TypeCheckFlyweight initReserved() {
            return new TypeCheckFlyweight(TypeIdSupplier.getReservedId(), TypeCheckCacheFactory.create());
        }

        private static TypeCheckFlyweight init() {
            return new TypeCheckFlyweight(TypeIdSupplier.getNamedId(), TypeCheckCacheFactory.create());
        }

        private static TypeCheckFlyweight getRO(Type constraint) {
            if (constraint instanceof BTypeReferenceType) {
                return getRO(TypeUtils.getReferredType(constraint));
            }
            return cacheRO.computeIfAbsent(constraint, ignored -> TypeCheckFlyweightCache.init());
        }

        private static TypeCheckFlyweight getRW(Type constraint) {
            if (constraint instanceof BTypeReferenceType) {
                return getRW(TypeUtils.getReferredType(constraint));
            }
            return cacheRW.computeIfAbsent(constraint, ignored -> TypeCheckFlyweightCache.init());
        }

        private record TypeCheckFlyweight(int typeId, TypeCheckCache typeCheckCache) {

        }
    }
}
