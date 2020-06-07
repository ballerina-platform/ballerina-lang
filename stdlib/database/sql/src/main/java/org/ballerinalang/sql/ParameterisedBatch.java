/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.sql;

import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.api.BString;

import java.util.ArrayList;
import java.util.List;

/**
 * Java class to represent a batch of SQL command.
 */
public class ParameterisedBatch {
    private String sqlQuery;
    private List<MapValue<BString, Object>> params;

    public ParameterisedBatch() {
        this.params = new ArrayList<>();
    }

    public ParameterisedBatch(String sqlQuery, MapValue<BString, Object> param) {
        this.sqlQuery = sqlQuery;
        this.params = new ArrayList<>();
        this.params.add(param);
    }

    public void addRecord(MapValue<BString, Object> record) {
        this.params.add(record);
    }

    public String getQuery() {
        return this.sqlQuery;
    }

    public List<MapValue<BString, Object>> getParams() {
        return params;
    }
}
