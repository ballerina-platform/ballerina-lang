/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.tree;

import org.ballerinalang.model.tree.FunctionNode;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;

import java.util.List;

/**
 * @since 0.94
 */
public class BLangFunction extends BLangInvokable implements FunctionNode {

    public BLangFunction(BLangIdentifier name,
                         List<BLangVariable> params,
                         List<BLangVariable> retParams,
                         BLangBlockStmt body,
                         long flags,
                         List<BLangAnnotationAttachment> annAttachments) {
        super(name, params, retParams, body, flags, annAttachments);
    }
}
