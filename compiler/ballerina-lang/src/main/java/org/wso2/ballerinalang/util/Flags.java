/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.util;

import org.ballerinalang.model.elements.Flag;

import java.util.HashSet;
import java.util.Set;

/**
 * @since 0.94
 */
public class Flags {

    public static final int PUBLIC = 1;
    public static final int NATIVE = PUBLIC << 1;
    public static final int FINAL = NATIVE << 1;
    public static final int ATTACHED = FINAL << 1;
    public static final int DEPRECATED = ATTACHED << 1;
    public static final int READONLY = DEPRECATED << 1;
    public static final int FUNCTION_FINAL = READONLY << 1;
    public static final int INTERFACE = FUNCTION_FINAL << 1;
    public static final int REQUIRED = INTERFACE << 1;
    public static final int RECORD = REQUIRED << 1;
    public static final int PRIVATE = RECORD << 1;
    public static final int COMPENSATE = PRIVATE << 1;
    public static final int ABSTRACT = COMPENSATE << 1;
    public static final int OPTIONAL = ABSTRACT << 1;
    public static final int TESTABLE = OPTIONAL << 1;
    public static final int CONSTANT = TESTABLE << 1;

    public static int asMask(Set<Flag> flagSet) {
        int mask = 0;
        for (Flag flag : flagSet) {
            switch (flag) {
                case PUBLIC:
                    mask |= PUBLIC;
                    break;
                case PRIVATE:
                    mask |= PRIVATE;
                    break;
                case NATIVE:
                    mask |= NATIVE;
                    break;
                case FINAL:
                    mask |= FINAL;
                    break;
                case ATTACHED:
                    mask |= ATTACHED;
                    break;
                case DEPRECATED:
                    mask |= DEPRECATED;
                    break;
                case READONLY:
                    mask |= READONLY;
                    break;
                case FUNCTION_FINAL:
                    mask |= FUNCTION_FINAL;
                    break;
                case INTERFACE:
                    mask |= INTERFACE;
                    break;
                case REQUIRED:
                    mask |= REQUIRED;
                    break;
                case RECORD:
                    mask |= RECORD;
                    break;
                case COMPENSATE:
                    mask |= COMPENSATE;
                    break;
                case ABSTRACT:
                    mask |= ABSTRACT;
                    break;
                case OPTIONAL:
                    mask |= OPTIONAL;
                    break;
                case TESTABLE:
                    mask |= TESTABLE;
                    break;
                case CONSTANT:
                    mask |= CONSTANT;
                    break;
            }
        }
        return mask;
    }

    public static Set<Flag> unMask(int mask) {
        Set<Flag> flagSet = new HashSet<>();
        int flagVal;
        for (Flag flag : Flag.values()) {
            switch (flag) {
                case PUBLIC:
                    flagVal = PUBLIC;
                    break;
                case PRIVATE:
                    flagVal = PRIVATE;
                    break;
                case NATIVE:
                    flagVal = NATIVE;
                    break;
                case FINAL:
                    flagVal = FINAL;
                    break;
                case ATTACHED:
                    flagVal = ATTACHED;
                    break;
                case DEPRECATED:
                    flagVal = DEPRECATED;
                    break;
                case READONLY:
                    flagVal = READONLY;
                    break;
                case FUNCTION_FINAL:
                    flagVal = FUNCTION_FINAL;
                    break;
                case INTERFACE:
                    flagVal = INTERFACE;
                    break;
                case REQUIRED:
                    flagVal = REQUIRED;
                    break;
                case RECORD:
                    flagVal = RECORD;
                    break;
                case COMPENSATE:
                    flagVal = COMPENSATE;
                    break;
                case ABSTRACT:
                    flagVal = ABSTRACT;
                    break;
                case OPTIONAL:
                    flagVal = OPTIONAL;
                    break;
                case CONSTANT:
                    flagVal = CONSTANT;
                    break;
                default:
                    continue;
            }
            addIfFlagOn(flagSet, mask, flagVal, flag);
        }
        return flagSet;
    }

    private static void addIfFlagOn(Set<Flag> flagSet, int mask, int flagVal, Flag flag) {
        if ((mask & flagVal) == flagVal) {
            flagSet.add(flag);
        }
    }
}
