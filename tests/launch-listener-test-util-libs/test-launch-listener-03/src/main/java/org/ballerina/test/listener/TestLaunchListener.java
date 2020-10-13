/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerina.test.listener;

import io.ballerina.runtime.launch.LaunchListener;
/**
 * Test Launcher Listener used for testing purpose.
 *
 * @since 1.3.0
 */
public class TestLaunchListener implements LaunchListener {


    @Override
    public void beforeRunProgram(boolean service) {
        // Do nothing.
    }

    @Override
    public void afterRunProgram(boolean service) {
        throw new RuntimeException("A runtime failure in afterRunProgram method");
    }
}
