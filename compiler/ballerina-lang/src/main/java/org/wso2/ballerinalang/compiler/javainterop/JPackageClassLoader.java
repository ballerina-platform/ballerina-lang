/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.javainterop;

import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * A class loader which loads java packages and classes used in Ballerina code.
 *
 * @since 0.991.0
 */
public class JPackageClassLoader extends ClassLoader {

    private static final CompilerContext.Key<JPackageClassLoader> JAVA_PACKAGE_CLASS_LOADER_KEY =
            new CompilerContext.Key<>();

    public static JPackageClassLoader getInstance(CompilerContext context) {
        JPackageClassLoader classLoader = context.get(JAVA_PACKAGE_CLASS_LOADER_KEY);
        if (classLoader == null) {
            classLoader = AccessController.doPrivileged(
                    (PrivilegedAction<JPackageClassLoader>) () -> new JPackageClassLoader(context));
        }

        return classLoader;
    }

    private JPackageClassLoader(CompilerContext context) {
        context.put(JAVA_PACKAGE_CLASS_LOADER_KEY, this);
    }

    boolean isPackageExists(String name) {
        return getPackage(name) != null;
    }
}
