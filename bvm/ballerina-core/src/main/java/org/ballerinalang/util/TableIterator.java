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
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BField;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.BUnionType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BBooleanArray;
import org.ballerinalang.model.values.BByteArray;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BNewArray;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.math.BigDecimal;
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
    protected BStructureType type;
    protected List<ColumnDefinition> columnDefs;

    public TableIterator(TableResourceManager rm, ResultSet rs, BStructureType type,
                         List<ColumnDefinition> columnDefs) {
        this.resourceManager = rm;
        this.rs = rs;
        this.type = type;
        this.columnDefs = columnDefs;
    }

    public TableIterator(TableResourceManager rm, ResultSet rs, BStructureType type) {
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
    public void close() {
        try {
            if (rs != null && !rs.isClosed()) {
                rs.close();
            }
            resourceManager.releaseResources();
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    public void reset() {
        close();
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
    public BMap<String, BValue> generateNext() {
        BMap<String, BValue> bStruct = new BMap<>(type);
        int index = 0;
        try {
            BField[] structFields = type.getFields();
            for (BField sf : structFields) {
                BType type = sf.getFieldType();
                String fieldName = sf.fieldName;
                BValue value = null;
                ++index;
                switch (type.getTag()) {
                    case TypeTags.INT_TAG:
                        long iValue = rs.getInt(index);
                        value = new BInteger(iValue);
                        break;
                    case TypeTags.STRING_TAG:
                        String sValue = rs.getString(index);
                        value = new BString(sValue);
                        break;
                    case TypeTags.FLOAT_TAG:
                        double dValue = rs.getDouble(index);
                        value = new BFloat(dValue);
                        break;
                    case TypeTags.BOOLEAN_TAG:
                        boolean boolValue = rs.getBoolean(index);
                        value = new BBoolean(boolValue);
                        break;
                    case TypeTags.JSON_TAG:
                        String jsonValue = rs.getString(index);
                        value = JsonParser.parse(jsonValue);
                        break;
                    case TypeTags.XML_TAG:
                        String xmlValue = rs.getString(index);
                        value = new BXMLItem(xmlValue);
                        break;
                    case TypeTags.ARRAY_TAG:
                        BType arrayElementType = ((BArrayType) type).getElementType();
                        if (arrayElementType.getTag() == TypeTags.BYTE_TAG) {
                            Blob blobValue = rs.getBlob(index);
                            value = new BByteArray(blobValue.getBytes(1L, (int) blobValue.length()));
                        } else {
                            Array arrayValue = rs.getArray(index);
                            value = getDataArray(arrayValue);
                        }
                        break;
                }

                bStruct.put(fieldName, value);

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
    public BStructureType getStructType() {
        return this.type;
    }

    protected BNewArray getDataArray(Array array) throws SQLException {
        Object[] dataArray = generateArrayDataResult(array);
        if (dataArray == null || dataArray.length == 0) {
            return null;
        }

        ArrayElementAttributes nullabilityAttributes = getArrayElementNullabilityInfo(dataArray);
        Object firstNonNullElement = nullabilityAttributes.getFirstNonNullElement();
        boolean containsNull = nullabilityAttributes.containsNull();

        int length = dataArray.length;
        if (firstNonNullElement == null) {
            // Each element is null so a nil element array is returned
            return new BRefValueArray(new BRefType[length], new BArrayType(BTypes.typeNull));
        } else if (containsNull) {
            // If there are some null elements, return a union-type element array
            return createAndPopulateRefValueArray(firstNonNullElement, dataArray);
        } else {
            // If there are no null elements, return a ballerina primitive-type array
            return createAndPopulatePrimitiveValueArray(firstNonNullElement, dataArray);
        }
    }

    private BNewArray createAndPopulatePrimitiveValueArray(Object firstNonNullElement, Object[] dataArray) {
        int length = dataArray.length;
        if (firstNonNullElement instanceof String) {
            BStringArray stringDataArray = new BStringArray();
            for (int i = 0; i < length; i++) {
                stringDataArray.add(i, (String) dataArray[i]);
            }
            return stringDataArray;
        } else if (firstNonNullElement instanceof Boolean) {
            BBooleanArray boolDataArray = new BBooleanArray();
            for (int i = 0; i < length; i++) {
                boolDataArray.add(i, ((Boolean) dataArray[i]) ? 1 : 0);
            }
            return boolDataArray;
        } else if (firstNonNullElement instanceof Integer) {
            BIntArray intDataArray = new BIntArray();
            for (int i = 0; i < length; i++) {
                intDataArray.add(i, ((Integer) dataArray[i]));
            }
            return intDataArray;
        } else if (firstNonNullElement instanceof Long) {
            BIntArray longDataArray = new BIntArray();
            for (int i = 0; i < length; i++) {
                longDataArray.add(i, (Long) dataArray[i]);
            }
            return longDataArray;
        } else if (firstNonNullElement instanceof Float) {
            BFloatArray floatDataArray = new BFloatArray();
            for (int i = 0; i < length; i++) {
                floatDataArray.add(i, (Float) dataArray[i]);
            }
            return floatDataArray;
        } else if (firstNonNullElement instanceof Double) {
            BFloatArray doubleDataArray = new BFloatArray();
            for (int i = 0; i < dataArray.length; i++) {
                doubleDataArray.add(i, (Double) dataArray[i]);
            }
            return doubleDataArray;
        } else if ((firstNonNullElement instanceof BigDecimal)) {
            BFloatArray doubleDataArray = new BFloatArray();
            for (int i = 0; i < dataArray.length; i++) {
                doubleDataArray.add(i, ((BigDecimal) dataArray[i]).doubleValue());
            }
            return doubleDataArray;
        } else {
            return null;
        }
    }

    private BRefValueArray createAndPopulateRefValueArray(Object firstNonNullElement, Object[] dataArray) {
        BRefValueArray refValueArray = null;
        int length = dataArray.length;
        if (firstNonNullElement instanceof String) {
            refValueArray = createEmptyRefValueArray(BTypes.typeString, length);
            for (int i = 0; i < length; i++) {
                refValueArray.add(i, dataArray[i] != null ? new BString((String) dataArray[i]) : null);
            }
        } else if (firstNonNullElement instanceof Boolean) {
            refValueArray = createEmptyRefValueArray(BTypes.typeBoolean, length);
            for (int i = 0; i < length; i++) {
                refValueArray.add(i, dataArray[i] != null ? new BBoolean((Boolean) dataArray[i]) : null);
            }
        } else if (firstNonNullElement instanceof Integer) {
            refValueArray = createEmptyRefValueArray(BTypes.typeInt, length);
            for (int i = 0; i < length; i++) {
                refValueArray.add(i, dataArray[i] != null ? new BInteger((Integer) dataArray[i]) : null);
            }
        } else if (firstNonNullElement instanceof Long) {
            refValueArray = createEmptyRefValueArray(BTypes.typeInt, length);
            for (int i = 0; i < length; i++) {
                refValueArray.add(i, dataArray[i] != null ? new BInteger((Long) dataArray[i]) : null);
            }
        } else if (firstNonNullElement instanceof Float) {
            refValueArray = createEmptyRefValueArray(BTypes.typeFloat, length);
            for (int i = 0; i < length; i++) {
                refValueArray.add(i, dataArray[i] != null ? new BFloat((Float) dataArray[i]) : null);
            }
        } else if (firstNonNullElement instanceof Double) {
            refValueArray = createEmptyRefValueArray(BTypes.typeFloat, length);
            for (int i = 0; i < length; i++) {
                refValueArray.add(i, dataArray[i] != null ? new BFloat((Double) dataArray[i]) : null);
            }
        } else if (firstNonNullElement instanceof BigDecimal) {
            refValueArray = createEmptyRefValueArray(BTypes.typeFloat, length);
            for (int i = 0; i < length; i++) {
                refValueArray
                        .add(i, dataArray[i] != null ? new BFloat(((BigDecimal) dataArray[i]).doubleValue()) : null);
            }
        }
        return refValueArray;
    }

    private BRefValueArray createEmptyRefValueArray(BType type, int length) {
        List<BType> memberTypes = new ArrayList<>(2);
        memberTypes.add(type);
        memberTypes.add(BTypes.typeNull);
        BUnionType unionType = new BUnionType(memberTypes);
        return new BRefValueArray(new BRefType[length], new BArrayType(unionType));
    }

    private ArrayElementAttributes getArrayElementNullabilityInfo(Object[] objects) {
        ArrayElementAttributes arrayElementAttributes = new ArrayElementAttributes();
        int i = 0;
        while (i < objects.length) {
            if (objects[i] != null) {
                arrayElementAttributes.setFirstNonNullElement(objects[i]);
                if (i > 0) {
                    // If the very first element is not the very first non-null element, that means the array
                    // contains null elements
                    arrayElementAttributes.setContainsNull(true);
                }
                i++;
                break;
            }
            i++;
        }
        // If we did not find out whether the array contains null, resume the loop here
        if (!arrayElementAttributes.containsNull()) {
            while (i < objects.length) {
                if (objects[i] == null) {
                    arrayElementAttributes.setContainsNull(true);
                    break;
                }
                i++;
            }
        }
        return arrayElementAttributes;
    }

    private void generateColumnDefinitions() {
        BField[] structFields = this.type.getFields();
        columnDefs = new ArrayList<>(structFields.length);
        for (BField sf : structFields) {
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
            case TypeTags.ARRAY_TAG:
                BType elementType = ((BArrayType) type).getElementType();
                if (elementType.getTag() == TypeTags.BYTE_TAG) {
                    typeKind = TypeKind.BLOB;
                } else {
                    typeKind = TypeKind.ARRAY;
                }
                break;
            }
            ColumnDefinition def = new ColumnDefinition(sf.fieldName, typeKind);
            columnDefs.add(def);
        }
    }

    private static class ArrayElementAttributes {
        private Object firstNonNullElement;
        private boolean containsNull;

        private void setFirstNonNullElement(Object firstNonNullElement) {
            this.firstNonNullElement = firstNonNullElement;
        }

        private void setContainsNull(boolean containsNull) {
            this.containsNull = containsNull;
        }

        private Object getFirstNonNullElement() {
            return firstNonNullElement;
        }

        private boolean containsNull() {
            return containsNull;
        }
    }
}
