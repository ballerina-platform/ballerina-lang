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

package org.wso2.siddhi.core.query.extension.util;

import org.wso2.siddhi.core.query.selector.attribute.handler.AttributeAggregator;
import org.wso2.siddhi.query.api.definition.Attribute.Type;


public class StringConcatAggregatorString implements AttributeAggregator {
    private static final long serialVersionUID = 1358667438272544590L;
    private String aggregatedStringValue = "";

    @Override
    public void init(Type type) {

    }

    @Override
    public Type getReturnType() {
        return Type.STRING;
    }


    @Override
    public Object processAdd(Object obj) {
        if (obj instanceof String) {
            String sender = (String) obj;
            aggregatedStringValue = aggregatedStringValue + sender;
        }
        return aggregatedStringValue;
    }


    @Override
    public Object processRemove(Object obj) {
        if (obj instanceof String) {
            String sender = (String)obj;
            aggregatedStringValue = aggregatedStringValue.replace(sender, "");
        }
        return aggregatedStringValue;
    }


    @Override
    public AttributeAggregator newInstance() {
        return new StringConcatAggregatorString();
    }


    @Override
    public void destroy() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}