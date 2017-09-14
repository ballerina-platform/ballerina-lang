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

import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.semantics.model.types.BPackageType;
import org.wso2.ballerinalang.compiler.util.Name;

import static org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag.PACKAGE;

/**
 * @since 0.94
 */
public class BPackageSymbol extends BTypeSymbol {

    public Name version;
    public Name name;

    public BPackageSymbol(Name pkgName, Name pkgVersion, BSymbol owner) {
        super(PACKAGE, pkgName, null, owner);
        this.version = pkgVersion;
        this.type = new BPackageType(this);
        this.kind = SymbolKind.PACKAGE;
    }

    @Override
    public SymbolKind getKind() {
        return kind;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BPackageSymbol that = (BPackageSymbol) o;
        return name.equals(that.name) && version.equals(that.version);

    }

    @Override
    public int hashCode() {
        int result = version.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
