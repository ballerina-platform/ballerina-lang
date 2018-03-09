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

package org.wso2.siddhi.core.query.input.stream.state.runtime;

import org.wso2.siddhi.core.query.input.stream.state.CountPostStateProcessor;
import org.wso2.siddhi.core.query.input.stream.state.CountPreStateProcessor;

/**
 * Created on 12/19/14.
 */
public class CountInnerStateRuntime extends StreamInnerStateRuntime {

    protected StreamInnerStateRuntime streamInnerStateRuntime;

    public CountInnerStateRuntime(StreamInnerStateRuntime streamInnerStateRuntime) {
        super(streamInnerStateRuntime.getStateType());
        this.streamInnerStateRuntime = streamInnerStateRuntime;
        singleStreamRuntimeList = streamInnerStateRuntime.singleStreamRuntimeList;
        firstProcessor = streamInnerStateRuntime.firstProcessor;
        lastProcessor = streamInnerStateRuntime.lastProcessor;
    }

    @Override
    public InnerStateRuntime clone(String key) {
        StreamInnerStateRuntime clonedStreamInnerStateRuntime = (StreamInnerStateRuntime) streamInnerStateRuntime
                .clone(key);
        CountPreStateProcessor countPreStateProcessor = (CountPreStateProcessor) clonedStreamInnerStateRuntime
                .getFirstProcessor();
        CountPostStateProcessor countPostStateProcessor = (CountPostStateProcessor) clonedStreamInnerStateRuntime
                .getLastProcessor();
        countPreStateProcessor.setCountPostStateProcessor(countPostStateProcessor);
        return new CountInnerStateRuntime(clonedStreamInnerStateRuntime);

    }
}
