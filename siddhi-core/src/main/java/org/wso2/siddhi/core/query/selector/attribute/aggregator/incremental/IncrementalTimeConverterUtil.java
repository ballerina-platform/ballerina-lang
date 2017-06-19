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

package org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental;

import org.wso2.siddhi.query.api.aggregation.TimePeriod;

public class IncrementalTimeConverterUtil {

    public static long getNextEmitTime(long currentTime, TimePeriod.Duration duration) {
        switch (duration) {
        case SECONDS:
            return currentTime - currentTime % 1000 + 1000;
        case MINUTES:
            return currentTime - currentTime % 60000 + 60000;
        // TODO: 5/26/17 add rest
        default:
            return -1; // TODO: 5/26/17 This must be corrected
        }
    }

    public static long getStartTimeOfAggregates(long currentTime, TimePeriod.Duration duration) {
        switch (duration) {
        case SECONDS:
            return currentTime - currentTime % 1000;
        case MINUTES:
            return currentTime - currentTime % 60000;
        // TODO: 5/26/17 add rest
        default:
            return -1; // TODO: 5/26/17 This must be corrected
        }
    }

    public static long getEmitTimeOfEventToRemove(long eventTime, TimePeriod.Duration duration,
                                                  int bufferCount) {
        switch (duration) {
        case SECONDS:
            return eventTime - bufferCount * 1000;
        case MINUTES:
            return eventTime - bufferCount * 60000;
        // TODO: 5/26/17 add rest
        default:
            return -1; // TODO: 5/26/17 This must be corrected
        }
    }
}
