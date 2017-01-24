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

import java.io.IOException;

/**
 * Empty iterator for default usage. This will represent empty dataframe object in ballerina
 */
public class EmptyDataIterator implements DataIterator {
    @Override
    public void close() throws IOException {
    }

    @Override
    public boolean next() {
        return false;
    }

    @Override
    public Object getColumnValue(int columnIndex) {
        return new Object();
    }

    @Override
    public Object getColumnValue(String columnName) {
        return new Object();
    }

    @Override
    public String getString(int index) {
        return null;
    }

    @Override
    public long getLong(int index) {
        return 0;
    }

    @Override
    public int getInt(int index) {
        return 0;
    }

    @Override
    public float getFloat(int index) {
        return 0;
    }

    @Override
    public double getDouble(int index) {
        return 0;
    }

    @Override
    public boolean getBoolean(int index) {
        return false;
    }
}
