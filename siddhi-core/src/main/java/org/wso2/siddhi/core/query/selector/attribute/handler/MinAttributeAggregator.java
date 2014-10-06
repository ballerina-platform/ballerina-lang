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

public class MinAttributeAggregator implements AttributeAggregator {

    private MinAttributeAggregator minOutputAttributeAggregator;

    public void init(Attribute.Type type) {
        switch (type) {
            case FLOAT:
                minOutputAttributeAggregator = new MinAttributeAggregatorFloat();
                break;
            case INT:
                minOutputAttributeAggregator = new MinAttributeAggregatorInt();
                break;
            case LONG:
                minOutputAttributeAggregator = new MinAttributeAggregatorLong();
                break;
            case DOUBLE:
                minOutputAttributeAggregator = new MinAttributeAggregatorDouble();
                break;
            default:
                throw new OperationNotSupportedException("Min not supported for " + type);
        }

    }

    public Attribute.Type getReturnType() {
        return minOutputAttributeAggregator.getReturnType();
    }

    @Override
    public Object processAdd(Object obj) {
        return minOutputAttributeAggregator.processAdd(obj);
    }

    @Override
    public Object processRemove(Object obj) {
        return minOutputAttributeAggregator.processRemove(obj);
    }

    @Override
    public AttributeAggregator newInstance() {
        return minOutputAttributeAggregator.newInstance();
    }

    @Override
    public void start() {
        //Nothing to start
    }

    @Override
    public void stop() {
        //nothing to stop
    }

    class MinAttributeAggregatorDouble extends MinAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.DOUBLE;
        private Deque<Double> minDeque = new LinkedList<Double>();
        private volatile Double minValue = null;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public synchronized Object processAdd(Object obj) {
            Double value = (Double) obj;
            for (Iterator<Double> iterator = minDeque.descendingIterator(); iterator.hasNext(); ) {

                if (iterator.next() > value) {
                    iterator.remove();
                }
            }
            minDeque.addLast(value);
            if (minValue == null) {
                minValue = value;
            } else if (minValue > value) {
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
        public AttributeAggregator newInstance() {
            return new MinAttributeAggregatorDouble();
        }

    }

    class MinAttributeAggregatorFloat extends MinAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.FLOAT;
        private Deque<Float> minDeque = new LinkedList<Float>();
        private volatile Float minValue = null;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public synchronized Object processAdd(Object obj) {
            Float value = (Float) obj;
            for (Iterator<Float> iterator = minDeque.descendingIterator(); iterator.hasNext(); ) {

                if (iterator.next() > value) {
                    iterator.remove();
                }
            }
            minDeque.addLast(value);
            if (minValue == null) {
                minValue = value;
            } else if (minValue > value) {
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
        public AttributeAggregator newInstance() {
            return new MinAttributeAggregatorFloat();
        }

    }

    class MinAttributeAggregatorInt extends MinAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.INT;
        private Deque<Integer> minDeque = new LinkedList<Integer>();
        private volatile Integer minValue = null;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public synchronized Object processAdd(Object obj) {
            Integer value = (Integer) obj;
            for (Iterator<Integer> iterator = minDeque.descendingIterator(); iterator.hasNext(); ) {

                if (iterator.next() > value) {
                    iterator.remove();
                }
            }
            minDeque.addLast(value);
            if (minValue == null) {
                minValue = value;
            } else if (minValue > value) {
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
        public AttributeAggregator newInstance() {
            return new MinAttributeAggregatorInt();
        }

    }

    class MinAttributeAggregatorLong extends MinAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.LONG;
        private Deque<Long> minDeque = new LinkedList<Long>();
        private volatile Long minValue = null;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public synchronized Object processAdd(Object obj) {
            Long value = (Long) obj;
            for (Iterator<Long> iterator = minDeque.descendingIterator(); iterator.hasNext(); ) {

                if (iterator.next() > value) {
                    iterator.remove();
                }
            }
            minDeque.addLast(value);
            if (minValue == null) {
                minValue = value;
            } else if (minValue > value) {
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
        public AttributeAggregator newInstance() {
            return new MinAttributeAggregatorLong();
        }

    }

}
