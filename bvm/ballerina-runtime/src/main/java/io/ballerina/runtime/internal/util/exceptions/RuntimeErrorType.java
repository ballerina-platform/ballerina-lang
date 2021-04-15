/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal.util.exceptions;

/**
 * Error types, codes and Error message keys to represent the runtime errors.
 */

public enum RuntimeErrorType {

    // TypeCastError
    TYPE_CAST_ERROR("TypeCastError", "incompatible.types.cannot.cast", "RT-0001"),
    J_TYPE_CAST_ERROR("TypeCastError", "incompatible.jtypes.cannot.cast", "RT-0002"),

    // InherentTypeViolation
    INCOMPATIBLE_TYPE_ERROR("IncompatibleType", "incompatible.types", "RT-0003"),
    ILLEGAL_ARRAY_SIZE_ERROR("IllegalArraySize", "illegal.array.size", "RT-0004"),
    ILLEGAL_TUPLE_SIZE_ERROR("IllegalTupleSize", "illegal.tuple.size", "RT-0005"),
    ILLEGAL_TUPLE_WITH_REST_TYPE_SIZE("IllegalTupleWithRestTypeSize", "illegal.rest.tuple.size", "RT-0006"),
    INVALID_MAP_INSERTION("InvalidMapInsertion", "invalid.map.insertion", "RT-0007"),
    INVALID_RECORD_FIELD_ADDITION("InvalidRecordFieldAddition", "invalid.record.field.addition", "RT-0008"),
    INVALID_OBJECT_FIELD_VALUE("InvalidObjectFieldValue", "invalid.object.field.value", "RT-0009"),
    INHERENT_TABLE_TYPE_VIOLATION("InherentTableTypeViolation", "type.inconsistent.with.inherent.table", "RT-0010"),
    INCOMPATIBLE_TYPE_FOR_CASTING_JSON("IncompatibleTypeForJsonCasting", "incompatible.types.in.json", "RT-0011"),
    JSON_SET_ERROR("JsonSetError", "json.set.error", "RT-0012"),
    JSON_GET_ERROR("JsonGetError", "json.get.error", "RT-0013"),

    // IndexOutOfRangeError
    INDEX_NUMBER_TOO_LARGE("IndexNumberTooLarge", "index.number.too.large", "RT-0014"),
    ARRAY_INDEX_OUT_OF_RANGE("ArrayIndexOutOfRange", "array.index.out.of.range", "RT-0015"),
    TUPLE_INDEX_OUT_OF_RANGE("TupleIndexOutOfRange", "tuple.index.out.of.range", "RT-0016"),
    STRING_INDEX_OUT_OF_RANGE("StringIndexOutOfRange", "string.index.out.of.range", "RT-0017"),
    SUBSTRING_INDEX_OUT_OF_RANGE("StringIndexOutOfRange", "substring.index.out.of.range", "RT-0018"),
    XML_SEQUENCE_INDEX_OUT_OF_RANGE("XmlSequenceIndexOutOfRange", "xml.index.out.of.range", "RT-0019"),

    // StringOperationError
    INVALID_SUBSTRING_RANGE("InvalidSubstringRange", "invalid.substring.range", "RT-0020"),
    STRING_INDEX_TOO_LARGE("StringIndexTooLarge", "string.index.number.too.large", "RT-0021"),

    // ConversionError
    INCOMPATIBLE_NUMBER_CONVERSION_ERROR("NumberConversionError", "incompatible.convert.operation", "RT-0022"),
    SIMPLE_TYPE_NUMBER_CONVERSION_ERROR("NumberConversionError",
            "incompatible.simple.type.convert.operation", "RT-0023"),
    CANNOT_CONVERT_NIL("CannotConvertNil", "cannot.convert.nil", "RT-0024"),
    CYCLIC_VALUE_REFERENCE("CyclicValueReference", "cyclic.value.reference", "RT-0025"),
    INCOMPATIBLE_CONVERT_OPERATION("IncompatibleConvertOperation", "incompatible.convert.operation", "RT-0026"),
    BOOLEAN_PARSING_ERROR("BooleanParsingError", "incompatible.simple.type.convert.operation", "RT-0027"),

    // IllegalListInsertionError
    ILLEGAL_ARRAY_INSERTION("IllegalArrayInsertion", "illegal.array.insertion", "RT-0028"),
    ILLEGAL_TUPLE_INSERTION("IllegalTupleInsertion", "illegal.tuple.insertion", "RT-0029"),

    // InvalidUpdate
    OBJECT_INVALID_FINAL_FIELD_UPDATE("InvalidFinalFieldUpdate", "object.invalid.final.field.update", "RT-0030"),
    INVALID_READONLY_FIELD_UPDATE("InvalidReadonlyFieldUpdate", "record.invalid.readonly.field.update", "RT-0031"),
    INVALID_READONLY_VALUE_UPDATE("InvalidReadonlyValueUpdate", "invalid.update.on.readonly.value", "RT-0032"),

    // KeyNotFoundError
    MAP_KEY_NOT_FOUND("MapKeyNotFound", "map.key.not.found", "RT-0033"),
    TABLE_KEY_NOT_FOUND("TableKeyNotFound", "table.key.not.found", "RT-0034"),
    TABLE_KEY_NOT_DEFINED("TableKeyNotFound", "key.not.defined", "RT-0035"),
    KEY_NOT_FOUND_IN_VALUE("TableKeyNotFound", "key.not.found.in.value", "RT-0036"),
    INVALID_RECORD_FIELD_ACCESS("InvalidRecordFieldAccess", "invalid.record.field.access", "RT-0037"),

    // KeyConstraintViolation
    TABLE_KEY_CONSTRAINT_VIOLATION("TableKeyConstraintViolation", "table.has.a.value.for.key", "RT-0038"),

    // JsonOperationError
    JSON_OPERATION_ERROR("JSONOperationError", "json.value.not.a.mapping", "RT-0039"),
    MERGE_JSON_ERROR("MergeJsonError", "merge.json.values.of.different.types", "RT-0040"),
    MERGE_JSON_WITH_CYCLIC_REFERENCE("MergeJsonError", "merge.json.values.with.cyclic.reference", "RT-0041"),

    // OperationNotSupportedError
    UNSUPPORTED_COMPARISON_OPERATION("UnsupportedComparisonOperation", "unsupported.comparison.operation", "RT-0042"),

    // UnorderedTypesError
    UNORDERED_TYPES_IN_COMPARISON("UnorderedTypes", "unordered.types.in.comparison", "RT-0043"),

    // TableIteratorMutabilityError
    TABLE_ITERATOR_MUTABILITY_ERROR("TableIteratorMutabilityError", "mutated.table.iterator", "RT-0044");

    private String errorName;
    private String errorMsgKey;
    private String errorCode;


    RuntimeErrorType(String errorName, String errorMsgKey, String errorCode) {
        this.errorName = errorName;
        this.errorMsgKey = errorMsgKey;
        this.errorCode = errorCode;
    }



    public String getErrorName() {
        return errorName;
    }

    public String getErrorMsgKey() {
        return errorMsgKey;
    }

    public String getErrorCode() {
        return errorCode;
    }

}
