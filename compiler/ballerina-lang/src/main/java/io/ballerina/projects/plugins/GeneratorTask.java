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
 * Represent a code generator task that accepts an analysis context and return no result.
 *
 * @param <T> the type of the code generator context
 * @since 2.0.0
 */
@FunctionalInterface
public interface GeneratorTask<T> {

    /**
     * Performs a code generation with the passed context.
     *
     * @param t code generator context
     */
    void generate(T t);
}
