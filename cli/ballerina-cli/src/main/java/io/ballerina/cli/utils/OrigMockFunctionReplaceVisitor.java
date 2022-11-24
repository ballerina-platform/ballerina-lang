/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.cli.utils;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Method;

import static org.ballerinalang.test.runtime.util.TesterinaConstants.ORIGINAL_FUNC_NAME_PREFIX;

/**
 * Remove existing method body and replace it with a method call.
 *
 * @since 2201.1.0
 */
public class OrigMockFunctionReplaceVisitor extends ClassVisitor {

    private final String methodName;
    private final String methodDesc;
    private final Method mockFunc;

    public OrigMockFunctionReplaceVisitor
            (int api, ClassWriter cw, String name, String methodDescriptor, Method mockFunc) {
        super(api, cw);
        this.methodName = name;
        this.methodDesc = methodDescriptor;
        this.mockFunc = mockFunc;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor;
        if (!name.equals(methodName) || !desc.equals(methodDesc)) {
            // reproduce the methods where no changes needed
            methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        } else {
            // Rename function as $ORIG_<name> to restore the original function with a different name.
            methodVisitor = super.visitMethod(access, ORIGINAL_FUNC_NAME_PREFIX + name,
                    desc, signature, exceptions);

        }

        return methodVisitor;
    }

}
