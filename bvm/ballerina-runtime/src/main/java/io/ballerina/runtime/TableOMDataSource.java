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
package io.ballerina.runtime;

import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.types.BField;
import io.ballerina.runtime.types.BStructureType;
import io.ballerina.runtime.util.exceptions.BallerinaException;
import io.ballerina.runtime.values.DecimalValue;
import io.ballerina.runtime.values.IteratorValue;
import io.ballerina.runtime.values.MapValueImpl;
import io.ballerina.runtime.values.TableValueImpl;
import io.ballerina.runtime.values.TupleValueImpl;
import org.apache.axiom.om.ds.AbstractPushOMDataSource;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * This will provide custom OMDataSource implementation by wrapping BTable.
 * This will use to convert result set into XML stream.
 *
 * @since 0.8.0
 */
public class TableOMDataSource extends AbstractPushOMDataSource {

    private static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String XSI_PREFIX = "xsi";
    private static final String ARRAY_ELEMENT_NAME = "element";
    private static final String DEFAULT_ROOT_WRAPPER = "results";
    private static final String DEFAULT_ROW_WRAPPER = "result";

    private TableValueImpl table;
    private String rootWrapper;
    private String rowWrapper;

    public TableOMDataSource(TableValueImpl table, String rootWrapper, String rowWrapper) {
        this.table = table;
        this.rootWrapper = rootWrapper != null ? rootWrapper : DEFAULT_ROOT_WRAPPER;
        this.rowWrapper = rowWrapper != null ? rowWrapper : DEFAULT_ROW_WRAPPER;
    }

    @Override
    public void serialize(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("", this.rootWrapper, "");
        IteratorValue itr = this.table.getIterator();

        while (itr.hasNext()) {
            table.getIterator().next();
            xmlStreamWriter.writeStartElement("", this.rowWrapper, "");
            TupleValueImpl tupleValue = (TupleValueImpl) itr.next();
            MapValueImpl record = ((MapValueImpl) tupleValue.get(0));

            BStructureType structType = (BStructureType) record.getType();
            BField[] structFields = null;
            if (structType != null) {
                structFields = structType.getFields().values().toArray(new BField[0]);
            }
            for (int i = 0; i < structFields.length; i++) {
                BField internalStructField = structFields[i];
                int type = internalStructField.getFieldType().getTag();
                String fieldName = internalStructField.getFieldName();

                writeElement(record, xmlStreamWriter, fieldName, type, i, structFields);
            }
            xmlStreamWriter.writeEndElement();
        }
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.flush();
    }

    private void writeElement(MapValueImpl record, XMLStreamWriter xmlStreamWriter, String name, int type, int index,
                              BField[] structFields) throws XMLStreamException {
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
                if (structFields == null) {
                    BArray structData = record.getArrayValue(key);
                    processArray(xmlStreamWriter, structData);
                } else {
                    BMap structData = record.getMapValue(key);
                    processStruct(xmlStreamWriter, structData, structFields, index);
                }
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
        if (array != null) {
            for (int i = 0; i < array.size(); i++) {
                xmlStreamWriter.writeStartElement("", ARRAY_ELEMENT_NAME, "");
                xmlStreamWriter.writeCharacters(String.valueOf(array.get(i)));
                xmlStreamWriter.writeEndElement();
            }
        }
    }

    private void processStruct(XMLStreamWriter xmlStreamWriter, BMap structData,
                               BField[] structFields, int index) throws XMLStreamException {
        boolean structError = true;
        Type internalType = structFields[index].getFieldType();
        if (internalType.getTag() == TypeTags.OBJECT_TYPE_TAG
                || internalType.getTag() == TypeTags.RECORD_TYPE_TAG) {
            BField[] internalStructFields = ((BStructureType) internalType).getFields()
                    .values().toArray(new BField[0]);
            if (internalStructFields != null) {
                for (int i = 0; i < internalStructFields.length; i++) {
                    BString internalKeyName = StringUtils.fromString(internalStructFields[i].name);
                    Object val = structData.get(internalKeyName);
                    xmlStreamWriter.writeStartElement("", internalStructFields[i].getFieldName(), "");
                    if (val instanceof MapValueImpl) {
                        processStruct(xmlStreamWriter, (MapValueImpl) val, internalStructFields, i);
                    } else {
                        xmlStreamWriter.writeCharacters(val.toString());
                    }
                    xmlStreamWriter.writeEndElement();
                }
                structError = false;
            }
        }
        if (structError) {
            throw new BallerinaException("error in constructing the xml element from struct type data");
        }
    }

    @Override
    public boolean isDestructiveWrite() {
        return true;
    }
}
