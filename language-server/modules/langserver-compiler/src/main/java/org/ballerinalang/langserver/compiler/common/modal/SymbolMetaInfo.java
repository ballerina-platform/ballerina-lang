/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.compiler.common.modal;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

/**
 * Meta information of a symbol.
 * 
 * @since 0.985
 */
public final class SymbolMetaInfo {

    private String name;
    private String pkgOrgName;
    private String pkgName;
    private String pkgAlias;
    private boolean caller;
    private String kind;
    private BSymbol bSymbol;
    private String typeName;
    private boolean isLocal;
    private DiagnosticPos position;


    private SymbolMetaInfo(String name, String pkgOrgName, String pkgName, String pkgAlias, boolean caller,
                           String kind, BSymbol bSymbol, String typeName, boolean isLocal, DiagnosticPos position) {
        this.name = name;
        this.pkgOrgName = pkgOrgName;
        this.pkgName = pkgName;
        this.pkgAlias = pkgAlias;
        this.caller = caller;
        this.kind = kind;
        this.bSymbol = bSymbol;
        this.typeName = typeName;
        this.isLocal = isLocal;
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public String getPkgOrgName() {
        return pkgOrgName;
    }

    public String getPkgName() {
        return pkgName;
    }

    public String getPkgAlias() {
        return pkgAlias;
    }

    public boolean isCaller() {
        return caller;
    }

    public String getKind() {
        return kind;
    }

    public BSymbol getbSymbol() {
        return bSymbol;
    }

    public String getTypeName() {
        return typeName;
    }

    public JsonElement getJson() {
        Gson gson = new Gson();
        JsonElement element = gson.toJsonTree(this);
        if (this.position != null) {
            JsonObject positionJson = new JsonObject();
            positionJson.addProperty("startColumn", position.getStartColumn());
            positionJson.addProperty("startLine", position.getStartLine());
            positionJson.addProperty("endColumn", position.getEndColumn());
            positionJson.addProperty("endLine", position.getEndLine());
            element.getAsJsonObject().add("position", positionJson);
        }
        return element;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public DiagnosticPos getPosition() {
        return position;
    }

    /**
     * Builder for Symbol Meta information.
     * 
     * @since 0.985
     */
    public static class SymbolMetaInfoBuilder {
        private String name;
        private String pkgOrgName;
        private String pkgName;
        private String pkgAlias;
        private boolean caller = false;
        private String kind = "";
        private BSymbol bSymbol;
        private String typeName;
        private boolean isLocal;
        private DiagnosticPos pos;

        public SymbolMetaInfoBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public SymbolMetaInfoBuilder setCaller(boolean caller) {
            this.caller = caller;
            return this;
        }

        public SymbolMetaInfoBuilder setKind(String kind) {
            this.kind = kind;
            return this;
        }

        public SymbolMetaInfoBuilder setPkgOrgName(String pkgOrgName) {
            this.pkgOrgName = pkgOrgName;
            return this;
        }

        public SymbolMetaInfoBuilder setPkgName(String pkgName) {
            this.pkgName = pkgName;
            return this;
        }

        public SymbolMetaInfoBuilder setPkgAlias(String pkgAlias) {
            this.pkgAlias = pkgAlias;
            return this;
        }

        public SymbolMetaInfoBuilder setbSymbol(BSymbol bSymbol) {
            this.bSymbol = bSymbol;
            return this;
        }

        public SymbolMetaInfoBuilder setTypeName(String typeName) {
            this.typeName = typeName;
            return this;
        }

        public SymbolMetaInfoBuilder setLocal(boolean local) {
            isLocal = local;
            return this;
        }

        public SymbolMetaInfoBuilder setPos(DiagnosticPos pos) {
            this.pos = pos;
            return this;
        }

        public SymbolMetaInfo build() {
            return new SymbolMetaInfo(this.name, this.pkgOrgName, this.pkgName, this.pkgAlias, this.caller,
                    this.kind, this.bSymbol, this.typeName, this.isLocal, this.pos);
        }
    }
}
