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
package org.wso2.siddhi.core.util.statemachine;

import org.wso2.siddhi.query.api.query.input.BasicStream;
import org.wso2.siddhi.query.api.query.input.TransformedStream;

public class UtilState {

    private TransformedStream transformedStream;
    private int stateNumber;
//    private List<Integer> nextStateNumbers = new ArrayList<Integer>();
    private int next = -1;
    private boolean first = false;
    private boolean last = false;
    private int nextEvery =-1;

    public UtilState(int stateNumber, TransformedStream transformedStream) {
        this.stateNumber = stateNumber;
        this.transformedStream = transformedStream;
    }

    public TransformedStream getTransformedStream() {
        return transformedStream;
    }

    public void setTransformedStream(BasicStream transformedStream) {
        this.transformedStream = transformedStream;
    }

    public int getStateNumber() {
        return stateNumber;
    }

    public void setStateNumber(int stateNumber) {
        this.stateNumber = stateNumber;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

//    public void addNext(int number) {
//        nextStateNumbers.add(number);
//    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isFirst() {
        return first;
    }

    public void setNextEvery(int nextEvery) {
        this.nextEvery = nextEvery;
    }

    public int getNextEvery() {
        return nextEvery;
    }

    public int getNext() {
        return next;
    }

    public void setNext(int next) {
        this.next = next;
    }
}
