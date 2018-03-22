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
package org.wso2.ballerinalang.compiler.semantics.model.symbols;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.AnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachmentPoint;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag.ANNOTATION;

/**
 * @since 0.94
 */
public class BAnnotationSymbol extends BTypeSymbol implements AnnotationSymbol {

    @Deprecated
    public List<BAnnotationAttributeSymbol> attributes;
    public BTypeSymbol attachedType;
    public List<BLangAnnotationAttachmentPoint> attachmentPoints;

    public BAnnotationSymbol(Name name, int flags, PackageID pkgID, BType type, BSymbol owner) {
        super(ANNOTATION, flags, name, pkgID, type, owner);
        attributes = new ArrayList<>();
        attachmentPoints =  new ArrayList<>();
    }

    @Override
    public List<BAnnotationAttributeSymbol> getAttributes() {
        return attributes;
    }

    public List<BLangAnnotationAttachmentPoint> getAttachmentPoints() {
        return attachmentPoints;
    }

    @Override
    public String toString() {
        return pkgID != null && !pkgID.toString().equals(".") ?
                pkgID.toString() + ":" + this.name : this.name.toString();
    }

    public String bvmAlias() {
        String pkg = pkgID.bvmAlias();
        return !pkg.equals(".") ? pkg + ":" + this.name : this.name.toString();
    }
}
