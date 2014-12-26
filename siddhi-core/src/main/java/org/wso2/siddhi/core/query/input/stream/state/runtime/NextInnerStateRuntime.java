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

package org.wso2.siddhi.core.query.input.stream.state.runtime;

import org.wso2.siddhi.core.query.processor.Processor;

/**
 * Created on 12/19/14.
 */
public class NextInnerStateRuntime extends StreamInnerStateRuntime {

    private final InnerStateRuntime currentInnerStateRuntime;
    private final InnerStateRuntime nextInnerStateRuntime;

    public NextInnerStateRuntime(InnerStateRuntime currentInnerStateRuntime, InnerStateRuntime nextInnerStateRuntime) {

        this.currentInnerStateRuntime = currentInnerStateRuntime;
        this.nextInnerStateRuntime = nextInnerStateRuntime;
    }

    @Override
    public void setQuerySelector(Processor commonProcessor) {
//        if (!(nextInnerStateRuntime instanceof NextInnerStateRuntime)) {
            nextInnerStateRuntime.setQuerySelector(commonProcessor);
//        }
//        if (!(currentInnerStateRuntime instanceof StreamInnerStateRuntime)) {
//            currentInnerStateRuntime.setQuerySelector(commonProcessor);
//        }
    }

    @Override
    public void setStartState() {
        currentInnerStateRuntime.setStartState();
    }

    @Override
    public void init() {
        currentInnerStateRuntime.init();
        nextInnerStateRuntime.init();
    }
}
