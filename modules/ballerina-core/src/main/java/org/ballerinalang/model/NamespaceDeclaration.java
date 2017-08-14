/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.model;

import org.ballerinalang.model.symbols.BLangSymbol;

import javax.xml.XMLConstants;

/**
 * {@code NamespaceDeclaration} represents a namespace declaration in ballerina.
 * <p>
 * e.g
 * xmlns "http://example.com/ns/a" as ns0;
 * xmlns "http://example.com/ns/b" as ns1
 * xmlns "http://example.com/ns/c"
 * <p>
 * A ballerina namespace declaration contains namspaceUri and the prefix (optional). 
 * A namespace declaration without the prefix would declare a default namespace.
 * <p>
 * e.g.
 * NamespaceUri: "http://example.com/ns/a"
 * Namespace prefix: ns0
 *
 * @since 0.89
 */
public class NamespaceDeclaration implements BLangSymbol, Node, CompilationUnit {
    private NodeLocation location;
    private WhiteSpaceDescriptor whiteSpaceDescriptor;
    private String namespaceUri;
    private String prefix;
    private String pkgPath;
    private SymbolName symbolName;
    private Identifier identifier;
    private boolean isDefault;
    private SymbolScope symbolScope;
    
    public NamespaceDeclaration(NodeLocation location, WhiteSpaceDescriptor wsDescriptor, String namespaceUri, 
            String prefix, String pkgPath, Identifier identifier, SymbolScope symbolScope) {
        this.location = location;
        this.whiteSpaceDescriptor = wsDescriptor;
        this.namespaceUri = namespaceUri;
        
        if (prefix == null) {
            this.prefix = XMLConstants.DEFAULT_NS_PREFIX;
            this.isDefault = true;
        } else {
            this.prefix = prefix;
            this.isDefault = false;
        }
        
        this.pkgPath = pkgPath;
        this.identifier = identifier;
        this.symbolScope = symbolScope;
        this.symbolName = new SymbolName(this.prefix);
    }

    /**
     * Get the name of the import  package.
     *
     * @return name of the import package
     */
    public String getNamespaceUri() {
        return namespaceUri;
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean isDefaultNamespace() {
        return isDefault;
    }
    
    @Override
    public SymbolName getSymbolName() {
        return symbolName;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NamespaceDeclaration)) {
            return false;
        }

        NamespaceDeclaration other = (NamespaceDeclaration) obj;
        
        // prefix is the unique identifier for a namespace delcr.
        return this.prefix.equals(other.prefix);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeLocation getNodeLocation() {
        return location;
    }

    public void setWhiteSpaceDescriptor(WhiteSpaceDescriptor whiteSpaceDescriptor) {
        this.whiteSpaceDescriptor = whiteSpaceDescriptor;
    }

    @Override
    public WhiteSpaceDescriptor getWhiteSpaceDescriptor() {
        return whiteSpaceDescriptor;
    }

    @Override
    public String getName() {
        return symbolName.getName();
    }

    @Override
    public Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public String getPackagePath() {
        return pkgPath;
    }

    @Override
    public boolean isPublic() {
        return true;
    }

    @Override
    public boolean isNative() {
        return false;
    }

    @Override
    public SymbolScope getSymbolScope() {
        return symbolScope;
    }
}
