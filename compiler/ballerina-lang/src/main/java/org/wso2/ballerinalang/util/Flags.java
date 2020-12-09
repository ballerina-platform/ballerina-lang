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

    public static final long PUBLIC = 1;                                        //  0
    public static final long NATIVE = PUBLIC << 1;                              //  1
    public static final long FINAL = NATIVE << 1;                               //  2
    public static final long ATTACHED = FINAL << 1;                             //  3

    public static final long DEPRECATED = ATTACHED << 1;                        //  4
    public static final long READONLY = DEPRECATED << 1;                        //  5
    public static final long FUNCTION_FINAL = READONLY << 1;                    //  6
    public static final long INTERFACE = FUNCTION_FINAL << 1;                   //  7

    // Marks as a field for which the user MUST provide a value
    public static final long REQUIRED = INTERFACE << 1;                         //  8

    public static final long RECORD = REQUIRED << 1;                            //  9
    public static final long PRIVATE = RECORD << 1;                             //  10
    public static final long ANONYMOUS = PRIVATE << 1;                          //  11

    public static final long OPTIONAL = ANONYMOUS << 1;                         //  12
    public static final long TESTABLE = OPTIONAL << 1;                          //  13
    public static final long CONSTANT = TESTABLE << 1;                          //  14
    public static final long REMOTE = CONSTANT << 1;                            //  15

    public static final long CLIENT = REMOTE << 1;                              //  16
    public static final long RESOURCE = CLIENT << 1;                            //  17
    public static final long SERVICE = RESOURCE << 1;                           //  18
    public static final long LISTENER = SERVICE << 1;                           //  19

    public static final long LAMBDA = LISTENER << 1;                            //  20
    public static final long TYPE_PARAM = LAMBDA << 1;                          //  21
    public static final long LANG_LIB = TYPE_PARAM << 1;                        //  22
    public static final long WORKER = LANG_LIB << 1;                            //  23

    public static final long FORKED = WORKER << 1;                              //  24
    public static final long TRANSACTIONAL = FORKED << 1;                       //  25
    public static final long PARAMETERIZED = TRANSACTIONAL << 1;                //  26
    public static final long DISTINCT = PARAMETERIZED << 1;                     //  27

    public static final long CLASS = DISTINCT << 1;                             //  28
    public static final long ISOLATED = CLASS << 1;                             //  29
    public static final long ISOLATED_PARAM = ISOLATED << 1;                    //  30
    public static final long CONFIGURABLE = ISOLATED_PARAM << 1;                //  31
    public static final long OBJECT_CTOR = CONFIGURABLE << 1;                   //  32

    public static final long ENUM = OBJECT_CTOR << 1;                           //  33

    public static long asMask(Set<Flag> flagSet) {
        long mask = 0;
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
                case CONFIGURABLE:
                    mask |= CONFIGURABLE;
                    break;
                case OBJECT_CTOR:
                    mask |= OBJECT_CTOR;
                    break;
                case ENUM:
                    mask |= ENUM;
                    break;
            }
        }
        return mask;
    }

    public static Set<Flag> unMask(long mask) {
        Set<Flag> flagSet = new HashSet<>();
        long flagVal;
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
                case CONFIGURABLE:
                    flagVal = CONFIGURABLE;
                    break;
                case OBJECT_CTOR:
                    flagVal = OBJECT_CTOR;
                    break;
                case ENUM:
                    flagVal = ENUM;
                    break;
                default:
                    continue;
            }
            addIfFlagOn(flagSet, mask, flagVal, flag);
        }
        return flagSet;
    }

    public static long unset(long mask, long flag) {
        return mask & (~flag);
    }

    private static void addIfFlagOn(Set<Flag> flagSet, long mask, long flagVal, Flag flag) {
        if ((mask & flagVal) == flagVal) {
            flagSet.add(flag);
        }
    }
}
