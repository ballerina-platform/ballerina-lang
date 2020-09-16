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
package org.ballerinalang.jvm.api;

import org.ballerinalang.jvm.DecimalValueKind;
import org.ballerinalang.jvm.JSONDataSource;
import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.api.values.BArray;
import org.ballerinalang.jvm.api.values.BDecimal;
import org.ballerinalang.jvm.api.values.BFunctionPointer;
import org.ballerinalang.jvm.api.values.BMap;
import org.ballerinalang.jvm.api.values.BObject;
import org.ballerinalang.jvm.api.values.BStream;
import org.ballerinalang.jvm.api.values.BStreamingJson;
import org.ballerinalang.jvm.api.values.BString;
import org.ballerinalang.jvm.api.values.BTypedesc;
import org.ballerinalang.jvm.api.values.BXML;
import org.ballerinalang.jvm.api.values.BXMLQName;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.State;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BFunctionType;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BStreamType;
import org.ballerinalang.jvm.types.BTupleType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.DecimalValue;
import org.ballerinalang.jvm.values.FPValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.MappingInitialValueEntry;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.StreamValue;
import org.ballerinalang.jvm.values.StreamingJsonValue;
import org.ballerinalang.jvm.values.TupleValueImpl;
import org.ballerinalang.jvm.values.TypedescValueImpl;
import org.ballerinalang.jvm.values.ValueCreator;
import org.ballerinalang.jvm.values.XMLItem;
import org.ballerinalang.jvm.values.XMLQName;
import org.ballerinalang.jvm.values.XMLSequence;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.xml.namespace.QName;

 /**
  * Class @{@link BValueCreator} provides apis to create ballerina value instances.
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
     public static BArray createArrayValue(BString[] values) {
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
      * @param type {@code BArrayType} of the array.
      * @param length maximum length
      * @return fixed length ref value array
      */
     public static BArray createArrayValue(BArrayType type, int length) {
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
     public static BFunctionPointer createFPValue(Function function, BFunctionType type) {
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
     public static BFunctionPointer createFPValue(Function function, BFunctionType type, String strandName) {
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
     public static BStream createStreamValue(BStreamType type) {
         return new StreamValue(type);
     }

     /**
      * Create a type descriptor value.
      *
      * @param describingType {@code BType} of the value describe by this value
      * @return type descriptor
      */
     public static BTypedesc createTypedescValue(BType describingType) {
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
     public static BXMLQName createXMLQName(String qNameStr) {
         return new XMLQName(qNameStr);
     }

     /**
      * Create an empty xml sequence.
      *
      * @return xml sequence
      */
     public static BXML createXMLSequence() {
         return new XMLSequence();
     }

     /**
      * Create a {@code XMLSequence} from a {@link org.apache.axiom.om.OMNode} object.
      *
      * @param sequence xml sequence array
      * @return xml sequence
      */
     public static BXML createXMLSequence(ArrayValue sequence) {
         List<BXML> children = new ArrayList<>();
         for (Object value : sequence.getValues()) {
             children.add((BXML) value);
         }
         return new XMLSequence(children);
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
     public static BMap<BString, Object> createMapValue(BType mapType) {
         return new MapValueImpl<>(mapType);
     }

     /**
      * Create a runtime map value wwith given initial values and given type.
      *
      * @param mapType   map type.
      * @param keyValues initial map values to be populated.
      * @return map value
      */
     public static BMap<BString, Object> createMapValue(BMapType mapType,
                                                        MappingInitialValueEntry.KeyValueEntry[] keyValues) {
         return new MapValueImpl<>(mapType, keyValues);
     }


     /**
      * Create a record value using the given package id and record type name.
      *
      * @param packageId      the package id that the record type resides.
      * @param recordTypeName name of the record type.
      * @return value of the record.
      */
     public static BMap<BString, Object> createRecordValue(BPackage packageId, String recordTypeName) {
         ValueCreator valueCreator = ValueCreator.getValueCreator(packageId.toString());
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
     public static BMap<BString, Object> createRecordValue(BPackage packageId, String recordTypeName,
                                                           Map<String, Object> valueMap) {
         BMap<BString, Object> record = createRecordValue(packageId, recordTypeName);
         for (Map.Entry<String, Object> fieldEntry : valueMap.entrySet()) {
             Object val = fieldEntry.getValue();
             if (val instanceof String) {
                 val = BStringUtils.fromString((String) val);
             }
             record.put(BStringUtils.fromString(fieldEntry.getKey()), val);
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
         for (Map.Entry<String, BField> fieldEntry : recordType.getFields().entrySet()) {
             Object value = values[i++];
             if (Flags.isFlagOn(fieldEntry.getValue().flags, Flags.OPTIONAL) && value == null) {
                 continue;
             }

             mapValue.put(BStringUtils.fromString(fieldEntry.getKey()), value instanceof String ?
                     BStringUtils.fromString((String) value) : value);
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
     public static BObject createObjectValue(BPackage packageId, String objectTypeName, Object... fieldValues) {
         Strand currentStrand = getStrand();
         // This method duplicates the createObjectValue with referencing the issue in runtime API getting strand
         ValueCreator valueCreator = ValueCreator.getValueCreator(packageId.toString());
         Object[] fields = new Object[fieldValues.length * 2];

         // Here the variables are initialized with default values
         Scheduler scheduler = null;
         State prevState = State.RUNNABLE;
         boolean prevBlockedOnExtern = false;
         ObjectValue objectValue;

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

     private static Strand getStrand() {
         try {
             return Scheduler.getStrand();
         } catch (Exception ex) {
             // Ignore : issue #22871 is opened to fix this
         }
         return null;
     }
 }
