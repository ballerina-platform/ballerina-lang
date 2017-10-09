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
package org.wso2.ballerinalang.compiler.semantics.model.types;

import org.ballerinalang.model.types.InvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.List;

/**
 * @since 0.94
 */
public class BInvokableType extends BType implements InvokableType {

    public List<BType> paramTypes;
    public List<BType> retTypes;
    public String typeDescriptor;

    // This field is only applicable for functions and actions at the moment.
    private BType receiverType;

    public BInvokableType(List<BType> paramTypes,
                          List<BType> retTypes, BTypeSymbol tsymbol) {
        super(TypeTags.INVOKABLE, tsymbol);
        this.paramTypes = paramTypes;
        this.retTypes = retTypes;
    }

    @Override
    public List<BType> getParameterTypes() {
        return paramTypes;
    }

    @Override
    public List<BType> getReturnTypes() {
        return retTypes;
    }

    @Override
    public String getDesc() {
        return typeDescriptor;
    }

    public BType getReceiverType() {
        return receiverType;
    }

    public void setReceiverType(BType receiverType) {
        this.receiverType = receiverType;
    }
}
