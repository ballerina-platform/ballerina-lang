/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.compiler.api;

/**
 * Represents a module information in ballerina.
 *
 * @since 2.0.0
 */
public interface ModuleID {

    /**
     * Get organization name component of this module ID.
     * 
     * @return Organization name
     */
    String orgName();

    /**
     * Get the Package name of this module ID.
     *
     * @return Package name
     */
    String packageName();

    /**
     * Get module name component of this module ID.
     * 
     * @return Module name
     */
    String moduleName();

    /**
     * Get version.
     * 
     * @return Version
     */
    String version();

    /**
     * Get module prefix component of this module ID.
     * 
     * @return Module prefix
     */
    String modulePrefix();

    /**
     * Get module is created for test sources.
     *
     * @return Module has tests
     */
    default boolean isTestable() {
        return false;
    }
}
