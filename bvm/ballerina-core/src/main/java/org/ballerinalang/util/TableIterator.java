/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.util;

import org.ballerinalang.model.ColumnDefinition;
import org.ballerinalang.model.DataIterator;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBooleanArray;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BNewArray;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.sql.Array;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;

/**
 * Iterator implementation for table data types.
 *
 * @since 0.963.0
 */
public class TableIterator implements DataIterator {

    protected ResultSet rs;
    protected TableResourceManager resourceManager;
    protected BStructType type;
    protected List<ColumnDefinition> columnDefs;

    public TableIterator(TableResourceManager rm, ResultSet rs, BStructType type,
            List<ColumnDefinition> columnDefs) {
        this.resourceManager = rm;
        this.rs = rs;
        this.type = type;
        this.columnDefs = columnDefs;
    }

    public TableIterator(TableResourceManager rm, ResultSet rs, BStructType type) {
        this.resourceManager = rm;
        this.rs = rs;
        this.type = type;
        generateColumnDefinitions();
    }

    @Override
    public boolean next() {
        if (rs == null) {
            return false;
        }
        try {
            return rs.next();
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    @Override
    public void close(boolean isInTransaction) {
        try {
            if (rs != null && !rs.isClosed()) {
                rs.close();
            }
            resourceManager.releaseResources();
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    public void reset(boolean isInTransaction) {
        close(false);
    }

    @Override
    public String getString(int columnIndex) {
        try {
            return rs.getString(columnIndex);
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    @Override
    public long getInt(int columnIndex) {
        try {
            return rs.getLong(columnIndex);
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    @Override
    public double getFloat(int columnIndex) {
        try {
            return rs.getDouble(columnIndex);
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    @Override
    public boolean getBoolean(int columnIndex) {
        try {
            return rs.getBoolean(columnIndex);
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    @Override
    public String getBlob(int columnIndex) {
        try {
            Blob bValue = rs.getBlob(columnIndex);
            byte[] bdata = bValue.getBytes(1, (int) bValue.length());
            return new String(bdata);
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    @Override
    public Object[] getStruct(int columnIndex) {
        Object[] objArray = null;
        try {
            Struct data = (Struct) rs.getObject(columnIndex);
            if (data != null) {
                objArray = data.getAttributes();
            }
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
        return objArray;
    }

    @Override
    public Object[] getArray(int columnIndex) {
        try {
            return generateArrayDataResult(rs.getArray(columnIndex));
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    private Object[] generateArrayDataResult(Array array) throws SQLException {
        Object[] objArray = null;
        if (!rs.wasNull()) {
            objArray = (Object[]) array.getArray();
        }
        return objArray;
    }

    @Override
    public BStruct generateNext() {
        BStruct bStruct = new BStruct(type);
        int longRegIndex = -1;
        int doubleRegIndex = -1;
        int stringRegIndex = -1;
        int booleanRegIndex = -1;
        int refRegIndex = -1;
        int blobRegIndex = -1;
        int index = 0;
        try {
            BStructType.StructField[] structFields = type.getStructFields();
            for (BStructType.StructField sf : structFields) {
                BType type = sf.getFieldType();
                ++index;
                switch (type.getTag()) {
                case TypeTags.INT_TAG:
                    long iValue = rs.getInt(index);
                    bStruct.setIntField(++longRegIndex, iValue);
                    break;
                case TypeTags.STRING_TAG:
                    String sValue = rs.getString(index);
                    bStruct.setStringField(++stringRegIndex, sValue);
                    break;
                case TypeTags.FLOAT_TAG:
                    double dalue = rs.getDouble(index);
                    bStruct.setFloatField(++doubleRegIndex, dalue);
                    break;
                case TypeTags.BOOLEAN_TAG:
                    boolean boolValue = rs.getBoolean(index);
                    bStruct.setBooleanField(++booleanRegIndex, boolValue ? 1 : 0);
                    break;
                case TypeTags.JSON_TAG:
                    String jsonValue = rs.getString(index);
                    bStruct.setRefField(++refRegIndex, new BJSON(jsonValue));
                    break;
                case TypeTags.XML_TAG:
                    String xmlValue = rs.getString(index);
                    bStruct.setRefField(++refRegIndex, new BXMLItem(xmlValue));
                    break;
                case TypeTags.BLOB_TAG:
                    Blob blobValue = rs.getBlob(index);
                    bStruct.setBlobField(++blobRegIndex, blobValue.getBytes(1L, (int) blobValue.length()));
                    break;
                case TypeTags.ARRAY_TAG:
                    Array arrayValue = rs.getArray(index);
                    bStruct.setRefField(++refRegIndex, getDataArray(arrayValue));
                    break;
                }

            }
        } catch (SQLException e) {
            throw new BallerinaException("error in generating next row of data :" + e.getMessage());
        }
        return bStruct;
    }

    @Override
    public List<ColumnDefinition> getColumnDefinitions() {
        return this.columnDefs;
    }

    @Override
    public BStructType getStructType() {
        return this.type;
    }

    protected BNewArray getDataArray(Array array) throws SQLException {
        Object[] dataArray = generateArrayDataResult(array);
        if (dataArray == null || dataArray.length == 0) {
            return null;
        }
        Object obj = dataArray[0];
        int length = dataArray.length;
        if (obj instanceof String) {
            BStringArray stringDataArray = new BStringArray();
            for (int i = 0; i < length; i++) {
                stringDataArray.add(i, (String) dataArray[i]);
            }
            return stringDataArray;
        } else if (obj instanceof Boolean) {
            BBooleanArray boolDataArray = new BBooleanArray();
            for (int i = 0; i < length; i++) {
                boolDataArray.add(i, ((Boolean) dataArray[i]) ? 1 : 0);
            }
            return boolDataArray;
        } else if (obj instanceof Integer) {
            BIntArray intDataArray = new BIntArray();
            for (int i = 0; i < length; i++) {
                intDataArray.add(i, ((Integer) dataArray[i]));
            }
            return intDataArray;
        } else if (obj instanceof Long) {
            BIntArray longDataArray = new BIntArray();
            for (int i = 0; i < length; i++) {
                longDataArray.add(i, (Long) dataArray[i]);
            }
            return longDataArray;
        } else if (obj instanceof Float) {
            BFloatArray floatDataArray = new BFloatArray();
            for (int i = 0; i < length; i++) {
                floatDataArray.add(i, (Float) dataArray[i]);
            }
            return floatDataArray;
        } else if (obj instanceof Double) {
            BFloatArray doubleDataArray = new BFloatArray();
            for (int i = 0; i < dataArray.length; i++) {
                doubleDataArray.add(i, (Double) dataArray[i]);
            }
            return doubleDataArray;
        } else {
            return  null;
        }
    }

    private void generateColumnDefinitions() {
        BStructType.StructField[] structFields = this.type.getStructFields();
        columnDefs = new ArrayList<>(structFields.length);
        for (BStructType.StructField sf : structFields) {
            BType type = sf.getFieldType();
            TypeKind typeKind = TypeKind.ANY;
            switch (type.getTag()) {
            case TypeTags.INT_TAG:
                typeKind = TypeKind.INT;
                break;
            case TypeTags.STRING_TAG:
                typeKind = TypeKind.STRING;
                break;
            case TypeTags.FLOAT_TAG:
                typeKind = TypeKind.FLOAT;
                break;
            case TypeTags.BOOLEAN_TAG:
                typeKind = TypeKind.BOOLEAN;
                break;
            case TypeTags.JSON_TAG:
                typeKind = TypeKind.JSON;
                break;
            case TypeTags.XML_TAG:
                typeKind = TypeKind.XML;
                break;
            case TypeTags.BLOB_TAG:
                typeKind = TypeKind.BLOB;
                break;
            case TypeTags.ARRAY_TAG:
                typeKind = TypeKind.ARRAY;
                break;
            }
            ColumnDefinition def = new ColumnDefinition(sf.fieldName, typeKind);
            columnDefs.add(def);
        }
    }
}
