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
package org.wso2.siddhi.query.api.query.input.pattern;

import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.expression.constant.Constant;
import org.wso2.siddhi.query.api.query.QueryEventSource;
import org.wso2.siddhi.query.api.query.input.BasicStream;
import org.wso2.siddhi.query.api.query.input.Stream;
import org.wso2.siddhi.query.api.query.input.pattern.element.CountElement;
import org.wso2.siddhi.query.api.query.input.pattern.element.FollowedByElement;
import org.wso2.siddhi.query.api.query.input.pattern.element.LogicalElement;
import org.wso2.siddhi.query.api.query.input.pattern.element.PatternElement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class PatternStream implements Stream, PatternElement {

    private PatternElement patternElement;
    private List<String> streamIdList;
    private Constant within;

    public PatternStream(PatternElement patternElement, Constant within) {
        this.patternElement = patternElement;
        this.streamIdList = new ArrayList<String>(collectStreamIds(patternElement, new HashSet<String>()));
        this.within = within;
    }

    @Override
    public List<String> getStreamIds() {
        return streamIdList;
    }

    @Override
    public List<QueryEventSource> constructQueryEventSourceList(
            ConcurrentMap<String, AbstractDefinition> streamTableDefinitionMap,
            List<QueryEventSource> queryEventSources) {
        return constructEventStreamList(patternElement, streamTableDefinitionMap, queryEventSources);
    }

    public Constant getWithin() {
        return within;
    }

    public PatternElement getPatternElement() {
        return patternElement;
    }

    private HashSet<String> collectStreamIds(PatternElement patternElement,
                                             HashSet<String> streamIds) {
        if (patternElement instanceof PatternStream) {
            streamIds.addAll(((PatternStream) patternElement).getStreamIds());
        } else if (patternElement instanceof BasicStream) {
            streamIds.addAll(((BasicStream) patternElement).getStreamIds());
        } else if (patternElement instanceof LogicalElement) {
            collectStreamIds(((LogicalElement) patternElement).getTransformedStream1(), streamIds);
            collectStreamIds(((LogicalElement) patternElement).getTransformedStream2(), streamIds);
        } else if (patternElement instanceof CountElement) {
            collectStreamIds(((CountElement) patternElement).getTransformedStream(), streamIds);
        } else if (patternElement instanceof FollowedByElement) {
            collectStreamIds(((FollowedByElement) patternElement).getPatternElement(), streamIds);
            collectStreamIds(((FollowedByElement) patternElement).getFollowedByPatternElement(), streamIds);
        }
        return streamIds;
    }

    public List<QueryEventSource> constructEventStreamList(PatternElement patternElement,
                                                           ConcurrentMap<String, AbstractDefinition> streamTableDefinitionMap,
                                                           List<QueryEventSource> queryEventSources) {


        if (patternElement instanceof BasicStream) {
            ((BasicStream) patternElement).constructQueryEventSourceList(streamTableDefinitionMap, queryEventSources);
        } else if (patternElement instanceof LogicalElement) {
            constructEventStreamList(((LogicalElement) patternElement).getTransformedStream1(), streamTableDefinitionMap, queryEventSources);
            constructEventStreamList(((LogicalElement) patternElement).getTransformedStream2(), streamTableDefinitionMap, queryEventSources);
        } else if (patternElement instanceof CountElement) {
            constructEventStreamList(((CountElement) patternElement).getTransformedStream(), streamTableDefinitionMap, queryEventSources);
        } else if (patternElement instanceof FollowedByElement) {
            constructEventStreamList(((FollowedByElement) patternElement).getPatternElement(), streamTableDefinitionMap, queryEventSources);
            constructEventStreamList(((FollowedByElement) patternElement).getFollowedByPatternElement(), streamTableDefinitionMap, queryEventSources);
        } else if (patternElement instanceof PatternStream) {
            ((PatternStream) patternElement).constructQueryEventSourceList(streamTableDefinitionMap, queryEventSources);
        }

        return queryEventSources;
    }
}
