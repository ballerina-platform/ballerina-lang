/*
*  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.Annotatable;
import org.ballerinalang.model.symbols.AnnotationAttachmentSymbol;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNoType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.List;

/**
 * Symbol for client declarations.
 *
 * @since 2201.3.0
 */
public class BClientDeclarationSymbol extends BSymbol implements Annotatable {

    private List<BAnnotationAttachmentSymbol> annotationAttachments = new ArrayList<>();

    public String uri;

    public BClientDeclarationSymbol(Name prefix, String uri, PackageID pkgID, BSymbol owner, Location pos,
                                    SymbolOrigin origin) {
        super(SymTag.CLIENT_DECL, 0, prefix, pkgID, new BNoType(TypeTags.NONE), owner, pos, origin);
        this.uri = uri;
        this.kind = SymbolKind.CLIENT_DECL;
    }

    @Override
    public SymbolKind getKind() {
        return SymbolKind.CLIENT_DECL;
    }

    @Override
    public void addAnnotation(AnnotationAttachmentSymbol symbol) {
        if (symbol == null) {
            return;
        }
        
        this.annotationAttachments.add((BAnnotationAttachmentSymbol) symbol);
    }

    @Override
    public List<? extends AnnotationAttachmentSymbol> getAnnotations() {
        return this.annotationAttachments;
    }

    public String getUri() {
        return this.uri;
    }
}
