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
import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.Lists;

/**
 * {@code BConversionOperatorSymbol} represents symbol for type conversion.
 *
 * @since 0.94
 */
public class BConversionOperatorSymbol extends BOperatorSymbol {

    public boolean safe;

    public BConversionOperatorSymbol(PackageID pkgID,
                                     BType type,
                                     BType sourceType,
                                     BSymbol owner,
                                     boolean safe) {

        super(Names.CONVERSION_OP, pkgID, type, owner);
        this.kind = SymbolKind.CONVERSION_OPERATOR;
        this.params = Lists.of(new BVarSymbol(0, new Name("_"), pkgID, sourceType, this));
        this.safe = safe;
    }

    public boolean isSafeOperation() {
        return this.safe;
    }
}
