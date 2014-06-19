/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.query.selector.attribute.handler.avg;

import org.wso2.siddhi.core.query.selector.attribute.handler.OutputAttributeAggregator;
import org.wso2.siddhi.query.api.definition.Attribute;

public class AvgOutputAttributeAggregatorFloat implements OutputAttributeAggregator {

    private double value = 0.0;
    private long count=0;
    private final static Attribute.Type type = Attribute.Type.DOUBLE;


    public Attribute.Type getReturnType() {
        return this.type;
    }

    @Override
    public Object processAdd(Object obj) {
//        if(obj==null){
//            System.out.println(obj);
//        }
        count++;
        value += (Float) obj;
        if(count==0){
            return 0;
        }
        return value/count;
    }

    @Override
    public Object processRemove(Object obj) {
        count--;
        value -= (Float) obj;
        if(count==0){
            return 0;
        }
        return value/count;
    }

    @Override
    public OutputAttributeAggregator newInstance() {
        return new AvgOutputAttributeAggregatorFloat();
    }

    @Override
    public void destroy(){

    }
}
