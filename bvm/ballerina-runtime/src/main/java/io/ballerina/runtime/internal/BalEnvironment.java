/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.repository.Repository;
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.internal.repository.RepositoryImpl;
import io.ballerina.runtime.internal.scheduling.Strand;

import java.util.function.Supplier;

/**
 * When {@link Environment} is used as the first argument of an interop method, Ballerina will inject an instance
 * of this class when calling. That instance can be used to communicate with currently executing Ballerina runtime.
 *
 * @since 2201.6.0
 */
public class BalEnvironment extends Environment {

    private final Strand strand;
    private final Module currentModule;
    private final String funcName;
    private final Parameter[] funcPathParams;
    private final Repository repository;

    public BalEnvironment(Strand strand, Module currentModule, String funcName, Parameter[] funcPathParams) {
        this.strand = strand;
        this.currentModule = currentModule;
        this.funcName = funcName;
        this.funcPathParams = funcPathParams;
        this.repository = new RepositoryImpl();
    }


    @Override
    public String getFunctionName() {
        return funcName;
    }

    @Override
    public Parameter[] getFunctionPathParameters() {
        return funcPathParams;
    }

    @Override
    public <T> T yieldAndRun(Supplier<T> supplier) {
        try {
            strand.yield();
            return supplier.get();
        } finally {
            strand.resume();
        }
    }

    @Override
    public BalRuntime getRuntime() {
        return strand.scheduler.runtime;
    }

    @Override
    public Module getCurrentModule() {
        return currentModule;
    }

    @Override
    public int getStrandId() {
        return strand.getId();
    }

    @Override
    public String getStrandName() {
        return strand.name;
    }

    @Override
    public void setStrandLocal(String key, Object value) {
        strand.setProperty(key, value);
    }

    @Override
    public Object getStrandLocal(String key) {
        return strand.getProperty(key);
    }

    @Override
    public Repository getRepository() {
        return this.repository;
    }
}
