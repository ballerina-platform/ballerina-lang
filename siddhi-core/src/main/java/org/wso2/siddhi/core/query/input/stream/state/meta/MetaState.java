/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.query.input.stream.state.meta;

import org.wso2.siddhi.query.api.execution.query.input.state.StateElement;

/**
 * Created on 12/18/14.
 */
public class MetaState {
    private MetaState nextState;
    private MetaState partnerState;
    private StateElement stateElement;
    private int stateNo;

    public MetaState(int stateNo) {

        this.stateNo = stateNo;
    }

    public void setNext(MetaState nextState) {

        this.nextState = nextState;
    }

    public void setPartner(MetaState partnerState) {

        this.partnerState = partnerState;
    }

    public void setStateElement(StateElement stateElement) {

        this.stateElement = stateElement;
    }

    public MetaState getNextState() {
        return nextState;
    }

    public MetaState getPartnerState() {
        return partnerState;
    }

    public StateElement getStateElement() {
        return stateElement;
    }

    public int getStateNo() {
        return stateNo;
    }
}
