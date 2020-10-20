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
package io.ballerina.runtime.services.spi;

import io.ballerina.runtime.api.async.StrandMetadata;

import java.util.Optional;

/**
 * This represents the Java SPI interface for the resource runner.
 *
 * @since 0.964
 */
public interface EmbeddedExecutor {

    /**
     * Executes the main function of a module.
     *
     * @param moduleName    Name of the module.
     * @param moduleVersion Version of the module.
     * @param strandName    name for newly creating strand which is used to execute the function pointer.
     * @param metaData      meta data of new strand.
     * @param args          The arguments for the function.
     * @return Program execution output.
     */
    Optional<RuntimeException> executeMainFunction(String moduleName, String moduleVersion, String strandName,
                                                   StrandMetadata metaData, String... args);

    /**
     * Executes a service of a module.
     *
     * @param moduleName    Name of the module.
     * @param moduleVersion Version of the module.
     * @param strandName    name for newly creating strand which is used to execute the function pointer.
     * @param metaData      meta data of new strand.
     * @return Program execution output.
     */
    Optional<RuntimeException> executeService(String moduleName, String moduleVersion, String strandName,
                                              StrandMetadata metaData);
}
