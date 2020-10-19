/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.api;

import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.ErrorType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.FiniteType;
import io.ballerina.runtime.api.types.JSONType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.StreamType;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.types.TupleType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.types.XMLType;
import io.ballerina.runtime.types.BArrayType;
import io.ballerina.runtime.types.BErrorType;
import io.ballerina.runtime.types.BField;
import io.ballerina.runtime.types.BFiniteType;
import io.ballerina.runtime.types.BJSONType;
import io.ballerina.runtime.types.BMapType;
import io.ballerina.runtime.types.BObjectType;
import io.ballerina.runtime.types.BRecordType;
import io.ballerina.runtime.types.BStreamType;
import io.ballerina.runtime.types.BTableType;
import io.ballerina.runtime.types.BTupleType;
import io.ballerina.runtime.types.BUnionType;
import io.ballerina.runtime.types.BXMLType;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class @{@link TypeCreator} provides APIs to create ballerina type instances.
 *
 * @since 2.0.0
 */
public class TypeCreator {

    /**
     * Creates a new array type with given element type.
     *
     * @param elementType array element type
     * @return the new array type
     */
    public static ArrayType createArrayType(Type elementType) {
        return new BArrayType(elementType);
    }

    /**
     * Creates a new array type with given element type and size.
     *
     * @param elementType array element type
     * @param size        array size
     * @return the new array type
     */
    public static ArrayType createArrayType(Type elementType, int size) {
        return new BArrayType(elementType, size, false);
    }

    /**
     * Creates a new array type with given element type, size and read only option.
     *
     * @param elementType array element type
     * @param size        array size
     * @param readonly    array type is read only or not
     * @return the new array type
     */
    public static ArrayType createArrayType(Type elementType, int size, boolean readonly) {
        return new BArrayType(elementType, size, readonly);
    }

    /**
     * Create a {@code TupleType} which represents the tuple type.
     *
     * @param typeList of the tuple type
     * @return the new tuple type
     */
    public static TupleType createTupleType(List<Type> typeList) {
        return new BTupleType(typeList);
    }

    /**
     * Create a {@code TupleType} which represents the tuple type.
     *
     * @param typeList  of the tuple type
     * @param typeFlags flags associated with the type
     * @return the new tuple type
     */
    public static TupleType createTupleType(List<Type> typeList, int typeFlags) {
        return new BTupleType(typeList, typeFlags);
    }

    /**
     * Create a {@code TupleType} which represents the tuple type.
     *
     * @param typeList  of the tuple type
     * @param restType  of the tuple type
     * @param typeFlags flags associated with the type
     * @param readonly  whether immutable
     * @return the new tuple type
     */
    public static TupleType createTupleType(List<Type> typeList, Type restType, int typeFlags, boolean readonly) {
        return new BTupleType(typeList, restType, typeFlags, readonly);
    }

    /**
     * Create a {@code MapType} which represents the map type.
     *
     * @param constraint constraint type which particular map is bound to.
     * @return the new map type
     */
    public static MapType createMapType(Type constraint) {
        return new BMapType(constraint);
    }

    /**
     * Create a {@code MapType} which represents the map type.
     *
     * @param constraint constraint type which particular map is bound to.
     * @param readonly   whether immutable
     * @return the new map type
     */
    public static MapType createMapType(Type constraint, boolean readonly) {
        return new BMapType(constraint, readonly);
    }

    /**
     * Create a {@code MapType} which represents the map type.
     *
     * @param typeName   string name of the type.
     * @param constraint constraint type which particular map is bound to.
     * @param module     package for the type.
     * @return the new map type
     */
    public static MapType createMapType(String typeName, Type constraint, Module module) {
        return new BMapType(typeName, constraint, module);
    }

    /**
     * Create a {@code MapType} which represents the map type.
     *
     * @param typeName   string name of the type.
     * @param constraint constraint type which particular map is bound to.
     * @param module     package for the type.
     * @param readonly   whether immutable
     * @return the new map type
     */
    public static MapType createMapType(String typeName, Type constraint, Module module, boolean readonly) {
        return new BMapType(typeName, constraint, module, readonly);
    }

    /**
     * Create a {@code BRecordType} which represents the user defined record type.
     *
     * @param typeName  string name of the record type
     * @param module    package of the record type
     * @param flags     of the record type
     * @param sealed    flag indicating the sealed status
     * @param typeFlags flags associated with the type
     * @return the new record type
     */
    public static RecordType createRecordType(String typeName, Module module, int flags, boolean sealed,
                                              int typeFlags) {
        return new BRecordType(typeName, module, flags, sealed, typeFlags);
    }

    /**
     * Create a {@code BRecordType} which represents the user defined record type.
     *
     * @param typeName      string name of the record type
     * @param module        package of the record type
     * @param flags         of the record type
     * @param fields        record fields
     * @param restFieldType type of the rest field
     * @param sealed        flag to indicate whether the record is sealed
     * @param typeFlags     flags associated with the type
     * @return the new record type
     */
    public static RecordType createRecordType(String typeName, Module module, int flags, Map<String, Field> fields,
                                              Type restFieldType,
                                              boolean sealed, int typeFlags) {
        return new BRecordType(typeName, module, flags, fields, restFieldType, sealed, typeFlags);
    }

    /**
     * Creates a new object type.
     *
     * @param typeName object type name
     * @param module   module of the object type
     * @param flags    object type flags
     * @return the new object type
     */
    public static ObjectType createObjectType(String typeName, Module module, int flags) {
        return new BObjectType(typeName, module, flags);
    }

    /**
     * Creates a {@link BStreamType} which represents the stream type.
     *
     * @param constraint the type by which this stream is constrained
     * @return the new stream type
     */
    public static StreamType createStreamType(Type constraint) {
        return new BStreamType(constraint);
    }

    /**
     * Creates a {@link BStreamType} which represents the stream type.
     *
     * @param typeName   string name of the type
     * @param constraint the type by which this stream is constrained
     * @param modulePath package path
     * @return the new stream type
     */
    public static StreamType createStreamType(String typeName, Type constraint, Module modulePath) {
        return new BStreamType(typeName, constraint, modulePath);
    }

    /**
     * Create a {@code BUnionType} which represents the union type.
     *
     * @param memberTypes of the union type
     * @return the new union type
     */
    public static UnionType createUnionType(List<Type> memberTypes) {
        return new BUnionType(memberTypes);
    }

    /**
     * Create a {@code BUnionType} which represents the union type.
     *
     * @param memberTypes of the union type
     * @param typeFlags   flags associated with the type
     * @return the new union type
     */
    public static UnionType createUnionType(List<Type> memberTypes, int typeFlags) {
        return new BUnionType(memberTypes, typeFlags, false);
    }

    /**
     * Create a {@code BUnionType} which represents the union type.
     *
     * @param memberTypes of the union type
     * @param readonly    whether immutable
     * @return the new union type
     */
    public static UnionType createUnionType(List<Type> memberTypes, boolean readonly) {
        return new BUnionType(memberTypes, readonly);
    }

    /**
     * Create a {@code BUnionType} which represents the union type.
     *
     * @param memberTypes of the union type
     * @param typeFlags   flags associated with the type
     * @param readonly    whether immutable
     * @return the new union type
     */
    public static UnionType createUnionType(List<Type> memberTypes, int typeFlags, boolean readonly) {
        return new BUnionType(memberTypes, typeFlags, readonly);
    }

    /**
     * Create a {@code ErrorType} which represents the error type.
     *
     * @param typeName type name
     * @param module   module
     * @return the new error type
     */
    public static ErrorType createErrorType(String typeName, Module module) {
        return new BErrorType(typeName, module);
    }

    /**
     * Create a {@code ErrorType} which represents the error type.
     *
     * @param typeName   type name
     * @param module     module
     * @param detailType detail type
     * @return the new error type
     */
    public static ErrorType createErrorType(String typeName, Module module, Type detailType) {
        return new BErrorType(typeName, module, detailType);
    }

    /**
     * Create a {@code Field} which represents a field in user defined type.
     *
     * @param fieldType field type
     * @param fieldName filed name
     * @param flags     flags
     * @return new field
     */
    public static Field createField(Type fieldType, String fieldName, int flags) {
        return new BField(fieldType, fieldName, flags);
    }

    /**
     * Create a {@code Table} which represents the table type.
     *
     * @param constraint constraint type
     * @param fieldNames filed names
     * @param readonly   whether immutable
     * @return new table type
     */
    public static TableType createTableType(Type constraint, String[] fieldNames, boolean readonly) {
        return new BTableType(constraint, fieldNames, readonly);
    }

    /**
     * Create a {@code Table} which represents the table type.
     *
     * @param constraint constraint type
     * @param keyType    key type
     * @param readonly   whether immutable
     * @return new table type
     */
    public static TableType createTableType(Type constraint, Type keyType, boolean readonly) {
        return new BTableType(constraint, keyType, readonly);
    }

    /**
     * Create a {@code Table} which represents the table type.
     *
     * @param constraint constraint type
     * @param readonly   whether immutable
     * @return new table type
     */
    public static TableType createTableType(Type constraint, boolean readonly) {
        return new BTableType(constraint, readonly);
    }

    /**
     * Create a {@code BXMLType} which represents the boolean type.
     *
     * @param typeName   string name of the type
     * @param constraint constraint of the xml sequence
     * @param module     module
     * @return new xml type
     */
    public static XMLType createXMLType(String typeName, Type constraint, Module module) {
        return new BXMLType(typeName, constraint, module);
    }

    /**
     * Create a {@code BXMLType} which represents the boolean type.
     *
     * @param typeName string name of the type
     * @param module   module
     * @param tag      tag
     * @param readonly whether immutable
     * @return new xml type
     */
    public static XMLType createXMLType(String typeName, Module module, int tag, boolean readonly) {
        return new BXMLType(typeName, module, tag, readonly);
    }

    /**
     * Create a {@code XMLType} which represents the boolean type.
     *
     * @param constraint constraint of the xml sequence
     * @param readonly   whether immutable
     * @return new xml type
     */
    public static XMLType createXMLType(Type constraint, boolean readonly) {
        return new BXMLType(constraint, readonly);
    }

    /**
     * Create a {@code JSONType} which represents the JSON type.
     *
     * @param typeName string name of the type
     * @param module   of the type
     * @param readonly whether immutable
     * @return new xml type
     */
    public static JSONType createJSONType(String typeName, Module module, boolean readonly) {
        return new BJSONType(typeName, module, readonly);
    }

    /**
     * Create a {@code FiniteType} which represents the finite type.
     *
     * @param typeName string name of the type
     * @return new finite type
     */
    public static FiniteType createFiniteType(String typeName) {
        return new BFiniteType(typeName);
    }

    /**
     * Create a {@code FiniteType} which represents the finite type.
     *
     * @param typeName  string name of the type
     * @param values    values for type
     * @param typeFlags type flags
     * @return new finite type
     */
    public static FiniteType createFiniteType(String typeName, Set<Object> values, int typeFlags) {
        return new BFiniteType(typeName, values, typeFlags);
    }

}
