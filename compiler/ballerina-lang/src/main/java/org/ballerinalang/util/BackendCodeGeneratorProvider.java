/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.util;

import org.ballerinalang.spi.CompilerBackendCodeGenerator;

import java.util.NoSuchElementException;
import java.util.ServiceLoader;

/**
 * Ballerina package provider class used in backend code generation.
 *
 * @since 0.995.0
 */
public class BackendCodeGeneratorProvider {

    private static final BackendCodeGeneratorProvider PROVIDER = new BackendCodeGeneratorProvider();

    private final ServiceLoader<CompilerBackendCodeGenerator> loader;

    /**
     * Constructor of BackendCodeGeneratorProvider.
     */
    private BackendCodeGeneratorProvider() {
        loader = ServiceLoader.load(CompilerBackendCodeGenerator.class);
    }

    /**
     * Creates an instance of the BackendCodeGeneratorProvider if its not already created.
     *
     * @return instance of BackendCodeGeneratorProvider
     */
    public static BackendCodeGeneratorProvider getInstance() {
        return PROVIDER;
    }

    /**
     * Get an instance of CompilerBackendCodeGenerator after iterating over the loaded ServiceLoaders.
     *
     * @return instance of CompilerBackendCodeGenerator
     */
    public CompilerBackendCodeGenerator getBackendCodeGenerator() {
        CompilerBackendCodeGenerator service = loader.iterator().next();
        if (service != null) {
            return service;
        } else {
            throw new NoSuchElementException("No implementation of the CompilerBackendCodeGenerator");
        }
    }
}
