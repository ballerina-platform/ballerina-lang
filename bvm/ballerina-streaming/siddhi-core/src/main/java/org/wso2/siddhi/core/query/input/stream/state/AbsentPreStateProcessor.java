/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.siddhi.core.query.input.stream.state;

import org.ballerinalang.siddhi.core.query.processor.SchedulingProcessor;
import org.ballerinalang.siddhi.core.util.extension.holder.EternalReferencedHolder;

/**
 * PreStateProcessor of events not received by Siddhi.
 */
public interface AbsentPreStateProcessor extends SchedulingProcessor, EternalReferencedHolder {

    /**
     * Update the timestamp of the event arrived to this processor and met the filter conditions.
     *
     * @param timestamp the timestamp if the event
     */
    void updateLastArrivalTime(long timestamp);
}
