/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.semantics.model.symbols;

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.AnnotationAttachmentSymbol;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;

import static org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag.ANNOTATION_ATTACHMENT;

/**
 * Symbol for annotation attachments.
 *
 * @since 2.1.0
 */
public class BAnnotationAttachmentSymbol extends BSymbol implements AnnotationAttachmentSymbol {

    public PackageID annotPkgID;
    public Name annotTag;

    public BAnnotationAttachmentSymbol(BAnnotationSymbol annotationSymbol, PackageID pkgID, BSymbol owner,
                                       Location pos, SymbolOrigin origin, BType attachedType) {
        super(ANNOTATION_ATTACHMENT, 0, Names.EMPTY, pkgID, attachedType, owner, pos, origin);
        this.annotPkgID = annotationSymbol.pkgID;
        this.annotTag = annotationSymbol.name;
    }

    public BAnnotationAttachmentSymbol(PackageID annotPkgID, Name annotTag, PackageID pkgID, BSymbol owner,
                                       Location pos, SymbolOrigin origin, BType attachedType) {
        super(ANNOTATION_ATTACHMENT, 0, Names.EMPTY, pkgID, attachedType, owner, pos, origin);
        this.annotPkgID = annotPkgID;
        this.annotTag = annotTag;
    }

    @Override
    public boolean isConstAnnotation() {
        return false;
    }

    /**
     * Symbol for const annotation attachments, including a constant symbol for the value.
     *
     * @since 2.0.0
     */
    public static class BConstAnnotationAttachmentSymbol extends BAnnotationAttachmentSymbol {

        public BConstantSymbol attachmentValueSymbol;

        public BConstAnnotationAttachmentSymbol(BAnnotationSymbol annotationSymbol, PackageID pkgID, BSymbol owner,
                                                Location pos, SymbolOrigin origin,
                                                BConstantSymbol attachmentValueSymbol) {
            super(annotationSymbol, pkgID, owner, pos, origin, annotationSymbol.attachedType);
            this.attachmentValueSymbol = attachmentValueSymbol;
        }

        public BConstAnnotationAttachmentSymbol(PackageID annotPkgID, Name annotTag, PackageID pkgID, BSymbol owner,
                                                Location pos, SymbolOrigin origin,
                                                BConstantSymbol attachmentValueSymbol, BType attachedType) {
            super(annotPkgID, annotTag, pkgID, owner, pos, origin, attachedType);
            this.attachmentValueSymbol = attachmentValueSymbol;
        }

        @Override
        public boolean isConstAnnotation() {
            return true;
        }
    }
}
