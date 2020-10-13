/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.runtime.services;

import io.ballerina.runtime.services.spi.EmbeddedExecutor;

import java.util.NoSuchElementException;
import java.util.ServiceLoader;

/**
 * Ballerina package provider class used in package management.
 */
public class EmbeddedExecutorProvider {
    private static EmbeddedExecutorProvider provider = new EmbeddedExecutorProvider();

    private final ServiceLoader<EmbeddedExecutor> loader;

    /**
     * Constructor of EmbeddedExecutorProvider.
     */
    private EmbeddedExecutorProvider() {
        loader = ServiceLoader.load(EmbeddedExecutor.class);
    }

    /**
     * Creates an instance of the EmbeddedExecutorProvider if its not already created.
     *
     * @return instance of EmbeddedExecutorProvider
     */
    public static EmbeddedExecutorProvider getInstance() {
        return provider;
    }

    /**
     * Get an instance of EmbeddedExecutor after iterating over the loaded ServiceLoaders.
     *
     * @return instance of EmbeddedExecutor
     */
    public EmbeddedExecutor getExecutor() {
        EmbeddedExecutor service = loader.iterator().next();
        if (service != null) {
            return service;
        } else {
            throw new NoSuchElementException("No implementation of the EmbeddedExecutor");
        }
    }
}
