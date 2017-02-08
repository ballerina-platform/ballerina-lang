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

package org.wso2.ballerina.core.model;

import org.apache.axiom.om.ds.AbstractPushOMDataSource;
import org.wso2.ballerina.core.model.values.BDataTable;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * This will provide custom OMDataSource implementation by wrapping BDataTable.
 * This will use to convert result set into XML stream.
 */
public class DataTableOMDataSource extends AbstractPushOMDataSource {

    private static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String XSI_PREFIX = "xsi";

    private BDataTable dataframe;
    private String rootWrapper;
    private String rowWrapper;

    public DataTableOMDataSource(BDataTable dataframe, String rootWrapper, String rowWrapper) {
        this.dataframe = dataframe;
        this.rootWrapper = rootWrapper;
        this.rowWrapper = rowWrapper;
    }

    @Override
    public void serialize(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartElement(this.rootWrapper != null ? this.rootWrapper : "results");
        while (dataframe.next()) {
            xmlStreamWriter.writeStartElement(this.rowWrapper != null ? this.rowWrapper : "result");
            for (BDataTable.ColumnDefinition col : dataframe.getColumnDefs()) {
                xmlStreamWriter.writeStartElement(col.getName());
                String value = null;
                boolean processed = false;
                switch (col.getType()) {
                case BOOLEAN:
                    value = String.valueOf(dataframe.getBoolean(col.getName()));
                    break;
                case STRING:
                    value = dataframe.getString(col.getName());
                    break;
                case INT:
                    value = String.valueOf(dataframe.getInt(col.getName()));
                    break;
                case LONG:
                    value = String.valueOf(dataframe.getLong(col.getName()));
                    break;
                case FLOAT:
                    value = String.valueOf(dataframe.getFloat(col.getName()));
                    break;
                case DOUBLE:
                    value = String.valueOf(dataframe.getDouble(col.getName()));
                    break;
                case ARRAY:
                    processArray(xmlStreamWriter, col);
                    processed = true;
                    break;
                default:
                    value = dataframe.getObjectAsString(col.getName());
                    break;
                }
                if (!processed) {
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
        dataframe.close();
        xmlStreamWriter.flush();
    }

    private void processArray(XMLStreamWriter xmlStreamWriter, BDataTable.ColumnDefinition col)
            throws XMLStreamException {
        switch (col.getElementType()) {
        case BOOLEAN:
            boolean[] booleanArray = dataframe.getBooleanArray(col.getName());
            for (boolean b : booleanArray) {
                xmlStreamWriter.writeCharacters(String.valueOf(b));
            }
            break;
        case STRING:
            String[] stringArray = dataframe.getStringArray(col.getName());
            for (String s : stringArray) {
                xmlStreamWriter.writeCharacters(s);
            }
            break;
        case INT:
            int[] intArray = dataframe.getIntArray(col.getName());
            for (int i : intArray) {
                xmlStreamWriter.writeStartElement(String.valueOf(i));
            }
            break;
        case LONG:
            long[] longArray = dataframe.getLongArray(col.getName());
            for (long l : longArray) {
                xmlStreamWriter.writeCharacters(String.valueOf(l));
            }
            break;
        case FLOAT:
            float[] floatArray = dataframe.getFloatArray(col.getName());
            for (float f : floatArray) {
                xmlStreamWriter.writeCharacters(String.valueOf(f));
            }
            break;
        case DOUBLE:
            double[] doubleArray = dataframe.getDoubleArray(col.getName());
            for (double d : doubleArray) {
                xmlStreamWriter.writeCharacters(String.valueOf(d));
            }
            break;
        default:
            xmlStreamWriter.writeCharacters(dataframe.getObjectAsString(col.getName()));
            break;
        }
    }

    @Override
    public boolean isDestructiveWrite() {
        return false;
    }
}
