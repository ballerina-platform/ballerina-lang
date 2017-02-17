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
import org.ballerinalang.model.values.BDataTable;

import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * This will provide custom OMDataSource implementation by wrapping BDataTable.
 * This will use to convert result set into XML stream.
 *
 * @since 0.8.0
 */
public class DataTableOMDataSource extends AbstractPushOMDataSource {

    private static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String XSI_PREFIX = "xsi";
    private static final String ARRAY_ELEMENT_NAME = "element";
    private static final String DEFAULT_ROOT_WRAPPER = "results";
    private static final String DEFAULT_ROW_WRAPPER = "result";

    private BDataTable dataTable;
    private String rootWrapper;
    private String rowWrapper;

    public DataTableOMDataSource(BDataTable dataTable, String rootWrapper, String rowWrapper) {
        this.dataTable = dataTable;
        this.rootWrapper = rootWrapper != null ? rootWrapper : DEFAULT_ROOT_WRAPPER;
        this.rowWrapper = rowWrapper != null ? rowWrapper : DEFAULT_ROW_WRAPPER;
    }

    @Override
    public void serialize(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement(this.rootWrapper);
        while (dataTable.next()) {
            xmlStreamWriter.writeStartElement(this.rowWrapper);
            for (BDataTable.ColumnDefinition col : dataTable.getColumnDefs()) {
                boolean isArray = false;
                xmlStreamWriter.writeStartElement(col.getName());
                String value = null;
                switch (col.getType()) {
                case BOOLEAN:
                    value = String.valueOf(dataTable.getBoolean(col.getName()));
                    break;
                case STRING:
                    value = dataTable.getString(col.getName());
                    break;
                case INT:
                    value = String.valueOf(dataTable.getInt(col.getName()));
                    break;
                case LONG:
                    value = String.valueOf(dataTable.getLong(col.getName()));
                    break;
                case FLOAT:
                    value = String.valueOf(dataTable.getFloat(col.getName()));
                    break;
                case DOUBLE:
                    value = String.valueOf(dataTable.getDouble(col.getName()));
                    break;
                case ARRAY:
                    isArray = true;
                    processArray(xmlStreamWriter, col);
                    break;
                default:
                    value = dataTable.getObjectAsString(col.getName());
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
            xmlStreamWriter.writeEndElement();
        }
        xmlStreamWriter.writeEndElement();
        dataTable.close();
        xmlStreamWriter.flush();
    }

    private void processArray(XMLStreamWriter xmlStreamWriter, BDataTable.ColumnDefinition col)
            throws XMLStreamException {
        Map<String, Object> array = dataTable.getArray(col.getName());
        if (array != null && !array.isEmpty()) {
            for (Map.Entry<String, Object> values : array.entrySet()) {
                xmlStreamWriter.writeStartElement(ARRAY_ELEMENT_NAME);
                xmlStreamWriter.writeCharacters(String.valueOf(values.getValue()));
                xmlStreamWriter.writeEndElement();
            }
        }
    }

    @Override
    public boolean isDestructiveWrite() {
        return false;
    }
}
