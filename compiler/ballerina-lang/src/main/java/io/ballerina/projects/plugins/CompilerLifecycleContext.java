/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects.plugins;

/**
 * Represent the context required to initialize a {@code CompilerLifecycleListener}.
 * <p>
 * This class can be used to add various lifecycle tasks during the {@code CompilerLifecycleListener} initialization.
 *
 * @since 2.0.0
 */
public interface CompilerLifecycleContext {

    /**
     * Add a compiler lifecycle task to be triggered once the code-generation is completed.
     *
     * @param lifecycleTask the lifecycle task to be executed
     */
    void addCodeGenerationCompletedTask(CompilerLifecycleTask<CompilerLifecycleEventContext> lifecycleTask);
}
