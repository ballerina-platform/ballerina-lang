/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.runtime.api.types.semtype;

import com.github.benmanes.caffeine.cache.Cache;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BValue;
import io.ballerina.runtime.internal.types.BObjectType;
import io.ballerina.runtime.internal.types.TypeWithAcceptedType;
import io.ballerina.runtime.internal.types.TypeWithShape;
import io.ballerina.runtime.internal.types.semtype.CacheFactory;
import io.ballerina.runtime.internal.values.DecimalValue;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for performing shape related operations.
 *
 * @since 2201.12.0
 */
public class ShapeAnalyzer {

    private ShapeAnalyzer() {
    }

    public static SemType acceptedTypeOf(Context cx, Type typeDesc) {
        if (typeDesc instanceof TypeWithAcceptedType typeWithAcceptedType) {
            return typeWithAcceptedType.acceptedTypeOf(cx);
        }
        return SemType.tryInto(cx, typeDesc);
    }

    public static Optional<SemType> shapeOf(Context cx, Object object) {
        switch (object) {
            case null -> {
                return Optional.of(Builder.getNilType());
            }
            case DecimalValue decimalValue -> {
                return Optional.of(Builder.getDecimalConst(decimalValue.value()));
            }
            case Double doubleValue -> {
                return Optional.of(Builder.getFloatConst(doubleValue));
            }
            case Number intValue -> {
                long value =
                        intValue instanceof Byte byteValue ? Byte.toUnsignedLong(byteValue) : intValue.longValue();
                return Optional.of(Builder.getIntConst(value));
            }
            case Boolean booleanValue -> {
                return Optional.of(Builder.getBooleanConst(booleanValue));
            }
            case BString stringValue -> {
                return Optional.of(Builder.getStringConst(stringValue.getValue()));
            }
            case BValue bValue -> {
                Type type = bValue.getType();
                if (type instanceof TypeWithShape typeWithShape) {
                    return typeWithShape.shapeOf(cx, ShapeAnalyzer::shapeOf, object);
                } else {
                    return Optional.empty();
                }
            }
            default -> {
                return Optional.empty();
            }
        }
    }

    public static Optional<SemType> inherentTypeOf(Context cx, Object object) {
        switch (object) {
            case BValue bValue -> {
                return bValue.inherentTypeOf(cx);
            }
            case null -> {
                return Optional.of(Builder.getNilType());
            }
            case Double doubleValue -> {
                return Optional.of(Builder.getFloatConst(doubleValue));
            }
            case Number intValue -> {
                long value =
                        intValue instanceof Byte byteValue ? Byte.toUnsignedLong(byteValue) : intValue.longValue();
                return Optional.of(Builder.getIntConst(value));
            }
            case Boolean booleanValue -> {
                return Optional.of(Builder.getBooleanConst(booleanValue));
            }
            default -> {
                return Optional.empty();
            }
        }
    }

    public static boolean canOptimizeInherentTypeCheck(Context cx, Type sourceType, Type targetType) {
        if (sourceType instanceof CacheableTypeDescriptor cacheableSource &&
                targetType instanceof CacheableTypeDescriptor cacheableTarget) {
            InherentShapeTestKeys key = new InherentShapeTestKeys(cacheableSource.typeId(), cacheableTarget.typeId());
            Boolean result = inherentShapeTestCache.getIfPresent(key);
            if (result == null) {
                result = canOptimizeInherentTypeCheckInner(cx, cacheableSource, cacheableTarget);
                inherentShapeTestCache.put(key, result);
            }
            return result;
        }
        return canOptimizeInherentTypeCheckInner(cx, sourceType, targetType);
    }

    private static boolean canOptimizeInherentTypeCheckInner(Context cx, Type sourceType, Type targetType) {
        sourceType = TypeUtils.getReferredType(sourceType);
        targetType = TypeUtils.getReferredType(targetType);
        if (sourceType instanceof TypeWithShape typeWithShape && !typeWithShape.couldInherentTypeBeDifferent()) {
            return true;
        }
        if (sourceType instanceof BObjectType sourceObjectType && targetType instanceof BObjectType targetObjectType) {
            return canOptimizeInherentTypeCheckForObjectTypes(sourceObjectType, targetObjectType);
        }
        return Core.isEmpty(cx, Core.intersect(SemType.tryInto(cx, sourceType), SemType.tryInto(cx, targetType)));
    }

    // Here we are using the fact that source type defines the upper bound of method and field names, even if not
    // the types (whereas the actual type defines the lower bound).
    private static boolean canOptimizeInherentTypeCheckForObjectTypes(BObjectType sourceType, BObjectType targetType) {
        Set<String> sourceFieldNames = sourceType.getFields().keySet();
        for (String each : targetType.getFields().keySet()) {
            if (!sourceFieldNames.contains(each)) {
                return true;
            }
        }
        Set<String> sourceMethodNames = Arrays.stream(sourceType.getMethods()).map(Type::getName).collect(
                Collectors.toSet());
        for (Type each : targetType.getMethods()) {
            if (!sourceMethodNames.contains(each.getName())) {
                return true;
            }
        }
        return false;
    }

    record InherentShapeTestKeys(int sourceTypeId, int targetTypeId) {

    }

    private static final Cache<InherentShapeTestKeys, Boolean> inherentShapeTestCache = CacheFactory.createCache();
}
