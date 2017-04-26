/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.extension.table.rdbms;

import org.wso2.siddhi.core.util.collection.operator.CompiledCondition;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.Map;

public class RDBMSCompiledCondition implements CompiledCondition {

    private String compiledQuery;
    private String tableName;
    private Map<String, Attribute.Type> streamTypeMap;


    public RDBMSCompiledCondition(String tableName, String compiledQuery, Map<String, Attribute.Type> streamTypeMap) {
        this.tableName = tableName;
        this.compiledQuery = compiledQuery;
        this.streamTypeMap = streamTypeMap;
    }

    @Override
    public CompiledCondition cloneCompiledCondition(String key) {
        return null;
    }

    public String getCompiledQuery() {
        return compiledQuery;
    }

    public String toString() {
        return getCompiledQuery();
    }

    public Attribute.Type getStreamType(String id) {
        return this.streamTypeMap.get(id);
    }

}
