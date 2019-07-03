/*
 *  Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package io.ballerina.transactions;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Checks whether transactions is a nested transaction.
 *
 * @since 0.991.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "transactions",
        functionName = "isNestedTransaction",
        returnType = {@ReturnType(type = TypeKind.BOOLEAN)}
)
public class IsNestedTransaction extends BlockingNativeCallableUnit {
    public void execute(Context ctx) {
        ctx.setReturnValues(new BBoolean(ctx.getStrand().getLocalTransactionContext() != null));
    }

    public static boolean isNestedTransaction(Strand strand) {
        return strand.getLocalTransactionContext() != null;
    }
}
