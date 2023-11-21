/*
 *  Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.Comparator;

/**
 * Util functions required for handling BIR.
 *
 * @since 2201.6.0
 */
public class BIRGenUtils {

    private BIRGenUtils () {

    }

    public static void rearrangeBasicBlocks(BIRNode.BIRPackage birPackage) {
        for (BIRNode.BIRTypeDefinition tDef : birPackage.typeDefs) {
            for (BIRNode.BIRFunction birFunction : tDef.attachedFuncs) {
                rearrangeBasicBlocks(birFunction);
            }
        }
        for (BIRNode.BIRFunction birFunction : birPackage.getFunctions()) {
            rearrangeBasicBlocks(birFunction);
        }
    }

    public static void rearrangeBasicBlocks(BIRNode.BIRFunction birFunction) {
        int currentBBId = 0;
        // Re-arrange basic blocks
        for (BIRNode.BIRBasicBlock bb : birFunction.basicBlocks) {
            currentBBId = renumberBasicBlock(currentBBId, bb);
        }
        // Re-arrange error entries
        birFunction.errorTable.sort(Comparator.comparingInt(o -> o.trapBB.number));
        birFunction.getEnclosedFunctions().forEach(BIRGenUtils::rearrangeBasicBlocks);
    }

    public static int renumberBasicBlock(int newBBNum, BIRBasicBlock bb) {
        bb.number = newBBNum;
        bb.id = new Name(BIRBasicBlock.BIR_BASIC_BLOCK_PREFIX + newBBNum);
        return newBBNum + 1;
    }
}
