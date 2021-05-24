/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.ballerinalang.observe.nativeimpl;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.observability.ObserveUtils;
import io.ballerina.runtime.observability.ObserverContext;
import io.ballerina.runtime.observability.tracer.BSpan;

/**
 * This implements the getSpanContext function for observe.
 * The map returned by the method contains traceId and spanId if the request was sampled.
 *
 * @since 2.0.0
 */

public class GetSpanContext {

    private static final BMap<BString, Object> EMPTY_BSPAN_CONTEXT = ValueCreator.createMapValue(
            TypeCreator.createMapType(PredefinedTypes.TYPE_STRING, true));

    public static BMap<BString, Object> getSpanContext(Environment env) {

        ObserverContext observerContext = ObserveUtils.getObserverContextOfCurrentFrame(env);
        if (observerContext != null) {
            BSpan bSpan = observerContext.getSpan();
            if (bSpan != null) {
                return bSpan.getBSpanContext();
            }
        }
        return EMPTY_BSPAN_CONTEXT;
    }

}
