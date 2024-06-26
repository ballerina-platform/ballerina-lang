/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package io.ballerina.projects.buildtools;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a CodeGeneratorTool class with ToolConfig to provide the command name and the list of subcommands.
 *
 * @since 2201.9.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ToolConfig {

    /**
     * Set the command name.
     *
     * @return the name of the command
     */
    String name();

    /**
     * Set the subcommands belonging to the command.
     *
     * @return Array of subcommands
     */
    Class<? extends CodeGeneratorTool>[] subcommands() default {};

    /**
     * Set whether the annotated command should be hidden from users.
     *
     * @return true if the command should be hidden, false otherwise
     */
    boolean hidden() default false;
}
