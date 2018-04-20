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

import java.util.Set;

/**
 * @since 0.94
 */
public class Flags {
    public static final int PUBLIC = 1;
    public static final int NATIVE = 2;
    public static final int FINAL = 4;
    public static final int ATTACHED = 8;
    public static final int DEPRECATED = 16;
    public static final int READONLY = 32;
    public static final int FUNCTION_FINAL = 64;
    public static final int INTERFACE = 128;
    public static final int DEFAULTABLE_CHECKED = 256;
    public static final int DEFAULTABLE = 512;
    public static final int RECORD = 1024;

    public static int asMask(Set<Flag> flagSet) {
        int mask = 0;
        for (Flag flag : flagSet) {
            switch (flag) {
                case PUBLIC:
                    mask |= PUBLIC;
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
                case DEFAULTABLE_CHECKED:
                    mask |= DEFAULTABLE_CHECKED;
                    break;
                case DEFAULTABLE:
                    mask |= DEFAULTABLE;
                    break;
                case RECORD:
                    mask |= RECORD;
                    break;
            }
        }
        return mask;
    }
}
