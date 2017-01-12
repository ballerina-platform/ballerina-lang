/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.core.model;

/**
 * {@code {@link Function}} represents any Ballerina function.
 */
public interface Function {

    /**
     * Get Name of the function.
     *
     * @return name of the function.
     */
    String getName();

    // TODO Refactor function name related methods
    String getFunctionName();

    /**
     * Get package name of the function.
     *
     * @return package name of the function.
     */
    String getPackageName();

    /**
     * Get the function Identifier
     *
     * @return function identifier
     */
    SymbolName getSymbolName();

    void setSymbolName(SymbolName symbolName);

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
     * Get all the variableDcls declared in the scope of BallerinaFunction
     *
     * @return list of all BallerinaFunction scoped variableDcls
     */
    VariableDcl[] getVariableDcls();

    /**
     * Get list of return parameters
     *
     * @return list of Return types
     */
    Parameter[] getReturnParameters();

    /**
     * Check whether function is public, which means function is visible outside the package
     *
     * @return whether function is public
     */
    boolean isPublic();

    /**
     * get stack frame size
     */
    int getStackFrameSize();


    /**
     * set stack frame size
     */
    void setStackFrameSize(int frameSize);

    /**
     * Get the location of this function in the ballerina source file.
     * Returns the ballerina file and line number of the function.
     *
     * @return location of this function in the ballerina source file
     */
    Position getLocation();

    /**
     * Set the location of this function in the ballerina source file.
     *
     * @param location Location of this function in the ballerina source file
     */
    void setLocation(Position location);

}
