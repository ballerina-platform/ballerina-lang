/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.executor;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.query.api.definition.Attribute;

import static org.wso2.siddhi.core.util.SiddhiConstants.STREAM_EVENT_CHAIN_INDEX;
import static org.wso2.siddhi.core.util.SiddhiConstants.STREAM_EVENT_INDEX_IN_CHAIN;
import static org.wso2.siddhi.core.util.SiddhiConstants.UNKNOWN_STATE;

/**
 * Executor class for Siddhi event attributes. This executor is used to extract attribute value from
 * {@link ComplexEvent}.
 */
public class VariableExpressionExecutor implements ExpressionExecutor {
    private Attribute attribute;
    private int[] position = new int[]{UNKNOWN_STATE, UNKNOWN_STATE, UNKNOWN_STATE, UNKNOWN_STATE};
    //Position[stream event chain index, stream event index, stream attribute type index, stream attribute index]
    //stream attribute type index -> outData = 2; onAfterWindowData = 1; beforeWindowData = 0;

    public VariableExpressionExecutor(Attribute attribute, int streamEventChainIndex, int streamEventIndexInChain) {
        this.attribute = attribute;
        position[STREAM_EVENT_CHAIN_INDEX] = streamEventChainIndex;
        position[STREAM_EVENT_INDEX_IN_CHAIN] = streamEventIndexInChain;
    }

    @Override
    public Object execute(ComplexEvent event) {
        return event.getAttribute(position);
    }


    public Attribute.Type getReturnType() {
        return attribute.getType();
    }

    @Override
    public ExpressionExecutor cloneExecutor(String key) {
        return this;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public int[] getPosition() {
        return position;
    }

    /**
     * Method to set the position of variable to be executed. For stream events(simple queries) position can be an array
     * of size 2 and for state events(join/pattern queries) position should be an array of size 4.
     *
     * @param position attribute position
     */
    public void setPosition(int[] position) {
        if (position.length == 2) {
            this.position[2] = position[0];
            this.position[3] = position[1];
        } else if (position.length == 4) {
            this.position = position.clone();
        }
    }


}
