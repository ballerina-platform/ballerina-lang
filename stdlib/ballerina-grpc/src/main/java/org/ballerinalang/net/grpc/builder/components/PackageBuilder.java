/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.net.grpc.builder.components;

import static org.ballerinalang.net.grpc.builder.BalGenConstants.GRPC_NATIVE_PACKAGE;
import static org.ballerinalang.net.grpc.builder.BalGenConstants.NEW_LINE_CHARACTER;

/**
 * Building the package name of ballerina file.
 */
public class PackageBuilder { //package is mandatory
    private String packageName;
    
    public PackageBuilder(String packageName) {
        this.packageName = packageName;
    }
    
    public String build() {
        String str = "package %s;" + NEW_LINE_CHARACTER +
                "import " + GRPC_NATIVE_PACKAGE + ";" + NEW_LINE_CHARACTER +
                NEW_LINE_CHARACTER;
        return String.format(str, packageName);
    }
}
