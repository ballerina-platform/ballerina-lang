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
package org.wso2.siddhi.core.query.selector.attribute.handler.min;

import org.wso2.siddhi.core.query.selector.attribute.handler.OutputAttributeAggregator;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

public class MinOutputAttributeAggregatorDouble implements OutputAttributeAggregator {

    private Deque<Double> minDeque = new LinkedList<Double>();
    private volatile Double minValue = null;
    private  static final Attribute.Type type = Attribute.Type.DOUBLE;

    public Attribute.Type getReturnType() {
        return type;
    }

    @Override
    public synchronized Object processAdd(Object obj) {
        Double value = ((Double) obj);
        for (Iterator<Double> iterator = minDeque.descendingIterator(); iterator.hasNext(); ) {

            if (iterator.next() > value) {
                iterator.remove();
            }
        }
        minDeque.addLast(value);
        if(minValue==null)
            minValue = value;
        else if (minValue > value) {
            minValue = value;
        }
        return minValue;
    }

    @Override
    public synchronized Object processRemove(Object obj) {
        minDeque.removeFirstOccurrence(obj);
        minValue = minDeque.peekFirst();
        return minValue;
    }

    @Override
    public OutputAttributeAggregator newInstance() {
        return new MinOutputAttributeAggregatorDouble();
    }

    @Override
    public void destroy(){

    }
}
