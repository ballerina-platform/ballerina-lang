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
public class LogicalInnerStateRuntime extends StreamInnerStateRuntime {

    private final InnerStateRuntime innerStateRuntime1;
    private final InnerStateRuntime innerStateRuntime2;

    public LogicalInnerStateRuntime(InnerStateRuntime innerStateRuntime1, InnerStateRuntime innerStateRuntime2) {
        this.innerStateRuntime1 = innerStateRuntime1;
        this.innerStateRuntime2 = innerStateRuntime2;
    }

    @Override
    public void setQuerySelector(Processor commonProcessor) {
        innerStateRuntime2.setQuerySelector(commonProcessor);
        innerStateRuntime1.setQuerySelector(commonProcessor);
    }

    @Override
    public void setStartState() {
        innerStateRuntime2.setStartState();
        innerStateRuntime1.setStartState();
    }

    @Override
    public void init() {
        innerStateRuntime2.init();
        innerStateRuntime1.init();
    }
}
