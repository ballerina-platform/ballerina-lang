/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime;

import io.ballerina.runtime.types.BStructureType;
import io.ballerina.runtime.values.DecimalValue;
import io.ballerina.runtime.values.MapValue;

import java.util.List;

/**
 *  This interface represents an data iterator operations.
 *  Each data source need to implement their own implementation by implementing this interface.
 *  Known implementations: {@code SQLDataIterator}
 *
 *  @since 0.995.0
 */
public interface DataIterator {

    boolean next();

    void close();

    void reset();

    String getString(int columnIndex);

    Long getInt(int columnIndex);

    Double getFloat(int columnIndex);

    Boolean getBoolean(int columnIndex);

    String getBlob(int columnIndex);

    DecimalValue getDecimal(int columnIndex);

    Object[] getStruct(int columnIndex);

    Object[] getArray(int columnIndex);

    MapValue<?, ?> generateNext();

    List<ColumnDefinition> getColumnDefinitions();

    BStructureType getStructType();
}
