/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;

/**
 * {@link BResourcePathSegmentSymbol} represents a resource path segment in a resource method.
 *
 * @since 2201.3.1
 */
public class BResourcePathSegmentSymbol extends BSymbol {
    public BResourcePathSegmentSymbol parentResource;
    public BResourceFunction resourceMethod;
    
    public BResourcePathSegmentSymbol(int tag, long flags, Name name, PackageID pkgID, BType type, BSymbol owner,
                                      Location location) {
        super(tag, flags, name, pkgID, type, owner, location, SymbolOrigin.SOURCE);
    }
}
