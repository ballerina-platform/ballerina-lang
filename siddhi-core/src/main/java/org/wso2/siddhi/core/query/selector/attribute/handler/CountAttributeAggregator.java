/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.core.query.selector.attribute.handler;

import org.wso2.siddhi.query.api.definition.Attribute;

public class CountAttributeAggregator implements AttributeAggregator {

    private long value = 0;
    private static Attribute.Type type = Attribute.Type.LONG;

    @Override
    public void init(Attribute.Type type) {
        //type is always long
    }

    public Attribute.Type getReturnType() {
        return type;
    }

    @Override
    public Object processAdd(Object obj) {
        value ++;
        return value;
    }

    @Override
    public Object processRemove(Object obj) {
        value --;
        return value;
    }

    @Override
    public AttributeAggregator newInstance() {
        return new CountAttributeAggregator();
    }

    @Override
    public void destroy(){
        //nothing to destroy
    }
}
