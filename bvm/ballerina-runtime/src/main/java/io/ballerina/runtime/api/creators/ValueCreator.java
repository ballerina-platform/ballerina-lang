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

package io.ballerina.runtime.api.creators;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.FunctionType;
import io.ballerina.runtime.api.types.StreamType;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.types.TupleType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BHandle;
import io.ballerina.runtime.api.values.BListInitialValueEntry;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BMapInitialValueEntry;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BStream;
import io.ballerina.runtime.api.values.BStreamingJson;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.api.values.BXmlItem;
import io.ballerina.runtime.api.values.BXmlQName;
import io.ballerina.runtime.api.values.BXmlSequence;
import io.ballerina.runtime.internal.DecimalValueKind;
import io.ballerina.runtime.internal.JsonDataSource;
import io.ballerina.runtime.internal.ValueUtils;
import io.ballerina.runtime.internal.XmlFactory;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import io.ballerina.runtime.internal.values.DecimalValue;
import io.ballerina.runtime.internal.values.FPValue;
import io.ballerina.runtime.internal.values.HandleValue;
import io.ballerina.runtime.internal.values.ListInitialValueEntry;
import io.ballerina.runtime.internal.values.MapValueImpl;
import io.ballerina.runtime.internal.values.MappingInitialValueEntry;
import io.ballerina.runtime.internal.values.StreamValue;
import io.ballerina.runtime.internal.values.StreamingJsonValue;
import io.ballerina.runtime.internal.values.TableValueImpl;
import io.ballerina.runtime.internal.values.TupleValueImpl;
import io.ballerina.runtime.internal.values.TypedescValueImpl;
import io.ballerina.runtime.internal.values.XmlItem;
import io.ballerina.runtime.internal.values.XmlQName;
import io.ballerina.runtime.internal.values.XmlSequence;

import java.io.InputStream;
import java.math.BigDecimal;
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
     * Creates a new integer array.
     *
     * @param values initial array values
     * @return integer array
     */
    public static BArray createArrayValue(long[] values) {
        return new ArrayValueImpl(values, false);
    }

    /**
     * Creates a new integer array.
     *
     * @param values initial array values
     * @return integer array
     */
    public static BArray createReadonlyArrayValue(long[] values) {
        return new ArrayValueImpl(values, true);
    }

    /**
     * Creates a new boolean array.
     *
     * @param values initial array values
     * @return boolean array
     */
    public static BArray createArrayValue(boolean[] values) {
        return new ArrayValueImpl(values, false);
    }

    /**
     * Creates a new boolean array.
     *
     * @param values initial array values
     * @return boolean array
     */
    public static BArray createReadonlyArrayValue(boolean[] values) {
        return new ArrayValueImpl(values, true);
    }

    /**
     * Creates a new byte array.
     *
     * @param values initial array values
     * @return byte array
     */
    public static BArray createArrayValue(byte[] values) {
        return new ArrayValueImpl(values, false);
    }

    /**
     * Creates a new byte array.
     *
     * @param values initial array values
     * @return byte array
     */
    public static BArray createReadonlyArrayValue(byte[] values) {
        return new ArrayValueImpl(values, true);
    }

    /**
     * Creates a new float array.
     *
     * @param values initial array values
     * @return float array
     */
    public static BArray createArrayValue(double[] values) {
        return new ArrayValueImpl(values, false);
    }

    /**
     * Creates a new float array.
     *
     * @param values initial array values
     * @return float array
     */
    public static BArray createReadonlyArrayValue(double[] values) {
        return new ArrayValueImpl(values, true);
    }

    /**
     * Creates a new string array.
     *
     * @param values initial array values
     * @return string array
     */
    public static BArray createArrayValue(BString[] values) {
        return new ArrayValueImpl(values, false);
    }

    /**
     * Creates a new string array.
     *
     * @param values initial array values
     * @return string array
     */
    public static BArray createReadonlyArrayValue(BString[] values) {
        return new ArrayValueImpl(values, true);
    }

    /**
     * Create a new Ref value array.
     *
     * @param values initial array values
     * @param type   {@code ArrayType} of the array.
     * @return ref Value array
     */
    public static BArray createArrayValue(Object[] values, ArrayType type) {
        return new ArrayValueImpl(values, type);
    }

    /**
     * Create a ref value array with given maximum length.
     *
     * @param type   {@code ArrayType} of the array.
     * @param length maximum length
     * @return fixed length ref value array
     */
    public static BArray createArrayValue(ArrayType type, int length) {
        return new ArrayValueImpl(type, length);
    }

    /**
     * Create a ref value array with given maximum length.
     *
     * @param type          {@code ArrayType} of the array.
     * @param size          array size
     * @param initialValues initial values
     * @return fixed length ref value array
     */
    public static BArray createArrayValue(ArrayType type, long size, BListInitialValueEntry[] initialValues) {
        return new ArrayValueImpl(type, size, initialValues);
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
     * Creates a new tuple with given tuple type.
     *
     * @param type          the {@code TupleType} object representing the type
     * @param size          size of the tuple
     * @param initialValues initial values
     * @return the new tuple
     */
    public static BArray createTupleValue(TupleType type, long size, BListInitialValueEntry[] initialValues) {
        return new TupleValueImpl(type, size, initialValues);
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
     * @param value     string value
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
    public static BStreamingJson createStreamingJsonValue(JsonDataSource datasource) {
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
    public static BXmlItem createXmlItem() {
        return new XmlItem(new QName(null), new XmlSequence());
    }

    /**
     * Create a {@code XMLItem} from a XML string.
     *
     * @param name     QName
     * @param children Xml sequence
     * @return {@code XMLItem}
     */
    public static BXmlItem createXmlItem(QName name, BXmlSequence children) {
        return new XmlItem(name, (XmlSequence) children);
    }

    /**
     * Create a {@code XMLItem} from a XML string.
     *
     * @param name QName
     * @return {@code XMLItem}
     */
    public static BXmlItem createXmlItem(QName name) {
        return new XmlItem(name);
    }

    /**
     * Create a {@code XMLItem} from a XML string.
     *
     * @param name     QName
     * @param readonly Whether the element is immutable
     * @return {@code XMLItem}
     */
    public static BXmlItem createXmlItem(QName name, boolean readonly) {
        return new XmlItem(name, readonly);
    }

    /**
     * Create a {@code XMLItem} from a XML string.
     *
     * @param name     QName
     * @param children Xml sequence
     * @param readonly Whether the element is immutable
     * @return {@code XMLItem}
     */
    public static BXmlItem createXmlItem(QName name, BXmlSequence children, boolean readonly) {
        return new XmlItem(name, (XmlSequence) children, readonly);
    }

    /**
     * Create a {@code XMLItem} from a XML string.
     *
     * @param xmlValue A XML string
     * @return {@code XMLItem}
     */
    public static BXml createXmlValue(String xmlValue) {
        return XmlFactory.parse(xmlValue);
    }

    /**
     * Create a {@code XMLItem} from a {@link InputStream}.
     *
     * @param inputStream Input Stream
     * @return {@code XMLItem}
     */
    public static BXml createXmlValue(InputStream inputStream) {
        return XmlFactory.parse(inputStream);
    }

    /**
     * Create an element type BXml.
     *
     * @param startTagName Name of the start tag of the element
     * @param endTagName   Name of the end tag of element
     * @param defaultNsUri Default namespace URI
     * @return BXml Element type BXml
     */
    @Deprecated
    public static BXml createXmlValue(BXmlQName startTagName, BXmlQName endTagName, String defaultNsUri) {
        return XmlFactory.createXMLElement(startTagName, endTagName, defaultNsUri);
    }

    /**
     * Create an element type BXml.
     *
     * @param startTagName Name of the start tag of the element
     * @param defaultNsUri Default namespace URI
     * @return BXml Element type BXml
     */
    @Deprecated
    public static BXml createXmlValue(BXmlQName startTagName, String defaultNsUri) {
        return XmlFactory.createXMLElement(startTagName, defaultNsUri);
    }

    public static BXml createXmlValue(BXmlQName startTagName, BString defaultNsUriVal) {
        return XmlFactory.createXMLElement(startTagName, defaultNsUriVal);
    }

    /**
     * Create an element type BXml, specifying the type which will indicate mutability.
     *
     * @param startTagName Name of the start tag of the element
     * @param defaultNsUri Default namespace URI
     * @param readonly     Whether the element is immutable
     * @return BXml Element type BXml
     */
    @Deprecated
    public static BXml createXmlValue(BXmlQName startTagName, String defaultNsUri, boolean readonly) {
        return XmlFactory.createXMLElement(startTagName, defaultNsUri, readonly);
    }

    public static BXml createXmlValue(BXmlQName startTagName, BString defaultNsUriVal, boolean readonly) {
        return XmlFactory.createXMLElement(startTagName, defaultNsUriVal, readonly);
    }

    /**
     * Create attribute map with an XML.
     *
     * @param qNameStr Qualified name string
     * @param prefix   Namespace prefix
     * @return XML qualified name
     */
    public static BXmlQName createXmlQName(BString qNameStr, String prefix) {
        return new XmlQName(qNameStr.getValue(), prefix);
    }

    /**
     * Create attribute map with an XML.
     *
     * @param localName Local part of the qualified name
     * @param uri       Namespace URI
     * @param prefix    Namespace prefix
     * @return XML qualified name
     */
    public static BXmlQName createXmlQName(String localName, String uri, String prefix) {
        return new XmlQName(localName, uri, prefix);
    }

    /**
     * Create attribute map with an XML.
     *
     * @param localName Local part of the qualified name
     * @param uri       Namespace URI
     * @param prefix    Namespace prefix
     * @return XML qualified name
     */
    public static BXmlQName createXmlQName(BString localName, BString uri, BString prefix) {
        return new XmlQName(localName, uri, prefix);
    }

    /**
     * Create attribute map with a qualified name string.
     *
     * @param qNameStr qualified name string
     * @return XML qualified name
     */
    public static BXmlQName createXmlQName(String qNameStr) {
        return new XmlQName(qNameStr);
    }

    /**
     * Create attribute map with a qualified name string.
     *
     * @param qNameStr qualified name string
     * @return XML qualified name
     */
    public static BXmlQName createXmlQName(BString qNameStr) {
        return new XmlQName(qNameStr);
    }

    /**
     * Create an empty xml sequence.
     *
     * @return xml sequence
     */
    public static BXmlSequence createXmlSequence() {
        return XmlFactory.createXmlSequence();
    }

    /**
     * Create a {@code XMLSequence} from a {@link org.apache.axiom.om.OMNode} object.
     *
     * @param sequence xml sequence array
     * @return xml sequence
     */
    public static BXmlSequence createXmlSequence(BArray sequence) {
        return XmlFactory.createXmlSequence(sequence);
    }

    /**
     * Create a {@code XMLSequence} from a {@link org.apache.axiom.om.OMNode} object.
     *
     * @param sequence xml sequence array
     * @return xml sequence
     */
    public static BXmlSequence createXmlSequence(List<BXml> sequence) {
        return XmlFactory.createXmlSequence(sequence);
    }

    /**
     * Create a {@code XMLSequence} from a {@link org.apache.axiom.om.OMNode} object.
     *
     * @param child xml content
     * @return xml sequence
     */
    public static BXmlSequence createXmlSequence(BXml child) {
        return XmlFactory.createXmlSequence(child);
    }

    /**
     * Create a comment type BXml.
     *
     * @param content Comment content
     * @return BXml Comment type BXml
     */
    @Deprecated
    public static BXml createXmlComment(String content) {
        return XmlFactory.createXMLComment(content);
    }

    /**
     * Create a comment type BXml.
     *
     * @param content Comment content
     * @return BXml Comment type BXml
     */
    public static BXml createXmlComment(BString content) {
        return XmlFactory.createXMLComment(content.getValue());
    }

    /**
     * Create a comment type BXml, specifying the type which will indicate mutability.
     *
     * @param content  Comment content
     * @param readonly Whether the comment is immutable
     * @return BXml Comment type BXml
     */
    @Deprecated
    public static BXml createXmlComment(String content, boolean readonly) {
        return XmlFactory.createXMLComment(content, readonly);
    }

    /**
     * Create a comment type BXml, specifying the type which will indicate mutability.
     *
     * @param content  Comment content
     * @param readonly Whether the comment is immutable
     * @return BXml Comment type BXml
     */
    public static BXml createXmlComment(BString content, boolean readonly) {
        return XmlFactory.createXMLComment(content, readonly);
    }

    /**
     * Create a comment type BXml.
     *
     * @param content Text content
     * @return BXml Text type BXml
     */
    @Deprecated
    public static BXml createXmlText(String content) {
        return XmlFactory.createXMLText(content);
    }

    /**
     * Create a comment type BXml.
     *
     * @param content Text content
     * @return BXml Text type BXml
     */
    public static BXml createXmlText(BString content) {
        return XmlFactory.createXMLText(content);
    }

    /**
     * Create a processing instruction type BXml.
     *
     * @param tartget PI target
     * @param data    PI data
     * @return BXml Processing instruction type BXml
     */
    @Deprecated
    public static BXml createXmlProcessingInstruction(String tartget, String data) {
        return XmlFactory.createXMLProcessingInstruction(data, tartget);
    }

    /**
     * Create a processing instruction type BXml.
     *
     * @param tartget PI target
     * @param data    PI data
     * @return BXml Processing instruction type BXml
     */
    public static BXml createXmlProcessingInstruction(BString tartget, BString data) {
        return XmlFactory.createXMLProcessingInstruction(tartget, data);
    }

    /**
     * Create a processing instruction type BXml, specifying the type which will indicate mutability.
     *
     * @param target   PI target
     * @param data     PI data
     * @param readonly Whether the PI is immutable
     * @return BXml Processing instruction type BXml
     */
    @Deprecated
    public static BXml createXmlProcessingInstruction(String target, String data, boolean readonly) {
        return XmlFactory.createXMLProcessingInstruction(data, target, readonly);
    }

    /**
     * Create a processing instruction type BXml, specifying the type which will indicate mutability.
     *
     * @param target   PI target
     * @param data     PI data
     * @param readonly Whether the PI is immutable
     * @return BXml Processing instruction type BXml
     */
    public static BXml createXmlProcessingInstruction(BString target, BString data, boolean readonly) {
        return XmlFactory.createXMLProcessingInstruction(target, data, readonly);
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
    public static BMap<BString, Object> createMapValue(Type mapType, BMapInitialValueEntry[] keyValues) {
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
     * @param mappingValue mapping value.
     * @return spread field entry
     */
    public static BMapInitialValueEntry createSpreadFieldEntry(BMap mappingValue) {
        return new MappingInitialValueEntry.SpreadFieldEntry(mappingValue);
    }

    /**
     * Create a list initial value entry.
     *
     * @param value value.
     * @return list initial value entry
     */
    public static BListInitialValueEntry createListInitialValueEntry(Object value) {
        return new ListInitialValueEntry.ExpressionEntry(value);
    }

    /**
     * Create a record value using the given package id and record type name.
     *
     * @param packageId      the package id that the record type resides.
     * @param recordTypeName name of the record type.
     * @return value of the record.
     * @throws BError if given record type is not defined in the ballerina module.
     */
    public static BMap<BString, Object> createRecordValue(Module packageId, String recordTypeName) {
        return ValueUtils.createRecordValue(packageId, recordTypeName);
    }

    /**
     * Create a record value that populates record fields using the given package id, record type name and a map of
     * field names and associated values for fields.
     *
     * @param packageId      the package id that the record type resides.
     * @param recordTypeName name of the record type.
     * @param valueMap       values to be used for fields when creating the record.
     * @return value of the populated record.
     * @throws BError if given record type is not defined in the ballerina module.
     */
    public static BMap<BString, Object> createRecordValue(Module packageId, String recordTypeName,
                                                          Map<String, Object> valueMap) {
        return ValueUtils.createRecordValue(packageId, recordTypeName, valueMap);
    }

    /**
     * Create a readonly record value that populates record fields using the given package id, record type name and a
     * map of field names and associated values for fields.
     *
     * @param packageId      the package id that the record type resides.
     * @param recordTypeName name of the record type.
     * @param valueMap       values to be used for fields when creating the record.
     * @return value of the populated record.
     * @throws BError if given record type is not defined in the ballerina module.
     */
    public static BMap<BString, Object> createReadonlyRecordValue(Module packageId, String recordTypeName,
                                                                  Map<String, Object> valueMap) {
        MapValueImpl<BString, Object> bmap = (MapValueImpl<BString, Object>) ValueUtils.createRecordValue(
                packageId, recordTypeName, valueMap);
        bmap.freezeDirect();
        return bmap;
    }

    /**
     * Populate a runtime record value with given field values.
     *
     * @param record which needs to get populated
     * @param values field values of the record.
     * @return value of the record.
     */
    public static BMap<BString, Object> createRecordValue(BMap<BString, Object> record, Object... values) {
        return ValueUtils.createRecordValue(record, values);
    }

    /**
     * Create an object value using the given package id and object type name.
     *
     * @param packageId      the package id that the object type resides.
     * @param objectTypeName name of the object type.
     * @param fieldValues    values to be used for fields when creating the object value instance.
     * @return value of the object.
     * @throws BError if given object type is not defined in the ballerina module.
     */
    public static BObject createObjectValue(Module packageId, String objectTypeName, Object... fieldValues) {
        return ValueUtils.createObjectValue(packageId, objectTypeName, fieldValues);
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

    /**
     * Create an table value using the given type.
     *
     * @param tableType  table type.
     * @param data       table data
     * @param fieldNames table field names
     * @return table value for given type.
     */
    public static BTable createTableValue(TableType tableType, BArray data, BArray fieldNames) {
        return new TableValueImpl(tableType, (ArrayValue) data, (ArrayValue) fieldNames);
    }

    private ValueCreator() {
    }
}
