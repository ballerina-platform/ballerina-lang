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
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.ballerinalang.model.WhiteSpaceDescriptor;
import org.ballerinalang.util.parser.BallerinaParser;
import org.ballerinalang.util.parser.BallerinaParser.AttributeContext;

import java.util.List;
import java.util.Optional;

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
    public static final String OPENING_CURLY_BRACE = "{";
    public static final String CLOSING_CURLY_BRACE = "}";
    public static final String LEFT_ANGLE_BRACKET = "<";
    public static final String RIGHT_ANGLE_BRACKET = ">";
    public static final String SYMBOL_COLON = ":";
    public static final String NATIVE_KEYWORD = "native";
    public static final String KEYWORD_THROWS = "throws";
    public static final String EQUAL_OPERATOR = "=";
    public static final String VAR_KEYWORD = "var";
    public static final String IF_KEYWORD = "if";
    public static final String DOT_OPERATOR = ".";
    public static final String COMMITTED_CLAUSE = "CommittedClause";
    public static final String ABORTED_CLAUSE = "AbortedClause";
    public static final String SOME_KEYWORD = "some";
    public static final String ALL_KEYWORD = "all";

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
        ws.addWhitespaceRegion(WhiteSpaceRegions.SERVICE_DEF_SERVICE_KEYWORD_TO_LEFT_ANGLE_BRACKET,
                getWhitespaceToLeft(tokenStream, getFirstTokenWithText(ctx.children,
                        LEFT_ANGLE_BRACKET).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.SERVICE_DEF_LEFT_ANGLE_BRACKET_TO_IDENTIFIER, getWhitespaceToRight
                (tokenStream, getFirstTokenWithText(ctx.children, LEFT_ANGLE_BRACKET).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.SERVICE_DEF_IDENTIFIER_TO_RIGHT_ANGLE_BRACKET, getWhitespaceToLeft
                (tokenStream, getFirstTokenWithText(ctx.children, RIGHT_ANGLE_BRACKET).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.SERVICE_DEF_RIGHT_ANGLE_BRACKET_TO_IDENTIFIER,
                getWhitespaceToRight(tokenStream, getFirstTokenWithText(ctx.children,
                        RIGHT_ANGLE_BRACKET).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.SERVICE_DEF_IDENTIFIER_TO_BODY_START,
                getWhitespaceToRight(tokenStream, ctx.Identifier(1).getSymbol().getTokenIndex()));
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
                        getFirstTokenWithText(ctx.children, OPENING_CURLY_BRACE).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ANNOTATION_ATCHMNT_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getAnnotationAttributeWS(CommonTokenStream tokenStream,
                                                                BallerinaParser.AnnotationAttributeContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.ANNOTATION_ATTRIB_KEY_START_TO_LAST_TOKEN,
                getWhitespaceToLeft(tokenStream, ctx.Identifier().getSymbol().getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ANNOTATION_ATTRIB_KEY_TO_COLON,
                getWhitespaceToRight(tokenStream, ctx.Identifier().getSymbol().getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ANNOTATION_ATTRIB_COLON_TO_VALUE_START,
                getWhitespaceToRight(tokenStream, getFirstTokenWithText(ctx.children, SYMBOL_COLON).getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getAnnotationAttributeValueWS(CommonTokenStream tokenStream,
                                                                 BallerinaParser.AnnotationAttributeValueContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.ANNOTATION_ATTRIB_VALUE_START_TO_LAST_TOKEN,
                     getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ANNOTATION_ATTRIB_VALUE_END_TO_NEXT_TOKEN,
                    getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getFunctionDefWS(CommonTokenStream tokenStream,
                                                        BallerinaParser.FunctionDefinitionContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        boolean isNative = NATIVE_KEYWORD.equals(ctx.getChild(0).getText());
        if (isNative) {
            ws.addWhitespaceRegion(WhiteSpaceRegions.FUNCTION_DEF_NATIVE_KEYWORD_TO_FUNCTION_KEYWORD,
                    getWhitespaceToRight(tokenStream,
                            getFirstTokenWithText(ctx.children, NATIVE_KEYWORD).getTokenIndex()));
        }
        ws.addWhitespaceRegion(WhiteSpaceRegions.FUNCTION_DEF_FUNCTION_KEYWORD_TO_IDENTIFIER_START,
                getWhitespaceToLeft(tokenStream,
                        ctx.callableUnitSignature().Identifier().getSymbol().getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.FUNCTION_DEF_IDENTIFIER_TO_PARAM_LIST_START,
                getWhitespaceToRight(tokenStream,
                        ctx.callableUnitSignature().Identifier().getSymbol().getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.FUNCTION_DEF_PARAM_LIST_START_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream,
                       getFirstTokenWithText(ctx.callableUnitSignature().children, STARTING_PAREN).getTokenIndex()));
        if (ctx.callableUnitSignature().returnParameters() != null) {
            ws.addWhitespaceRegion(WhiteSpaceRegions.FUNCTION_DEF_PARAM_LIST_END_TO_RETURN_PARAM_START,
                    getWhitespaceToLeft(tokenStream,
                            ctx.callableUnitSignature().returnParameters().start.getTokenIndex()));
            ws.addWhitespaceRegion(WhiteSpaceRegions.FUNCTION_DEF_RETURN_PARAM_START_TO_NEXT_TOKEN,
                    getWhitespaceToRight(tokenStream,
                            ctx.callableUnitSignature().returnParameters().start.getTokenIndex()));
        }
        Token throwsToken = getFirstTokenWithText(ctx.callableUnitSignature().children, KEYWORD_THROWS);
        if (!isNative) {
            ws.addWhitespaceRegion(WhiteSpaceRegions.FUNCTION_DEF_BODY_START_TO_LAST_TOKEN,
                    getWhitespaceToLeft(tokenStream, ctx.callableUnitBody().start.getTokenIndex()));
            ws.addWhitespaceRegion(WhiteSpaceRegions.FUNCTION_DEF_BODY_START_TO_NEXT_TOKEN,
                    getWhitespaceToRight(tokenStream, ctx.callableUnitBody().start.getTokenIndex()));
            ws.addWhitespaceRegion(WhiteSpaceRegions.FUNCTION_DEF_BODY_END_TO_NEXT_TOKEN,
                    getWhitespaceToRight(tokenStream, ctx.callableUnitBody().stop.getTokenIndex()));
        }

        return ws;
    }

    public static WhiteSpaceDescriptor getConnectorDefWS(CommonTokenStream tokenStream,
                                                         BallerinaParser.ConnectorDefinitionContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.CONNECTOR_DEF_CONNECTOR_KEYWORD_TO_IDENTIFIER,
                                getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.CONNECTOR_DEF_IDENTIFIER_TO_PARAM_LIST_START,
                getWhitespaceToRight(tokenStream, ctx.Identifier().getSymbol().getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.CONNECTOR_DEF_PARAM_LIST_END_TO_BODY_START,
                getWhitespaceToLeft(tokenStream, ctx.connectorBody().start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.CONNECTOR_DEF_PARAM_BODY_START_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.connectorBody().start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.CONNECTOR_DEF_BODY_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.connectorBody().stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getFilterConnectorDefWS(CommonTokenStream tokenStream,
                                                   BallerinaParser.ConnectorDefinitionContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.FILTER_CONNECTOR_DEF_CONNECTOR_KEYWORD_TO_IDENTIFIER,
                getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.FILTER_CONNECTOR_DEF_IDENTIFIER_TO_LT_SIGN,
                getWhitespaceToRight(tokenStream, ctx.Identifier().getSymbol().getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.FILTER_CONNECTOR_DEF_LT_SIGN_TO_PARAMETER,
                getWhitespaceToLeft(tokenStream, ctx.parameter().start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.FILTER_CONNECTOR_DEF_PARAMETER_TO_GT_SIGN,
                getWhitespaceToRight(tokenStream, ctx.parameter().start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.FILTER_CONNECTOR_DEF_GT_SIGN_TO_PARAM_LIST_START,
                getWhitespaceToLeft(tokenStream, ctx.parameterList().start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.FILTER_CONNECTOR_DEF_PARAM_LIST_END_TO_BODY_START,
                getWhitespaceToLeft(tokenStream, ctx.connectorBody().start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.FILTER_CONNECTOR_DEF_PARAM_BODY_START_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.connectorBody().start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.FILTER_CONNECTOR_DEF_BODY_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.connectorBody().stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getStructDefWS(CommonTokenStream tokenStream,
                                                      BallerinaParser.StructDefinitionContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.STRUCT_DEF_STRUCT_KEYWORD_TO_IDENTIFIER,
                getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.STRUCT_DEF_IDENTIFIER_TO_BODY_START,
                getWhitespaceToRight(tokenStream, ctx.Identifier().getSymbol().getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.STRUCT_DEF_BODY_START_TO_FIRST_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.structBody().start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.STRUCT_DEF_BODY_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.structBody().stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getTypeMapperDef(CommonTokenStream tokenStream,
                                                        BallerinaParser.TypeMapperDefinitionContext ctx) {
        boolean isNative = NATIVE_KEYWORD.equals(ctx.getChild(0).getText());
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        if (isNative) {
            ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_MAP_DEF_NATIVE_KEYWORD_TO_SIGNATURE_START,
                    getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        }
        ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_MAP_DEF_TYPE_MAPPER_KEYWORD_TO_IDENTIFIER,
                getWhitespaceToRight(tokenStream, ctx.typeMapperSignature().start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_MAP_DEF_IDENTIFIER_PARAM_WRAPPER_START,
                getWhitespaceToRight(tokenStream, ctx.typeMapperSignature().Identifier().getSymbol().getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_MAP_DEF_PARAM_WRAPPER_END_TO_RETURN_TYPE_WRAPPER_START,
                getWhitespaceToRight(tokenStream,
                        getFirstTokenWithText(ctx.typeMapperSignature().children, CLOSING_PAREN).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_MAP_DEF_RETURN_TYPE_WRAPPER_TO_BODY_START,
                getWhitespaceToLeft(tokenStream, ctx.typeMapperBody().start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_MAP_DEF_BODY_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.typeMapperBody().stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getConstantDefWS(CommonTokenStream tokenStream,
                                                        BallerinaParser.ConstantDefinitionContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.CONST_DEF_CONST_KEYWORD_TO_VAL_TYPE,
                getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.CONST_DEF_VAL_TYPE_TO_IDENTIFIER,
                getWhitespaceToLeft(tokenStream, ctx.Identifier().getSymbol().getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.CONST_DEF_IDENTIFIER_TO_EQUAL_OPERATOR,
                getWhitespaceToRight(tokenStream, ctx.Identifier().getSymbol().getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.CONST_DEF_EQUAL_OPERATOR_TO_LITERAL_START,
                getWhitespaceToRight(tokenStream, getFirstTokenWithText(ctx.children, EQUAL_OPERATOR).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.CONST_DEF_LITERAL_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.expression().stop.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.CONST_DEF_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getAnnotationDefWS(CommonTokenStream tokenStream,
                                                          BallerinaParser.AnnotationDefinitionContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.ANNOTATION_DEF_ANNOTATION_KEYWORD_TO_IDENTIFIER,
                            getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ANNOTATION_DEF_IDENTIFIER_TO_ATTACH_KEYWORD,
                getWhitespaceToRight(tokenStream, ctx.Identifier().getSymbol().getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ANNOTATION_DEF_BODY_START_TO_LAST_TOKEN,
                getWhitespaceToLeft(tokenStream, ctx.annotationBody().start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ANNOTATION_DEF_BODY_START_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.annotationBody().start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ANNOTATION_DEF_BODY_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.annotationBody().stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getAttachmentPointWS(CommonTokenStream tokenStream,
                                                            BallerinaParser.AttachmentPointContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.ANNOTATION_ATTACHMENT_POINT_PRECEDING_WS,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ANNOTATION_ATTACHMENT_POINT_FOLLOWING_WS,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    protected static Token getFirstTokenWithText(List<ParseTree> children, String text) {
        Optional<ParseTree> terminalNode = children.stream()
                .filter((child) -> child instanceof TerminalNode)
                .filter((node) -> ((TerminalNode) node).getSymbol().getText().equals(text))
                .findFirst();
        return (terminalNode.isPresent()) ? ((TerminalNode) terminalNode.get()).getSymbol() : null;
    }

    public static WhiteSpaceDescriptor getGlobalVariableDefWS(CommonTokenStream tokenStream,
                                                      BallerinaParser.GlobalVariableDefinitionContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.GLOBAL_VAR_DEF_TYPE_NAME_TO_IDENTIFIER,
                getWhitespaceToRight(tokenStream, ctx.typeName().stop.getTokenIndex()));
        if (ctx.expression() != null) {
            ws.addWhitespaceRegion(WhiteSpaceRegions.GLOBAL_VAR_DEF_IDENTIFIER_TO_EQUAL_OPERATOR,
                    getWhitespaceToRight(tokenStream, ctx.Identifier().getSymbol().getTokenIndex()));
            ws.addWhitespaceRegion(WhiteSpaceRegions.GLOBAL_VAR_DEF_EQUAL_OPERATOR_TO_EXPRESSION_START,
                    getWhitespaceToLeft(tokenStream, ctx.expression().start.getTokenIndex()));
        }
        ws.addWhitespaceRegion(WhiteSpaceRegions.GLOBAL_VAR_DEF_END_TO_LAST_TOKEN,
                getWhitespaceToLeft(tokenStream, ctx.stop.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.GLOBAL_VAR_DEF_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getVariableDefWS(CommonTokenStream tokenStream,
                                                        BallerinaParser.VariableDefinitionStatementContext ctx,
                                                        boolean exprAvailable) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.VAR_DEF_TYPE_NAME_TO_IDENTIFIER,
                getWhitespaceToRight(tokenStream, ctx.typeName().stop.getTokenIndex()));

        if (exprAvailable) {
            ws.addWhitespaceRegion(WhiteSpaceRegions.VAR_DEF_IDENTIFIER_TO_EQUAL_OPERATOR,
                    getWhitespaceToRight(tokenStream, ctx.Identifier().getSymbol().getTokenIndex()));
            ws.addWhitespaceRegion(WhiteSpaceRegions.VAR_DEF_EQUAL_OPERATOR_TO_EXPRESSION_START,
                    getWhitespaceToRight(tokenStream,
                            getFirstTokenWithText(ctx.children, EQUAL_OPERATOR).getTokenIndex()));
        }
        ws.addWhitespaceRegion(WhiteSpaceRegions.VAR_DEF_END_TO_LAST_TOKEN,
                getWhitespaceToLeft(tokenStream, ctx.stop.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.VAR_DEF_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getVariableDefWS(CommonTokenStream tokenStream,
                      BallerinaParser.ExpressionVariableDefinitionStatementContext ctx, boolean exprAvailable) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.VAR_DEF_TYPE_NAME_TO_IDENTIFIER,
                               getWhitespaceToRight(tokenStream, ctx.typeName().stop.getTokenIndex()));

        if (exprAvailable) {
            ws.addWhitespaceRegion(WhiteSpaceRegions.VAR_DEF_IDENTIFIER_TO_EQUAL_OPERATOR,
                                   getWhitespaceToRight(tokenStream, ctx.Identifier().getSymbol().getTokenIndex()));
            ws.addWhitespaceRegion(WhiteSpaceRegions.VAR_DEF_EQUAL_OPERATOR_TO_EXPRESSION_START,
                                   getWhitespaceToRight(tokenStream,
                                           getFirstTokenWithText(ctx.children, EQUAL_OPERATOR).getTokenIndex()));
        }
        ws.addWhitespaceRegion(WhiteSpaceRegions.VAR_DEF_END_TO_LAST_TOKEN,
                               getWhitespaceToLeft(tokenStream, ctx.stop.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.VAR_DEF_END_TO_NEXT_TOKEN,
                               getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getParamWS(CommonTokenStream tokenStream,
                                                  BallerinaParser.ParameterContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.PARAM_DEF_TYPENAME_START_TO_LAST_TOKEN,
                    getWhitespaceToLeft(tokenStream, ctx.typeName().start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.PARAM_DEF_TYPENAME_TO_IDENTIFIER,
                getWhitespaceToRight(tokenStream, ctx.typeName().stop.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.PARAM_DEF_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getActionDefWS(CommonTokenStream tokenStream,
                                                      BallerinaParser.ActionDefinitionContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        boolean isNative = NATIVE_KEYWORD.equals(ctx.getChild(0).getText());
        if (isNative) {
            ws.addWhitespaceRegion(WhiteSpaceRegions.ACTION_DEF_NATIVE_KEYWORD_TO_ACTION_KEYWORD,
                    getWhitespaceToRight(tokenStream,
                            getFirstTokenWithText(ctx.children, NATIVE_KEYWORD).getTokenIndex()));
        }
        ws.addWhitespaceRegion(WhiteSpaceRegions.ACTION_DEF_ACTION_KEYWORD_TO_IDENTIFIER_START,
                getWhitespaceToLeft(tokenStream,
                        ctx.callableUnitSignature().Identifier().getSymbol().getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ACTION_DEF_IDENTIFIER_TO_PARAM_LIST_START,
                getWhitespaceToRight(tokenStream,
                        ctx.callableUnitSignature().Identifier().getSymbol().getTokenIndex()));
        if (ctx.callableUnitSignature().returnParameters() != null) {
            ws.addWhitespaceRegion(WhiteSpaceRegions.ACTION_DEF_PARAM_LIST_END_TO_RETURN_PARAM_START,
                    getWhitespaceToLeft(tokenStream,
                            ctx.callableUnitSignature().returnParameters().start.getTokenIndex()));
        }

        Token throwsToken = getFirstTokenWithText(ctx.callableUnitSignature().children, KEYWORD_THROWS);
        if (!isNative) {
            ws.addWhitespaceRegion(WhiteSpaceRegions.ACTION_DEF_BODY_START_TO_LAST_TOKEN,
                    getWhitespaceToLeft(tokenStream, ctx.callableUnitBody().start.getTokenIndex()));
            ws.addWhitespaceRegion(WhiteSpaceRegions.ACTION_DEF_BODY_START_TO_NEXT_TOKEN,
                    getWhitespaceToRight(tokenStream, ctx.callableUnitBody().start.getTokenIndex()));
            ws.addWhitespaceRegion(WhiteSpaceRegions.ACTION_DEF_BODY_END_TO_NEXT_TOKEN,
                    getWhitespaceToRight(tokenStream, ctx.callableUnitBody().stop.getTokenIndex()));
        }
        return ws;
    }

    public static WhiteSpaceDescriptor getWorkerDeclarationWS(CommonTokenStream tokenStream,
                                                              BallerinaParser.WorkerDeclarationContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.WORKER_DEC_PRECEDING_WHITESPACE,
                        getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.WORKER_DEC_WORKER_KEYWORD_TO_IDENTIFIER,
                getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.WORKER_DEC_IDENTIFIER_TO_BODY_START,
                getWhitespaceToRight(tokenStream, ctx.workerDefinition().Identifier().getSymbol().getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.WORKER_DEC_BODY_START_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, getFirstTokenWithText(ctx.children, OPENING_CURLY_BRACE)
                        .getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.WORKER_DEC_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getTypeNameWS(CommonTokenStream tokenStream,
                                                     BallerinaParser.TypeNameContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_NAME_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_NAME_FOLLOWING_WHITESPACE,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getValueTypeNameWS(CommonTokenStream tokenStream,
                                                     BallerinaParser.ValueTypeNameContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_NAME_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_NAME_FOLLOWING_WHITESPACE,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getBuiltInRefTypeNameWS(CommonTokenStream tokenStream,
                                                               BallerinaParser.BuiltInReferenceTypeNameContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_NAME_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_NAME_FOLLOWING_WHITESPACE,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getRefTypeNameWS(CommonTokenStream tokenStream,
                                                        BallerinaParser.ReferenceTypeNameContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_NAME_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_NAME_FOLLOWING_WHITESPACE,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getNameRefWS(CommonTokenStream tokenStream,
                                                    BallerinaParser.NameReferenceContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.NAME_REF_START_TO_LAST_TOKEN,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        // has a package ref
        if (ctx.Identifier().size() == 2) {
            ws.addWhitespaceRegion(WhiteSpaceRegions.NAME_REF_PACKAGE_NAME_TO_COLON,
                    getWhitespaceToRight(tokenStream, ctx.Identifier().get(0).getSymbol().getTokenIndex()));
            ws.addWhitespaceRegion(WhiteSpaceRegions.NAME_REF_COLON_TO_REF_NAME,
                    getWhitespaceToLeft(tokenStream, ctx.Identifier().get(1).getSymbol().getTokenIndex()));
        }
        ws.addWhitespaceRegion(WhiteSpaceRegions.NAME_REF_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getAssignmentStmtWS(CommonTokenStream tokenStream,
                                                           BallerinaParser.AssignmentStatementContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.ASSIGN_STMT_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        if (getFirstTokenWithText(ctx.children, VAR_KEYWORD) != null) {
            ws.addWhitespaceRegion(WhiteSpaceRegions.ASSIGN_STMT_VAR_KEYWORD_TO_VAR_REF_LIST,
                 getWhitespaceToRight(tokenStream, getFirstTokenWithText(ctx.children, VAR_KEYWORD).getTokenIndex()));
        }
        ws.addWhitespaceRegion(WhiteSpaceRegions.ASSIGN_STMT_VAR_REF_LIST_TO_EQUAL_OPERATOR,
                getWhitespaceToRight(tokenStream, ctx.variableReferenceList().stop.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ASSIGN_STMT_EQUAL_OPERATOR_TO_EXPRESSION_START,
                getWhitespaceToRight(tokenStream,
                        getFirstTokenWithText(ctx.children, EQUAL_OPERATOR).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ASSIGN_STMT_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getAssignmentStmtWS(CommonTokenStream tokenStream,
                                                           BallerinaParser.ExpressionAssignmentStatementContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.ASSIGN_STMT_PRECEDING_WHITESPACE,
                               getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ASSIGN_STMT_VAR_REF_LIST_TO_EQUAL_OPERATOR,
                               getWhitespaceToRight(tokenStream, ctx.variableReferenceList().stop.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ASSIGN_STMT_EQUAL_OPERATOR_TO_EXPRESSION_START,
                               getWhitespaceToRight(tokenStream,
                                                    getFirstTokenWithText
                                                            (ctx.children, EQUAL_OPERATOR).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ASSIGN_STMT_END_TO_NEXT_TOKEN,
                               getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getIfClauseWS(CommonTokenStream tokenStream,
                                                     BallerinaParser.IfClauseContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.IF_CLAUSE_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.IF_CLAUSE_IF_KEYWORD_TO_CONDITION_WRAPPER_START,
                getWhitespaceToLeft(tokenStream, getFirstTokenWithText(ctx.children, STARTING_PAREN).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.IF_CLAUSE_CONDITION_WRAPPER_START_TO_CONDITION,
                getWhitespaceToRight(tokenStream, getFirstTokenWithText(ctx.children, STARTING_PAREN).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.IF_CLAUSE_CONDITION_WRAPPER_END_TO_BODY_START,
                getWhitespaceToRight(tokenStream, getFirstTokenWithText(ctx.children, CLOSING_PAREN).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.IF_CLAUSE_BODY_START_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, getFirstTokenWithText(ctx.children, OPENING_CURLY_BRACE)
                        .getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.IF_CLAUSE_BODY_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getElseIfClauseWS(CommonTokenStream tokenStream,
                                                         BallerinaParser.ElseIfClauseContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.ELSE_IF_CLAUSE_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ELSE_IF_CLAUSE_ELSE_KEYWORD_TO_IF_KEYWORD,
                getWhitespaceToLeft(tokenStream, getFirstTokenWithText(ctx.children, IF_KEYWORD).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ELSE_IF_CLAUSE_IF_KEYWORD_TO_CONDITION_WRAPPER_START,
                getWhitespaceToLeft(tokenStream, getFirstTokenWithText(ctx.children, STARTING_PAREN).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ELSE_IF_CLAUSE_CONDITION_WRAPPER_START_TO_CONDITION,
                getWhitespaceToRight(tokenStream, getFirstTokenWithText(ctx.children, STARTING_PAREN).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ELSE_IF_CLAUSE_CONDITION_WRAPPER_END_TO_BODY_START,
                getWhitespaceToRight(tokenStream, getFirstTokenWithText(ctx.children, CLOSING_PAREN).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ELSE_IF_CLAUSE_BODY_START_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, getFirstTokenWithText(ctx.children, OPENING_CURLY_BRACE)
                        .getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ELSE_IF_CLAUSE_BODY_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getElseClauseWS(CommonTokenStream tokenStream,
                                                       BallerinaParser.ElseClauseContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.ELSE_CLAUSE_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ELSE_CLAUSE_ELSE_KEYWORD_TO_BODY_START,
                getWhitespaceToLeft(tokenStream,
                        getFirstTokenWithText(ctx.children, OPENING_CURLY_BRACE).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ELSE_CLAUSE_BODY_START_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream,
                        getFirstTokenWithText(ctx.children, OPENING_CURLY_BRACE).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ELSE_CLAUSE_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getWhileStmtWS(CommonTokenStream tokenStream,
                                                      BallerinaParser.WhileStatementContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.WHILE_STMT_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.WHILE_STMT_WHILE_KEYWORD_TO_CONDITION_WRAPPER,
                getWhitespaceToLeft(tokenStream,
                        getFirstTokenWithText(ctx.children, STARTING_PAREN).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.WHILE_STMT_CONDITION_WRAPPER_CONDITION_START,
                getWhitespaceToRight(tokenStream,
                        getFirstTokenWithText(ctx.children, STARTING_PAREN).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.WHILE_STMT_CONDITION_WRAPPER_TO_BODY_START,
                getWhitespaceToLeft(tokenStream,
                        getFirstTokenWithText(ctx.children, OPENING_CURLY_BRACE).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.WHILE_STMT_BODY_START_TO_FIRST_TOKEN,
                getWhitespaceToRight(tokenStream,
                        getFirstTokenWithText(ctx.children, OPENING_CURLY_BRACE).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.WHILE_STMT_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getBreakStatementWS(CommonTokenStream tokenStream,
                                                           BallerinaParser.BreakStatementContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.BREAK_STMT_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.BREAK_STMT_BREAK_KEYWORD_TO_END,
                getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.BREAK_STMT_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getContinueStatementWS(CommonTokenStream tokenStream,
                                                              BallerinaParser.ContinueStatementContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.CONTINUE_STMT_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.CONTINUE_STMT_CONTINUE_KEYWORD_TO_END,
                getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.CONTINUE_STMT_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getCatchClauseWS(CommonTokenStream tokenStream,
                                                        BallerinaParser.CatchClauseContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.CATCH_CLAUSE_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.CATCH_CLAUSE_CATCH_KEYWORD_TO_EXCEPTION_WRAPPER,
                getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.CATCH_CLAUSE_EXCEPTION_WRAPPER_START_TO_EXCEPTION_TYPE,
                getWhitespaceToRight(tokenStream,
                        getFirstTokenWithText(ctx.children, STARTING_PAREN).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.CATCH_CLAUSE_EXCEPTION_TYPE_TO_EXCEPTION_IDENTIFIER,
                getWhitespaceToLeft(tokenStream, ctx.Identifier().getSymbol().getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.CATCH_CLAUSE_EXCEPTION_IDENTIFIER_TO_EXCEPTION_WRAPPER_END,
                getWhitespaceToRight(tokenStream, ctx.Identifier().getSymbol().getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.CATCH_CLAUSE_EXCEPTION_WRAPPER_END_TO_BODY_START,
                getWhitespaceToRight(tokenStream,
                        getFirstTokenWithText(ctx.children, CLOSING_PAREN).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.CATCH_CLAUSE_BODY_START_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream,
                        getFirstTokenWithText(ctx.children, OPENING_CURLY_BRACE).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.CATCH_CLAUSE_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getTryClauseWS(CommonTokenStream tokenStream,
                                                      BallerinaParser.TryCatchStatementContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.TRY_CLAUSE_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TRY_CLAUSE_TRY_KEYWORD_TO_BODY_START,
                getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TRY_CLAUSE_BODY_START_TO_NEXT_TOKEN,
                getWhitespaceToRight(
                        tokenStream, getFirstTokenWithText(ctx.children, OPENING_CURLY_BRACE).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TRY_CLAUSE_END_NEXT_TOKEN,
                getWhitespaceToLeft(tokenStream, ctx.catchClauses().start.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getFinallyClauseWS(CommonTokenStream tokenStream,
                                                      BallerinaParser.FinallyClauseContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.FINALLY_CLAUSE_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.FINALLY_CLAUSE_FINALLY_KEYWORD_TO_BODY_START,
                getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.FINALLY_CLAUSE_BODY_START_TO_NEXT_TOKEN,
                getWhitespaceToRight(
                        tokenStream, getFirstTokenWithText(ctx.children, OPENING_CURLY_BRACE).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.FINALLY_CLAUSE_END_TO_NEXT_TOKEN,
                getWhitespaceToLeft(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }


    public static WhiteSpaceDescriptor getThrowStmtWS(CommonTokenStream tokenStream,
                                                      BallerinaParser.ThrowStatementContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.THROW_STMT_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.THROW_STMT_THROW_KEYWORD_TO_EXPRESSION,
                getWhitespaceToLeft(tokenStream, ctx.expression().start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.THROW_STMT_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getReturnStmtWS(CommonTokenStream tokenStream,
                                                       BallerinaParser.ReturnStatementContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.RETURN_STMT_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        if (ctx.expressionList() != null) {
            ws.addWhitespaceRegion(WhiteSpaceRegions.RETURN_STMT_RETURN_KEYWORD_TO_EXPRESSION_LIST,
                    getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        }
        ws.addWhitespaceRegion(WhiteSpaceRegions.RETURN_STMT_END_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.stop.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.RETURN_STMT_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getReplyStmtWS(CommonTokenStream tokenStream,
                                                      BallerinaParser.ReplyStatementContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.REPLY_STMT_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.REPLY_STMT_REPLY_KEYWORD_TO_EXPRESSION,
                getWhitespaceToLeft(tokenStream, ctx.expression().start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.REPLY_STMT_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getWorkerInvokeStmtWS(CommonTokenStream tokenStream,
                                                             BallerinaParser.InvokeWorkerContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.WORKER_INVOKE_STMT_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.WORKER_INVOKE_STMT_EXP_TO_ARROW_OPERATOR,
                getWhitespaceToRight(tokenStream, ctx.expressionList().stop.getTokenIndex()));

        if (ctx.Identifier() != null) {
            ws.addWhitespaceRegion(WhiteSpaceRegions.WORKER_INVOKE_STMT_ARROW_OPERATOR_TO_WORKER_ID,
                    getWhitespaceToLeft(tokenStream, ctx.Identifier().getSymbol().getTokenIndex()));
            ws.addWhitespaceRegion(WhiteSpaceRegions.WORKER_INVOKE_STMT_WORKER_ID_TO_END,
                    getWhitespaceToRight(tokenStream, ctx.Identifier().getSymbol().getTokenIndex()));
        }
        ws.addWhitespaceRegion(WhiteSpaceRegions.WORKER_INVOKE_STMT_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getForkInvokeStmtWS(CommonTokenStream tokenStream,
                                                             BallerinaParser.InvokeForkContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.WORKER_INVOKE_STMT_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.WORKER_INVOKE_STMT_EXP_TO_ARROW_OPERATOR,
                getWhitespaceToRight(tokenStream, ctx.expressionList().stop.getTokenIndex()));

//            ws.addWhitespaceRegion(WhiteSpaceRegions.WORKER_INVOKE_STMT_ARROW_OPERATOR_TO_WORKER_ID,
//                    getWhitespaceToLeft(tokenStream, ctx.getSymbol().getTokenIndex()));
//            ws.addWhitespaceRegion(WhiteSpaceRegions.WORKER_INVOKE_STMT_WORKER_ID_TO_END,
//                    getWhitespaceToRight(tokenStream, ctx.Identifier().getSymbol().getTokenIndex()));

        ws.addWhitespaceRegion(WhiteSpaceRegions.WORKER_INVOKE_STMT_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getWorkerReplyStmtWS(CommonTokenStream tokenStream,
                                                            BallerinaParser.WorkerReplyContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.WORKER_REPLY_STMT_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.WORKER_REPLY_STMT_EXP_TO_ARROW_OPERATOR,
                getWhitespaceToRight(tokenStream, ctx.expressionList().stop.getTokenIndex()));

        if (ctx.Identifier() != null) {
            ws.addWhitespaceRegion(WhiteSpaceRegions.WORKER_REPLY_STMT_ARROW_OPERATOR_TO_WORKER_ID,
                    getWhitespaceToLeft(tokenStream, ctx.Identifier().getSymbol().getTokenIndex()));
            ws.addWhitespaceRegion(WhiteSpaceRegions.WORKER_REPLY_STMT_WORKER_ID_TO_END,
                    getWhitespaceToRight(tokenStream, ctx.Identifier().getSymbol().getTokenIndex()));
        }
        ws.addWhitespaceRegion(WhiteSpaceRegions.WORKER_REPLY_STMT_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getCommentStmtWS(CommonTokenStream tokenStream,
                                                        BallerinaParser.CommentStatementContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.COMMENT_STMT_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.COMMENT_STMT_FOLLOWING_WHITESPACE,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getActionInvocationExprWS(CommonTokenStream tokenStream,
                                                                 BallerinaParser.ActionInvocationContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.ACTION_INVOCATION_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ACTION_INVOCATION_NAME_REF_TO_DOT_OPERATOR,
                getWhitespaceToRight(tokenStream, ctx.nameReference().stop.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ACTION_INVOCATION_DOT_OPERATOR_TO_IDENTIFIER,
                getWhitespaceToLeft(tokenStream, ctx.Identifier().getSymbol().getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ACTION_INVOCATION_IDENTIFIER_TO_EXP_LIST_WRAPPER,
                getWhitespaceToRight(tokenStream, ctx.Identifier().getSymbol().getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ACTION_INVOCATION_EXP_LIST_START_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, getFirstTokenWithText(ctx.children, STARTING_PAREN).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ACTION_INVOCATION_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getFunctionInvocationStmtWS(CommonTokenStream tokenStream,
                                                           BallerinaParser.FunctionInvocationStatementContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.FUNCTION_INVOCATION_EXPR_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.FUNCTION_INVOCATION_EXPR_NAME_REF_TO_ARG_LIST_START,
                getWhitespaceToRight(tokenStream, ctx.variableReference().stop.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.FUNCTION_INVOCATION_EXPR_ARG_LIST_START_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, getFirstTokenWithText(ctx.children, STARTING_PAREN).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.FUNCTION_INVOCATION_EXPR_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, getFirstTokenWithText(ctx.children, CLOSING_PAREN).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.FUNCTION_INVOCATION_STMT_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getFunctionInvocationExprWS(CommonTokenStream tokenStream,
                                                      BallerinaParser.FunctionInvocationReferenceContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.FUNCTION_INVOCATION_EXPR_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.FUNCTION_INVOCATION_EXPR_NAME_REF_TO_ARG_LIST_START,
                getWhitespaceToRight(tokenStream, ctx.variableReference().stop.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.FUNCTION_INVOCATION_EXPR_ARG_LIST_START_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, getFirstTokenWithText(ctx.children, STARTING_PAREN).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.FUNCTION_INVOCATION_EXPR_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getSimpleLiteralWS(CommonTokenStream tokenStream,
                                                          BallerinaParser.SimpleLiteralContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.SIMPLE_LITERAL_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.SIMPLE_LITERAL_FOLLOWING_WHITESPACE,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getArrayLiteralExpWS(CommonTokenStream tokenStream,
                                                            BallerinaParser.ArrayLiteralContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.ARRAY_INIT_EXP_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ARRAY_INIT_EXP_OPENING_SQUARE_BRACE_TO_EXP_LIST_START,
                getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ARRAY_INIT_EXP_EXP_LIST_END_TO_CLOSING_SQUARE_BRACE,
                getWhitespaceToLeft(tokenStream, ctx.stop.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ARRAY_INIT_EXP_FOLLOWING_WHITESPACE,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getMapStructKeyValueWS(CommonTokenStream tokenStream,
                                                              BallerinaParser.MapStructKeyValueContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.MAP_STRUCT_KEY_VAL_EXP_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.MAP_STRUCT_KEY_VAL_EXP_KEY_EXP_TO_COLON,
                getWhitespaceToRight(tokenStream, ctx.expression().get(0).stop.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.MAP_STRUCT_KEY_VAL_EXP_COLON_TO_VAL_EXP,
                getWhitespaceToLeft(tokenStream, ctx.expression().get(1).start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.MAP_STRUCT_KEY_VAL_EXP_FOLLOWING_WHITESPACE,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getMapStructLiteralWS(CommonTokenStream tokenStream,
                                                             BallerinaParser.MapStructLiteralContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.MAP_STRUCT_LITERAL_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.MAP_STRUCT_LITERAL_BODY_START_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.MAP_STRUCT_LITERAL_FOLLOWING_WHITESPACE,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getConnectorInitExpWS(CommonTokenStream tokenStream,
                                                             BallerinaParser.ConnectorInitExpressionContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.CONNECTOR_INIT_EXP_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.CONNECTOR_INIT_EXP_CREATE_KEYWORD_TO_NAME_REF,
                getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.CONNECTOR_INIT_EXP_NAME_REF_TO_EXP_LIST_WRAPPER,
                getWhitespaceToRight(tokenStream, ctx.nameReference().stop.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.CONNECTOR_INIT_EXP_FOLLOWING_WHITESPACE,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getConnectorInitWithFilterExpWS(CommonTokenStream tokenStream,
                                                             BallerinaParser.ConnectorInitExpressionContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.CONNECTOR_INIT_EXP_FILTER_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.CONNECTOR_INIT_EXP_FILTER_CREATE_KEYWORD_TO_NAME_REF,
                getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.CONNECTOR_INIT_EXP_FILTER_NAME_REF_TO_EXP_LIST_WRAPPER,
                getWhitespaceToRight(tokenStream, ctx.nameReference().stop.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.CONNECTOR_INIT_EXP_FILTER_EXP_LIST_WRAPPER_TO_WITH_KEYWORD,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.CONNECTOR_INIT_EXP_FILTER_WITH_KEYWORD_TO_FILTER_EXPRESSION_LIST_START,
                getWhitespaceToLeft(tokenStream, ctx.filterInitExpressionList().start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.
                        CONNECTOR_INIT_EXP_FILTER_FILTER_EXPRESSION_LIST_START_TO_FILTER_EXPRESSION_LIST_END,
                getWhitespaceToLeft(tokenStream, ctx.filterInitExpressionList().stop.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.CONNECTOR_INIT_EXP_FILTER_FOLLOWING_WHITESPACE,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

//    public static WhiteSpaceDescriptor getStructFieldIdentifierWS(CommonTokenStream tokenStream,
//                                                                  BallerinaParser.StructFieldIdentifierContext ctx) {
//        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
//        ws.addWhitespaceRegion(WhiteSpaceRegions.STRUCT_FIELD_IDENTIFIER_PRECEDING_WHITESPACE,
//                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
//        ws.addWhitespaceRegion(WhiteSpaceRegions.STRUCT_FIELD_IDENTIFIER_FOLLOWING_WHITESPACE,
//                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
//        return ws;
//    }
//
//    public static WhiteSpaceDescriptor getSimpleVariableIdentifierWS(CommonTokenStream tokenStream,
//
// BallerinaParser.SimpleVariableIdentifierContext ctx) {
//        return getNameRefWS(tokenStream, ctx.nameReference());
//    }
//
//    public static WhiteSpaceDescriptor getMapArrayVarIdentifierWS(CommonTokenStream tokenStream,
//                                                              BallerinaParser.MapArrayVariableIdentifierContext ctx) {
//        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
//        ws.addWhitespaceRegion(WhiteSpaceRegions.MAP_ARR_VAR_ID_PRECEDING_WHITESPACE,
//                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
//        ws.addWhitespaceRegion(WhiteSpaceRegions.MAP_ARR_VAR_ID_EXP_OPENING_SQUARE_BRACE_PRECEDING,
//                getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
//        ws.addWhitespaceRegion(WhiteSpaceRegions.MAP_ARR_VAR_ID_FOLLOWING_WHITESPACE,
//                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
//        return ws;
//    }

    public static WhiteSpaceDescriptor getTypeCastingExpWS(CommonTokenStream tokenStream,
                                                           BallerinaParser.TypeCastingExpressionContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_CAST_EXP_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_CAST_EXP_TYPE_CAST_START_TO_TYPE_NAME,
                getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_CAST_EXP_TYPE_CAST_END_TO_EXP,
                getWhitespaceToLeft(tokenStream, ctx.expression().start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_CAST_EXP_FOLLOWING_WHITESPACE,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getTypeConversionExpWS(CommonTokenStream tokenStream,
        BallerinaParser.TypeConversionExpressionContext ctx) {
    WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
    ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_CONVERSION_EXP_PRECEDING_WHITESPACE,
        getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
    ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_CONVERSION_EXP_TYPE_CAST_START_TO_TYPE_NAME,
        getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
    ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_CONVERSION_EXP_TYPE_CAST_END_TO_EXP,
        getWhitespaceToLeft(tokenStream, ctx.expression().start.getTokenIndex()));
    ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_CONVERSION_EXP_FOLLOWING_WHITESPACE,
        getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
    return ws;
}

    public static WhiteSpaceDescriptor getUnaryExpWS(CommonTokenStream tokenStream,
                                                     BallerinaParser.UnaryExpressionContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.UNARY_EXP_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.UNARY_EXP_OPERATOR_TO_EXP,
                getWhitespaceToLeft(tokenStream, ctx.expression().start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.UNARY_EXP_FOLLOWING_WHITESPACE,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getFieldDefWS(CommonTokenStream tokenStream,
                                                     BallerinaParser.FieldDefinitionContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.FILED_DEF_TYPE_NAME_TO_ID,
                getWhitespaceToRight(tokenStream, ctx.typeName().stop.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.FILED_DEF_ID_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.Identifier().getSymbol().getTokenIndex()));
        if (ctx.simpleLiteral() != null) {
            ws.addWhitespaceRegion(WhiteSpaceRegions.FILED_DEF_EQUAL_OPERATOR_TO_LITERAL,
                    getWhitespaceToLeft(tokenStream, ctx.simpleLiteral().start.getTokenIndex()));
            ws.addWhitespaceRegion(WhiteSpaceRegions.FILED_DEF_LITERAL_TO_NEXT_TOKEN,
                    getWhitespaceToRight(tokenStream, ctx.simpleLiteral().stop.getTokenIndex()));
        }
        ws.addWhitespaceRegion(WhiteSpaceRegions.FILED_DEF_FOLLOWING_WHITESPACE,
                    getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getBinaryExprWS(CommonTokenStream tokenStream, ParserRuleContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ParserRuleContext leftExpr = getSubExpressionAt(ctx, 0);
        ParserRuleContext rightExpr = getSubExpressionAt(ctx, 1);
        if (leftExpr != null) {
            ws.addWhitespaceRegion(WhiteSpaceRegions.BINARY_EXP_PRECEDING_WHITESPACE,
                    getWhitespaceToLeft(tokenStream, leftExpr.start.getTokenIndex()));
            ws.addWhitespaceRegion(WhiteSpaceRegions.BINARY_EXP_LEFT_EXP_TO_OPERATOR,
                    getWhitespaceToRight(tokenStream, leftExpr.stop.getTokenIndex()));
        }
        if (rightExpr != null) {
            ws.addWhitespaceRegion(WhiteSpaceRegions.BINARY_EXP_OPERATOR_TO_RIGHT_EXP,
                    getWhitespaceToLeft(tokenStream, rightExpr.start.getTokenIndex()));
            ws.addWhitespaceRegion(WhiteSpaceRegions.BINARY_EXP_FOLLOWING_WHITESPACE,
                    getWhitespaceToRight(tokenStream, rightExpr.stop.getTokenIndex()));
        }
        return ws;
    }

    private static ParserRuleContext getSubExpressionAt(ParserRuleContext ctx, int index) {
        ParserRuleContext subExpr = null;
        List<BallerinaParser.ExpressionContext> expressionContexts = null;
        if (ctx instanceof BallerinaParser.BinaryAddSubExpressionContext) {
            expressionContexts = ((BallerinaParser.BinaryAddSubExpressionContext) ctx).expression();
        } else if (ctx instanceof BallerinaParser.BinaryAndExpressionContext) {
            expressionContexts = ((BallerinaParser.BinaryAndExpressionContext) ctx).expression();
        } else if (ctx instanceof BallerinaParser.BinaryCompareExpressionContext) {
            expressionContexts = ((BallerinaParser.BinaryCompareExpressionContext) ctx).expression();
        } else if (ctx instanceof BallerinaParser.BinaryDivMulModExpressionContext) {
            expressionContexts = ((BallerinaParser.BinaryDivMulModExpressionContext) ctx).expression();
        } else if (ctx instanceof BallerinaParser.BinaryEqualExpressionContext) {
            expressionContexts = ((BallerinaParser.BinaryEqualExpressionContext) ctx).expression();
        } else if (ctx instanceof BallerinaParser.BinaryOrExpressionContext) {
            expressionContexts = ((BallerinaParser.BinaryOrExpressionContext) ctx).expression();
        } else if (ctx instanceof BallerinaParser.BinaryPowExpressionContext) {
            expressionContexts = ((BallerinaParser.BinaryPowExpressionContext) ctx).expression();
        }
        if (expressionContexts != null && expressionContexts.size() >= index + 1) {
            subExpr = expressionContexts.get(index);
        }
        return subExpr;
    }

    public static WhiteSpaceDescriptor getTransformStmtWS(CommonTokenStream tokenStream,
                                                          BallerinaParser.TransformStatementContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.TRANSFORM_STMT_PRECEDING_WHITESPACE,
                               getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TRANSFORM_STMT_TO_BODY_START,
                               getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TRANSFORM_STMT_BODY_START_TO_FIRST_CHILD,
                               getWhitespaceToRight(tokenStream,
                                          getFirstTokenWithText(ctx.children, OPENING_CURLY_BRACE).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TRANSFORM_STMT_END_TO_NEXT_TOKEN,
                               getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getActionInvocationStmtWS(CommonTokenStream tokenStream,
                                                                 BallerinaParser.ActionInvocationStatementContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.ACTION_INVOCATION_STMT_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ACTION_INVOCATION_STMT_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getTransactionWS(CommonTokenStream tokenStream,
                                                        BallerinaParser.TransactionStatementContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.TRANSACTION_STMT_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TRANSACTION_STMT_TRANSACTION_KEYWORD_TO_BODY_START,
                getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TRANSACTION_STMT_BODY_START_TO_FIRST_TOKEN,
                getWhitespaceToRight(tokenStream,
                        getFirstTokenWithText(ctx.children, OPENING_CURLY_BRACE).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TRANSACTION_STMT_BODY_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream,
                        getFirstTokenWithText(ctx.children, CLOSING_CURLY_BRACE).getTokenIndex()));
        if (ctx.transactionHandlers().committedClause() != null) {
            ws.addChildDescriptor(COMMITTED_CLAUSE, getCommittedClauseWS(tokenStream,
                    ctx.transactionHandlers().committedClause()));
        }
        if (ctx.transactionHandlers().abortedClause() != null) {
            ws.addChildDescriptor(ABORTED_CLAUSE, getAbortedClauseWS(tokenStream,
                    ctx.transactionHandlers().abortedClause()));
        }
        return ws;
    }

    public static WhiteSpaceDescriptor getCommittedClauseWS(CommonTokenStream tokenStream,
                                                            BallerinaParser.CommittedClauseContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.COMMITTED_STMT_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.COMMITTED_STMT_TRANSACTION_KEYWORD_TO_BODY_START,
                getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.COMMITTED_STMT_BODY_START_TO_FIRST_TOKEN,
                getWhitespaceToRight(tokenStream,
                        getFirstTokenWithText(ctx.children, OPENING_CURLY_BRACE).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.COMMITTED_STMT_BODY_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream,
                        getFirstTokenWithText(ctx.children, CLOSING_CURLY_BRACE).getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getAbortedClauseWS(CommonTokenStream tokenStream,
                                                          BallerinaParser.AbortedClauseContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.ABORTED_STMT_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ABORTED_STMT_TRANSACTION_KEYWORD_TO_BODY_START,
                getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ABORTED_STMT_BODY_START_TO_FIRST_TOKEN,
                getWhitespaceToRight(tokenStream,
                        getFirstTokenWithText(ctx.children, OPENING_CURLY_BRACE).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ABORTED_STMT_BODY_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream,
                        getFirstTokenWithText(ctx.children, CLOSING_CURLY_BRACE).getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getAbortStmtWS(CommonTokenStream tokenStream,
                                                      BallerinaParser.AbortStatementContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.ABORT_STMT_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ABORT_STMT_ABORT_KEYWORD_TO_END,
                getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ABORT_STMT_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getNamespaceDeclarationWS(CommonTokenStream tokenStream,
                                                                 BallerinaParser.NamespaceDeclarationContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.NAMESPACE_DEC_IMPORT_KEYWORD_TO_PKG_NAME_START,
                getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.NAMESPACE_DEC_PKG_NAME_END_TO_NEXT,
                getWhitespaceToRight(tokenStream, ctx.QuotedStringLiteral().getSymbol().getTokenIndex()));

        // if (as Identifier) is present, there can be five whitespace regions
        if (ctx.Identifier() != null) {
            ws.addWhitespaceRegion(WhiteSpaceRegions.NAMESPACE_DEC_AS_KEYWORD_TO_IDENTIFIER,
                    getWhitespaceToRight(tokenStream,
                            getFirstTokenWithText(ctx.children, KEYWORD_AS).getTokenIndex()));
            ws.addWhitespaceRegion(WhiteSpaceRegions.NAMESPACE_DEC_IDENTIFIER_TO_IMPORT_DEC_END,
                    getWhitespaceToRight(tokenStream, ctx.Identifier().getSymbol().getTokenIndex()));
        }

        ws.addWhitespaceRegion(WhiteSpaceRegions.NAMESPACE_DEC_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getForkJoinStatementWS(CommonTokenStream tokenStream,
                                                              BallerinaParser.ForkJoinStatementContext ctx) {

        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.FORK_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.FORK_KEYWORD_TO_BODY_START,
                getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.FORK_START_TO_FIRST_CHILD,
                getWhitespaceToRight(tokenStream,
                        getFirstTokenWithText(ctx.children, OPENING_CURLY_BRACE).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.FORK_BODY_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream,
                        getFirstTokenWithText(ctx.children, CLOSING_CURLY_BRACE).getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getJoinClauseWS(CommonTokenStream tokenStream,
                                                       BallerinaParser.JoinClauseContext ctx) {

        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.JOIN_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.JOIN_KEYWORD_TO_BODY_START,
                getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));

        ws.addWhitespaceRegion(WhiteSpaceRegions.JOIN_CONDITION_WRAPPER_END_TO_PARAM_WRAPPER,
                getWhitespaceToRight(tokenStream,
                        getFirstTokenWithText(ctx.children, CLOSING_PAREN).getTokenIndex()));

        int identifierIndex = ctx.Identifier().getSymbol().getTokenIndex();
        ws.addWhitespaceRegion(WhiteSpaceRegions.JOIN_PARAM_TYPE_TO_PARAM_IDENTIFIER,
                getWhitespaceToLeft(tokenStream, identifierIndex));
        ws.addWhitespaceRegion(WhiteSpaceRegions.JOIN_PARAM_IDENTIFIER_TO_PARAM_WRAPPER_END,
                getWhitespaceToRight(tokenStream, identifierIndex));
        ws.addWhitespaceRegion(WhiteSpaceRegions.JOIN_PARAM_WRAPPER_END_TO_JOIN_START,
                getWhitespaceToLeft(tokenStream,
                        getFirstTokenWithText(ctx.children, OPENING_CURLY_BRACE).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.JOIN_START_TO_FIRST_CHILD,
                getWhitespaceToRight(tokenStream,
                        getFirstTokenWithText(ctx.children, OPENING_CURLY_BRACE).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.JOIN_BODY_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream,
                        getFirstTokenWithText(ctx.children, CLOSING_CURLY_BRACE).getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getTimeoutClauseWS(CommonTokenStream tokenStream,
                                                          BallerinaParser.TimeoutClauseContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.TIMEOUT_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TIMEOUT_KEYWORD_TO_BODY_START,
                getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TIMEOUT_CONDITION_WRAPPER_START_TO_CONDITION,
                getWhitespaceToRight(tokenStream,
                        getFirstTokenWithText(ctx.children, STARTING_PAREN).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TIMEOUT_CONDITION_WRAPPER_END_TO_PARAM_WRAPPER,
                getWhitespaceToRight(tokenStream,
                        getFirstTokenWithText(ctx.children, CLOSING_PAREN).getTokenIndex()));
        int identifierIndex = ctx.Identifier().getSymbol().getTokenIndex();
        ws.addWhitespaceRegion(WhiteSpaceRegions.TIMEOUT_PARAM_TYPE_TO_PARAM_IDENTIFIER,
                getWhitespaceToLeft(tokenStream, identifierIndex));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TIMEOUT_PARAM_IDENTIFIER_TO_PARAM_WRAPPER_END,
                getWhitespaceToRight(tokenStream, identifierIndex));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TIMEOUT_PARAM_WRAPPER_END_TO_TIMEOUT_START,
                getWhitespaceToLeft(tokenStream,
                        getFirstTokenWithText(ctx.children, OPENING_CURLY_BRACE).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TIMEOUT_START_TO_FIRST_CHILD,
                getWhitespaceToRight(tokenStream,
                        getFirstTokenWithText(ctx.children, OPENING_CURLY_BRACE).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TIMEOUT_BODY_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream,
                        getFirstTokenWithText(ctx.children, CLOSING_CURLY_BRACE).getTokenIndex()));
        return ws;
    }


    public static WhiteSpaceDescriptor getJoinWorkerWS(CommonTokenStream tokenStream, TerminalNode t) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.JOIN_WORKER_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, t.getSymbol().getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.JOIN_WORKER_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, t.getSymbol().getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getJoinConditionWS(CommonTokenStream tokenStream,
                                                          BallerinaParser.JoinConditionsContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.JOIN_CONDITION_WRAPPER_TO_JOIN_CONDITION,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.JOIN_CONDITION_TYPE_TO_JOIN_CONDITION_COUNT,
                getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.JOIN_CONDITION_END_TO_CONDITION_WRAPPER_END,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getFieldBasedVarRedWS(CommonTokenStream tokenStream,
                                                             BallerinaParser.FieldVariableReferenceContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.FIELD_VAR_REF_EXPR_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.variableReference().start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.FIELD_VAR_REF_EXPR_VAR_REF_TO_DOT_OPERATOR,
                getWhitespaceToRight(tokenStream, ctx.variableReference().stop.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.FIELD_VAR_REF_EXPR_DOT_OPERATOR_TO_FIELD_NAME_START,
                getWhitespaceToLeft(tokenStream, ctx.field().Identifier().getSymbol().getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.FIELD_VAR_REF_EXPR_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.field().stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getIndexBasedVarRefWS(CommonTokenStream tokenStream,
                                                             BallerinaParser.MapArrayVariableReferenceContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.INDEX_VAR_REF_EXPR_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.variableReference().start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.INDEX_VAR_REF_EXPR_VAR_REF_TO_INDEX_EXPR_WRAPPER,
                getWhitespaceToRight(tokenStream, ctx.variableReference().stop.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.INDEX_VAR_REF_EXPR_INDEX_EXPR_WRAPPER_TO_INDEX_EXPR_START,
                getWhitespaceToLeft(tokenStream, ctx.index().expression().start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.INDEX_VAR_REF_EXPR_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.index().stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getXMLAttributeWS(CommonTokenStream tokenStream, AttributeContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.XML_ATTRIBUTE_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.XML_ATTRIBUTE_KEY_EXP_TO_COLON,
                getWhitespaceToRight(tokenStream, ctx.xmlQualifiedName().stop.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.XML_ATTRIBUTE_EQUAL_OPERATOR_TO_VAL_EXP,
                getWhitespaceToLeft(tokenStream, ctx.xmlQuotedString().start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.XML_ATTRIBUTE_FOLLOWING_WHITESPACE,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }
}
