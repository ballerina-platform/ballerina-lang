/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.compiler.internal.parser.tree;

/**
 * Represents a set of flags that can be attached to an internal syntax node.
 *
 * @since 2.0.0
 */
public class STNodeFlags {
    public static final byte HAS_DIAGNOSTIC = 1 << 0x1;
    public static final byte IS_MISSING = 1 << 0x2;

    /**
     * Checks whether the given flag is set in the given flags.
     * @param flags the flags to check
     * @param flag the flag to check for
     * @return <code>true</code> if the flag is set. <code>false</code> otherwise
     */
    public static boolean isFlagSet(byte flags, byte flag) {
        return (flags & flag) != 0;
    }

    /**
     * Sets a flag in the given flags.
     *
     * @param flags the original flags
     * @param flag  the flag to set
     * @return the updated flags with the specified flag
     */
    public static byte withFlag(byte flags, byte flag) {
        return (byte) (flags | flag);
    }
}
