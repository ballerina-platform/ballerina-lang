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

import java.security.Permission;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A helper class for class load invoker so that
 * the system.exit calls will not exit the VM.
 * TODO: Remove this and fix VM exit bug.
 *
 * @since 2.0.0
 */
public class NoExitVmSecManager extends SecurityManager {
    private static final String EXIT_VM_REGEXP = "exitVM\\.([0-9]+)";

    private final SecurityManager baseSecurityManager;
    private int exitCode;
    private boolean isExitCodeSet;

    public NoExitVmSecManager(SecurityManager baseSecurityManager) {
        this.baseSecurityManager = baseSecurityManager;
        this.isExitCodeSet = false;
        this.exitCode = 1;
    }

    @Override
    public void checkPermission(Permission permission) {
        // We check for exitVM permission.
        // According to docs: https://docs.oracle.com/javase/9/docs/api/java/lang/RuntimePermission.html
        // format is exitVM.{exit status} So we can extract the exit code.
        // Only the first time exit counts. All others are panics caused by the security exception.
        if (permission.getName().startsWith("exitVM")) {
            if (!isExitCodeSet) {
                // Set exit code
                Pattern pattern = Pattern.compile(EXIT_VM_REGEXP);
                Matcher matcher = pattern.matcher(permission.getName());
                if (matcher.matches()) { // According to docs, this should pass.
                    String exitCodeStr = matcher.group(1);
                    exitCode = Integer.parseInt(exitCodeStr);
                    isExitCodeSet = true;
                }
            }
            throw new SecurityException();
        }
        // If already exited, no need to write error file.
        if (isExitCodeSet) {
            if (permission.getName().contains("ballerina-internal.log")) {
                throw new SecurityException();
            }
        }
        if (baseSecurityManager != null) {
            baseSecurityManager.checkPermission(permission);
        }
    }

    public int getExitCode() {
        return exitCode;
    }
}
