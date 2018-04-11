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
package org.wso2.ballerinalang.util;

import org.ballerinalang.model.elements.TypeFlag;

import java.util.Set;

/**
 * @since 0.970.0
 */
public class TypeFlags {
    public static final int DEFAULTABLE_CHECKED = 1;
    public static final int DEFAULTABLE = 2;

    public static int asMask(Set<TypeFlag> flagSet) {
        int mask = 0;
        for (TypeFlag flag : flagSet) {
            switch (flag) {
                case DEFAULTABLE_CHECKED:
                    mask |= DEFAULTABLE_CHECKED;
                    break;
                case DEFAULTABLE:
                    mask |= DEFAULTABLE;
                    break;
            }
        }
        return mask;
    }
}
