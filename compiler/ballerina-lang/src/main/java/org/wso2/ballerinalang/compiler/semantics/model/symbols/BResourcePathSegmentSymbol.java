/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.ballerinalang.compiler.semantics.model.symbols;

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;

/**
 * {@link BResourcePathSegmentSymbol} represents a resource path segment in a resource method.
 *
 * @since 2201.3.0
 */
public class BResourcePathSegmentSymbol extends BSymbol {
    /**
     * If the path segment is not the first segment, then the parentResource will not be null.
     */
    public BResourcePathSegmentSymbol parentResource;
    public BResourceFunction resourceMethod;
    
    public BResourcePathSegmentSymbol(Name name, PackageID pkgID, BType type, BSymbol owner, Location location,
                                      BResourcePathSegmentSymbol parentResource, BResourceFunction resourceMethod,
                                      SymbolOrigin origin) {
        super(SymTag.RESOURCE_PATH_SEGMENT, 0, name, pkgID, type, owner, location, origin);
        this.parentResource = parentResource;
        this.resourceMethod = resourceMethod;
        if (name.value.contains("^^")) {
            this.kind = SymbolKind.RESOURCE_PATH_REST_PARAM_SEGMENT;
        } else if (name.value.contains("^")) {
            this.kind = SymbolKind.RESOURCE_PATH_PARAM_SEGMENT; 
        } else if (name.value.equals(".")) {
            this.kind = SymbolKind.RESOURCE_ROOT_PATH_SEGMENT;
        } else {
            this.kind = SymbolKind.RESOURCE_PATH_IDENTIFIER_SEGMENT;
        }
    }
    
    public BResourcePathSegmentSymbol getParentResource() {
        return parentResource;
    }

    public BResourceFunction getResourceMethod() {
        return resourceMethod;
    }
}
