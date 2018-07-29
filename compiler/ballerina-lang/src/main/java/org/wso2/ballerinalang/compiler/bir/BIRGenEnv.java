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
package org.wso2.ballerinalang.compiler.bir;

import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;

/**
 * Stores the state such as the current node, enclosing package, function etc, during bir generation.
 *
 * @since 0.980.0
 */
public class BIRGenEnv {

    public BIRNode node;

    public BIRPackage enclPkg;

    public BIRFunction enclFunc;

    private BIRGenEnv() {
    }

    public static BIRGenEnv packageEnv(BIRPackage birPkg) {
        BIRGenEnv env = new BIRGenEnv();
        env.enclPkg = birPkg;
        return env;
    }
}
