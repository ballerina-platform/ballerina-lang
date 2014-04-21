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
package org.wso2.siddhi.query.api;

import org.wso2.siddhi.query.api.condition.Condition;
import org.wso2.siddhi.query.api.definition.partition.PartitionDefinition;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.expression.constant.Constant;
import org.wso2.siddhi.query.api.query.Query;
import org.wso2.siddhi.query.api.query.input.BasicStream;
import org.wso2.siddhi.query.api.query.input.JoinStream;
import org.wso2.siddhi.query.api.query.input.SingleStream;
import org.wso2.siddhi.query.api.query.input.Stream;
import org.wso2.siddhi.query.api.query.input.pattern.PatternStream;
import org.wso2.siddhi.query.api.query.input.pattern.element.PatternElement;
import org.wso2.siddhi.query.api.query.input.sequence.SequenceStream;
import org.wso2.siddhi.query.api.query.input.sequence.element.SequenceElement;
import org.wso2.siddhi.query.api.query.selection.Selector;

public abstract class QueryFactory {

    public static Query createQuery() {
        return new Query();
    }

    public static StreamDefinition createStreamDefinition() {
        return new StreamDefinition();
    }

    public static BasicStream inputStream(String streamId) {
        return new BasicStream(streamId, streamId);
    }

    public static Selector outputSelector() {
        return new Selector();
    }

    public static Stream joinStream(SingleStream leftStream, JoinStream.Type type,
                                    SingleStream rightStream,
                                    Condition onCompare,
                                    Constant within) {
        return new JoinStream(leftStream, type, rightStream, onCompare, within, JoinStream.EventTrigger.ALL);
    }

    public static Stream joinStream(SingleStream leftStream, JoinStream.Type type,
                                    SingleStream rightStream,
                                    Condition onCompare, Constant within,
                                    JoinStream.EventTrigger trigger) {
        return new JoinStream(leftStream, type, rightStream, onCompare, within, trigger);
    }

    public static Stream joinStream(SingleStream leftStream, JoinStream.Type type,
                                    SingleStream rightStream, Constant within) {
        return new JoinStream(leftStream, type, rightStream, null, within, JoinStream.EventTrigger.ALL);
    }

    public static Stream joinStream(SingleStream leftStream, JoinStream.Type type,
                                    SingleStream rightStream, Condition onCompare) {
        return new JoinStream(leftStream, type, rightStream, onCompare, null, JoinStream.EventTrigger.ALL);
    }

    public static Stream joinStream(SingleStream leftStream, JoinStream.Type type,
                                    SingleStream rightStream) {
        return new JoinStream(leftStream, type, rightStream, null, null, JoinStream.EventTrigger.ALL);
    }

    public static BasicStream inputStream(String streamReferenceId, String streamId) {
        return new BasicStream(streamReferenceId, streamId);

    }

    public static PatternStream patternStream(PatternElement patternElement) {
        return new PatternStream(patternElement, null);
    }

    public static PatternStream patternStream(PatternElement patternElement, Constant within) {
        return new PatternStream(patternElement, within);
    }

    public static SequenceStream sequenceStream(SequenceElement sequenceElement) {
        return new SequenceStream(sequenceElement, null);
    }

    public static SequenceStream sequenceStream(SequenceElement sequenceElement, Constant within) {
        return new SequenceStream(sequenceElement,within);
    }

    public static TableDefinition createTableDefinition() {
        return new TableDefinition();
    }

	public static PartitionDefinition createPartitionDefinition() {
	    return new PartitionDefinition();
    }
}
