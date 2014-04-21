/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.query.api.expression;

import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.query.QueryEventSource;

import java.util.List;

public class Type extends Expression {
    private Attribute.Type type;

    public Type(Attribute.Type type) {
        this.type = type;
    }

    @Override
    protected void validate(List<QueryEventSource> queryEventSources, String streamReferenceId,
                            boolean processInStreamDefinition) {
        //nothing to validate
    }

    public Attribute.Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Type{" +
               "type=" + type +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Type type1 = (Type) o;

        if (type != type1.type) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return type != null ? type.hashCode() : 0;
    }
}
