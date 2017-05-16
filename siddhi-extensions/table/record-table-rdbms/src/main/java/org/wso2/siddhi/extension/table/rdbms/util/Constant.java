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
package org.wso2.siddhi.extension.table.rdbms.util;

import org.wso2.siddhi.query.api.definition.Attribute;

/**
 * Denotes a bean class which is used to keep track of constants which may be specified in Siddhi query conditions.
 * At compile-time, the constant is evaluated and kept for future (runtime) use.
 */
public class Constant {

    private Object value;
    private Attribute.Type type;

    public Constant(Object value, Attribute.Type type) {
        this.value = value;
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public Attribute.Type getType() {
        return type;
    }
}
