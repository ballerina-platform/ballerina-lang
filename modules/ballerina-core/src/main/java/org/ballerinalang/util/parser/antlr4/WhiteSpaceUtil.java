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
package org.ballerinalang.util.parser.antlr4;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.ballerinalang.model.WhiteSpaceDescriptor;
import org.ballerinalang.util.parser.BallerinaParser;

import java.util.List;

/**
 * Provide helpers to extract whitespace for each construct from token stream.
 *
 * @see BLangAntlr4Listener
 * @since 0.9.0
 */
public class WhiteSpaceUtil {

    public static final String KEYWORD_RESOURCE = "resource";
    public static final String STARTING_PAREN = "(";
    public static final String CLOSING_PAREN = ")";
    public static final String KEYWORD_AS = "as";
    public static final String OPENNING_CURLEY_BRACE = "{";

    public static String getFileStartingWhiteSpace(CommonTokenStream tokenStream) {
        // find first non-whitespace token
        Token firstNonWhiteSpaceToken = tokenStream.getTokens().stream()
                .filter(token -> token.getChannel() != Token.HIDDEN_CHANNEL)
                .findFirst()
                .get();
        return getWhitespaceToLeft(tokenStream, firstNonWhiteSpaceToken.getTokenIndex());
    }

    public static WhiteSpaceDescriptor getImportDeclarationWS(CommonTokenStream tokenStream,
                                                              BallerinaParser.ImportDeclarationContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.IMPORT_DEC_IMPORT_KEYWORD_TO_PKG_NAME_START,
                                getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.IMPORT_DEC_PKG_NAME_END_TO_NEXT,
                getWhitespaceToRight(tokenStream, ctx.packageName().stop.getTokenIndex()));

        // if (as Identifier) is present, there can be five whitespace regions
        if (ctx.Identifier() != null) {
            ws.addWhitespaceRegion(WhiteSpaceRegions.IMPORT_DEC_AS_KEYWORD_TO_IDENTIFIER,
                    getWhitespaceToRight(tokenStream, getFirstTokenWithText(ctx.children, KEYWORD_AS).getTokenIndex()));
            ws.addWhitespaceRegion(WhiteSpaceRegions.IMPORT_DEC_IDENTIFIER_TO_IMPORT_DEC_END,
                    getWhitespaceToRight(tokenStream, ctx.Identifier().getSymbol().getTokenIndex()));
        }

        ws.addWhitespaceRegion(WhiteSpaceRegions.IMPORT_DEC_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static String getWhitespaceToRight(CommonTokenStream tokenStream, int tokenIndex) {
        StringBuilder whitespaceBuilder = new StringBuilder();
        if (tokenStream != null) {
            List<Token> hiddenTokensToRight = tokenStream.getHiddenTokensToRight(tokenIndex, Token.HIDDEN_CHANNEL);
            if (hiddenTokensToRight != null) {
                for (Token next : hiddenTokensToRight) {
                    whitespaceBuilder.append(next.getText());
                }
            }
        }
        return whitespaceBuilder.toString();
    }

    public static String getWhitespaceToLeft(CommonTokenStream tokenStream, int tokenIndex) {
        StringBuilder whitespaceBuilder = new StringBuilder();
        if (tokenStream != null) {
            List<Token> hiddenTokensToRight = tokenStream.getHiddenTokensToLeft(tokenIndex, Token.HIDDEN_CHANNEL);
            if (hiddenTokensToRight != null) {
                for (Token next : hiddenTokensToRight) {
                    whitespaceBuilder.append(next.getText());
                }
            }
        }
        return whitespaceBuilder.toString();
    }

    public static WhiteSpaceDescriptor getPackageDeclarationWS(CommonTokenStream tokenStream,
                                                               BallerinaParser.PackageDeclarationContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.BFILE_PKG_KEYWORD_TO_PKG_NAME_START,
                getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.BFILE_PKG_NAME_END_TO_SEMICOLON,
                getWhitespaceToRight(tokenStream, ctx.packageName().stop.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.BFILE_PKG_DEC_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getServiceDefinitionWS(CommonTokenStream tokenStream,
                                                              BallerinaParser.ServiceDefinitionContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.SERVICE_DEF_SERVICE_KEYWORD_TO_IDENTIFIER,
                getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.SERVICE_DEF_IDENTIFIER_TO_BODY_START,
                getWhitespaceToRight(tokenStream, ctx.Identifier().getSymbol().getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.SERVICE_DEF_BODY_START_TO_FIRST_CHILD,
                getWhitespaceToRight(tokenStream, ctx.serviceBody().start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.SERVICE_DEF_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.serviceBody().stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getResourceDefinitionWS(CommonTokenStream tokenStream,
                                                               BallerinaParser.ResourceDefinitionContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.RESOURCE_DEF_RESOURCE_KEYWORD_TO_IDENTIFIER,
                getWhitespaceToRight(tokenStream,
                        getFirstTokenWithText(ctx.children, KEYWORD_RESOURCE).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.RESOURCE_DEF_IDENTIFIER_TO_PARAM_LIST_START,
                getWhitespaceToRight(tokenStream, ctx.Identifier().getSymbol().getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.RESOURCE_DEF_PARAM_LIST_START_TO_FIRST_PARAM,
                getWhitespaceToRight(tokenStream, getFirstTokenWithText(ctx.children, STARTING_PAREN).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.RESOURCE_DEF_PARAM_LIST_END_TO_BODY_START,
                getWhitespaceToRight(tokenStream, getFirstTokenWithText(ctx.children, CLOSING_PAREN).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.RESOURCE_DEF_BODY_START_TO_FIRST_CHILD,
                getWhitespaceToRight(tokenStream, ctx.callableUnitBody().start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.RESOURCE_DEF_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.callableUnitBody().stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getAnnotationAttachmentWS(CommonTokenStream tokenStream,
                                                                 BallerinaParser.AnnotationAttachmentContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.ANNOTATION_ATCHMNT_AT_KEYWORD_TO_IDENTIFIER,
                getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ANNOTATION_ATCHMNT_IDENTIFIER_TO_ATTRIB_LIST_START,
                getWhitespaceToRight(tokenStream, ctx.nameReference().stop.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ANNOTATION_ATCHMNT_ATTRIB_LIST_START_TO_FIRST_ATTRIB,
                getWhitespaceToRight(tokenStream,
                        getFirstTokenWithText(ctx.children, OPENNING_CURLEY_BRACE).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ANNOTATION_ATCHMNT_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    protected static Token getFirstTokenWithText(List<ParseTree> children, String text) {
        return ((TerminalNode) children.stream()
                .filter((child) -> child instanceof TerminalNode)
                .filter((node) -> ((TerminalNode) node).getSymbol().getText().equals(text))
                .findFirst().get()).getSymbol();
    }

}
