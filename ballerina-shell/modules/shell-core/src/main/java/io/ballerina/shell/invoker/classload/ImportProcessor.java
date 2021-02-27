/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.invoker.classload;

import io.ballerina.shell.exceptions.InvokerException;

/**
 * Signature of a function that processes an import and returns its prefix.
 *
 * @since 2.0.0
 */
public interface ImportProcessor {
    /**
     * Tries to import using the default prefix, if not
     * imports with a random name and returns it.
     *
     * @param moduleName    Module to import.
     * @param defaultPrefix Default prefix to use.
     * @return The prefix for the import.
     * @throws InvokerException If compilation failed.
     */
    String processImplicitImport(String moduleName, String defaultPrefix) throws InvokerException;
}
