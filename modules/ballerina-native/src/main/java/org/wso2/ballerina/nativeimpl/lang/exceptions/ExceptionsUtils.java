/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.nativeimpl.lang.exceptions;

import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BException;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.ReturnType;

/**
 * Native functions for ballerina.lang.exceptions
 */
public class ExceptionsUtils {

    /**
     * Get message to an exception.
     *
     * @since 0.8.0
     */
    @BallerinaFunction(
            packageName = "ballerina.lang.exceptions",
            functionName = "setMessages",
            args = {@Argument(name = "exception", type = TypeEnum.EXCEPTION),
                    @Argument(name = "message", type = TypeEnum.STRING)},
            isPublic = true
    )
    public static class SetMessage extends AbstractNativeFunction {
        @Override
        public BValue[] execute(Context context) {
            BException exception = (BException) getArgument(context, 0);
            String message = getArgument(context, 1).stringValue();
            exception.setMessage(message);
            return VOID_RETURN;
        }
    }

    /**
     * Set message to an exception.
     *
     * @since 0.8.0
     */
    @BallerinaFunction(
            packageName = "ballerina.lang.exceptions",
            functionName = "getMessages",
            args = {@Argument(name = "exception", type = TypeEnum.EXCEPTION)},
            returnType = {@ReturnType(type = TypeEnum.STRING)},
            isPublic = true
    )
    public static class GetMessage extends AbstractNativeFunction {
        @Override
        public BValue[] execute(Context context) {
            BException exception = (BException) getArgument(context, 0);
            return new BValue[]{exception.getMessage()};
        }
    }

    /**
     * Set category to an exception.
     *
     * @since 0.8.0
     */
    @BallerinaFunction(
            packageName = "ballerina.lang.exceptions",
            functionName = "setCategory",
            args = {@Argument(name = "exception", type = TypeEnum.EXCEPTION),
                    @Argument(name = "category", type = TypeEnum.STRING)},
            isPublic = true
    )
    public static class SetCategory extends AbstractNativeFunction {
        @Override
        public BValue[] execute(Context context) {
            BException exception = (BException) getArgument(context, 0);
            String category = getArgument(context, 1).stringValue();
            exception.setCategory(category);
            return VOID_RETURN;
        }
    }

    /**
     * Get category to an exception.
     *
     * @since 0.8.0
     */
    @BallerinaFunction(
            packageName = "ballerina.lang.exceptions",
            functionName = "getCategory",
            args = {@Argument(name = "exception", type = TypeEnum.EXCEPTION)},
            returnType = {@ReturnType(type = TypeEnum.STRING)},
            isPublic = true
    )
    public static class GetCategory extends AbstractNativeFunction {
        @Override
        public BValue[] execute(Context context) {
            BException exception = (BException) getArgument(context, 0);
            return new BValue[]{exception.getCategory()};
        }
    }

    /**
     * Set message and category.
     */
    @BallerinaFunction(
            packageName = "ballerina.lang.exceptions",
            functionName = "set",
            args = {@Argument(name = "exception", type = TypeEnum.EXCEPTION),
                    @Argument(name = "message", type = TypeEnum.STRING),
                    @Argument(name = "category", type = TypeEnum.STRING)},
            isPublic = true
    )
    public static class Set extends AbstractNativeFunction {
        @Override
        public BValue[] execute(Context context) {

            BException exception = (BException) getArgument(context, 0);
            String message = getArgument(context, 1).stringValue();
            String category = getArgument(context, 2).stringValue();
            exception.setMessage(message);
            exception.setCategory(category);
            return VOID_RETURN;
        }
    }

//    /**
//     * Set message and category.
//     */
//    @BallerinaFunction(
//            packageName = "ballerina.lang.exceptions",
//            functionName = "setCause",
//            args = {@Argument(name = "exception", type = TypeEnum.EXCEPTION),
//                    @Argument(name = "cause", type = TypeEnum.EXCEPTION)},
//            isPublic = true
//    )
//    public class SetCause extends AbstractNativeFunction {
//        @Override
//        public BValue[] execute(Context context) {
//            BException exception = (BException) getArgument(context, 0);
//            BException cause = (BException) getArgument(context, 1);
//            if(cause.getStackTrace().size() > 0){
//                exception
//            }
//            for(int i = cause.getStackTrace().size() ; ; i--){
//                exception.addStackTrace("Cause by :\n" + cause.getStackTrace().get(i));
//            }
//            return VOID_RETURN;
//        }
//    }


}
