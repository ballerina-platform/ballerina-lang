/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.model.tree.clauses;

import org.ballerinalang.model.tree.Node;

/**
 * @since 0.965.0
 *
 * The interface which represents patterns in streams/tables in SQLish syntax.
 * <pre>Grammar:
 *      FROM (streamingInput (joinStreamingInput)? | patternClause) selectClause? orderByClause? outputRate?
 *      streamingAction
 *
 * E.g.
 *     from every(RegulatorStream as e1 ) followed by TempStream where e1.roomNo==roomNo [1..) as e2 followed by
 *     RegulatorStream where e1.roomNo==roomNo as e3 select e1.roomNo, e2[0].temp - e2[e2.legth -1].temp as tempDiff
 *     insert into TempDiffStream;
 * </pre>
 */

public interface PatternClause extends Node {

    boolean isForAllEvents();

    void setForAllEvents(boolean isForAllEvents);

    PatternStreamingInputNode getPatternStreamingNode();

    void setPatternStreamingInputNode(PatternStreamingInputNode pattern);

    WithinClause getWithinClause();

    void setWithinClause(WithinClause within);
}
