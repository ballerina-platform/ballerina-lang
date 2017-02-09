/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.stream.input.source;

import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.query.output.callback.OutputCallback;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.execution.io.map.AttributeMapping;

import java.util.List;
import java.util.Map;

/**
 * Convert custom input from {@link InputTransport} to {@link org.wso2.siddhi.core.event.ComplexEventChunk}.
 */
public interface InputMapper extends InputCallback {

    void init(StreamDefinition outputStreamDefinition, OutputCallback outputCallback, MetaStreamEvent
            metaStreamEvent, Map<String, String> options, List<AttributeMapping> attributeMappingList);
}
