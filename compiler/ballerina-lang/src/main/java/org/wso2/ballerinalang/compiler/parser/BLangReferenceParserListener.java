/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.parser;

import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParserBaseListener;

/**
 * Lightweight parser class for parsing the content inside backticks for Ballerina Markdown Documentation.
 * @since 1.1.0
 */
public class BLangReferenceParserListener extends BallerinaParserBaseListener {
    private boolean isInErrorState;

    // Used to store the resolved identifiers of the current content.
    private String pkgName;
    private String typeName;
    private String identifier;
    private boolean hasBrackets; // Has brackets in the content string, which is only allowed with functions.

    private static final String EMPTY_STRING = "";

    public BLangReferenceParserListener() {
        this.isInErrorState = false;
        this.pkgName = EMPTY_STRING;
        this.typeName = EMPTY_STRING;
        this.identifier = EMPTY_STRING;
        this.hasBrackets = false;
    }

    @Override
    public void exitDocumentationFullyqualifiedIdentifier(
            BallerinaParser.DocumentationFullyqualifiedIdentifierContext ctx) {
        if (isInErrorState) {
            return;
        }
        BallerinaParser.DocumentationIdentifierQualifierContext qualifierCtx = ctx.documentationIdentifierQualifier();
        BallerinaParser.DocumentationIdentifierTypenameContext typeNameCtx = ctx.documentationIdentifierTypename();
        BallerinaParser.DocumentationIdentifierContext identifierCtx = ctx.documentationIdentifier();
        BallerinaParser.BraketContext bracketCtx = ctx.braket();

        this.pkgName = (qualifierCtx != null) ? qualifierCtx.Identifier().getText() : EMPTY_STRING;
        this.typeName = (typeNameCtx != null) ? typeNameCtx.Identifier().getText() : EMPTY_STRING;
        this.identifier = (identifierCtx != null) ? identifierCtx.getText() : EMPTY_STRING;
        // brackets with keywords other than function is not allowed.
        this.hasBrackets = bracketCtx != null;
    }

    @Override
    public void exitDocumentationFullyqualifiedFunctionIdentifier(
            BallerinaParser.DocumentationFullyqualifiedFunctionIdentifierContext ctx) {
        if (isInErrorState) {
            return;
        }
        BallerinaParser.DocumentationIdentifierQualifierContext qualifierCtx = ctx.documentationIdentifierQualifier();
        BallerinaParser.DocumentationIdentifierTypenameContext typeNameCtx = ctx.documentationIdentifierTypename();
        BallerinaParser.DocumentationIdentifierContext identifierCtx = ctx.documentationIdentifier();

        this.pkgName = (qualifierCtx != null) ? qualifierCtx.Identifier().getText() : EMPTY_STRING;
        this.typeName = (typeNameCtx != null) ? typeNameCtx.Identifier().getText() : EMPTY_STRING;
        this.identifier = (identifierCtx != null) ? identifierCtx.getText() : EMPTY_STRING;
    }

    /**
     * Mark that this listener is in error state.
     */
    public void setErrorState() {
        this.isInErrorState = true;
    }

    public void reset() {
        this.isInErrorState = false;
        this.pkgName = EMPTY_STRING;
        this.typeName = EMPTY_STRING;
        this.identifier = EMPTY_STRING;
        this.hasBrackets = false;
    }

    // If the parser is in error state, invalid identifier.
    public boolean getState() {
        return this.isInErrorState;
    }

    public String getPkgName() {
        return this.pkgName;
    }

    public String getTypeName() {
        return this.typeName;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public boolean hasBrackets() {
        return this.hasBrackets;
    }
}
