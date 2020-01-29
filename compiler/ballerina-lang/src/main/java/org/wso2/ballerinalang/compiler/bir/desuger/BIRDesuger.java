/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.bir.desuger;

import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRVisitor;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

/**
 * A desuger phase for BIR model before generating JVM bytecodes.
 *
 * @since 1.2.0
 */
public class BIRDesuger extends BIRVisitor {
    private static final CompilerContext.Key<BIRDesuger> BIR_DESUGER = new CompilerContext.Key<>();

    public static BIRDesuger getInstance(CompilerContext context) {
        BIRDesuger birDesuger = context.get(BIR_DESUGER);
        if (birDesuger == null) {
            birDesuger = new BIRDesuger(context);
        }

        return birDesuger;
    }

    private BIRDesuger(CompilerContext context) {
        context.put(BIR_DESUGER, this);
    }

    public BIRNode.BIRPackage perform(BIRNode.BIRPackage birPackage) {
        // TODO move desuger code to here

        return birPackage;
    }

}
