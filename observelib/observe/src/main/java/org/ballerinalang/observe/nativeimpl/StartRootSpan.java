/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.ballerinalang.observe.nativeimpl;

import org.ballerinalang.jvm.observability.ObservabilityConstants;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.api.BString;

import static org.ballerinalang.observe.nativeimpl.OpenTracerBallerinaWrapper.ROOT_SPAN_INDICATOR;

/**
 * This function which implements the startSpan method for observe.
 */

public class StartRootSpan {

    public static long startRootSpan(BString spanName, Object tags) {
        return OpenTracerBallerinaWrapper.getInstance().startSpan(
                (String) Scheduler.getStrand().getProperty(ObservabilityConstants.SERVICE_NAME),
                spanName.getValue(), Utils.toStringMap((MapValue<BString, ?>) tags), ROOT_SPAN_INDICATOR,
                Scheduler.getStrand());
    }
}
