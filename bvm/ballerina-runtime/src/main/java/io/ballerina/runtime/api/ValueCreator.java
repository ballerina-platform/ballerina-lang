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
package io.ballerina.runtime.api;

import io.ballerina.runtime.DecimalValueKind;
import io.ballerina.runtime.JSONDataSource;
import io.ballerina.runtime.XMLFactory;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.FunctionType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.StreamType;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.types.TupleType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BHandle;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BMapInitialValueEntry;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BStream;
import io.ballerina.runtime.api.values.BStreamingJson;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.api.values.BXML;
import io.ballerina.runtime.api.values.BXMLQName;
import io.ballerina.runtime.api.values.BXMLSequence;
import io.ballerina.runtime.scheduling.Scheduler;
import io.ballerina.runtime.scheduling.State;
import io.ballerina.runtime.scheduling.Strand;
import io.ballerina.runtime.types.BRecordType;
import io.ballerina.runtime.util.Flags;
import io.ballerina.runtime.values.ArrayValueImpl;
import io.ballerina.runtime.values.DecimalValue;
import io.ballerina.runtime.values.FPValue;
import io.ballerina.runtime.values.HandleValue;
import io.ballerina.runtime.values.MapValue;
import io.ballerina.runtime.values.MapValueImpl;
import io.ballerina.runtime.values.MappingInitialValueEntry;
import io.ballerina.runtime.values.StreamValue;
import io.ballerina.runtime.values.StreamingJsonValue;
import io.ballerina.runtime.values.TableValueImpl;
import io.ballerina.runtime.values.TupleValueImpl;
import io.ballerina.runtime.values.TypedescValueImpl;
import io.ballerina.runtime.values.XMLItem;
import io.ballerina.runtime.values.XMLQName;
import io.ballerina.runtime.values.XMLSequence;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.xml.namespace.QName;

 /**
  * Class @{@link ValueCreator} provides apis to create ballerina value instances.
  *
  * @since 1.1.0
  */
 public class ValueCreator {
     /**
      * Creates a new array with given array type.
      *
      * @param type the {@code ArrayType} object representing the type
      * @return the new array
      */
     public static BArray createArrayValue(ArrayType type) {
         return new ArrayValueImpl(type);
     }

     /**
      * Creates a new tuple with given tuple type.
      *
      * @param type the {@code TupleType} object representing the type
      * @return the new array
      */
     public static BArray createTupleValue(TupleType type) {
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
     public static BArray createArrayValue(BString[] values) {
         return new ArrayValueImpl(values);
     }

     /**
      * Create a new Ref value array.
      *
      * @param values initial array values
      * @param type {@code ArrayType} of the array.
      * @return ref Value array
      */
     public static BArray createArrayValue(Object[] values, ArrayType type) {
         return new ArrayValueImpl(values, type);
     }

     /**
      * Create a ref value array with given maximum length.
      *
      * @param type {@code ArrayType} of the array.
      * @param length maximum length
      * @return fixed length ref value array
      */
     public static BArray createArrayValue(ArrayType type, int length) {
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
      * Create function pointer to the given function with given {@code BType}.
      *
      * @param function pointing function
      * @param type     {@code BFunctionType} of the function pointer
      * @return function pointer
      */
     public static BFunctionPointer createFPValue(Function function, FunctionType type) {
         return new FPValue(function, type, null, false);
     }

     /**
      * Create function pointer to the given function with given {@code BType}.
      *
      * @param function   pointing function
      * @param type       {@code BFunctionType} of the function pointer
      * @param strandName name for newly creating strand which is used to run the function pointer
      * @return function pointer
      */
     public static BFunctionPointer createFPValue(Function function, FunctionType type, String strandName) {
         return new FPValue(function, type, strandName, false);
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
     public static BStream createStreamValue(StreamType type) {
         return new StreamValue(type);
     }

     /**
      * Create a stream with given constraint type and iterator object.
      *
      * @param type        constraint type
      * @param iteratorObj
      * @return stream value
      */
     public static BStream createStreamValue(StreamType type, BObject iteratorObj) {
         return new StreamValue(type, iteratorObj);
     }

     /**
      * Create a type descriptor value.
      *
      * @param describingType {@code BType} of the value describe by this value
      * @return type descriptor
      */
     public static BTypedesc createTypedescValue(Type describingType) {
         return new TypedescValueImpl(describingType);
     }

     /**
      * Create an empty {@code XMLItem}.
      *
      * @return {@code XMLItem}
      */
     public static BXML createXMLItem() {
         return new XMLItem(new QName(null), new XMLSequence());
     }

     /**
      * Create a {@code XMLItem} from a XML string.
      *
      * @param xmlValue A XML string
      * @return {@code XMLItem}
      */
     public static BXML createXMLItem(String xmlValue) {
         return XMLFactory.parse(xmlValue);
     }

     /**
      * Create a {@code XMLItem} from a {@link InputStream}.
      *
      * @param inputStream Input Stream
      * @return {@code XMLItem}
      */
     public static BXML createXMLItem(InputStream inputStream) {
         return XMLFactory.parse(inputStream);
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
     public static BXMLQName createXMLQName(BString qNameStr) {
         return new XMLQName(qNameStr);
     }

     /**
      * Create an empty xml sequence.
      *
      * @return xml sequence
      */
     public static BXMLSequence createXMLSequence() {
         return new XMLSequence();
     }

     /**
      * Create a {@code XMLSequence} from a {@link org.apache.axiom.om.OMNode} object.
      *
      * @param sequence xml sequence array
      * @return xml sequence
      */
     public static BXML createXMLSequence(BArray sequence) {
         List<BXML> children = new ArrayList<>();
         for (Object value : sequence.getValues()) {
             children.add((BXML) value);
         }
         return new XMLSequence(children);
     }

     /**
      * Create a {@code XMLSequence} from a {@link org.apache.axiom.om.OMNode} object.
      *
      * @param sequence xml sequence array
      * @return xml sequence
      */
     public static BXML createXMLSequence(List<BXML> sequence) {
         return new XMLSequence(sequence);
     }

     /**
      * Create a {@code XMLSequence} from a {@link org.apache.axiom.om.OMNode} object.
      *
      * @param child xml content
      * @return xml sequence
      */
     public static BXML createXMLSequence(BXML child) {
         return new XMLSequence(child);
     }

     /**
      * Create a runtime map value.
      *
      * @return value of the record.
      */
     public static BMap<BString, Object> createMapValue() {
         return new MapValueImpl<>();
     }

     /**
      * Create a runtime map value with given type.
      *
      * @param mapType map type.
      * @return map value
      */
     public static BMap<BString, Object> createMapValue(Type mapType) {
         return new MapValueImpl<>(mapType);
     }

     /**
      * Create a runtime map value with given initial values and given type.
      *
      * @param mapType   map type.
      * @param keyValues initial map values to be populated.
      * @return map value
      */
     public static BMap<BString, Object> createMapValue(MapType mapType,
                                                        BMapInitialValueEntry[] keyValues) {
         return new MapValueImpl<>(mapType, keyValues);
     }

     /**
      * Create a key field entry in a mapping constructor expression.
      *
      * @param key   key.
      * @param value value.
      * @return key field entry
      */
     public static BMapInitialValueEntry createKeyFieldEntry(Object key, Object value) {
         return new MappingInitialValueEntry.KeyValueEntry(key, value);
     }

     /**
      * Create a spread field entry in a mapping constructor expression.
      *
      * @param mappingValue  mapping value.
      * @return spread field entry
      */
     public static BMapInitialValueEntry createSpreadFieldEntry(BMap mappingValue) {
         return new MappingInitialValueEntry.SpreadFieldEntry(mappingValue);
     }

     /**
      * Create a record value using the given package id and record type name.
      *
      * @param packageId      the package id that the record type resides.
      * @param recordTypeName name of the record type.
      * @return value of the record.
      */
     public static BMap<BString, Object> createRecordValue(Module packageId, String recordTypeName) {
         io.ballerina.runtime.values.ValueCreator
                 valueCreator = io.ballerina.runtime.values.ValueCreator.getValueCreator(packageId.toString());
         return valueCreator.createRecordValue(recordTypeName);
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
     public static BMap<BString, Object> createRecordValue(Module packageId, String recordTypeName,
                                                           Map<String, Object> valueMap) {
         BMap<BString, Object> record = createRecordValue(packageId, recordTypeName);
         for (Map.Entry<String, Object> fieldEntry : valueMap.entrySet()) {
             Object val = fieldEntry.getValue();
             if (val instanceof String) {
                 val = StringUtils.fromString((String) val);
             }
             record.put(StringUtils.fromString(fieldEntry.getKey()), val);
         }

         return record;
     }

     /**
      * Populate a runtime record value with given field values.
      *
      * @param record which needs to get populated
      * @param values field values of the record.
      * @return value of the record.
      */
     public static BMap<BString, Object> createRecordValue(BMap<BString, Object> record, Object... values) {
         BRecordType recordType = (BRecordType) record.getType();
         MapValue<BString, Object> mapValue = new MapValueImpl<>(recordType);
         int i = 0;
         for (Map.Entry<String, Field> fieldEntry : recordType.getFields().entrySet()) {
             Object value = values[i++];
             if (Flags.isFlagOn(fieldEntry.getValue().getFlags(), Flags.OPTIONAL) && value == null) {
                 continue;
             }

             mapValue.put(StringUtils.fromString(fieldEntry.getKey()), value instanceof String ?
                     StringUtils.fromString((String) value) : value);
         }
         return mapValue;
     }

     /**
      * Create an object value using the given package id and object type name.
      *
      * @param packageId the package id that the object type resides.
      * @param objectTypeName name of the object type.
      * @param fieldValues values to be used for fields when creating the object value instance.
      * @return value of the object.
      */
     public static BObject createObjectValue(Module packageId, String objectTypeName, Object... fieldValues) {
         Strand currentStrand = getStrand();
         // This method duplicates the createObjectValue with referencing the issue in runtime API getting strand
         io.ballerina.runtime.values.ValueCreator
                 valueCreator = io.ballerina.runtime.values.ValueCreator.getValueCreator(packageId.toString());
         Object[] fields = new Object[fieldValues.length * 2];

         // Here the variables are initialized with default values
         Scheduler scheduler = null;
         State prevState = State.RUNNABLE;
         boolean prevBlockedOnExtern = false;
         BObject objectValue;

         // Adding boolean values for each arg
         for (int i = 0, j = 0; i < fieldValues.length; i++) {
             fields[j++] = fieldValues[i];
             fields[j++] = true;
         }
         try {
             // Check for non-blocking call
             if (currentStrand != null) {
                 scheduler = currentStrand.scheduler;
                 prevBlockedOnExtern = currentStrand.blockedOnExtern;
                 prevState = currentStrand.getState();
                 currentStrand.blockedOnExtern = false;
                 currentStrand.setState(State.RUNNABLE);
             }
             objectValue = valueCreator.createObjectValue(objectTypeName, scheduler, currentStrand,
                                                          null, fields);
         } finally {
             if (currentStrand != null) {
                 currentStrand.blockedOnExtern = prevBlockedOnExtern;
                 currentStrand.setState(prevState);
             }
         }
         return objectValue;
     }

     /**
      * Create an handle value using the given value.
      *
      * @param value object value.
      * @return handle value for given object value.
      */
     public static BHandle createHandleValue(Object value) {
         return new HandleValue(value);
     }

     /**
      * Create an table value using the given type.
      *
      * @param tableType table type.
      * @return table value for given type.
      */
     public static BTable createTableValue(TableType tableType) {
         return new TableValueImpl(tableType);
     }

     private static Strand getStrand() {
         try {
             return Scheduler.getStrand();
         } catch (Exception ex) {
             // Ignore : issue #22871 is opened to fix this
         }
         return null;
     }
 }
