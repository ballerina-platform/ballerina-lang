/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.semtype.port.test;

import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter.getTypeOrClassName;

public abstract class SemTypeResolver<SemType> {

    protected static int from(Map<String, BLangNode> mod, BLangNode expr) {
        if (expr instanceof BLangLiteral literal) {
            return SemTypeResolver.listSize((Number) literal.value);
        } else if (expr instanceof BLangSimpleVarRef varRef) {
            String varName = varRef.variableName.value;
            return SemTypeResolver.from(mod, mod.get(varName));
        } else if (expr instanceof BLangConstant constant) {
            return SemTypeResolver.listSize((Number) constant.symbol.value.value);
        }
        throw new UnsupportedOperationException("Unsupported expr kind " + expr.getKind());
    }

    private static int listSize(Number size) {
        if (size.longValue() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("list sizes greater than " + Integer.MAX_VALUE + " not yet supported");
        }
        return size.intValue();
    }

    public void defineSemTypes(List<BLangNode> moduleDefs, TypeTestContext<SemType> cx) {
        Map<String, BLangNode> modTable = new LinkedHashMap<>();
        for (BLangNode typeAndClassDef : moduleDefs) {
            modTable.put(getTypeOrClassName(typeAndClassDef), typeAndClassDef);
        }
        modTable = Collections.unmodifiableMap(modTable);

        for (BLangNode def : moduleDefs) {
            if (def.getKind() == NodeKind.CLASS_DEFN) {
                throw new UnsupportedOperationException("Semtype are not supported for class definitions yet");
            } else if (def.getKind() == NodeKind.CONSTANT) {
                resolveConstant(cx, modTable, (BLangConstant) def);
            } else {
                BLangTypeDefinition typeDefinition = (BLangTypeDefinition) def;
                resolveTypeDefn(cx, modTable, typeDefinition);
            }
        }
    }

    protected abstract void resolveConstant(TypeTestContext<SemType> cx,
                                            Map<String, BLangNode> modTable, BLangConstant constant);

    protected abstract void resolveTypeDefn(TypeTestContext<SemType> cx,
                                            Map<String, BLangNode> mod, BLangTypeDefinition defn);
}
