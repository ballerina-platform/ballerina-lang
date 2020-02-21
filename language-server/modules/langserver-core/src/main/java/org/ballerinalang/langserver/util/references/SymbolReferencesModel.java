/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.util.references;

import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents a BLang Symbol reference.
 *
 * @since 0.990.4
 */
public class SymbolReferencesModel {
    private List<Reference> references = new ArrayList<>();
    private List<Reference> definitions = new ArrayList<>();
    private Reference referenceAtCursor = null;

    public List<Reference> getReferences() {
        return references;
    }

    public List<Reference> getDefinitions() {
        return definitions;
    }

    public void addReference(Reference reference) {
        this.references.add(reference);
    }

    public void addDefinition(Reference definition) {
        this.definitions.add(definition);
    }

    public Optional<Reference> getReferenceAtCursor() {
        return Optional.ofNullable(referenceAtCursor);
    }

    public void setReferenceAtCursor(Reference symbol) {
        this.referenceAtCursor = symbol;
    }

    /**
     * Represents a found reference.
     */
    public static class Reference {
        private DiagnosticPos position;
        private BSymbol symbol;
        private BLangNode bLangNode;
        private String compilationUnit;
        private String symbolPkgName;
        private String sourcePkgName;

        public Reference(DiagnosticPos position, BSymbol symbol, BLangNode bLangNode) {
            this.position = position;
            this.symbol = symbol;
            this.bLangNode = bLangNode;
            this.symbolPkgName = (symbol != null)
                    ? symbol.pkgID.nameComps.stream().map(Name::getValue).collect(Collectors.joining("."))
                    : "";
            this.compilationUnit = position.src.cUnitName;
            this.sourcePkgName = position.src.pkgID.name.value;
        }

        public DiagnosticPos getPosition() {
            return position;
        }

        public String getCompilationUnit() {
            return this.compilationUnit;
        }

        public String getSourcePkgName() {
            return this.sourcePkgName;
        }

        public String getSymbolPkgName() {
            return this.symbolPkgName;
        }

        public BSymbol getSymbol() {
            return symbol;
        }

        public BLangNode getbLangNode() {
            return bLangNode;
        }
    }
}

