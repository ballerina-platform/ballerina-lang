/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.test.util.terminator;

/**
 * Launcher Terminator factory.
 */
public class TerminatorFactory {

    /**
     * Retrieves an os-specific process terminator.
     *
     * @param os - running operating system
     * @return Terminator instance
     */
    public static Terminator getTerminator(String os) {
        if ("unix".equalsIgnoreCase(os)) {
            return new TerminatorUnix();
        } else if ("windows".equalsIgnoreCase(os)) {
            return new TerminatorWindows();
        } else if ("mac".equalsIgnoreCase(os)) {
            return new TerminatorMac();
        } else {
            return null;
        }
    }
}
