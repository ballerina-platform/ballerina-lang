/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.query.api.execution.query.input.stream;

import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.input.state.StateElement;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.constant.Constant;

import java.util.List;

public abstract class InputStream {

    public abstract List<String> getAllStreamIds();

    public abstract List<String> getUniqueStreamIds();

    public static InputStream joinStream(SingleInputStream leftStream, JoinInputStream.Type type,
                                         SingleInputStream rightStream,
                                         Expression onCompare,
                                         Constant within) {
        return new JoinInputStream(leftStream, type, rightStream, onCompare, within, JoinInputStream.EventTrigger.ALL);
    }

    public static InputStream joinStream(SingleInputStream leftStream, JoinInputStream.Type type,
                                         SingleInputStream rightStream,
                                         Expression onCompare, Constant within,
                                         JoinInputStream.EventTrigger trigger) {
        return new JoinInputStream(leftStream, type, rightStream, onCompare, within, trigger);
    }

    public static InputStream joinStream(SingleInputStream leftStream, JoinInputStream.Type type,
                                         SingleInputStream rightStream,
                                         Constant within,
                                         JoinInputStream.EventTrigger trigger) {
        return new JoinInputStream(leftStream, type, rightStream, null, within, trigger);
    }

    public static InputStream joinStream(SingleInputStream leftStream, JoinInputStream.Type type,
                                         SingleInputStream rightStream,
                                         Expression onCompare,
                                         JoinInputStream.EventTrigger trigger) {
        return new JoinInputStream(leftStream, type, rightStream, onCompare, null, trigger);
    }

    public static InputStream joinStream(SingleInputStream leftStream, JoinInputStream.Type type,
                                         SingleInputStream rightStream,
                                         JoinInputStream.EventTrigger trigger) {
        return new JoinInputStream(leftStream, type, rightStream, null, null, trigger);
    }


    public static InputStream joinStream(SingleInputStream leftStream, JoinInputStream.Type type,
                                         SingleInputStream rightStream, Constant within) {
        return new JoinInputStream(leftStream, type, rightStream, null, within, JoinInputStream.EventTrigger.ALL);
    }

    public static InputStream joinStream(SingleInputStream leftStream, JoinInputStream.Type type,
                                         SingleInputStream rightStream, Expression onCompare) {
        return new JoinInputStream(leftStream, type, rightStream, onCompare, null, JoinInputStream.EventTrigger.ALL);
    }

    public static InputStream joinStream(SingleInputStream leftStream, JoinInputStream.Type type,
                                         SingleInputStream rightStream) {
        return new JoinInputStream(leftStream, type, rightStream, null, null, JoinInputStream.EventTrigger.ALL);
    }

    public static StateInputStream patternStream(StateElement patternElement) {
        return new StateInputStream(StateInputStream.Type.PATTERN, patternElement);
    }

    public static StateInputStream sequenceStream(StateElement sequenceElement) {
        return new StateInputStream(StateInputStream.Type.SEQUENCE, sequenceElement);
    }

    public static BasicSingleInputStream innerStream(String streamId) {
        return new BasicSingleInputStream(null, streamId, true);
    }

    public static BasicSingleInputStream innerStream(String streamReferenceId, String streamId) {
        return new BasicSingleInputStream(streamReferenceId, streamId, true);
    }

    public static BasicSingleInputStream stream(String streamId) {
        return new BasicSingleInputStream(null, streamId);
    }

    public static BasicSingleInputStream stream(String streamReferenceId, String streamId) {
        return new BasicSingleInputStream(streamReferenceId, streamId);
    }

    public static SingleInputStream stream(Query query) {
        return new AnonymousInputStream(query);
    }
}
