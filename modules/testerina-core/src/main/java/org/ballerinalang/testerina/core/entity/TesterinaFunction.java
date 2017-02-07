/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.testerina.core.entity;

import org.ballerinalang.testerina.core.langutils.Functions;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.Function;
import org.wso2.ballerina.core.model.values.BValue;

/**
 * TesterinaFunction entity class
 */
public class TesterinaFunction {

    private String name;
    private Type type;
    private Function bFunction;
    private TesterinaFile tFile;

    public static final String PREFIX_TEST = "test";
    public static final String PREFIX_BEFORETEST = "beforeTest";
    public static final String PREFIX_AFTERTEST = "afterTest";

    /**
     * Prefixes for the test function names
     */
    public enum Type {
        TEST(PREFIX_TEST), BEFORETEST(PREFIX_BEFORETEST), AFTERTEST(PREFIX_AFTERTEST);
        private String prefix;

        private Type(String prefix) {
            this.prefix = prefix;
        }
    }

    TesterinaFunction(String name, Type type, Function bFunction, TesterinaFile tFile) {
        this.name = name;
        this.type = type;
        this.bFunction = bFunction;
        this.tFile = tFile;
    }

    public BValue[] invoke() throws BallerinaException {
        return Functions.invoke(this.tFile.getBFile(), this.name);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Function getbFunction() {
        return this.bFunction;
    }

    public void setbFunction(Function bFunction) {
        this.bFunction = bFunction;
    }

}
