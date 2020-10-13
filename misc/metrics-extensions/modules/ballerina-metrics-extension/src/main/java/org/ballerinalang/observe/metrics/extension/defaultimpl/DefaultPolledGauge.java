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
package org.ballerinalang.observe.metrics.extension.defaultimpl;

import io.ballerina.runtime.observability.metrics.AbstractMetric;
import io.ballerina.runtime.observability.metrics.MetricId;
import io.ballerina.runtime.observability.metrics.PolledGauge;

import java.lang.ref.WeakReference;
import java.util.function.ToDoubleFunction;

/**
 * An implementation of {@link PolledGauge}.
 *
 * @param <T> The type of the object used to poll the gauge's value.
 */
public class DefaultPolledGauge<T> extends AbstractMetric implements PolledGauge {

    private final WeakReference<T> ref;
    private final ToDoubleFunction<T> toDoubleFunction;

    DefaultPolledGauge(MetricId id, T obj, ToDoubleFunction<T> toDoubleFunction) {
        super(id);
        this.ref = new WeakReference<>(obj);
        this.toDoubleFunction = toDoubleFunction;
    }


    @Override
    public double getValue() {
        T obj = ref.get();
        return obj != null ? toDoubleFunction.applyAsDouble(obj) : Double.NaN;
    }
}
