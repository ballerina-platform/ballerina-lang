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
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.jvm.values.freeze.State;
import org.ballerinalang.jvm.values.freeze.Status;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BLangFreezeException;
import org.ballerinalang.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.ballerinalang.compiler.util.TypeTags;

/**
 * Performs freezing a given value.
 *
 * @since 0.990.4
 */
@BallerinaFunction(orgName = "ballerina",
        packageName = "utils",
        functionName = "freeze",
        args = {@Argument(name = "value", type = TypeKind.ANYDATA)},
        returnType = { @ReturnType(type = TypeKind.ANYDATA), @ReturnType(type = TypeKind.ERROR) })
public class Freeze extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context ctx) {
        BValue value = ctx.getNullableRefArgument(0);
        if (value == null) {
            // assuming we reach here because the value is nil (()), the frozen value would also be nil.
            return;
        } else if (value.getType().getTag() == TypeTags.ERROR) {
            // If the value is of type error, return an error indicating an error cannot be frozen.
            // Freeze is only allowed on errors if they are part of a structure.
            ctx.setReturnValues(BLangVMErrors.createError(ctx.getStrand(), BallerinaErrorReasons.FREEZE_ERROR,
                                                          "'freeze()' not allowed on 'error'"));
            return;
        }

        BVM.FreezeStatus freezeStatus = new BVM.FreezeStatus(BVM.FreezeStatus.State.MID_FREEZE);
        try {
            value.attemptFreeze(freezeStatus);

            // if freeze is successful, set the status as frozen and the value itself as the return value
            freezeStatus.setFrozen();
            ctx.setReturnValues(value);
        } catch (BLangFreezeException e) {
            // if freeze is unsuccessful due to an invalid value, set the frozen status of the value and its
            // constituents to false, and return an error
            freezeStatus.setUnfrozen();
            ctx.setReturnValues(BLangVMErrors.createError(ctx.getStrand(), BallerinaErrorReasons.FREEZE_ERROR,
                    e.getMessage()));
        } catch (BallerinaException e) {
            // if freeze is unsuccessful due to concurrent freeze attempts, set the frozen status of the value
            // and its constituents to false, and panic
            freezeStatus.setUnfrozen();
            ctx.setError(BLangVMErrors.createError(ctx.getStrand(), e.getMessage(), e.getDetail()));
            BVM.handleError(ctx.getStrand());
        }
    }

    public static Object freeze(Strand strand, Object value) {

        if (value == null) {
            // assuming we reach here because the value is nil (()), the frozen value would also be nil.
            return null;
        }
        if (!(value instanceof RefValue)) {
            return value;
        }
        RefValue refValue = (RefValue) value;
        if (refValue.getType().getTag() == org.ballerinalang.jvm.types.TypeTags.ERROR_TAG) {
            // If the value is of type error, return an error indicating an error cannot be frozen.
            // Freeze is only allowed on errors if they are part of a structure.
            return BallerinaErrors.createError(org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.FREEZE_ERROR,
                                 "'freeze()' not allowed on 'error'");
        }
        Status freezeStatus = new Status(State.MID_FREEZE);
        try {
            refValue.attemptFreeze(freezeStatus);
            // if freeze is successful, set the status as frozen and the value itself as the return value
            freezeStatus.setFrozen();
            return refValue;
        } catch (org.ballerinalang.jvm.util.exceptions.BLangFreezeException e) {
            // if freeze is unsuccessful due to an invalid value, set the frozen status of the value and its
            // constituents to false, and return an error
            freezeStatus.setUnfrozen();
            return BallerinaErrors.createError(org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.FREEZE_ERROR,
                                 e.getMessage());
        } catch (org.ballerinalang.jvm.util.exceptions.BallerinaException e) {
            // if freeze is unsuccessful due to concurrent freeze attempts, set the frozen status of the value
            // and its constituents to false, and panic
            freezeStatus.setUnfrozen();
            throw BallerinaErrors.createError(e.getMessage(), e.getDetail());
        }
    }
}
