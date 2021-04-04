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

package io.ballerina.shell.test.evaluator.base;

import io.ballerina.shell.exceptions.InvokerException;
import io.ballerina.shell.invoker.classload.ClassLoadInvoker;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

/**
 * Class load invoker made testable.
 *
 * @since 2.0.0
 */
public class TestInvoker extends ClassLoadInvoker {
    private final ByteArrayOutputStream stdOutBaOs;
    private final PrintStream stdOutMock;

    public TestInvoker() {
        this.stdOutBaOs = new ByteArrayOutputStream();
        this.stdOutMock = new PrintStream(stdOutBaOs, true, Charset.defaultCharset());
    }

    @Override
    protected Object invokeScheduledMethod(ClassLoader classLoader, String className, String methodName)
            throws InvokerException {
        PrintStream stdOut = System.out;
        try {
            System.setOut(stdOutMock);
            return super.invokeScheduledMethod(classLoader, className, methodName);
        } finally {
            System.setOut(stdOut);
        }
    }

    @Override
    protected PrintStream getErrorStream() {
        return stdOutMock;
    }

    public String getStdOut() {
        String output = stdOutBaOs.toString(Charset.defaultCharset());
        return output.replace("\r\n", "\n");
    }

    public void reset() {
        this.stdOutBaOs.reset();
    }
}
