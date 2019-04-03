/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/

package org.ballerinalang.utils;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BVM;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.JBLangVMErrors;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.util.exceptions.JBLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.JBallerinaErrorReasons;
import org.ballerinalang.jvm.util.exceptions.JRuntimeErrors;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.jvm.values.StreamingJsonValue;
import org.ballerinalang.jvm.values.XMLAttributes;
import org.ballerinalang.jvm.values.XMLItem;
import org.ballerinalang.jvm.values.XMLQName;
import org.ballerinalang.jvm.values.XMLSequence;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.util.exceptions.RuntimeErrors;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.HashMap;

/**
 * Performs a deep copy, recursively copying all structural values and their members.
 *
 * @since 0.990.4
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "utils",
        functionName = "clone",
        args = {@Argument(name = "value", type = TypeKind.ANYDATA)},
        returnType = { @ReturnType(type = TypeKind.ANYDATA) }
)
public class Clone extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context ctx) {
        BValue refRegVal = ctx.getNullableRefArgument(0);
        if (refRegVal == null) {
            return;
        } else if (refRegVal.getType().getTag() == TypeTags.ERROR) {
            ctx.setReturnValues(BLangVMErrors.createError(ctx.getStrand(), BallerinaErrorReasons.CLONE_ERROR,
                                                          BLangExceptionHelper.getErrorMessage(
                                                                  RuntimeErrors.UNSUPPORTED_CLONE_OPERATION,
                                                                  "error")));
            return;
        }

        if (!BVM.checkIsLikeType(refRegVal, BTypes.typePureType)) {
            ctx.setReturnValues(BLangVMErrors.createError(ctx.getStrand(), BallerinaErrorReasons.CLONE_ERROR,
                                                          BLangExceptionHelper.getErrorMessage(
                                                                  RuntimeErrors.UNSUPPORTED_CLONE_OPERATION,
                                                                  refRegVal.getType())));
            return;
        }
        ctx.setReturnValues(refRegVal.copy(new HashMap<>()));
    }

    public static Object clone(Strand strand, Object value) {
        return cloneValue((RefValue) value);
    }

    public static Object clone(Strand strand, ArrayValue value) {
        return cloneValue(value);
    }

    public static Object clone(Strand strand, ErrorValue value) {
        return JBLangVMErrors.createError(JBallerinaErrorReasons.CLONE_ERROR, JBLangExceptionHelper
                .getErrorMessage(JRuntimeErrors.UNSUPPORTED_CLONE_OPERATION, value.getType()));
    }

    public static Object clone(Strand strand, MapValue value) {
        return cloneValue(value);
    }

    public static Object clone(Strand strand, ObjectValue value) {
        return cloneValue(value);
    }

    public static Object clone(Strand strand, StreamingJsonValue value) {
        return cloneValue(value);
    }

    public static Object clone(Strand strand, XMLAttributes value) {
        return cloneValue(value);
    }

    public static Object clone(Strand strand, XMLItem value) {
        return cloneValue(value);
    }

    public static Object clone(Strand strand, XMLQName value) {
        return cloneValue(value);
    }

    public static Object clone(Strand strand, XMLSequence value) {
        return cloneValue(value);
    }

    public static Object clone(Strand strand, XMLValue value) {
        return cloneValue(value);
    }

    private static Object cloneValue(RefValue value) {
        if (value == null) {
            return null;
        } else if (!TypeChecker.checkIsLikeType(value, org.ballerinalang.jvm.types.BTypes.typePureType)) {
            return JBLangVMErrors.createError(BallerinaErrorReasons.CLONE_ERROR, BLangExceptionHelper
                    .getErrorMessage(RuntimeErrors.UNSUPPORTED_CLONE_OPERATION, value.getType()));
        }
        return value.copy(new HashMap<>());
    }
}
