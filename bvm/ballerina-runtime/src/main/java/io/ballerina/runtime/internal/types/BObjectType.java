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

import io.ballerina.identifier.Utils;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.FunctionType;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.api.types.ResourceMethodType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypeId;
import io.ballerina.runtime.api.types.TypeIdSet;
import io.ballerina.runtime.api.types.TypeTags;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.scheduling.Strand;
import io.ballerina.runtime.internal.utils.ValueUtils;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.CellAtomicType;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.Core;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.ValueUtils;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.scheduling.Strand;
import io.ballerina.runtime.internal.types.semtype.FunctionDefinition;
import io.ballerina.runtime.internal.types.semtype.ListDefinition;
import io.ballerina.runtime.internal.types.semtype.Member;
import io.ballerina.runtime.internal.types.semtype.ObjectDefinition;
import io.ballerina.runtime.internal.types.semtype.ObjectQualifiers;
import io.ballerina.runtime.internal.values.AbstractObjectValue;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.ballerina.runtime.api.types.TypeTags.SERVICE_TAG;

/**
 * {@code BObjectType} represents a user defined object type in Ballerina.
 *
 * @since 0.995.0
 */
public class BObjectType extends BStructureType implements ObjectType, PartialSemTypeSupplier, TypeWithShape {

    private MethodType[] methodTypes;
    private MethodType initMethod;
    public MethodType generatedInitMethod;

    private final boolean readonly;
    protected IntersectionType immutableType;
    private IntersectionType intersectionType = null;
    public BTypeIdSet typeIdSet;

    private String cachedToString;
    private boolean resolving;
    private ObjectDefinition od;
    private final Env env = Env.getInstance();
    // FIXME: better name
    private SemType softSemTypeCache;
    private final DistinctIdSupplier distinctIdSupplier;

    /**
     * Create a {@code BObjectType} which represents the user defined struct type.
     *
     * @param typeName string name of the type
     * @param pkg package of the struct
     * @param flags flags of the object type
     */
    public BObjectType(String typeName, Module pkg, long flags) {
        super(typeName, pkg, flags, Object.class);
        this.readonly = SymbolFlags.isFlagOn(flags, SymbolFlags.READONLY);
        this.distinctIdSupplier = new DistinctIdSupplier(env);
    }

    @Override
    public <V extends Object> V getZeroValue() {
        return (V) createObjectValueWithDefaultValues(this.pkg, this);
    }

    private static BObject createObjectValueWithDefaultValues(Module packageId, BObjectType objectType) {
        Strand currentStrand = Scheduler.getStrand();
        Map<String, Field> fieldsMap = objectType.getFields();
        Field[] fields = fieldsMap.values().toArray(new Field[0]);
        Object[] fieldValues = new Object[fields.length];

        for (int i = 0, j = 0; i < fields.length; i++) {
            Type type = fields[i].getFieldType();
            // Add default value of the field type as initial argument.
            fieldValues[j++] = type.getZeroValue();
        }
        return ValueUtils.createObjectValue(currentStrand, packageId, objectType.getName(), fieldValues);
    }

    @Override
    public String getAnnotationKey() {
        return Utils.decodeIdentifier(this.typeName);
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        return null;
    }

    @Override
    public int getTag() {
        return TypeTags.OBJECT_TYPE_TAG;
    }
    
    @Override
    public MethodType[] getMethods() {
        return methodTypes;
    }

    @Override
    public MethodType getInitMethod() {
        return initMethod;
    }

    public MethodType getGeneratedInitMethod() {
        return generatedInitMethod;
    }

    @Override
    public boolean isIsolated() {
        return SymbolFlags.isFlagOn(getFlags(), SymbolFlags.ISOLATED);
    }

    @Override
    public boolean isIsolated(String methodName) {
        for (MethodType method : this.getMethods()) {
            if (method.getName().equals(methodName)) {
                return method.isIsolated();
            }
        }
        if (this.getTag() == SERVICE_TAG || (this.flags & SymbolFlags.CLIENT) == SymbolFlags.CLIENT) {
            for (ResourceMethodType method : ((BNetworkObjectType) this).getResourceMethods()) {
                if (method.getName().equals(methodName)) {
                    return method.isIsolated();
                }
            }
        }
        throw ErrorCreator.createError(StringUtils.fromString("No such method: " + methodName));
    }

    @Override
    public void setMethods(MethodType[] methodTypes) {
        this.methodTypes = methodTypes;
    }

    public void setInitMethod(MethodType initMethod) {
        this.initMethod = initMethod;
    }

    public void setGeneratedInitMethod(BMethodType generatedInitMethod) {
        this.generatedInitMethod = generatedInitMethod;
    }

    public void computeStringRepresentation() {
        if (cachedToString != null) {
            return;
        }
        final String name = (pkg == null || pkg.getName() == null || pkg.getName().equals(".")) ?
                typeName : pkg.getName() + ":" + typeName;
        if (!typeName.contains("$anon")) {
            cachedToString = name;
            return;
        }
        StringJoiner sj = new StringJoiner(",\n\t", name + " {\n\t", "\n}");
        for (Entry<String, Field> field : getFields().entrySet()) {
            sj.add(field.getKey() + " : " + field.getValue().getFieldType());
        }
        for (MethodType func : methodTypes) {
            sj.add(func.toString());
        }
        cachedToString = sj.toString();
    }

    public String toString() {
        if (resolving) {
            return "";
        }
        resolving = true;
        computeStringRepresentation();
        resolving = false;
        return cachedToString;
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
    public void setIntersectionType(IntersectionType intersectionType) {
        this.intersectionType = intersectionType;
    }

    public void setTypeIdSet(BTypeIdSet typeIdSet) {
        this.typeIdSet = typeIdSet;
    }

    public BObjectType duplicate() {
        BObjectType type = new BObjectType(this.typeName, this.pkg, this.flags);
        type.setFields(fields);
        type.setMethods(duplicateArray(methodTypes));
        type.immutableType = this.immutableType;
        type.typeIdSet = this.typeIdSet;
        return type;
    }

    protected  <T extends MethodType> T[] duplicateArray(T[] methodTypes) {
        Class<?> elemType = methodTypes.getClass().getComponentType();
        T[] array = (T[]) Array.newInstance(elemType, methodTypes.length);
        for (int i = 0; i < methodTypes.length; i++) {
            BMethodType functionType = (BMethodType) methodTypes[i];
            array[i] = (T) functionType.duplicate();
        }

        return array;
    }

    public boolean hasAnnotations() {
        return !annotations.isEmpty();
    }

    @Override
    public TypeIdSet getTypeIdSet() {
        return new BTypeIdSet(new ArrayList<>(typeIdSet.ids));
    }

    @Override
    synchronized SemType createSemType(Context cx) {
        return distinctIdSupplier.get().stream().map(ObjectDefinition::distinct)
                .reduce(semTypeInner(cx), Core::intersect);
    }

    private static boolean skipField(Set<String> seen, String name) {
        if (name.startsWith("$")) {
            return true;
        }
        return !seen.add(name);
    }

    private SemType semTypeInner(Context cx) {
        if (softSemTypeCache != null) {
            return softSemTypeCache;
        }
        if (od != null) {
            return od.getSemType(env);
        }
        od = new ObjectDefinition();
        ObjectQualifiers qualifiers = getObjectQualifiers();
        List<Member> members = new ArrayList<>();
        boolean hasBTypes = false;
        Set<String> seen = new HashSet<>(fields.size() + methodTypes.length);
        for (Entry<String, Field> entry : fields.entrySet()) {
            String name = entry.getKey();
            if (skipField(seen, name)) {
                continue;
            }
            Field field = entry.getValue();
            boolean isPublic = SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.PUBLIC);
            boolean isImmutable = qualifiers.readonly() | SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.READONLY);
            SemType ty = Builder.from(cx, field.getFieldType());
            SemType pureBTypePart = Core.intersect(ty, Core.B_TYPE_TOP);
            if (!Core.isNever(pureBTypePart)) {
                hasBTypes = true;
                ty = Core.intersect(ty, Core.SEMTYPE_TOP);
            }
            members.add(new Member(name, ty, Member.Kind.Field,
                    isPublic ? Member.Visibility.Public : Member.Visibility.Private, isImmutable));
        }
        for (MethodData method : allMethods(cx)) {
            String name = method.name();
            if (skipField(seen, name)) {
                continue;
            }
            boolean isPublic = SymbolFlags.isFlagOn(method.flags(), SymbolFlags.PUBLIC);
            SemType semType = method.semType();
            SemType pureBTypePart = Core.intersect(semType, Core.B_TYPE_TOP);
            if (!Core.isNever(pureBTypePart)) {
                hasBTypes = true;
                semType = Core.intersect(semType, Core.SEMTYPE_TOP);
            }
            members.add(new Member(name, semType, Member.Kind.Method,
                    isPublic ? Member.Visibility.Public : Member.Visibility.Private, true));
        }
        SemType semTypePart = od.define(env, qualifiers, members);
        if (hasBTypes || members.isEmpty()) {
            cx.markProvisionTypeReset();
            SemType bTypePart = BTypeConverter.wrapAsPureBType(this);
            softSemTypeCache = Core.union(semTypePart, bTypePart);
            return softSemTypeCache;
        }
        return semTypePart;
    }

    private ObjectQualifiers getObjectQualifiers() {
        boolean isolated = SymbolFlags.isFlagOn(getFlags(), SymbolFlags.ISOLATED);
        boolean readonly = SymbolFlags.isFlagOn(getFlags(), SymbolFlags.READONLY);
        ObjectQualifiers.NetworkQualifier networkQualifier;
        if (SymbolFlags.isFlagOn(getFlags(), SymbolFlags.SERVICE)) {
            networkQualifier = ObjectQualifiers.NetworkQualifier.Service;
        } else if (SymbolFlags.isFlagOn(getFlags(), SymbolFlags.CLIENT)) {
            networkQualifier = ObjectQualifiers.NetworkQualifier.Client;
        } else {
            networkQualifier = ObjectQualifiers.NetworkQualifier.None;
        }
        return new ObjectQualifiers(isolated, readonly, networkQualifier);
    }

    @Override
    public Optional<SemType> shapeOf(Context cx, Object object) {
        AbstractObjectValue abstractObjectValue = (AbstractObjectValue) object;
        SemType cachedShape = abstractObjectValue.shapeOf();
        if (cachedShape != null) {
            return Optional.of(cachedShape);
        }
        SemType shape = distinctIdSupplier.get().stream().map(ObjectDefinition::distinct).reduce(
                valueShape(cx, abstractObjectValue), Core::intersect);
        abstractObjectValue.cacheShape(shape);
        return Optional.of(shape);
    }

    private SemType valueShape(Context cx, AbstractObjectValue object) {
        ObjectDefinition od = new ObjectDefinition();
        List<Member> members = new ArrayList<>();
        Set<String> seen = new HashSet<>(fields.size() + methodTypes.length);
        ObjectQualifiers qualifiers = getObjectQualifiers();
        boolean hasBTypes = false;
        for (Entry<String, Field> entry : fields.entrySet()) {
            String name = entry.getKey();
            if (skipField(seen, name)) {
                continue;
            }
            Field field = entry.getValue();
            boolean isPublic = SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.PUBLIC);
            boolean isImmutable = qualifiers.readonly() | SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.READONLY) |
                    SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.FINAL);
            SemType ty = fieldShape(cx, field, object, isImmutable);
            SemType pureBTypePart = Core.intersect(ty, Core.B_TYPE_TOP);
            if (!Core.isNever(pureBTypePart)) {
                hasBTypes = true;
                ty = Core.intersect(ty, Core.SEMTYPE_TOP);
            }
            members.add(new Member(name, ty, Member.Kind.Field,
                    isPublic ? Member.Visibility.Public : Member.Visibility.Private, isImmutable));
        }
        for (MethodData method : allMethods(cx)) {
            String name = method.name();
            if (skipField(seen, name)) {
                continue;
            }
            boolean isPublic = SymbolFlags.isFlagOn(method.flags(), SymbolFlags.PUBLIC);
            SemType semType = method.semType();
            SemType pureBTypePart = Core.intersect(semType, Core.B_TYPE_TOP);
            if (!Core.isNever(pureBTypePart)) {
                hasBTypes = true;
                semType = Core.intersect(semType, Core.SEMTYPE_TOP);
            }
            members.add(new Member(name, semType, Member.Kind.Method,
                    isPublic ? Member.Visibility.Public : Member.Visibility.Private, true));
        }
        SemType semTypePart = od.define(env, qualifiers, members);
        if (hasBTypes) {
            SemType bTypePart = BTypeConverter.wrapAsPureBType(this);
            return Core.union(semTypePart, bTypePart);
        }
        return semTypePart;
    }

    private static SemType fieldShape(Context cx, Field field, AbstractObjectValue objectValue, boolean isImmutable) {
        if (!isImmutable) {
            return Builder.from(cx, field.getFieldType());
        }
        BString fieldName = StringUtils.fromString(field.getFieldName());
        Optional<SemType> shape = Builder.shapeOf(cx, objectValue.get(fieldName));
        assert !shape.isEmpty();
        return shape.get();
    }

    @Override
    public void resetSemTypeCache() {
        super.resetSemTypeCache();
        od = null;
    }

    private final class DistinctIdSupplier implements Supplier<Collection<Integer>> {

        private List<Integer> ids = null;
        private static final Map<TypeId, Integer> allocatedIds = new ConcurrentHashMap<>();
        private final Env env;

        private DistinctIdSupplier(Env env) {
            this.env = env;
        }

        public synchronized Collection<Integer> get() {
            if (ids != null) {
                return ids;
            }
            if (typeIdSet == null) {
                return List.of();
            }
            ids = typeIdSet.getIds().stream().map(typeId -> allocatedIds.computeIfAbsent(typeId,
                            ignored -> env.distinctAtomCountGetAndIncrement()))
                    .toList();
            return ids;
        }
    }

    protected Collection<MethodData> allMethods(Context cx) {
        return Arrays.stream(methodTypes).map(method -> MethodData.fromMethod(cx, method)).toList();
    }

    protected record MethodData(String name, long flags, SemType semType) {

        static MethodData fromMethod(Context cx, MethodType method) {
            return new MethodData(method.getName(), method.getFlags(),
                    Builder.from(cx, method.getType()));
        }

        static MethodData fromResourceMethod(Context cx, BResourceMethodType method) {
            StringBuilder sb = new StringBuilder();
            sb.append(method.getAccessor());
            for (var each : method.getResourcePath()) {
                sb.append(each);
            }
            String methodName = sb.toString();

            Type[] pathSegmentTypes = method.pathSegmentTypes;
            FunctionType innerFn = method.getType();
            List<SemType> paramTypes = new ArrayList<>();
            boolean hasBTypes = false;
            for (Type part : pathSegmentTypes) {
                if (part == null) {
                    paramTypes.add(Builder.anyType());
                } else {
                    SemType semType = Builder.from(cx, part);
                    if (!Core.isNever(Core.intersect(semType, Core.B_TYPE_TOP))) {
                        hasBTypes = true;
                        paramTypes.add(Core.intersect(semType, Core.SEMTYPE_TOP));
                    } else {
                        paramTypes.add(semType);
                    }
                }
            }
            for (Parameter paramType : innerFn.getParameters()) {
                SemType semType = Builder.from(cx, paramType.type);
                if (!Core.isNever(Core.intersect(semType, Core.B_TYPE_TOP))) {
                    hasBTypes = true;
                    paramTypes.add(Core.intersect(semType, Core.SEMTYPE_TOP));
                } else {
                    paramTypes.add(semType);
                }
            }
            SemType rest;
            Type restType = innerFn.getRestType();
            if (restType instanceof BArrayType arrayType) {
                rest = Builder.from(cx, arrayType.getElementType());
                if (!Core.isNever(Core.intersect(rest, Core.B_TYPE_TOP))) {
                    hasBTypes = true;
                    rest = Core.intersect(rest, Core.SEMTYPE_TOP);
                }
            } else {
                rest = Builder.neverType();
            }

            SemType returnType;
            if (innerFn.getReturnType() != null) {
                returnType = Builder.from(cx, innerFn.getReturnType());
                if (!Core.isNever(Core.intersect(returnType, Core.B_TYPE_TOP))) {
                    hasBTypes = true;
                    returnType = Core.intersect(returnType, Core.SEMTYPE_TOP);
                }
            } else {
                returnType = Builder.nilType();
            }
            ListDefinition paramListDefinition = new ListDefinition();
            Env env = cx.env;
            SemType paramType = paramListDefinition.defineListTypeWrapped(env, paramTypes.toArray(SemType[]::new),
                    paramTypes.size(), rest, CellAtomicType.CellMutability.CELL_MUT_NONE);
            FunctionDefinition fd = new FunctionDefinition();
            SemType semType = fd.define(env, paramType, returnType, innerFn.getQualifiers());
            if (hasBTypes) {
                semType = Core.union(semType, BTypeConverter.wrapAsPureBType((BType) innerFn));
            }
            return new MethodData(methodName, method.getFlags(), semType);
        }
    }
}
