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

package org.wso2.ballerina.core.model.values;

/**
 * This will hold column information.
 */
public class ParamValue {

    private Object value;
    private int columnTypes;
    private String columnNames;

    public ParamValue() {
    }

    public ParamValue(Object value, int columnTypes, String columnNames) {
        this.value = value;
        this.columnTypes = columnTypes;
        this.columnNames = columnNames;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public int getColumnTypes() {
        return columnTypes;
    }

    public void setColumnTypes(int columnTypes) {
        this.columnTypes = columnTypes;
    }

    public String getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String columnNames) {
        this.columnNames = columnNames;
    }
}
