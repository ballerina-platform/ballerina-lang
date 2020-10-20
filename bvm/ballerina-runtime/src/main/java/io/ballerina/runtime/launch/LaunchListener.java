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
 */
package io.ballerina.runtime.launch;

/**
 * This represents the Java SPI interface for listening Launcher events.
 *
 * @since 1.0
 */
public interface LaunchListener {

    /**
     * This is called before running the services or main.
     *
     * @param service A flag to indicate whether the program is a service.
     */
    void beforeRunProgram(boolean service);

    /**
     * This is called after running the services or main.
     *
     * @param service A flag to indicate whether the program is a service.
     */
    void afterRunProgram(boolean service);

}
