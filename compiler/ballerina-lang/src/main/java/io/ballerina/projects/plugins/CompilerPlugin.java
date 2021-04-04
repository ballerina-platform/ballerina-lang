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
 * The abstract representation of a Ballerina compiler plugin.
 * <p>
 * Ballerina compiler plugin provides a mechanism to analyze code and report diagnostics.
 *
 * @since 2.0.0
 */
public abstract class CompilerPlugin {

    /**
     * Initializes the compiler plugin with the plugin context.
     * <p>
     * This method is invoked for each run of the compilation for implementers
     * to add required compiler plugin tasks. It is recommended to make compiler plugin tasks
     * stateless as this method can be invoked by multiple threads.
     * If it is unable to make them stateless, add new instances of
     * analysis tasks in the init method.
     *
     * @param pluginContext the context to which compiler plugin tasks can be added
     */
    public abstract void init(CompilerPluginContext pluginContext);

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }
}
