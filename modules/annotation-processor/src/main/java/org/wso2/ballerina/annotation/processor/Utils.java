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
package org.wso2.ballerina.annotation.processor;

import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.ReturnType;

/**
 * Utility class for annotation processor
 */
public class Utils {

    public static void getInputParams(Argument[] args, StringBuilder sb) {
        sb.append(" (");
        for (int i = 1; i <= args.length; i++) {
            Argument arg = args[i - 1];
            sb.append(getArgumentType(arg.type(), arg.elementType())).append(" ").append(arg.name());
            if (i != args.length) {
                sb.append(", ");
            }
        }
        sb.append(")");
    }

    public static void getReturnParams(ReturnType[] args, StringBuilder sb) {
        sb.append(" (");
        for (int i = 1; i <= args.length; i++) {
            ReturnType arg = args[i - 1];
            sb.append(getArgumentType(arg.type(), arg.elementType()));
            if (i != args.length) {
                sb.append(", ");
            }
        }
        sb.append(")");
    }

    public static String getArgumentType(TypeEnum argType, TypeEnum argEltType) {
        if (TypeEnum.ARRAY.equals(argType)) {
            return argEltType.getName() + "[]";
        }
        return argType.getName();
    }
}
