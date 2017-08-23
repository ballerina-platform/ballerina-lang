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
import org.ballerinalang.model.tree.ServiceNode;

import java.util.List;
import java.util.Set;

/**
 * @since 0.94
 */
public class BLangService extends BLangNode implements ServiceNode {
    public BLangIdentifier name;
    public BLangIdentifier protocolPkgIdentifier;
    public List<BLangVariable> vars;
    public List<BLangResource> resources;
    public long flags;
    public List<BLangAnnotationAttachment> annAttachments;


    public BLangService(BLangIdentifier name,
                        BLangIdentifier protocolPkgIdentifier,
                        List<BLangVariable> vars,
                        List<BLangResource> resources,
                        long flags,
                        List<BLangAnnotationAttachment> annAttachments) {
        this.name = name;
        this.protocolPkgIdentifier = protocolPkgIdentifier;
        this.vars = vars;
        this.resources = resources;
        this.flags = flags;
        this.annAttachments = annAttachments;
    }

    @Override
    public BLangIdentifier getName() {
        return name;
    }

    @Override
    public BLangIdentifier getProtocolPackageIdentifier() {
        return protocolPkgIdentifier;
    }

    @Override
    public List<BLangVariable> getVariables() {
        return vars;
    }

    @Override
    public List<BLangResource> getResources() {
        return resources;
    }

    @Override
    public Set<Flag> getFlags() {
        return null;
    }

    @Override
    public List<BLangAnnotationAttachment> getAnnotationAttachments() {
        return annAttachments;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }
}
