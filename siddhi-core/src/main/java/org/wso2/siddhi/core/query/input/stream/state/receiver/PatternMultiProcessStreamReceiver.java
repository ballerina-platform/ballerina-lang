/*
 * Copyright (c) 2005 - 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.wso2.siddhi.core.query.input.stream.state.receiver;

import org.wso2.siddhi.core.query.input.MultiProcessStreamReceiver;
import org.wso2.siddhi.core.query.input.stream.state.PreStateProcessor;

public class PatternMultiProcessStreamReceiver extends MultiProcessStreamReceiver {


    public PatternMultiProcessStreamReceiver(String streamId, int processCount) {
        super(streamId, processCount);
    }

    public PatternMultiProcessStreamReceiver clone(String key) {
        return new PatternMultiProcessStreamReceiver(streamId + key, processCount);
    }

    protected void stabilizeStates() {
        if (stateProcessorsSize != 0) {
            for (PreStateProcessor preStateProcessor : stateProcessors) {
                preStateProcessor.updateState();
            }
        }
    }
}
