/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/
package org.ballerinalang.jvm;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * Simple scheduler for JBallerina.
 */
public class Scheduler {


    public static Strand schedule(Object[] params, Function function) {
        Strand strand = new Strand();
        // find the return type based on type
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        AsyncExecutor async = new AsyncExecutor(function, params);
        strand.future = executorService.submit(async);

        return strand;
    }

}

class AsyncExecutor<T> implements Callable<T> {

    Function function;
    Object[] params;

    public AsyncExecutor(Function f, Object[] params) {
        this.function = f;
        this.params = params;
    }

    @Override
    public T call() {
        return (T) function.apply(params);

    }
}
