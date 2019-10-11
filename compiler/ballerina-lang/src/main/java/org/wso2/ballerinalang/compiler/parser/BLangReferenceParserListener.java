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
 *
 * @since 1.1.0
 */
public class BLangReferenceParserListener extends BallerinaParserBaseListener {
    private BLangPackageBuilder pkgBuilder;
    private boolean isInErrorState = false;

    public BLangReferenceParserListener(BLangPackageBuilder pkgBuilder) {
        this.pkgBuilder = pkgBuilder;
    }

    @Override
    public void exitDocumentationFullyqualifiedIdentifier(
            BallerinaParser.DocumentationFullyqualifiedIdentifierContext ctx) {
        if (isInErrorState) {
            this.pkgBuilder.logWarningAndRemoveDocumentationReference(false);
            return;
        }
        BallerinaParser.DocumentationIdentifierQualifierContext qualifier = ctx.documentationIdentifierQualifier();
        BallerinaParser.DocumentationIdentifierTypenameContext typeName = ctx.documentationIdentifierTypename();
        BallerinaParser.DocumentationIdentifierContext identifier = ctx.documentationIdentifier();

        String qualifierString = (qualifier != null) ? qualifier.Identifier().getText() : "";
        String typeNameString = (typeName != null) ? typeName.Identifier().getText() : "";
        String identifierString = (identifier != null) ? identifier.getText() : "";

        this.pkgBuilder.endDocumentationFullyQualifiedIdentifier(qualifierString, typeNameString, identifierString);
    }

    @Override
    public void exitDocumentationFullyqualifiedFunctionIdentifier(
            BallerinaParser.DocumentationFullyqualifiedFunctionIdentifierContext ctx) {
        if (isInErrorState) {
            this.pkgBuilder.logWarningAndRemoveDocumentationReference(true);
            return;
        }
        BallerinaParser.DocumentationIdentifierQualifierContext qualifier = ctx.documentationIdentifierQualifier();
        BallerinaParser.DocumentationIdentifierTypenameContext typeName = ctx.documentationIdentifierTypename();
        BallerinaParser.DocumentationIdentifierContext identifier = ctx.documentationIdentifier();

        String qualifierString = (qualifier != null) ? qualifier.Identifier().getText() : "";
        String typeNameString = (typeName != null) ? typeName.Identifier().getText() : "";
        String identifierString = (identifier != null) ? identifier.getText() : "";

        this.pkgBuilder.endDocumentationFullyQualifiedIdentifier(qualifierString, typeNameString, identifierString);
    }

    /**
     * Mark that this listener is in error state.
     */
    public void setErrorState() {
        this.isInErrorState = true;
    }
}
