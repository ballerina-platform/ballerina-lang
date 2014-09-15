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

import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

public class MaxAttributeAggregator implements AttributeAggregator {

    private Attribute.Type type;
    private MaxAttributeAggregator maxOutputAttributeAggregator;

    public void init(Attribute.Type type){
        this.type =type;
        switch (type){
            case FLOAT:
                maxOutputAttributeAggregator = new MaxAttributeAggregatorFloat();
                break;
            case INT:
                maxOutputAttributeAggregator = new MaxAttributeAggregatorInt();
                break;
            case LONG:
                maxOutputAttributeAggregator = new MaxAttributeAggregatorLong();
                break;
            case DOUBLE:
                maxOutputAttributeAggregator = new MaxAttributeAggregatorDouble();
                break;
            default:
                throw new OperationNotSupportedException("Max not supported for " + type);
        }

    }

    public Attribute.Type getReturnType() {
        return maxOutputAttributeAggregator.getReturnType();
    }

    @Override
    public Object processAdd(Object obj) {
        return maxOutputAttributeAggregator.processAdd(obj);
    }

    @Override
    public Object processRemove(Object obj) {
        return maxOutputAttributeAggregator.processRemove(obj);
    }

    @Override
    public AttributeAggregator newInstance() {
        return maxOutputAttributeAggregator.newInstance();
    }

    @Override
    public void destroy() {

    }

    class MaxAttributeAggregatorDouble extends MaxAttributeAggregator {

        private Deque<Double> maxDeque = new LinkedList<Double>();
        private volatile Double maxValue = null;
        private final Attribute.Type type = Attribute.Type.DOUBLE;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public synchronized Object processAdd(Object obj) {
            Double value = (Double) obj;
            for (Iterator<Double> iterator = maxDeque.descendingIterator(); iterator.hasNext(); ) {
                if (iterator.next() < value) {
                    iterator.remove();
                } else {
                    break;
                }
            }
            maxDeque.addLast(value);
            if(maxValue==null){
                maxValue = value;
            } else if (maxValue < value) {
                maxValue = value;
            }
            return maxValue;
        }

        @Override
        public synchronized Object processRemove(Object obj) {
            maxDeque.removeFirstOccurrence(obj);
            maxValue = maxDeque.peekFirst();
            return maxValue;
        }

        @Override
        public AttributeAggregator newInstance() {
            return new MaxAttributeAggregatorDouble();
        }
    }

    class MaxAttributeAggregatorFloat extends MaxAttributeAggregator {

        private Deque<Float> maxDeque = new LinkedList<Float>();
        private volatile Float maxValue = null;
        private final Attribute.Type type = Attribute.Type.FLOAT;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public synchronized Object processAdd(Object obj) {
            Float value = (Float) obj;
            for (Iterator<Float> iterator = maxDeque.descendingIterator(); iterator.hasNext(); ) {

                if (iterator.next() < value) {
                    iterator.remove();
                } else {
                    break;
                }
            }
            maxDeque.addLast(value);
            if(maxValue==null) {
                maxValue = value;
            } else if (maxValue < value) {
                maxValue = value;
            }
            return maxValue;
        }

        @Override
        public synchronized Object processRemove(Object obj) {
            maxDeque.removeFirstOccurrence(obj);
            maxValue = maxDeque.peekFirst();
            return maxValue;
        }

        @Override
        public AttributeAggregator newInstance() {
            return new MaxAttributeAggregatorFloat();
        }

    }

    class MaxAttributeAggregatorInt extends MaxAttributeAggregator {

        private Deque<Integer> maxDeque = new LinkedList<Integer>();
        private volatile Integer maxValue = null;
        private final Attribute.Type type = Attribute.Type.INT;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public synchronized Object processAdd(Object obj) {
            Integer value = (Integer) obj;
            for (Iterator<Integer> iterator = maxDeque.descendingIterator(); iterator.hasNext(); ) {
                if (iterator.next() < value) {
                    iterator.remove();
                } else {
                    break;
                }
            }
            maxDeque.addLast(value);
            if(maxValue==null){
                maxValue = value;
            } else if (maxValue < value) {
                maxValue = value;
            }
            return maxValue;
        }

        @Override
        public synchronized Object processRemove(Object obj) {
            maxDeque.removeFirstOccurrence(obj);
            maxValue = maxDeque.peekFirst();
            return maxValue;
        }

        @Override
        public AttributeAggregator newInstance() {
            return new MaxAttributeAggregatorInt();
        }

    }

    class MaxAttributeAggregatorLong extends MaxAttributeAggregator {

        private Deque<Long> maxDeque = new LinkedList<Long>();
        private volatile Long maxValue = null;
        private final Attribute.Type type = Attribute.Type.LONG;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public synchronized Object processAdd(Object obj) {
            Long value = (Long) obj;
            for (Iterator<Long> iterator = maxDeque.descendingIterator(); iterator.hasNext(); ) {
                if (iterator.next() < value) {
                    iterator.remove();
                } else {
                    break;
                }
            }
            maxDeque.addLast(value);
            if(maxValue==null){
                maxValue = value;
            } else if (maxValue < value) {
                maxValue = value;
            }
            return maxValue;
        }

        @Override
        public synchronized Object processRemove(Object obj) {
            maxDeque.removeFirstOccurrence(obj);
            maxValue = maxDeque.peekFirst();
            return maxValue;
        }

        @Override
        public AttributeAggregator newInstance() {
            return new MaxAttributeAggregatorLong();
        }

    }

}
