/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.core.model;

import org.wso2.ballerina.core.interpreter.Interpreter;
import org.wso2.ballerina.core.model.types.Type;

/**
 * {@code {@link Function}} represents any Ballerina function.
 */
public interface Function extends Interpreter {

    /**
     * Get Name of the function.
     *
     * @return name of the function.
     */
    String getName();

    /**
     * Get the function Identifier
     *
     * @return function identifier
     */
    Identifier getIdentifier();

    /**
     * Get all the Annotations associated with a BallerinaFunction
     *
     * @return list of Annotations
     */
    Annotation[] getAnnotations();

    /**
     * Get list of Arguments associated with the function definition
     *
     * @return list of Arguments
     */
    Parameter[] getParameters();

    /**
     * Get list of return Types associated with function defintion.
     * @return list of Return types.
     */
    Type[] getReturnTypes();

    /**
     * Check whether function is public, which means function is visible outside the package
     *
     * @return whether function is public
     */
    boolean isPublic();

}
