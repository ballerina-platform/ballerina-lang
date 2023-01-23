/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.nativeimpl.jvm.runtime.api.tests;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.AnnotatableType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.FunctionType;
import io.ballerina.runtime.api.types.IntersectableReferenceType;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.ReferenceType;
import io.ballerina.runtime.api.types.RemoteMethodType;
import io.ballerina.runtime.api.types.ResourceMethodType;
import io.ballerina.runtime.api.types.ServiceType;
import io.ballerina.runtime.api.types.TupleType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypeId;
import io.ballerina.runtime.api.utils.IdentifierUtils;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BListInitialValueEntry;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BMapInitialValueEntry;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BFunctionType;
import io.ballerina.runtime.internal.types.BRecordType;
import io.ballerina.runtime.internal.types.BTupleType;
import org.ballerinalang.langlib.value.FromJsonWithType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class contains a set of utility methods required for runtime api @{@link ValueCreator} testing.
 *
 * @since 2.0.0
 */
public class Values {

    private static final Module objectModule = new Module("testorg", "values.objects", "1");
    private static final Module recordModule = new Module("testorg", "values.records", "1");
    private static final Module invalidValueModule = new Module("testorg", "invalid_values", "1");
    private static final BString intAnnotation = StringUtils.fromString("testorg/types.typeref:1:Int");
    private static final BError constraintError =
            ErrorCreator.createError(StringUtils.fromString("Validation failed for 'minValue' constraint(s)."));

    public static BMap<BString, Object> getRecord(BString recordName) {
        HashMap<String, Object> address = new HashMap<>();
        address.put("city", StringUtils.fromString("Nugegoda"));
        address.put("country", StringUtils.fromString("Sri Lanka"));
        address.put("postalCode", 10250);
        return ValueCreator.createRecordValue(recordModule, recordName.getValue(), address);
    }

    public static BMap<BString, Object> getRecordWithInitialValues() {
        BMap<BString, Object> address = ValueCreator.createMapValue();
        address.put(StringUtils.fromString("city"), StringUtils.fromString("Nugegoda"));
        address.put(StringUtils.fromString("country"), StringUtils.fromString("Sri Lanka"));
        address.put(StringUtils.fromString("postalCode"), 10250);
        return ValueCreator.createRecordValue(recordModule, "Address", address);
    }

    public static BMap<BString, Object> getRecordWithInvalidInitialValues() {
        BMap<BString, Object> address = ValueCreator.createMapValue();
        address.put(StringUtils.fromString("city"), StringUtils.fromString("Nugegoda"));
        address.put(StringUtils.fromString("district"), StringUtils.fromString("Colombo"));
        address.put(StringUtils.fromString("country"), StringUtils.fromString("Sri Lanka"));
        address.put(StringUtils.fromString("postalCode"), StringUtils.fromString("10250"));
        return ValueCreator.createRecordValue(recordModule, "Address", address);
    }

    public static BMap<BString, Object> getReadOnlyRecordWithInitialValues() {
        BMapInitialValueEntry[] mapInitialValueEntries = {ValueCreator.createKeyFieldEntry(
                StringUtils.fromString("name"), StringUtils.fromString("NameOfStudent1"))};
        BMap<BString, Object> student = ValueCreator.createMapValue(
                TypeCreator.createMapType(PredefinedTypes.TYPE_STRING), mapInitialValueEntries);
        return ValueCreator.createReadonlyRecordValue(recordModule, "Student", student);
    }

    public static BMap<BString, Object> getReadOnlyRecordWithInvalidInitialValues() {
        BMapInitialValueEntry[] mapInitialValueEntries = {ValueCreator.createKeyFieldEntry(
                StringUtils.fromString("name"), StringUtils.fromString("NameOfStudent2"))};
        BMap<BString, Object> student = ValueCreator.createMapValue(
                TypeCreator.createMapType(PredefinedTypes.TYPE_STRING), mapInitialValueEntries);
        student.put(StringUtils.fromString("postalCode"), StringUtils.fromString("10250"));
        return ValueCreator.createReadonlyRecordValue(recordModule, "Student", student);
    }

    public static BObject getObject(BString objectName) {
        BMap<BString, Object> address = getRecord(StringUtils.fromString("Address"));
        return ValueCreator.createObjectValue(objectModule, objectName.getValue(), StringUtils.fromString("Waruna"),
                14, address);
    }

    public static BArray getParameters(BObject object, BString methodName) {
        ObjectType objectType = (ObjectType) object.getType();
        Optional<MethodType> funcType = Arrays.stream(objectType.getMethods())
                .filter(r -> r.getName().equals(methodName.getValue())).findAny();
        TupleType tupleType = TypeCreator.createTupleType(List.of(PredefinedTypes.TYPE_STRING,
                PredefinedTypes.TYPE_BOOLEAN, PredefinedTypes.TYPE_STRING));
        if (funcType.isEmpty()) {
            return ValueCreator.createArrayValue(TypeCreator.createArrayType(tupleType, 0));
        }
        RemoteMethodType remoteType = (RemoteMethodType) funcType.get();
        Parameter[] parameters = remoteType.getParameters();
        int len = parameters.length;
        BListInitialValueEntry[] elements = new BListInitialValueEntry[len];
        for (int i = 0; i < len; i++) {
            BListInitialValueEntry[] initialTupleValues =
                    {ValueCreator.createListInitialValueEntry(StringUtils.fromString(parameters[i].name)),
                            ValueCreator.createListInitialValueEntry(parameters[i].isDefault),
                            ValueCreator.createListInitialValueEntry(
                                    (StringUtils.fromString(parameters[i].type.toString())))};
            elements[i] = ValueCreator
                    .createListInitialValueEntry(ValueCreator.createTupleValue(tupleType, 3, initialTupleValues));
        }
        return ValueCreator.createArrayValue(TypeCreator.createArrayType(tupleType), elements);
    }

    public static BString getFunctionString(BObject object, BString methodName) {
        ObjectType objectType = (ObjectType) object.getType();
        Optional<MethodType> funcType = Arrays.stream(objectType.getMethods())
                .filter(r -> r.getName().equals(methodName.getValue())).findAny();
        if (funcType.isPresent()) {
            return StringUtils.fromString(funcType.get().toString());
        }
        Optional<ResourceMethodType> resourceMethodType =
                Arrays.stream(((ServiceType) objectType).getResourceMethods())
                        .filter(r -> r.getResourcePath()[0].equals(methodName.getValue())).findAny();
        if (resourceMethodType.isPresent()) {
            return StringUtils.fromString(resourceMethodType.get().toString());
        }
        return StringUtils.fromString("");
    }

    public static BString getParamTypesString(BFunctionPointer func) {
        BFunctionType funcType = (BFunctionType) func.getType();
        StringBuilder sb = new StringBuilder();
        for (Type type : funcType.getParameterTypes()) {
            sb.append(type.toString()).append(" ");
        }
        return StringUtils.fromString(sb.toString());
    }

    public static BArray getConstituentTypes(BArray array) {
        Optional<IntersectionType> arrayType = ((IntersectableReferenceType) array.getType()).getIntersectionType();
        assert arrayType.isPresent();
        List<Type> constituentTypes = arrayType.get().getConstituentTypes();
        int size = constituentTypes.size();
        BArray arrayValue =
                ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_STRING, size));
        int index = 0;
        for (Type type : constituentTypes) {
            arrayValue.add(index, StringUtils.fromString(type.toString()));
            index++;
        }
        return arrayValue;
    }

    public static BArray getTypeIds(BObject bObject) {
        List<TypeId> typeIds = ((ObjectType) bObject.getType()).getTypeIdSet().getIds();
        int size = typeIds.size();
        BArray arrayValue = ValueCreator.createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_STRING,
                size));
        int index = 0;
        for (TypeId typeId : typeIds) {
            arrayValue.add(index, StringUtils.fromString(typeId.getName()));
            index++;
        }
        return arrayValue;
    }

    public static Object getMapValue() {
        BMap<BString, Object> mapValue = ValueCreator.createMapValue(
                TypeCreator.createMapType(PredefinedTypes.TYPE_ANYDATA));
        mapValue.put(StringUtils.fromString("a"), 5);
        return mapValue;
    }

    public static BMap<BString, Object> getMapValueWithInitialValues() {
        BMapInitialValueEntry[] mapInitialValueEntries = {ValueCreator.createKeyFieldEntry(
                StringUtils.fromString("aa"), StringUtils.fromString("55")), ValueCreator.createKeyFieldEntry(
                StringUtils.fromString("bb"), StringUtils.fromString("66"))};
        return ValueCreator.createMapValue(
                TypeCreator.createMapType(PredefinedTypes.TYPE_ANYDATA), mapInitialValueEntries);
    }

    public static Object getRecordValue() {
        Field stringField = TypeCreator.createField(PredefinedTypes.TYPE_STRING, "name", 1);
        Map<String, Field> fields = Map.ofEntries(Map.entry("name", stringField));
        RecordType recordType = TypeCreator.createRecordType("Student", null, 1, fields,
                null, true, 6);
        BMap<BString, Object> recordValue = ValueCreator.createRecordValue(recordType);
        recordValue.populateInitialValue(StringUtils.fromString("name"), StringUtils.fromString("nameOfStudent"));
        return recordValue;
    }

    public static BMap<BString, Object> getRecordValueWithInitialValues() {
        Map<String, Field> fieldMap = new HashMap<>();
        fieldMap.put("name", TypeCreator
                .createField(PredefinedTypes.TYPE_STRING, "name", SymbolFlags.REQUIRED + SymbolFlags.PUBLIC));
        fieldMap.put("id", TypeCreator
                .createField(PredefinedTypes.TYPE_INT, "id", SymbolFlags.REQUIRED + SymbolFlags.PUBLIC));
        RecordType recordType = TypeCreator.createRecordType("Details", null, SymbolFlags.READONLY
                , fieldMap, null, true, 0);
        BMapInitialValueEntry[] mapInitialValueEntries = {ValueCreator.createKeyFieldEntry(
                StringUtils.fromString("name"), StringUtils.fromString("studentName")),
                ValueCreator.createKeyFieldEntry(StringUtils.fromString("id"), 123L)};
        return ValueCreator.createRecordValue(recordType, mapInitialValueEntries);
    }

    public static Object getRecordValueFromJson(Object jsonValue, BTypedesc type) {
        return FromJsonWithType.convert(jsonValue, type.getDescribingType());
    }

    public static BObject getInvalidObject(BString objectName) {
        return ValueCreator.createObjectValue(invalidValueModule, objectName.getValue());
    }

    public static BMap<BString, Object> getInvalidRecord(BString recordName) {
        return ValueCreator.createRecordValue(invalidValueModule, recordName.getValue());
    }

    public static BError getInvalidError(BString errorName) {
        BString errorMsg = StringUtils.fromString("error message!");
        return ErrorCreator.createError(invalidValueModule, errorName.getValue(), errorMsg,
                ErrorCreator.createError(errorMsg), ValueCreator.createMapValue());
    }

    public static BString decodeIdentifier(BString identifier) {
        return StringUtils.fromString(IdentifierUtils.decodeIdentifier(identifier.getValue()));
    }

    public static BString escapeSpecialCharacters(BString identifier) {
        return StringUtils.fromString(IdentifierUtils.escapeSpecialCharacters(identifier.getValue()));
    }

    public static BString unescapeSpecialCharacters(BString string) {
        return StringUtils.fromString(IdentifierUtils.unescapeBallerina(string.getValue()));
    }

    public static BMap<BString, Object> getRecordNegative(BString recordName) {
        ArrayList<Integer> arrayList = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        Map<String, Object> map = Map.ofEntries(
                Map.entry("arrList", arrayList)
                                               );
        return ValueCreator.createRecordValue(recordModule, recordName.getValue(), map);
    }

    public static BMap<BString, Object> getRecordNegative2(BString recordName) {
        ArrayList<Integer> arrayList = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        Map<String, Object> map = Map.ofEntries(
                Map.entry("arrList", arrayList)
                                               );
        return ValueCreator.createRecordValue(recordModule, recordName.getValue(), (Map<String, Object>) null);
    }

    public static BMap<BString, Object> getReadonlyRecordNegative(BString recordName) {
        Map<String, Integer> map = Map.ofEntries(
                Map.entry("a", 1),
                Map.entry("b", 2)
                                                );
        Map<String, Object> valueMap = Map.ofEntries(
                Map.entry("valueMap", map)
                                                    );
        return ValueCreator.createRecordValue(recordModule, recordName.getValue(), valueMap);
    }

    public static BMap<BString, Object> getRecordWithRestFieldsNegative() {
        BMap<BString, Object> map = ValueCreator.createMapValue();
        map.put(StringUtils.fromString("key"), "map value");
        Object[] values = new Object[2];
        values[0] = 1;
        values[1] = "abc";
        return ValueCreator.createRecordValue(map, values);
    }

    public static Object validate(Object value, BTypedesc typedesc) {
        Type describingType = typedesc.getDescribingType();
        BMap<BString, Object> annotations = ((AnnotatableType) describingType).getAnnotations();
        if (annotations.containsKey(intAnnotation)) {
            Object annotValue = annotations.get(intAnnotation);
            Long minValue = (Long) ((BMap) annotValue).get(StringUtils.fromString("minValue"));
            if (((Long) value) >= minValue) {
                return value;
            }
        }
        return constraintError;
    }

    public static Object validateRecord(Object value, BTypedesc typedesc) {
        Type describingType = typedesc.getDescribingType();
        Long age = ((BMap) value).getIntValue(StringUtils.fromString("age"));
        for (Field field : ((BRecordType) describingType).getFields().values()) {
            BMap<BString, Object> annotations = ((AnnotatableType) field.getFieldType()).getAnnotations();
            if (annotations.containsKey(intAnnotation)) {
                Long minValue = (Long) ((BMap) annotations.get(intAnnotation)).get(StringUtils.fromString("minValue"));
                if (age < minValue) {
                    return constraintError;
                }
            }
        }
        return value;
    }

    public static Object validateArrayElements(Object value, BTypedesc typedesc) {
        Type describingType = typedesc.getDescribingType();
        BMap<BString, Object> annotations = ((AnnotatableType) describingType).getAnnotations();
        if (annotations.containsKey(intAnnotation)) {
            Object annotValue = annotations.get(intAnnotation);
            Long minValue = (Long) ((BMap) annotValue).get(StringUtils.fromString("minValue"));
            for (Object element : ((BArray) value).getValues()) {
                if (((Long) element) >= minValue) {
                    return value;
                }
            }
        }
        return constraintError;
    }

    public static Object validateArrayConstraint(Object value, BTypedesc typedesc) {
        Type describingType = typedesc.getDescribingType();
        BMap<BString, Object> annotations = ((AnnotatableType) describingType).getAnnotations();
        BString annotKey = StringUtils.fromString("testorg/types.typeref:1:Array");
        if (annotations.containsKey(annotKey)) {
            Object annotValue = annotations.get(annotKey);
            Long maxLength = (Long) ((BMap) annotValue).get(StringUtils.fromString("maxLength"));
            BArray array = (BArray) value;
            if (maxLength < array.getLength()) {
                return ErrorCreator.createError(
                        StringUtils.fromString("Validation failed for 'maxLength' constraint(s)."));
            }
            AnnotatableType eType = (AnnotatableType) ((ReferenceType) ((BArrayType) ((ReferenceType) describingType)
                    .getReferredType()).getElementType()).getReferredType();
            annotations = eType.getAnnotations();
            if (!annotations.containsKey(intAnnotation)) {
                return constraintError;
            }
            annotValue = annotations.get(intAnnotation);
            Long minValue = (Long) ((BMap) annotValue).get(StringUtils.fromString("minValue"));
            for (int i = 0; i < array.getLength(); i++) {
                if (((Long) array.get(i)) < minValue) {
                    return constraintError;
                }
            }
        }
        return value;
    }

    public static Object validateFunctionParameterExtern(BFunctionPointer fpValue) {
        return validateFunctionType((FunctionType) fpValue.getType());
    }

    @Nullable
    private static BError validateFunctionType(FunctionType functionType) {
        Parameter[] parameters = functionType.getParameters();
        assert parameters[0].type.getTag() == TypeTags.TYPE_REFERENCED_TYPE_TAG;
        AnnotatableType annotatableType = (AnnotatableType) parameters[0].type;
        if (annotatableType.getAnnotation(intAnnotation) == null) {
            return constraintError;
        }
        assert parameters[1].type.getTag() == TypeTags.TYPE_REFERENCED_TYPE_TAG;
        annotatableType = (AnnotatableType) parameters[1].type;
        if (annotatableType.getAnnotation(intAnnotation) == null) {
            return constraintError;
        }
        return null;
    }

    public static Object validateFunctionParameterFromObject(BObject object) {
        ObjectType type = (ObjectType) object.getType();
        for (MethodType methodType : type.getMethods()) {
            if (methodType.getName() == "testFunction") {
                return validateFunctionType(methodType.getType());
            }
        }
        return constraintError;
    }

    public static BArray getIntArray(BTypedesc typedesc) {
        BArrayType arrayType = (BArrayType) TypeUtils.getReferredType(typedesc.getDescribingType());
        BArray arrayValue = ValueCreator.createArrayValue(arrayType);
        arrayValue.add(0, 1L);
        arrayValue.add(1, 2L);
        arrayValue.add(2, 3L);
        return arrayValue;
    }

    public static BArray getIntArrayWithInitialValues(BTypedesc typedesc, BArray array) {
        BArrayType arrayType = (BArrayType) TypeUtils.getReferredType(typedesc.getDescribingType());
        int size = array.size();
        BListInitialValueEntry[] elements = new BListInitialValueEntry[size];
        for (int i = 0; i < size; i++) {
            elements[i] = ValueCreator.createListInitialValueEntry(array.get(i));
        }
        return ValueCreator.createArrayValue(arrayType, elements);
    }

    public static BArray getTupleWithInitialValues(BTypedesc typedesc, BArray array) {
        BTupleType tupleType = (BTupleType) TypeUtils.getReferredType(typedesc.getDescribingType());
        int size = array.size();
        BListInitialValueEntry[] elements = new BListInitialValueEntry[size];
        for (int i = 0; i < size; i++) {
            elements[i] = ValueCreator.createListInitialValueEntry(array.get(i));
        }
        return ValueCreator.createTupleValue(tupleType, elements);
    }

    public static Boolean checkInlineRecordAnnotations(BTypedesc typedesc, BTypedesc constraint) {
        Type describingType = typedesc.getDescribingType();
        if (describingType.getTag() == TypeTags.TYPE_REFERENCED_TYPE_TAG &&
                ((ReferenceType) describingType).getReferredType().getTag() == TypeTags.INTERSECTION_TAG) {
            describingType = ((IntersectionType)
                    ((ReferenceType) describingType).getReferredType()).getConstituentTypes().get(0);
        }
        if (!(describingType instanceof AnnotatableType)) {
            throw ErrorCreator.createError(StringUtils.fromString("Invalid type found."));
        }
        Object annotation = ((AnnotatableType) describingType).getAnnotation(StringUtils.fromString("$field$.title"));
        if (annotation == null) {
            throw ErrorCreator.createError(StringUtils.fromString("Annotation is not available."));
        }
        BString annotKey = StringUtils.fromString("testorg/types.typeref:1:String");
        return TypeChecker.checkIsType(((BMap) annotation).get(annotKey), constraint.getDescribingType());
    }
}
