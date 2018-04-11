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
 */

package org.ballerinalang.util.observability;

import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.bre.bvm.ObservableContext;
import org.ballerinalang.model.values.BStruct;

/**
 * {@link CallbackObserver} represents the callback functionality
 * for traceable functions.
 *
 * @since 0.965.0
 */
public class CallbackObserver implements CallableUnitCallback {

    private ObservableContext observableContext;

    public CallbackObserver(ObservableContext observableContext) {
        this.observableContext = observableContext;
    }

    @Override
    public void notifySuccess() {
        ObservabilityUtils.stopObservation(observableContext);
    }

    @Override
    public void notifyFailure(BStruct error) {
        ObserverContext observerContext = ObservabilityUtils.getCurrentContext(observableContext);
        observerContext.addProperty(ObservabilityConstants.PROPERTY_BSTRUCT_ERROR, error);
        ObservabilityUtils.stopObservation(observableContext);
    }
}
