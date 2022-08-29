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
package io.ballerina.runtime.api.flags;

/**
 * This class contains the list of symbol flags.
 *
 * @since 0.95.7
 */
public class SymbolFlags {

    public static final long PUBLIC = 1;
    public static final long NATIVE = 2;
    public static final long FINAL = 4;
    public static final long ATTACHED = 8;
    public static final long READONLY = 32;
    public static final long REQUIRED = 256;
    public static final long PRIVATE = 1024;
    public static final long OPTIONAL = 4096;
    public static final long REMOTE = 32768;
    public static final long CLIENT = 65636;
    public static final long RESOURCE = 131072;
    public static final long SERVICE = 262144;
    public static final long TRANSACTIONAL = 33554432;
    public static final long CLASS = 268435456;
    public static final long ISOLATED = 536870912;
    public static final long ENUM = 8589934592L;
    public static final long ANY_FUNCTION = 549755813888L;

    public static boolean isFlagOn(long bitmask, long flag) {
        return (bitmask & flag) == flag;
    }
}
