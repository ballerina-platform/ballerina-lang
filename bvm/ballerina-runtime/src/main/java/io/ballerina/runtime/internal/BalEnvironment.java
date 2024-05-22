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
import io.ballerina.runtime.api.Repository;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.internal.scheduling.State;
import io.ballerina.runtime.internal.scheduling.Strand;

import java.util.Optional;

/**
 * When {@link Environment} is used as the first argument of an interop method, Ballerina will inject an instance
 * of this class when calling. That instance can be used to communicate with currently executing Ballerina runtime.
 *
 * @since 2201.6.0
 */
public class BalEnvironment extends Environment {

    private final Strand strand;
    private BalFuture future;
    private Module currentModule;
    private String funcName;
    private Parameter[] funcPathParams;
    private Repository repository;

    public BalEnvironment(Strand strand) {
        this.strand = strand;
    }

    public BalEnvironment(Strand strand, Module currentModule) {
        this.strand = strand;
        this.currentModule = currentModule;
        future = new BalFuture(this.strand);
        this.repository = new RepositoryImpl();
    }

    public BalEnvironment(Strand strand, Module currentModule, String funcName, Parameter[] funcPathParams) {
        this(strand, currentModule);
        this.funcName = funcName;
        this.funcPathParams = funcPathParams;
        this.repository = new RepositoryImpl();
    }

    /**
     * Returns the Ballerina function name for the corresponding external interop method.
     *
     * @return function name
     */
    public String getFunctionName() {
        return funcName;
    }

    /**
     * Returns an array consisting of the path parameters of the resource function defined as external.
     *
     * @return array of {@link Parameter}
     */
    public Parameter[] getFunctionPathParameters() {
        return funcPathParams;
    }

    /**
     * Mark the current executing strand as async. Execution of Ballerina code after the current
     * interop will stop until given BalFuture is completed. However the java thread will not be blocked
     * and will be reused for running other Ballerina code in the meantime. Therefore callee of this method
     * must return as soon as possible to avoid starvation of ballerina code execution.
     *
     * @return BalFuture which will resume the current strand when completed.
     */
    public BalFuture markAsync() {
        strand.blockedOnExtern = true;
        strand.setState(State.BLOCK_AND_YIELD);
        return future;
    }

    /**
     * Gets an instance of Ballerina runtime.
     *
     * @return Ballerina runtime instance.
     */
    public BalRuntime getRuntime() {
        return new BalRuntime(strand.scheduler, currentModule);
    }

    /**
     * Gets current module {@link Module}.
     *
     * @return module of the environment.
     */
    public Module getCurrentModule() {
        return currentModule;
    }

    /**
     * Gets the strand id. This will be generated on strand initialization.
     *
     * @return Strand id.
     */
    public int getStrandId() {
        return strand.getId();
    }

    /**
     * Gets the strand name. This will be optional. Strand name can be either name given in strand annotation or async
     * call or function pointer variable name.
     *
     * @return Optional strand name.
     */
    public Optional<String> getStrandName() {
        return strand.getName();
    }

    /**
     * Gets {@link StrandMetadata}.
     *
     * @return metadata of the strand.
     */
    public StrandMetadata getStrandMetadata() {
        return strand.getMetadata();
    }

    /**
     * Sets given local key value pair in strand.
     *
     * @param key   string key
     * @param value value to be store in the strand
     */
    public void setStrandLocal(String key, Object value) {
        strand.setProperty(key, value);
    }

    /**
     * Gets the value stored in the strand on given key.
     *
     * @param key key
     * @return value stored in the strand.
     */
    public Object getStrandLocal(String key) {
        return strand.getProperty(key);
    }

    public Repository getRepository() {
        return this.repository;
    }
}
