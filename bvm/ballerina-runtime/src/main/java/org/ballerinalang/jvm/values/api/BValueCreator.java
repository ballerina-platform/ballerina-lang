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
package org.ballerinalang.jvm.values.api;

import org.apache.axiom.om.OMNode;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.DecimalValueKind;
import org.ballerinalang.jvm.JSONDataSource;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BErrorType;
import org.ballerinalang.jvm.types.BFunctionType;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.types.BStreamType;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.types.BTupleType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.DecimalValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.FPValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.StreamValue;
import org.ballerinalang.jvm.values.StreamingJsonValue;
import org.ballerinalang.jvm.values.TableValue;
import org.ballerinalang.jvm.values.TupleValueImpl;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinalang.jvm.values.XMLItem;
import org.ballerinalang.jvm.values.XMLQName;
import org.ballerinalang.jvm.values.XMLSequence;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Function;

 /**
  * Helper class to create ballerina value instances.
  *
  * @since 1.1.0
  */
 public class BValueCreator {
     /**
      * Creates a new array with given array type.
      *
      * @param type the {@code BArrayType} object representing the type
      * @return the new array
      */
     public static BArray createArrayValue(BArrayType type) {
         return new ArrayValueImpl(type);
     }

     /**
      * Creates a new tuple with given tuple type.
      *
      * @param type the {@code BTupleType} object representing the type
      * @return the new array
      */
     public static BArray createTupleValue(BTupleType type) {
         return new TupleValueImpl(type);
     }

     /**
      * Creates a new integer array.
      *
      * @param values initial array values
      * @return integer array
      */
     public static BArray createArrayValue(long[] values) {
         return new ArrayValueImpl(values);
     }

     /**
      * Creates a new boolean array.
      *
      * @param values initial array values
      * @return boolean array
      */
     public static  BArray createArrayValue(boolean[] values) {
         return new ArrayValueImpl(values);
     }

     /**
      * Creates a new byte array.
      *
      * @param values initial array values
      * @return byte array
      */
     public static BArray createArrayValue(byte[] values) {
         return new ArrayValueImpl(values);
     }

     /**
      * Creates a new float array.
      *
      * @param values initial array values
      * @return float array
      */
     public static BArray createArrayValue(double[] values) {
         return new ArrayValueImpl(values);
     }

     /**
      * Creates a new string array.
      *
      * @param values initial array values
      * @return string array
      */
     public static BArray createArrayValue(String[] values) {
         return new ArrayValueImpl(values);
     }

     /**
      * Create a new Ref value array.
      *
      * @param values initial array values
      * @param type {@code BArrayType} of the array.
      * @return ref Value array
      */
     public static BArray createArrayValue(Object[] values, BArrayType type) {
         return new ArrayValueImpl(values, type);
     }

     /**
      * Create a ref value array with given maximum length.
      *
      * @param values initial array values
      * @param type {@code BArrayType} of the array.
      * @param length maximum length
      * @return fixed length ref value array
      */
     public static BArray createArrayValue(Object[] values, BArrayType type, int length) {
         return new ArrayValueImpl(type, length);
     }

     /**
      * Create a decimal from given value.
      *
      * @param value the value of the decimal
      * @return decimal value
      */
     public static BDecimal createDecimalValue(BigDecimal value) {
         return new DecimalValue(value);
     }

     /**
      * Create a decimal from given string.
      *
      * @param value string value
      * @return decimal value
      */
     public static BDecimal createDecimalValue(String value) {
         return new DecimalValue(value);
     }

     /**
      * Create a decimal from given string and value kind.
      *
      * @param value string value
      * @param valueKind value kind
      * @return decimal value
      */
     public static BDecimal createDecimalValue(String value, DecimalValueKind valueKind) {
         return new DecimalValue(value, valueKind);
     }

     /**
      * Create error value with given reason and error details.
      *
      * @param reason error reason
      * @param details error detail
      * @return error value
      */
     public static BError createErrorValue(String reason, Object details) {
         return new ErrorValue(reason, details);
     }

     /**
      * Create error value with given type, reason and details.
      *
      * @param type {@code BErrorType} of the error
      * @param reason error reason
      * @param details error details
      * @return error value
      */
     public static BError createErrorValue(BErrorType type, String reason, Object details) {
         return new ErrorValue(type, reason, details);
     }

     /**
      * Create function pointer to the given function with given {@code BType}.
      *
      * @param function pointing function
      * @param type {@code BFunctionType} of the function pointer
      * @return function pointer
      */
     public static BFunctionPointer createFPValue(Function function, BFunctionType type) {
         return new FPValue(function, type);
     }

     /**
      * Create {@code StreamingJsonValue} with given datasource.
      *
      * @param datasource {@code JSONDataSource} to be used
      * @return created {@code StreamingJsonValue}
      */
     public static BStreamingJson createStreamingJsonValue(JSONDataSource datasource) {
         return new StreamingJsonValue(datasource);
     }

     /**
      * Create a stream with given constraint type.
      *
      * @param type constraint type
      * @return stream value
      */
     public static BStream createStreamValue(BStreamType type) {
         return new StreamValue(type);
     }

     /**
      * Create a table value.
      *
      * @return {@code TableValue}
      */
     public static BTable createTableValue() {
         return new TableValue();
     }

     /**
      * Create a table with given name and structure type.
      *
      * @param tableName name of the table
      * @param constraintType structure type of the table
      * @return {@code TableValue}
      */
     public static BTable createTableValue(String tableName, BStructureType constraintType) {
         return new TableValue(tableName, constraintType);
     }

     /**
      * Create a table with given structure type.
      *
      * @param constraintType structure type of the table
      * @return {@code TableValue}
      */
     public static BTable createTableValue(BStructureType constraintType) {
         return new TableValue(constraintType);
     }

     /**
      * Create a table based on given parameters.
      *
      * @param query string query for table creation
      * @param fromTable from {@code TableValue}
      * @param joinTable join {@code TableValue}
      * @param constraintType structure type of the table
      * @param params parameters for the query
      * @return {@code TableValue}
      */
     public static BTable createTableValue(String query, TableValue fromTable, TableValue joinTable,
                                               BStructureType constraintType, ArrayValue params) {
         return new TableValue(query, fromTable, joinTable, constraintType, params);
     }

     /**
      * Create a table with given initial values.
      *
      * @param constraintType structure type of the table
      * @param keyColumns column keys of the data set
      * @param dataRows initial daya set
      * @return {@code TableValue} with initial data
      */
     public static BTable createTableValue(BType constraintType, ArrayValue keyColumns, ArrayValue dataRows) {
         return new TableValue(constraintType, keyColumns, dataRows);
     }

     /**
      * Create a type descriptor value.
      *
      * @param describingType {@code BType} of the value describe by this value
      * @return type descriptor
      */
     public static BTypedesc createTypedescValue(BType describingType) {
         return new TypedescValue(describingType);
     }

     /**
      * Create an empty {@code XMLItem}.
      *
      * @return {@code XMLItem}
      */
     public static BXml createXMLItem() {
         return new XMLItem();
     }

     /**
      * Cretae a {@code XMLItem} from a XML string.
      *
      * @param xmlValue A XML string
      * @return {@code XMLItem}
      */
     public static BXml<OMNode> createXMLItem(String xmlValue) {
         return new XMLItem(xmlValue);
     }

     /**
      * Create a {@code XMLItem} from a {@link org.apache.axiom.om.OMNode} object.
      *
      * @param value xml object
      * @return {@code XMLItem}
      */
     public static BXml<OMNode> createXMLItem(OMNode value) {
         return new XMLItem(value);
     }

     /**
      * Create a {@code XMLItem} from a {@link InputStream}.
      *
      * @param inputStream Input Stream
      * @return {@code XMLItem}
      */
     public static BXml<OMNode> ctreateXMLItem(InputStream inputStream) {
         return new XMLItem(inputStream);
     }

     /**
      * Create attribute map with an XML.
      *
      * @param localName Local part of the qualified name
      * @param uri Namespace URI
      * @param prefix Namespace prefix
      * @return XML qualified name
      */
     public static BXMLQName createXMLQName(String localName, String uri, String prefix) {
         return new XMLQName(localName, uri, prefix);
     }

     /**
      * Create attribute map with a qualified name string.
      *
      * @param qNameStr qualified name string
      * @return  XML qualified name
      */
     public static BXMLQName createXMLQName(String qNameStr) {
         return new XMLQName(qNameStr);
     }

     /**
      * Create an empty xml sequence.
      *
      * @return xml sequence
      */
     public static BXml createXMLSequence() {
         return new XMLSequence();
     }

     /**
      * Create a {@code XMLSequence} from a {@link org.apache.axiom.om.OMNode} object.
      *
      * @param sequence xml sequence array
      * @return xml sequence
      */
     public static BXml createXMLSequence(ArrayValue sequence) {
         return new XMLSequence(sequence);
     }


     /**
      * Create a record value using the given package id and record type name.
      *
      * @param packageId the package id that the record type resides.
      * @param recordTypeName name of the record type.
      * @return value of the record.
      */
     public static BMap<String, Object> createRecordValue(BPackage packageId, String recordTypeName) {
         return (BMap<String, Object>) BallerinaValues.createRecordValue(packageId, recordTypeName);
     }

     /**
      * Create a record value that populates record fields using the given package id, record type name and a map of
      * field names and associated values for fields.
      *
      * @param packageId the package id that the record type resides.
      * @param recordTypeName name of the record type.
      * @param valueMap values to be used for fields when creating the record.
      * @return value of the populated record.
      */
     public static BMap<String, Object> createRecordValue(BPackage packageId, String recordTypeName,
                                                              Map<String, Object> valueMap) {
         return (BMap<String, Object>) BallerinaValues.createRecordValue(packageId, recordTypeName, valueMap);
     }

     /**
      * Create an object value using the given package id and object type name.
      *
      * @param packageId the package id that the object type resides.
      * @param objectTypeName name of the object type.
      * @param fieldValues values to be used for fields when creating the object value instance.
      * @return value of the object.
      */
     public static BObject createObjectValue(BPackage packageId, String objectTypeName, Object... fieldValues) {
         return (BObject) BallerinaValues.createObjectValue(packageId, objectTypeName, fieldValues);
     }

     /**
      * Populate a runtime record value with given field values.
      *
      * @param record which needs to get populated
      * @param values field values of the record.
      * @return value of the record.
      */
     public static BMap<String, Object> createRecord(BMap<String, Object> record, Object... values) {
         return (BMap) BallerinaValues.createRecord((MapValue<String, Object>) record, values);
     }

 }
