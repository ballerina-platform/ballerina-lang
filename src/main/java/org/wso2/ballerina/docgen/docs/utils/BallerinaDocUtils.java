/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.docgen.docs.utils;

import org.wso2.ballerina.core.model.types.BArrayType;
import org.wso2.ballerina.core.model.types.BType;
import org.wso2.ballerina.core.model.types.TypeEnum;
import java.io.PrintStream;

/**
 * Util methods used for doc generation.
 */
public class BallerinaDocUtils {

    private static final PrintStream out = System.out;

    /**
     * @return the string representation of a type
     */
    public static String getType(TypeEnum argType, TypeEnum argEltType) {
        if (TypeEnum.ARRAY.equals(argType)) {
            return argEltType.getName() + "[]";
        }
        return argType.getName();
    }

    /**
     * @return the string representation of a {@link BType}
     */
    public static String getType(BType type) {
        if (type instanceof BArrayType) {
            BArrayType t = (BArrayType) type;
            return t.getElementType().toString() + "[]";
        }
        return type.toString();
    }
}
