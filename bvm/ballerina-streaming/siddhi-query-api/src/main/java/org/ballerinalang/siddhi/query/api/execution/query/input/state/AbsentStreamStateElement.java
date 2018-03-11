/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.siddhi.query.api.execution.query.input.state;

import org.ballerinalang.siddhi.query.api.execution.query.input.stream.BasicSingleInputStream;
import org.ballerinalang.siddhi.query.api.expression.constant.TimeConstant;

/**
 * Absent state element used in pattern to handle not operations.
 */
public class AbsentStreamStateElement extends StreamStateElement {

    private static final long serialVersionUID = 1L;
    private TimeConstant waitingTime;
    private int[] queryContextStartIndex;
    private int[] queryContextEndIndex;

    public AbsentStreamStateElement(BasicSingleInputStream basicSingleInputStream) {
        super(basicSingleInputStream);
    }

    public AbsentStreamStateElement(BasicSingleInputStream basicSingleInputStream, TimeConstant within) {
        super(basicSingleInputStream, within);
    }

    public AbsentStreamStateElement waitingTime(TimeConstant waitingTime) {
        this.waitingTime = waitingTime;
        return this;
    }

    public TimeConstant getWaitingTime() {
        return waitingTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        AbsentStreamStateElement that = (AbsentStreamStateElement) o;

        return waitingTime != null ? waitingTime.equals(that.waitingTime) : that.waitingTime == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (waitingTime != null ? waitingTime.hashCode() : 0);
        return result;
    }

    @Override
    public int[] getQueryContextStartIndex() {
        return queryContextStartIndex;
    }

    @Override
    public void setQueryContextStartIndex(int[] lineAndColumn) {
        queryContextStartIndex = lineAndColumn;
    }

    @Override
    public int[] getQueryContextEndIndex() {
        return queryContextEndIndex;
    }

    @Override
    public void setQueryContextEndIndex(int[] lineAndColumn) {
        queryContextEndIndex = lineAndColumn;
    }
}
