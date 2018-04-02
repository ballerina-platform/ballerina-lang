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
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Names;

/**
 * @since 0.94
 */
public class BConversionOperatorSymbol extends BOperatorSymbol {

//    public boolean safe;
    public boolean implicit;

    public BConversionOperatorSymbol(PackageID pkgID,
                                     BType type,
                                     BSymbol owner,
                                     boolean implicit,
                                     boolean safe,
                                     int opcode) {
        super(Names.CONVERSION_OP, pkgID, type, owner, opcode);
//        this.safe = safe;
        this.implicit = implicit;
    }
}
