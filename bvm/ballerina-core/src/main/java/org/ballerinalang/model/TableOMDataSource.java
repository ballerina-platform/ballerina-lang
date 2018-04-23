/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model;

import org.apache.axiom.om.ds.AbstractPushOMDataSource;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BTable;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.sql.SQLException;
import java.sql.Struct;
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

    private BTable table;
    private String rootWrapper;
    private String rowWrapper;
    private boolean isInTransaction;

    public TableOMDataSource(BTable table, String rootWrapper, String rowWrapper, boolean isInTransaction) {
        this.table = table;
        this.rootWrapper = rootWrapper != null ? rootWrapper : DEFAULT_ROOT_WRAPPER;
        this.rowWrapper = rowWrapper != null ? rowWrapper : DEFAULT_ROW_WRAPPER;
        this.isInTransaction = isInTransaction;
    }

    @Override
    public void serialize(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("", this.rootWrapper, "");
        while (table.hasNext(this.isInTransaction)) {
            table.next();
            xmlStreamWriter.writeStartElement("", this.rowWrapper, "");
            BStructType structType = table.getStructType();
            BStructType.StructField[] structFields = null;
            if (structType != null) {
                structFields = structType.getStructFields();
            }
            int index = 1;
            for (ColumnDefinition col : table.getColumnDefs()) {
                String name;
                if (structFields != null) {
                    name = structFields[index - 1].getFieldName();
                } else {
                    name = col.getName();
                }
                writeElement(xmlStreamWriter, name, col.getType(), index, structFields);
                ++index;
            }
            xmlStreamWriter.writeEndElement();
        }
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.flush();
    }

    private void writeElement(XMLStreamWriter xmlStreamWriter, String name, TypeKind type, int index,
            BStructType.StructField[] structFields) throws XMLStreamException {
        boolean isArray = false;
        xmlStreamWriter.writeStartElement("", name, "");
        String value = null;
        switch (type) {
        case BOOLEAN:
            value = String.valueOf(table.getBoolean(index));
            break;
        case STRING:
            value = table.getString(index);
            break;
        case INT:
            value = String.valueOf(table.getInt(index));
            break;
        case FLOAT:
            value = String.valueOf(table.getFloat(index));
            break;
        case BLOB:
            value = table.getBlob(index);
            break;
        case ARRAY:
            isArray = true;
            Object[] array = table.getArray(index);
            processArray(xmlStreamWriter, array);
            break;
        case STRUCT:
            isArray = true;
            Object[] structData = table.getStruct(index);
            if (structFields == null) {
                processArray(xmlStreamWriter, structData);
            } else {
                processStruct(xmlStreamWriter, structData, structFields, index);
            }
            break;
        default:
            value = table.getString(index);
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

    private void processArray(XMLStreamWriter xmlStreamWriter, Object[] array) throws XMLStreamException {
        if (array != null) {
            for (Object value  : array) {
                xmlStreamWriter.writeStartElement("", ARRAY_ELEMENT_NAME, "");
                xmlStreamWriter.writeCharacters(String.valueOf(value));
                xmlStreamWriter.writeEndElement();
            }
        }
    }

    private void processStruct(XMLStreamWriter xmlStreamWriter, Object[] structData,
            BStructType.StructField[] structFields, int index) throws XMLStreamException {
        try {
            int i = 0;
            boolean structError = true;
            BType internaltType = structFields[index - 1].fieldType;
            if (internaltType.getTag() == TypeTags.STRUCT_TAG) {
                BStructType.StructField[] interanlStructFields = ((BStructType) internaltType).getStructFields();
                if (interanlStructFields != null) {
                    for (Object val : structData) {
                        xmlStreamWriter.writeStartElement("", interanlStructFields[i].fieldName, "");
                        if (val instanceof Struct) {
                            processStruct(xmlStreamWriter, ((Struct) val).getAttributes(), interanlStructFields, i + 1);
                        } else {
                            xmlStreamWriter.writeCharacters(val.toString());
                        }
                        xmlStreamWriter.writeEndElement();
                        ++i;
                    }
                    structError = false;
                }
            }
            if (structError) {
                throw new BallerinaException("error in constructing the xml element from struct type data");
            }
        } catch (SQLException e) {
            throw new BallerinaException(
                    "error in retrieving struct data to construct the inner xml element:" + e.getMessage());
        }
    }

    @Override
    public boolean isDestructiveWrite() {
        return true;
    }
}
