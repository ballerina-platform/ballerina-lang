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
package org.wso2.siddhi.query.api.execution.query.input.state;

import org.wso2.siddhi.query.api.execution.query.input.stream.BasicSingleInputStream;
import org.wso2.siddhi.query.api.expression.constant.TimeConstant;

/**
 * States of pattern
 */
public class State {

    public static StateElement every(StateElement stateElement) {
        return new EveryStateElement(stateElement);
    }


    public static StateElement every(StateElement stateElement, TimeConstant time) {
        return new EveryStateElement(stateElement, time);
    }

    public static StateElement logicalAnd(StreamStateElement streamStateElement1,
                                          StreamStateElement streamStateElement2) {
        return new LogicalStateElement(streamStateElement1, LogicalStateElement.Type.AND, streamStateElement2);
    }

    public static StateElement logicalOr(StreamStateElement streamStateElement1,
                                         StreamStateElement streamStateElement2) {
        return new LogicalStateElement(streamStateElement1, LogicalStateElement.Type.OR, streamStateElement2);
    }

    public static StateElement logicalAnd(StreamStateElement streamStateElement1,
                                          StreamStateElement streamStateElement2, TimeConstant time) {
        return new LogicalStateElement(streamStateElement1, LogicalStateElement.Type.AND, streamStateElement2, time);
    }

    public static StateElement logicalOr(StreamStateElement streamStateElement1,
                                         StreamStateElement streamStateElement2, TimeConstant time) {
        return new LogicalStateElement(streamStateElement1, LogicalStateElement.Type.OR, streamStateElement2, time);
    }

    public static StateElement logicalNot(StreamStateElement notStreamStateElement,
                                          TimeConstant time) {
        return new LogicalStateElement(null, LogicalStateElement.Type.NOT, notStreamStateElement, time);
    }

    public static StateElement logicalNotAnd(StreamStateElement notStreamStateElement,
                                             StreamStateElement andStreamStateElement,
                                             TimeConstant time) {
        return new LogicalStateElement(andStreamStateElement, LogicalStateElement.Type.NOT, notStreamStateElement,
                time);
    }

    public static StateElement logicalNotAnd(StreamStateElement notStreamStateElement,
                                             StreamStateElement andStreamStateElement) {
        return new LogicalStateElement(andStreamStateElement, LogicalStateElement.Type.NOT, notStreamStateElement);
    }

    public static StateElement next(StateElement stateElement,
                                    StateElement followedByStateElement) {
        return new NextStateElement(stateElement, followedByStateElement);
    }

    public static StateElement next(StateElement stateElement,
                                    StateElement followedByStateElement, TimeConstant time) {
        return new NextStateElement(stateElement, followedByStateElement, time);
    }

    public static StateElement count(StreamStateElement streamStateElement, int min, int max) {
        return new CountStateElement(streamStateElement, min, max);
    }

    public static StateElement countMoreThanEqual(StreamStateElement streamStateElement, int min) {
        return new CountStateElement(streamStateElement, min, CountStateElement.ANY);
    }

    public static StateElement countLessThanEqual(StreamStateElement streamStateElement, int max) {
        return new CountStateElement(streamStateElement, CountStateElement.ANY, max);
    }

    public static StateElement count(StreamStateElement streamStateElement, int min, int max, TimeConstant time) {
        return new CountStateElement(streamStateElement, min, max, time);
    }

    public static StateElement countMoreThanEqual(StreamStateElement streamStateElement, int min, TimeConstant time) {
        return new CountStateElement(streamStateElement, min, CountStateElement.ANY, time);
    }

    public static StateElement countLessThanEqual(StreamStateElement streamStateElement, int max, TimeConstant time) {
        return new CountStateElement(streamStateElement, CountStateElement.ANY, max, time);
    }

    public static StreamStateElement stream(BasicSingleInputStream basicSingleInputStream) {
        return new StreamStateElement(basicSingleInputStream);
    }

    public static StreamStateElement stream(BasicSingleInputStream basicSingleInputStream, TimeConstant time) {
        return new StreamStateElement(basicSingleInputStream, time);
    }

    public static StateElement zeroOrMany(StreamStateElement streamStateElement) {
        return new CountStateElement(streamStateElement, 0, CountStateElement.ANY);
    }

    public static StateElement zeroOrMany(StreamStateElement streamStateElement, TimeConstant within) {
        return new CountStateElement(streamStateElement, 0, CountStateElement.ANY, within);
    }

    public static StateElement zeroOrOne(StreamStateElement streamStateElement) {
        return new CountStateElement(streamStateElement, 0, 1);
    }

    public static StateElement zeroOrOne(StreamStateElement streamStateElement, TimeConstant within) {
        return new CountStateElement(streamStateElement, 0, 1, within);
    }

    public static StateElement oneOrMany(StreamStateElement streamStateElement) {
        return new CountStateElement(streamStateElement, 1, CountStateElement.ANY);
    }

    public static StateElement oneOrMany(StreamStateElement streamStateElement, TimeConstant within) {
        return new CountStateElement(streamStateElement, 1, CountStateElement.ANY, within);
    }

}
