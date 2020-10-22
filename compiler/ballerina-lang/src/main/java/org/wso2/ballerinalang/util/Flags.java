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
    public static final int REQUIRED = INTERFACE << 1; // Marks as a field for which the user MUST provide a value
    public static final int RECORD = REQUIRED << 1;
    public static final int PRIVATE = RECORD << 1;
    public static final int ANONYMOUS = PRIVATE << 1;
    public static final int OPTIONAL = ANONYMOUS << 1;
    public static final int TESTABLE = OPTIONAL << 1;
    public static final int CONSTANT = TESTABLE << 1;
    public static final int REMOTE = CONSTANT << 1;
    public static final int CLIENT = REMOTE << 1;
    public static final int RESOURCE = CLIENT << 1;
    public static final int SERVICE = RESOURCE << 1;
    public static final int LISTENER = SERVICE << 1;
    public static final int LAMBDA = LISTENER << 1;
    public static final int TYPE_PARAM = LAMBDA << 1;
    public static final int LANG_LIB = TYPE_PARAM << 1;
    public static final int WORKER = LANG_LIB << 1;
    public static final int FORKED = WORKER << 1;
    public static final int TRANSACTIONAL = FORKED << 1;
    public static final int PARAMETERIZED = TRANSACTIONAL << 1;
    public static final int DISTINCT = PARAMETERIZED << 1;
    public static final int CLASS = DISTINCT << 1;
    public static final int ISOLATED = CLASS << 1;
    public static final int ISOLATED_PARAM = ISOLATED << 1;

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
                case REMOTE:
                    mask |= REMOTE;
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
                case ANONYMOUS:
                    mask |= ANONYMOUS;
                    break;
                case OPTIONAL:
                    mask |= OPTIONAL;
                    break;
                case TESTABLE:
                    mask |= TESTABLE;
                    break;
                case CLIENT:
                    mask |= CLIENT;
                    break;
                case RESOURCE:
                    mask |= RESOURCE;
                    break;
                case SERVICE:
                    mask |= SERVICE;
                    break;
                case LISTENER:
                    mask |= LISTENER;
                    break;
                case CONSTANT:
                    mask |= CONSTANT;
                    break;
                case LAMBDA:
                    mask |= LAMBDA;
                    break;
                case TYPE_PARAM:
                    mask |= TYPE_PARAM;
                    break;
                case LANG_LIB:
                    mask |= LANG_LIB;
                    break;
                case WORKER:
                    mask |= WORKER;
                    break;
                case FORKED:
                    mask |= FORKED;
                    break;
                case TRANSACTIONAL:
                    mask |= TRANSACTIONAL;
                    break;
                case DISTINCT:
                    mask |= DISTINCT;
                    break;
                case CLASS:
                    mask |= CLASS;
                    break;
                case ISOLATED:
                    mask |= ISOLATED;
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
                case REMOTE:
                    flagVal = REMOTE;
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
                case ANONYMOUS:
                    flagVal = ANONYMOUS;
                    break;
                case OPTIONAL:
                    flagVal = OPTIONAL;
                    break;
                case CLIENT:
                    flagVal = CLIENT;
                    break;
                case RESOURCE:
                    flagVal = RESOURCE;
                    break;
                case SERVICE:
                    flagVal = SERVICE;
                    break;
                case LISTENER:
                    flagVal = LISTENER;
                    break;
                case CONSTANT:
                    flagVal = CONSTANT;
                    break;
                case LAMBDA:
                    flagVal = LAMBDA;
                    break;
                case TYPE_PARAM:
                    flagVal = TYPE_PARAM;
                    break;
                case LANG_LIB:
                    flagVal = LANG_LIB;
                    break;
                case FORKED:
                    flagVal = FORKED;
                    break;
                case TRANSACTIONAL:
                    flagVal = TRANSACTIONAL;
                    break;
                case DISTINCT:
                    flagVal = DISTINCT;
                    break;
                case CLASS:
                    flagVal = CLASS;
                    break;
                case ISOLATED:
                    flagVal = ISOLATED;
                    break;
                default:
                    continue;
            }
            addIfFlagOn(flagSet, mask, flagVal, flag);
        }
        return flagSet;
    }

    public static int unset(int mask, int flag) {
        return mask & (~flag);
    }

    private static void addIfFlagOn(Set<Flag> flagSet, int mask, int flagVal, Flag flag) {
        if ((mask & flagVal) == flagVal) {
            flagSet.add(flag);
        }
    }
}
