/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.ballerinalang.compiler.semantics.model.symbols;

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * {@link BServiceSymbol} represents a service symbol in a scope.
 *
 * @since 0.985.0
 */
public class BServiceSymbol extends BSymbol {

    private final BClassSymbol associatedClass;
    private final List<BType> listenerTypes;
    private List<String> absResourcePath;
    private String attachPointStringLiteral;

    public BServiceSymbol(BClassSymbol associatedClass, long flags, Name name, PackageID pkgID, BType type,
                          BSymbol owner, Location pos, SymbolOrigin origin) {
        super(SymTag.SERVICE, flags, name, pkgID, type, owner, pos, origin);
        this.associatedClass = associatedClass;
        this.kind = SymbolKind.SERVICE;
        this.listenerTypes = new ArrayList<>();
    }

    public Optional<List<String>> getAbsResourcePath() {
        return Optional.ofNullable(this.absResourcePath);
    }

    public Optional<String> getAttachPointStringLiteral() {
        return Optional.ofNullable(this.attachPointStringLiteral);
    }

    public BClassSymbol getAssociatedClassSymbol() {
        return this.associatedClass;
    }

    public List<BType> getListenerTypes() {
        return this.listenerTypes;
    }

    public void setAbsResourcePath(List<String> absResourcePath) {
        this.absResourcePath = absResourcePath;
    }

    public void setAttachPointStringLiteral(String attachPointStringLiteral) {
        this.attachPointStringLiteral = attachPointStringLiteral;
    }

    public void addListenerType(BType type) {
        Assert.assertNotNull(type, "Provided type for listener cannot be null");
        this.listenerTypes.add(type);
    }
}
