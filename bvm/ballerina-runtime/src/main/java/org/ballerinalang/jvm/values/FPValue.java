/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.jvm.values;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.util.BLangConstants;
import org.ballerinalang.jvm.values.api.BFunctionPointer;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * <p>
 * Ballerina runtime value representation of a function pointer.
 * </p>
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p>
 * 
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 *
 * @since 0.995.0
 */
public class FPValue<T, R> implements BFunctionPointer<T, R>, RefValue {

    final BType type;
    Function<T, R> function;

    @Deprecated
    public FPValue(Function<T, R> function, BType type) {
        this.function = function;
        this.type = type;
    }

    public R call(T t) {
        return this.function.apply(t);
    }

    @Deprecated
    public FPValue(Consumer<T> consumer, BType type) {
        this.function = val -> {
            consumer.accept(val);
            return null;
        };
        this.type = type;
    }

    @Deprecated
    public R apply(T t) {
        return this.function.apply(t);
    }

    @Deprecated
    public void accept(T t) {
        this.function.apply(t);
    }

    public Function<T, R> getFunction() {
        return this.function;
    }

    @Deprecated
    public Consumer<T> getConsumer() {
        return val -> this.function.apply(val);
    }

    @Override
    public String stringValue(Strand strand) {
        return "function " + type;
    }

    @Override
    public BType getType() {
        return type;
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        return this;
    }

    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        return this;
    }

    @Override
    public boolean isFrozen() {
        return true;
    }

    @Override
    public Object freeze() {
        return this;
    }

    @Override
    public String toString() {
        return BLangConstants.EMPTY;
    }
}
