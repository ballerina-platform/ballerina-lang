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

package org.ballerinalang.jvm.values;

import org.ballerinalang.jvm.ColumnDefinition;
import org.ballerinalang.jvm.DataIterator;
import org.ballerinalang.jvm.JSONParser;
import org.ballerinalang.jvm.TableResourceManager;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Iterator implementation for table data types.
 *
 * @since 0.995.0
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
            String val = rs.getString(columnIndex);
            return rs.wasNull() ? null : val;
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    @Override
    public Long getInt(int columnIndex) {
        try {
            long val = rs.getLong(columnIndex);
            return rs.wasNull() ? null : val;
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    @Override
    public Double getFloat(int columnIndex) {
        try {
            double val = rs.getDouble(columnIndex);
            return rs.wasNull() ? null : val;
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    @Override
    public Boolean getBoolean(int columnIndex) {
        try {
            boolean val = rs.getBoolean(columnIndex);
            return rs.wasNull() ? null : val;
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    @Override
    public String getBlob(int columnIndex) {
        try {
            Blob bValue = rs.getBlob(columnIndex);
            return rs.wasNull() ? null : new String(bValue.getBytes(1, (int) bValue.length()));
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    @Override
    public Object[] getStruct(int columnIndex) {
        Object[] objArray = null;
        try {
            Struct data = (Struct) rs.getObject(columnIndex);
            if (!rs.wasNull() && data != null) {
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
    public MapValue<String, Object> generateNext() {
        MapValue<String, Object> bStruct = new MapValueImpl<>(type);
        int index = 0;
        try {
            Collection<BField> structFields = type.getFields().values();
            for (BField sf : structFields) {
                BType type = sf.getFieldType();
                String fieldName = sf.getFieldName();
                Object value = null;
                ++index;
                switch (type.getTag()) {
                    case TypeTags.INT_TAG:
                        value = rs.getLong(index);
                        break;
                    case TypeTags.STRING_TAG:
                        value = rs.getString(index);
                        break;
                    case TypeTags.FLOAT_TAG:
                        value = rs.getDouble(index);
                        break;
                    case TypeTags.DECIMAL_TAG:
                        value = rs.getBigDecimal(index);
                        break;
                    case TypeTags.BOOLEAN_TAG:
                        value = rs.getBoolean(index);
                        break;
                    case TypeTags.JSON_TAG:
                        String jsonValue = rs.getString(index);
                        value = JSONParser.parse(jsonValue);
                        break;
                    case TypeTags.XML_TAG:
                        String xmlValue = rs.getString(index);
                        value = new XMLItem(xmlValue);
                        break;
                    case TypeTags.ARRAY_TAG:
                        BType arrayElementType = ((BArrayType) type).getElementType();
                        if (arrayElementType.getTag() == TypeTags.BYTE_TAG) {
                            Blob blobValue = rs.getBlob(index);
                            value = new ArrayValue(blobValue.getBytes(1L, (int) blobValue.length()));
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

    protected ArrayValue getDataArray(Array array) throws SQLException {
        Object[] dataArray = generateArrayDataResult(array);
        if (dataArray == null || dataArray.length == 0) {
            return null;
        }

        ArrayElementAttributes nullabilityAttributes = getArrayElementNullabilityInfo(dataArray);
        Object firstNonNullElement = nullabilityAttributes.getFirstNonNullElement();
        boolean containsNull = nullabilityAttributes.containsNull();

        if (firstNonNullElement == null) {
            // Each element is null so a nil element array is returned
            return new ArrayValue(new BArrayType(BTypes.typeNull));
        } else if (containsNull) {
            // If there are some null elements, return a union-type element array
            return createAndPopulateRefValueArray(firstNonNullElement, dataArray);
        } else {
            // If there are no null elements, return a ballerina primitive-type array
            return createAndPopulatePrimitiveValueArray(firstNonNullElement, dataArray);
        }
    }

    private ArrayValue createAndPopulatePrimitiveValueArray(Object firstNonNullElement, Object[] dataArray) {
        int length = dataArray.length;
        if (firstNonNullElement instanceof String) {
            ArrayValue stringDataArray = new ArrayValue(BTypes.typeString);
            for (int i = 0; i < length; i++) {
                stringDataArray.add(i, (String) dataArray[i]);
            }
            return stringDataArray;
        } else if (firstNonNullElement instanceof Boolean) {
            ArrayValue boolDataArray = new ArrayValue(BTypes.typeBoolean);
            for (int i = 0; i < length; i++) {
                boolDataArray.add(i, ((Boolean) dataArray[i]) ? 1 : 0);
            }
            return boolDataArray;
        } else if (firstNonNullElement instanceof Integer) {
            ArrayValue intDataArray = new ArrayValue(BTypes.typeInt);
            for (int i = 0; i < length; i++) {
                intDataArray.add(i, dataArray[i]);
            }
            return intDataArray;
        } else if (firstNonNullElement instanceof Long) {
            ArrayValue longDataArray = new ArrayValue(BTypes.typeInt);
            for (int i = 0; i < length; i++) {
                longDataArray.add(i, dataArray[i]);
            }
            return longDataArray;
        } else if (firstNonNullElement instanceof Float) {
            ArrayValue floatDataArray = new ArrayValue(BTypes.typeFloat);
            for (int i = 0; i < length; i++) {
                floatDataArray.add(i, dataArray[i]);
            }
            return floatDataArray;
        } else if (firstNonNullElement instanceof Double) {
            ArrayValue doubleDataArray = new ArrayValue(BTypes.typeFloat);
            for (int i = 0; i < dataArray.length; i++) {
                doubleDataArray.add(i, dataArray[i]);
            }
            return doubleDataArray;
        } else if ((firstNonNullElement instanceof BigDecimal)) {
            ArrayValue decimalDataArray = new ArrayValue(BTypes.typeDecimal);
            for (int i = 0; i < dataArray.length; i++) {
                decimalDataArray.add(i, dataArray[i]);
            }
            return decimalDataArray;
        } else {
            return null;
        }
    }

    private ArrayValue createAndPopulateRefValueArray(Object firstNonNullElement, Object[] dataArray) {
        ArrayValue refValueArray = null;
        int length = dataArray.length;
        if (firstNonNullElement instanceof String) {
            refValueArray = createEmptyRefValueArray(BTypes.typeString, length);
            for (int i = 0; i < length; i++) {
                refValueArray.add(i, dataArray[i] != null ? (String) dataArray[i] : null);
            }
        } else if (firstNonNullElement instanceof Boolean) {
            refValueArray = createEmptyRefValueArray(BTypes.typeBoolean, length);
            for (int i = 0; i < length; i++) {
                refValueArray.add(i, dataArray[i] != null ? (Boolean) dataArray[i] : null);
            }
        } else if (firstNonNullElement instanceof Integer) {
            refValueArray = createEmptyRefValueArray(BTypes.typeInt, length);
            for (int i = 0; i < length; i++) {
                refValueArray.add(i, dataArray[i] != null ? (Long) dataArray[i] : null);
            }
        } else if (firstNonNullElement instanceof Long) {
            refValueArray = createEmptyRefValueArray(BTypes.typeInt, length);
            for (int i = 0; i < length; i++) {
                refValueArray.add(i, dataArray[i] != null ? (Long) dataArray[i] : null);
            }
        } else if (firstNonNullElement instanceof Float) {
            refValueArray = createEmptyRefValueArray(BTypes.typeFloat, length);
            for (int i = 0; i < length; i++) {
                refValueArray.add(i, dataArray[i] != null ? (Double) dataArray[i] : null);
            }
        } else if (firstNonNullElement instanceof Double) {
            refValueArray = createEmptyRefValueArray(BTypes.typeFloat, length);
            for (int i = 0; i < length; i++) {
                refValueArray.add(i, dataArray[i] != null ? (Double) dataArray[i] : null);
            }
        } else if (firstNonNullElement instanceof BigDecimal) {
            refValueArray = createEmptyRefValueArray(BTypes.typeDecimal, length);
            for (int i = 0; i < length; i++) {
                refValueArray.add(i, dataArray[i] != null ? (BigDecimal) dataArray[i] : null);
            }
        }
        return refValueArray;
    }

    private ArrayValue createEmptyRefValueArray(BType type, int length) {
        List<BType> memberTypes = new ArrayList<>(2);
        memberTypes.add(type);
        memberTypes.add(BTypes.typeNull);
        BUnionType unionType = new BUnionType(memberTypes);
        return new ArrayValue(new BArrayType(unionType));
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
        Collection<BField> structFields = this.type.getFields().values();
        columnDefs = new ArrayList<>(structFields.size());
        for (BField sf : structFields) {
            BType type = sf.getFieldType();
            int typeTag = TypeTags.ANY_TAG;
            switch (type.getTag()) {
                case TypeTags.INT_TAG | TypeTags.STRING_TAG | TypeTags.FLOAT_TAG | TypeTags.BOOLEAN_TAG
                     | TypeTags.JSON_TAG | TypeTags.XML_TAG:
                    typeTag = type.getTag();
                    break;
                case TypeTags.ARRAY_TAG:
                    BType elementType = ((BArrayType) type).getElementType();
                    if (elementType.getTag() == TypeTags.BYTE_TAG) {
                        typeTag = TypeTags.BYTE_TAG;
                    } else {
                        typeTag = TypeTags.ARRAY_TAG;
                    }
                    break;
            }
            ColumnDefinition def = new ColumnDefinition(sf.getFieldName(), typeTag);
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
