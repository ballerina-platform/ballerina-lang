/**
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

package org.ballerinalang.nativeimpl.builtin.operationslib;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BVM;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BLangFreezeException;
import org.ballerinalang.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Performs freezing a given value.
 *
 * @since 0.991.0
 */
@BallerinaFunction(orgName = "ballerina",
        packageName = "builtin",
        functionName = "_freeze",
        returnType = {@ReturnType(type = TypeKind.ANYDATA)})
public class Freeze extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context ctx) {
        BValue value = ctx.getNullableRefArgument(0);
        if (value == null) {
            // assuming we reach here because the value is nil (()), the frozen value would also be nil.
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
}
