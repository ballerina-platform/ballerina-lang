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
    public static final String OPENNING_CURLEY_BRACE = "{";
    public static final String SYMBOL_COLON = ":";
    public static final String NATIVE_KEYWORD = "native";
    public static final String KEYWORD_THROWS = "throws";
    public static final String EQUAL_OPERATOR = "=";

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
        if (ctx.callableUnitSignature().returnParameters() != null) {
            ws.addWhitespaceRegion(WhiteSpaceRegions.FUNCTION_DEF_PARAM_LIST_END_TO_RETURN_PARAM_START,
                    getWhitespaceToLeft(tokenStream,
                            ctx.callableUnitSignature().returnParameters().start.getTokenIndex()));
        }
        Token throwsToken = getFirstTokenWithText(ctx.callableUnitSignature().children, KEYWORD_THROWS);
        if (throwsToken != null) {
            ws.addWhitespaceRegion(WhiteSpaceRegions.FUNCTION_DEF_RETURN_PARAM_END_TO_THROWS_KEYWORD,
                    getWhitespaceToLeft(tokenStream, throwsToken.getTokenIndex()));
            ws.addWhitespaceRegion(WhiteSpaceRegions.FUNCTION_DEF_THROWS_KEYWORD_TO_EXCEPTION_KEYWORD,
                    getWhitespaceToRight(tokenStream, throwsToken.getTokenIndex()));
        }
        if (!isNative) {
            ws.addWhitespaceRegion(WhiteSpaceRegions.FUNCTION_DEF_BODY_START_TO_LAST_TOKEN,
                    getWhitespaceToLeft(tokenStream, ctx.callableUnitBody().start.getTokenIndex()));
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
        ws.addWhitespaceRegion(WhiteSpaceRegions.CONNECTOR_DEF_BODY_END_TO_NEXT_TOKEN,
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
        ws.addWhitespaceRegion(WhiteSpaceRegions.STRUCT_DEF_BODY_END_TO_NEXT_TOKEN,
                getWhitespaceToLeft(tokenStream, ctx.structBody().stop.getTokenIndex()));
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
        ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_MAP_DEF_TYPEMAPPER_KEYWORD_TO_IDENTIFIER,
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
        ws.addWhitespaceRegion(WhiteSpaceRegions.ANNOTATION_DEF_BODY_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.annotationBody().stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getAttachmentPointWS(CommonTokenStream tokenStream,
                                                            BallerinaParser.AttachmentPointContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.ANNOTATION_ATTACHMENT_POINT_PRECEDING_WS,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ANNOTATION_ATTACHMENT_POINT_TAILING_WS,
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
                                                      BallerinaParser.GlobalVariableDefinitionStatementContext ctx) {
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
        if (throwsToken != null) {
            ws.addWhitespaceRegion(WhiteSpaceRegions.ACTION_DEF_RETURN_PARAM_END_TO_THROWS_KEYWORD,
                    getWhitespaceToLeft(tokenStream, throwsToken.getTokenIndex()));
            ws.addWhitespaceRegion(WhiteSpaceRegions.ACTION_DEF_THROWS_KEYWORD_TO_EXCEPTION_KEYWORD,
                    getWhitespaceToRight(tokenStream, throwsToken.getTokenIndex()));
        }
        if (!isNative) {
            ws.addWhitespaceRegion(WhiteSpaceRegions.ACTION_DEF_BODY_START_TO_LAST_TOKEN,
                    getWhitespaceToLeft(tokenStream, ctx.callableUnitBody().start.getTokenIndex()));
            ws.addWhitespaceRegion(WhiteSpaceRegions.ACTION_DEF_BODY_END_TO_NEXT_TOKEN,
                    getWhitespaceToRight(tokenStream, ctx.callableUnitBody().stop.getTokenIndex()));
        }
        return ws;
    }

    public static WhiteSpaceDescriptor getWorkerDeclarationWS(CommonTokenStream tokenStream,
                                                              BallerinaParser.WorkerDeclarationContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.WORKER_DEC_WORKER_KEYWORD_TO_IDENTIFIER,
                        getWhitespaceToRight(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.WORKER_DEC_IDENTIFIER_TO_PARAM_LIST_START,
                getWhitespaceToRight(tokenStream, ctx.Identifier().get(0).getSymbol().getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.WORKER_DEC_PARAM_LIST_START_TO_PARAM_TYPE,
                getWhitespaceToRight(tokenStream, getFirstTokenWithText(ctx.children, STARTING_PAREN).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.WORKER_DEC_PARAM_TYPE_TO_PARAM_IDENTIFIER,
                getWhitespaceToLeft(tokenStream, ctx.Identifier().get(1).getSymbol().getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.WORKER_DEC_PARAM_END_TO_PARAM_LIST_END,
                getWhitespaceToRight(tokenStream, ctx.Identifier().get(1).getSymbol().getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.WORKER_DEC_PARAM_LIST_END_TO_BODY_START,
                getWhitespaceToRight(tokenStream, getFirstTokenWithText(ctx.children, CLOSING_PAREN).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.WORKER_DEC_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getTypeNameWS(CommonTokenStream tokenStream,
                                                     BallerinaParser.TypeNameContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_NAME_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_NAME_TAILING_WHITESPACE,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getValueTypeNameWS(CommonTokenStream tokenStream,
                                                     BallerinaParser.ValueTypeNameContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_NAME_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_NAME_TAILING_WHITESPACE,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getBuiltInRefTypeNameWS(CommonTokenStream tokenStream,
                                                               BallerinaParser.BuiltInReferenceTypeNameContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_NAME_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_NAME_TAILING_WHITESPACE,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getRefTypeNameWS(CommonTokenStream tokenStream,
                                                        BallerinaParser.ReferenceTypeNameContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_NAME_PRECEDING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.TYPE_NAME_TAILING_WHITESPACE,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getNameRefWS(CommonTokenStream tokenStream,
                                                    BallerinaParser.NameReferenceContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.NAME_REF_START_TO_LAST_TOKEN,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        // has a package ref
        if (ctx.Identifier().size() == 2){
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
        ws.addWhitespaceRegion(WhiteSpaceRegions.ASSIGN_STMT_VAR_REF_LIST_TO_EQUAL_OPERATOR,
                getWhitespaceToRight(tokenStream, ctx.variableReferenceList().stop.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ASSIGN_STMT_EQUAL_OPERATOR_TO_EXPRESSION_START,
                getWhitespaceToRight(tokenStream,
                        getFirstTokenWithText(ctx.children, EQUAL_OPERATOR).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.ASSIGN_STMT_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }

    public static WhiteSpaceDescriptor getIfClauseWS(CommonTokenStream tokenStream,
                                                     BallerinaParser.IfClauseContext ctx) {
        WhiteSpaceDescriptor ws = new WhiteSpaceDescriptor();
        ws.addWhitespaceRegion(WhiteSpaceRegions.IF_CLAUSE_PRECEFING_WHITESPACE,
                getWhitespaceToLeft(tokenStream, ctx.start.getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.IF_CLAUSE_IF_KEYWORD_TO_CONDITION_WRAPPER_START,
                getWhitespaceToLeft(tokenStream, getFirstTokenWithText(ctx.children, STARTING_PAREN).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.IF_CLAUSE_CONDITION_WRAPPER_END_TO_BODY_START,
                getWhitespaceToRight(tokenStream, getFirstTokenWithText(ctx.children, CLOSING_PAREN).getTokenIndex()));
        ws.addWhitespaceRegion(WhiteSpaceRegions.IF_CLAUSE_BODY_END_TO_NEXT_TOKEN,
                getWhitespaceToRight(tokenStream, ctx.stop.getTokenIndex()));
        return ws;
    }
}
