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
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Iterator implementation for table data types.
 */
public class TableIterator implements DataIterator {

    private ResultSet rs;
    private BStructType type;
    private List<ColumnDefinition> columnDefs;

    public TableIterator(ResultSet rs, BStructType type) {
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
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
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
    public BStruct generateNext() {
        BStruct bStruct = new BStruct(type);
        int longRegIndex = -1;
        int doubleRegIndex = -1;
        int stringRegIndex = -1;
        int booleanRegIndex = -1;
        int index = 0;
        BStructType.StructField[] structFields = type.getStructFields();
        for (BStructType.StructField sf : structFields) {
            BType type = sf.getFieldType();
            try {
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
                }
            } catch (SQLException e) {
                throw new BallerinaException("error in generating next row data :" + e.getMessage());
            }
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
            }
            ColumnDefinition def = new ColumnDefinition(sf.fieldName, typeKind);
            columnDefs.add(def);
        }

    }
}
