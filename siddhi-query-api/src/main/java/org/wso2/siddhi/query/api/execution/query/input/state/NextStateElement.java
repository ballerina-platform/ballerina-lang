/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.query.api.execution.query.input.state;

import org.wso2.siddhi.query.api.expression.constant.TimeConstant;

public class NextStateElement implements StateElement {

    private StateElement StateElement;
    private StateElement nextStateElement;
    private TimeConstant within;

    public NextStateElement(StateElement StateElement,
                            StateElement nextStateElement, TimeConstant within) {
        this.StateElement = StateElement;
        this.nextStateElement = nextStateElement;
        this.within = within;
    }

    public NextStateElement(StateElement StateElement,
                            StateElement nextStateElement) {
        this(StateElement, nextStateElement, null);
    }

    public StateElement getStateElement() {
        return StateElement;
    }

    public StateElement getNextStateElement() {
        return nextStateElement;
    }

    public TimeConstant getWithin() {
        return within;
    }

    public void setWithin(TimeConstant within) {
        this.within = within;
    }

    @Override
    public String toString() {
        return "NextStateElement{" +
                "StateElement=" + StateElement +
                ", nextStateElement=" + nextStateElement +
                ", within=" + within +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NextStateElement)) return false;

        NextStateElement that = (NextStateElement) o;

        if (StateElement != null ? !StateElement.equals(that.StateElement) : that.StateElement != null) return false;
        if (nextStateElement != null ? !nextStateElement.equals(that.nextStateElement) : that.nextStateElement != null)
            return false;
        if (within != null ? !within.equals(that.within) : that.within != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = StateElement != null ? StateElement.hashCode() : 0;
        result = 31 * result + (nextStateElement != null ? nextStateElement.hashCode() : 0);
        result = 31 * result + (within != null ? within.hashCode() : 0);
        return result;
    }
}
