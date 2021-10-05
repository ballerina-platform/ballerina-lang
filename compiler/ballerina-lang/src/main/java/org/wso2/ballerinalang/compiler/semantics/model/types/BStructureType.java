/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructureTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * {@code BStructureType} represents structure type in Ballerina.
 *
 * @since 0.971.0
 */
public abstract class BStructureType extends BType {

    private static final String DOLLAR = "$";

    public LinkedHashMap<String, BField> fields;
    public List<BType> typeInclusions;

    public BStructureType(int tag, BTypeSymbol tSymbol) {
        super(tag, tSymbol);
        this.fields = new LinkedHashMap<>();
        this.typeInclusions = new ArrayList<>();
    }

    public BStructureType(int tag, BTypeSymbol tSymbol, long flags) {
        super(tag, tSymbol, flags);
        this.fields = new LinkedHashMap<>();
        this.typeInclusions = new ArrayList<>();
    }

    public LinkedHashMap<String, BField> getFields() {
        return fields;
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    public boolean shouldPrintShape() {
        Name name = tsymbol.name;
        if (tsymbol instanceof BStructureTypeSymbol && ((BStructureTypeSymbol) tsymbol).typeDefinitionSymbol != null) {
            name = ((BStructureTypeSymbol) tsymbol).typeDefinitionSymbol.name;
        }

        if (name == null) {
            return false;
        }

        String value = name.value;

        if (value.isEmpty() || value.startsWith(DOLLAR)) {
            return true;
        }

        if (!(Symbols.isFlagOn(this.tsymbol.flags, Flags.READONLY))) {
            return false;
        }

        return value.startsWith("($");
    }
}
