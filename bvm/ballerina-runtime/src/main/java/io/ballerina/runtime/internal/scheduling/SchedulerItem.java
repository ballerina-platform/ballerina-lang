/*
 * Copyright (c) (2019-2022), WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.runtime.internal.scheduling;

import io.ballerina.runtime.internal.values.FutureValue;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents an executable item in Scheduler.
 *
 * @since 0.995.0
 */
class SchedulerItem {
    private Function function;
    private Object[] params;
    final FutureValue future;
    boolean parked;

    public SchedulerItem(Function function, Object[] params, FutureValue future) {
        this.future = future;
        this.function = function;
        this.params = params;
    }

    @Deprecated
    public SchedulerItem(Consumer consumer, Object[] params, FutureValue future) {
        this.future = future;
        this.function = val -> {
            consumer.accept(val);
            return null;
        };
        this.params = params;
    }

    public Object execute() {
        return this.function.apply(this.params);
    }

    public boolean isYielded() {
        return this.future.strand.isYielded();
    }

    public State getState() {
        return this.future.strand.getState();
    }

    public void setState(State state) {
        this.future.strand.setState(state);
    }

    @Override
    public String toString() {
        return future == null ? "POISON_PILL" : String.valueOf(future.strand.hashCode());
    }
}
