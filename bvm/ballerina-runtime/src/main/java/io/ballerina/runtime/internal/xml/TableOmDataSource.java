/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.runtime.internal.xml;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypeTags;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BStructureType;
import io.ballerina.runtime.internal.values.DecimalValue;
import io.ballerina.runtime.internal.values.IteratorValue;
import io.ballerina.runtime.internal.values.MapValue;
import io.ballerina.runtime.internal.values.MapValueImpl;
import io.ballerina.runtime.internal.values.TableValue;
import io.ballerina.runtime.internal.values.TupleValueImpl;
import org.apache.axiom.om.ds.AbstractPushOMDataSource;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * This will provide custom OMDataSource implementation by wrapping BTable.
 * This will use to convert result set into XML stream.
 *
 * @since 0.8.0
 */
public class TableOmDataSource extends AbstractPushOMDataSource {

    private static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String XSI_PREFIX = "xsi";
    private static final String ARRAY_ELEMENT_NAME = "element";
    private static final String DEFAULT_ROOT_WRAPPER = "results";
    private static final String DEFAULT_ROW_WRAPPER = "result";

    private final TableValue<?, ?> table;
    private final String rootWrapper;
    private final String rowWrapper;

    public TableOmDataSource(TableValue<?, ?> table, @Nullable String rootWrapper, @Nullable String rowWrapper) {
        this.table = table;
        this.rootWrapper = rootWrapper != null ? rootWrapper : DEFAULT_ROOT_WRAPPER;
        this.rowWrapper = rowWrapper != null ? rowWrapper : DEFAULT_ROW_WRAPPER;
    }

    @Override
    public void serialize(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("", this.rootWrapper, "");
        IteratorValue<?> itr = this.table.getIterator();

        while (itr.hasNext()) {
            xmlStreamWriter.writeStartElement("", this.rowWrapper, "");
            TupleValueImpl tupleValue = (TupleValueImpl) itr.next();
            MapValue<?, ?> record = ((MapValue<?, ?>) tupleValue.get(0));

            BStructureType structType = (BStructureType) record.getType();
            if (structType != null) {
                for (Field internalStructField : structType.getFields().values()) {
                    writeElement(record, xmlStreamWriter, internalStructField);
                }
            }
            xmlStreamWriter.writeEndElement();
        }
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.flush();
    }

    private void writeElement(MapValue<?, ?> record, XMLStreamWriter xmlStreamWriter,
                              Field structField) throws XMLStreamException {
        int type = TypeUtils.getImpliedType(structField.getFieldType()).getTag();
        String name = structField.getFieldName();
        boolean isArray = false;
        xmlStreamWriter.writeStartElement("", name, "");
        BString key = StringUtils.fromString(name);
        String value = null;
        switch (type) {
            case TypeTags.BOOLEAN_TAG:
                value = String.valueOf(record.getBooleanValue(key));
                break;
            case TypeTags.STRING_TAG:
                value = String.valueOf(record.getStringValue(key));
                break;
            case TypeTags.INT_TAG:
                value = String.valueOf(record.getIntValue(key));
                break;
            case TypeTags.FLOAT_TAG:
                value = String.valueOf(record.getFloatValue(key));
                break;
            case TypeTags.DECIMAL_TAG:
                DecimalValue decimalVal = (DecimalValue) record.get(key);
                value = String.valueOf(decimalVal);
                break;
            case TypeTags.ARRAY_TAG:
                isArray = true;
                BArray array = record.getArrayValue(key);
                processArray(xmlStreamWriter, array);
                break;
            case TypeTags.OBJECT_TYPE_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                isArray = true;
                BMap<?, ?> structData = record.getMapValue(key);
                processStruct(xmlStreamWriter, structData, structField);
                break;
            default:
                value = String.valueOf(record.getStringValue(key));
                break;
        }
        if (!isArray) {
            if (value == null) {
                xmlStreamWriter.writeNamespace(XSI_PREFIX, XSI_NAMESPACE);
                xmlStreamWriter.writeAttribute(XSI_PREFIX, XSI_NAMESPACE, "nil", "true");
            } else {
                xmlStreamWriter.writeCharacters(value);
            }
        }
        xmlStreamWriter.writeEndElement();
    }

    private void processArray(XMLStreamWriter xmlStreamWriter, BArray array) throws XMLStreamException {
        if (array == null) {
            return;
        }
        for (int i = 0; i < array.size(); i++) {
            xmlStreamWriter.writeStartElement("", ARRAY_ELEMENT_NAME, "");
            xmlStreamWriter.writeCharacters(String.valueOf(array.get(i)));
            xmlStreamWriter.writeEndElement();
        }
    }

    private void processStruct(XMLStreamWriter xmlStreamWriter, BMap<?, ?> structData, Field structField)
            throws XMLStreamException {
        Type internalType = TypeUtils.getImpliedType(structField.getFieldType());
        if (internalType.getTag() != TypeTags.OBJECT_TYPE_TAG && internalType.getTag() != TypeTags.RECORD_TYPE_TAG) {
            throw ErrorCreator.createError(StringUtils.fromString(
                    "error in constructing the xml element from struct type data"));
        }

        Collection<Field> internalStructFields = ((BStructureType) internalType).getFields().values();
        for (Field internalStructField : internalStructFields) {
            BString internalKeyName = StringUtils.fromString(internalStructField.getFieldName());
            Object val = structData.get(internalKeyName);
            xmlStreamWriter.writeStartElement("", internalStructField.getFieldName(), "");
                if (val instanceof MapValueImpl<?, ?> mapValue) {
                    processStruct(xmlStreamWriter, mapValue, internalStructField);
            } else {
                xmlStreamWriter.writeCharacters(val.toString());
            }
            xmlStreamWriter.writeEndElement();
        }
    }

    @Override
    public boolean isDestructiveWrite() {
        return true;
    }
}
