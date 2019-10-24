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
 package org.ballerinalang.jvm.values;

 import org.apache.axiom.om.OMNode;
 import org.ballerinalang.jvm.DecimalValueKind;
 import org.ballerinalang.jvm.JSONDataSource;
 import org.ballerinalang.jvm.types.BStructureType;
 import org.ballerinalang.jvm.types.BType;

 import java.io.InputStream;
 import java.math.BigDecimal;
 import java.util.function.Consumer;
 import java.util.function.Function;

 /**
  * Helper class to create ballerina value instances.
  *
  * @since 1.1.0
  */
 public class ValueGen {
     //------------------------ ArrayValue -------------------------------------------------------------------

     /**
      * Creates a new array with given type.
      *
      * @param type the {@code BType} object representing the element
      *             type of the array
      * @return the new array
      */
     public static ArrayValue createArrayValue(BType type) {
         return new ArrayValue(type);
     }

     /**
      * Creates a new integer array.
      *
      * @param values initial array values
      * @return integer array
      */
     public static ArrayValue createArrayValue(long[] values) {
         return new ArrayValue(values);
     }

     /**
      * Creates a new boolean array.
      *
      * @param values initial array values
      * @return boolean array
      */
     public static ArrayValue createArrayValue(boolean[] values) {
         return new ArrayValue(values);
     }

     /**
      * Creates a new byte array.
      *
      * @param values initial array values
      * @return byte array
      */
     public static ArrayValue createArrayValue(byte[] values) {
         return new ArrayValue(values);
     }

     /**
      * Creates a new float array.
      *
      * @param values initial array values
      * @return float array
      */
     public static ArrayValue createArrayValue(double[] values) {
         return new ArrayValue(values);
     }

     /**
      * Creates a new string array.
      *
      * @param values initial array values
      * @return string array
      */
     public static ArrayValue createArrayValue(String[] values) {
         return new ArrayValue(values);
     }

     /**
      * Create a new Ref value array.
      *
      * @param values initial array values
      * @param type {@code BType} of the array. Do not use for primitive types.
      * @return ref Value array
      */
     public static ArrayValue createArrayValue(Object[] values, BType type) {
         return new ArrayValue(values, type);
     }

     /**
      * Create a ref value array with given maximum length.
      *
      * @param values initial array values
      * @param type {@code BType} of the array. Do not use for primitive types
      * @param length maximum length
      * @return fixed length ref value array
      */
     public static ArrayValue createArrayValue(Object[] values, BType type, int length) {
         return new ArrayValue(type, length);
     }

     //------------------------ DecimalValue -------------------------------------------------------------------

     /**
      * Create a decimal from given value.
      *
      * @param value the value of the decimal
      * @return decimal value
      */
     public static DecimalValue createDecimalValue(BigDecimal value) {
         return new DecimalValue(value);
     }

     /**
      * Create a decimal from given string.
      *
      * @param value string value
      * @return decimal value
      */
     public static DecimalValue createDecimalValue(String value) {
         return new DecimalValue(value);
     }

     /**
      * Create a decimal from given string and value kind.
      *
      * @param value string value
      * @param valueKind value kind
      * @return
      */
     public static DecimalValue createDecimalValue(String value, DecimalValueKind valueKind) {
         return new DecimalValue(value, valueKind);
     }

     /**
      * Create error value with given reason and error details.
      *
      * @param reason error reason
      * @param details error detail
      * @return error value
      */
     public static ErrorValue createErrorValue(String reason, Object details) {
         return new ErrorValue(reason, details);
     }

     /**
      * Create error value with given type, reason and details.
      *
      * @param type {@code BType} of the error
      * @param reason error reason
      * @param details error details
      * @return error value
      */
     public static ErrorValue createErrorValue(BType type, String reason, Object details) {
         return new ErrorValue(type, reason, details);
     }

     /**
      * Create function pointer to the given function with given {@code BType}.
      *
      * @param function pointing function
      * @param type {@code BType} of the function pointer
      * @return function pointer
      */
     public static FPValue createFPValue(Function function, BType type) {
         return new FPValue(function, type);
     }

     /**
      * Create function pointer to the given function with given {@code BType}.
      *
      * @param consumer pointing function
      * @param type {@code BType} of the function pointer
      * @return function pointer
      */
     public static FPValue createFPValue(Consumer consumer, BType type) {
         return new FPValue(consumer, type);
     }

     /**
      * Create {@code StreamingJsonValue} with given datasource.
      *
      * @param datasource {@code JSONDataSource} to be used
      * @return created {@code StreamingJsonValue}
      */
     public static StreamingJsonValue createStreamingJsonValue(JSONDataSource datasource) {
         return new StreamingJsonValue(datasource);
     }

     /**
      * Create a stream with given constraint type.
      *
      * @param type constraint type
      * @return stream value
      */
     public static StreamValue createStreamValue(BType type) {
         return new StreamValue(type);
     }

     /**
      * Create a table value.
      *
      * @return {@code TableValue}
      */
     public static TableValue createTableValue() {
         return new TableValue();
     }

     /**
      * Create a table with given name and structure type.
      *
      * @param tableName name of the table
      * @param constraintType structure type of the table
      * @return {@code TableValue}
      */
     public static TableValue createTableValue(String tableName, BStructureType constraintType) {
         return new TableValue(tableName, constraintType);
     }

     /**
      * Create a table with given structure type.
      *
      * @param constraintType structure type of the table
      * @return {@code TableValue}
      */
     public static TableValue createTableValue(BStructureType constraintType) {
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
     public static TableValue createTableValue(String query, TableValue fromTable, TableValue joinTable,
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
     public static TableValue createTableValue(BType constraintType, ArrayValue keyColumns, ArrayValue dataRows) {
         return new TableValue(constraintType, keyColumns, dataRows);
     }

     /**
      * Create a type descriptor value.
      *
      * @param describingType {@code BType} of the value describe by this value
      * @return type descriptor
      */
     public static TypedescValue createTypedescValue(BType describingType) {
         return new TypedescValue(describingType);
     }

     /**
      * Create an empty {@code XMLItem}.
      *
      * @return {@code XMLItem}
      */
     public static XMLItem createXMLItem() {
         return new XMLItem();
     }

     /**
      * Cretae a {@code XMLItem} from a XML string.
      *
      * @param xmlValue A XML string
      */
     public static XMLItem createXMLItem(String xmlValue) {
         return new XMLItem(xmlValue);
     }

     /**
      * Create a {@code XMLItem} from a {@link org.apache.axiom.om.OMNode} object.
      *
      * @param value xml object
      */
     public static XMLItem createXMLItem(OMNode value) {
         return new XMLItem(value);
     }

     /**
      * Create a {@code XMLItem} from a {@link InputStream}.
      *
      * @param inputStream Input Stream
      */
     public static XMLItem ctreateXMLItem(InputStream inputStream) {
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
     public static XMLQName createXMLQName(String localName, String uri, String prefix) {
         return new XMLQName(localName, uri, prefix);
     }

     /**
      * Create attribute map with a qualified name string.
      *
      * @param qNameStr qualified name string
      * @return  XML qualified name
      */
     public static XMLQName createXMLQName(String qNameStr) {
         return new XMLQName(qNameStr);
     }

     /**
      * Create an empty xml sequence.
      *
      * @return xml sequence
      */
     public static XMLSequence createXMLSequence() {
         return new XMLSequence();
     }

     /**
      * Create a {@code XMLSequence} from a {@link org.apache.axiom.om.OMNode} object.
      *
      * @param sequence xml sequence array
      * @return xml sequence
      */
     public static XMLSequence createXMLSequence(ArrayValue sequence) {
         return new XMLSequence(sequence);
     }



 }
