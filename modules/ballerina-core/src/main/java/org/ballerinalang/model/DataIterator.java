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

import org.ballerinalang.model.values.BValue;

import java.util.Map;

/**
 *  This interface represents an data iterator operations.
 *  Each data source need to implement their own implementation by implementing this interface.
 *  Methods which belongs to DataIterator will expose through native functions in ballerina.model.datatables package.
 *  Known implementations: {@code SQLDataIterator}
 *
 *  @since 0.8.0
 */
public interface DataIterator {

    boolean next();

    boolean isLast();

    void close(boolean isInTransaction);

    String getString(String columnName);

    long getInt(String columnName);

    double getFloat(String columnName);

    boolean getBoolean(String columnName);

    String getObjectAsString(String columnName);

    BValue get(String columnName, int type);

    Map<String, Object> getArray(String columnName);
}
