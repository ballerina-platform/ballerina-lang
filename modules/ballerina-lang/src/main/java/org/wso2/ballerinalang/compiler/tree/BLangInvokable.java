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

import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.InvokableNode;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;

import java.util.List;
import java.util.Set;

/**
 * @since 0.94
 */
public class BLangInvokable extends BLangNode implements InvokableNode {
    public BLangIdentifier name;
    public List<BLangVariable> params;
    public List<BLangVariable> retParams;
    public BLangBlockStmt body;
    public long flags;
    public List<BLangAnnotationAttachment> annAttachments;

    public BLangInvokable(BLangIdentifier name,
                          List<BLangVariable> params,
                          List<BLangVariable> retParams,
                          BLangBlockStmt body,
                          long flags,
                          List<BLangAnnotationAttachment> annAttachments) {
        this.name = name;
        this.params = params;
        this.retParams = retParams;
        this.body = body;
        this.flags = flags;
        this.annAttachments = annAttachments;
    }

    @Override
    public BLangIdentifier getName() {
        return name;
    }

    @Override
    public List<BLangVariable> getParameters() {
        return params;
    }

    @Override
    public List<BLangVariable> getReturnParameters() {
        return retParams;
    }

    @Override
    public BLangBlockStmt getBody() {
        return body;
    }

    @Override
    public Set<Flag> getFlags() {
        return null;
    }

    @Override
    public List<BLangAnnotationAttachment> getAnnotationAttachments() {
        return annAttachments;
    }
}
