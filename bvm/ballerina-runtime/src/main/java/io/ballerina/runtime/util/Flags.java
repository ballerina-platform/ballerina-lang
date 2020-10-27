/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.util;

/**
 * This class contains the list of symbol flags.
 *
 * @since 0.95.7
 */
public class Flags {

    public static final int PUBLIC = 1;
    public static final int NATIVE = 2;
    public static final int FINAL = 4;
    public static final int ATTACHED = 8;
    public static final int READONLY = 32;
    public static final int REQUIRED = 256;
    public static final int PRIVATE = 1024;
    public static final int OPTIONAL = 4096;
    public static final int REMOTE = 32768;
    public static final int RESOURCE = 131072;
    public static final int SERVICE = 262144;
    public static final int CLASS = 268435456;
    public static final int ISOLATED = 536870912;

    public static boolean isFlagOn(int bitmask, int flag) {
        return (bitmask & flag) == flag;
    }
}
