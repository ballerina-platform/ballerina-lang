/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.plugins.idea;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.antlr.jetbrains.adaptor.lexer.ANTLRLexerAdaptor;
import org.antlr.jetbrains.adaptor.lexer.PSIElementTypeFactory;
import org.antlr.jetbrains.adaptor.lexer.RuleIElementType;
import org.antlr.jetbrains.adaptor.lexer.TokenIElementType;
import org.antlr.jetbrains.adaptor.parser.ANTLRParserAdaptor;
import org.antlr.jetbrains.adaptor.psi.ANTLRPsiNode;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.ballerinalang.plugins.idea.grammar.BallerinaLexer;
import org.ballerinalang.plugins.idea.grammar.BallerinaParser;
import org.ballerinalang.plugins.idea.psi.ActionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ActionInvocationNode;
import org.ballerinalang.plugins.idea.psi.AliasNode;
import org.ballerinalang.plugins.idea.psi.AnnotationAttributeNode;
import org.ballerinalang.plugins.idea.psi.AnnotationAttachmentNode;
import org.ballerinalang.plugins.idea.psi.AnnotationAttributeValueNode;
import org.ballerinalang.plugins.idea.psi.AnnotationDefinitionNode;
import org.ballerinalang.plugins.idea.psi.AssignmentStatementNode;
import org.ballerinalang.plugins.idea.psi.AttachmentPointNode;
import org.ballerinalang.plugins.idea.psi.BallerinaFile;
import org.ballerinalang.plugins.idea.psi.ConnectorInitExpressionNode;
import org.ballerinalang.plugins.idea.psi.DefinitionNode;
import org.ballerinalang.plugins.idea.psi.ExpressionAssignmentStatementNode;
import org.ballerinalang.plugins.idea.psi.ExpressionVariableDefinitionStatementNode;
import org.ballerinalang.plugins.idea.psi.ForkJoinStatementNode;
import org.ballerinalang.plugins.idea.psi.FunctionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.FunctionInvocationNode;
import org.ballerinalang.plugins.idea.psi.GlobalVariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.IfElseStatementNode;
import org.ballerinalang.plugins.idea.psi.JoinConditionNode;
import org.ballerinalang.plugins.idea.psi.MapStructKeyValueNode;
import org.ballerinalang.plugins.idea.psi.MapStructLiteralNode;
import org.ballerinalang.plugins.idea.psi.NameReferenceNode;
import org.ballerinalang.plugins.idea.psi.CompilationUnitNode;
import org.ballerinalang.plugins.idea.psi.ConnectorBodyNode;
import org.ballerinalang.plugins.idea.psi.ConnectorDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ConstantDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ExpressionListNode;
import org.ballerinalang.plugins.idea.psi.ExpressionNode;
import org.ballerinalang.plugins.idea.psi.CallableUnitBodyNode;
import org.ballerinalang.plugins.idea.psi.FunctionInvocationStatementNode;
import org.ballerinalang.plugins.idea.psi.ImportDeclarationNode;
import org.ballerinalang.plugins.idea.psi.ReplyStatementNode;
import org.ballerinalang.plugins.idea.psi.ReturnParametersNode;
import org.ballerinalang.plugins.idea.psi.ReturnStatementNode;
import org.ballerinalang.plugins.idea.psi.SimpleLiteralNode;
import org.ballerinalang.plugins.idea.psi.PackageDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.PackagePathNode;
import org.ballerinalang.plugins.idea.psi.ParameterListNode;
import org.ballerinalang.plugins.idea.psi.ParameterNode;
import org.ballerinalang.plugins.idea.psi.ResourceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ReturnTypeListNode;
import org.ballerinalang.plugins.idea.psi.ServiceBodyNode;
import org.ballerinalang.plugins.idea.psi.StructBodyNode;
import org.ballerinalang.plugins.idea.psi.ThrowStatementNode;
import org.ballerinalang.plugins.idea.psi.TransformStatementBodyNode;
import org.ballerinalang.plugins.idea.psi.TransformStatementNode;
import org.ballerinalang.plugins.idea.psi.TriggerWorkerNode;
import org.ballerinalang.plugins.idea.psi.TypeNameNode;
import org.ballerinalang.plugins.idea.psi.ServiceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.StatementNode;
import org.ballerinalang.plugins.idea.psi.StructDefinitionNode;
import org.ballerinalang.plugins.idea.psi.FieldDefinitionNode;
import org.ballerinalang.plugins.idea.psi.TypeMapperBodyNode;
import org.ballerinalang.plugins.idea.psi.TypeMapperNode;
import org.ballerinalang.plugins.idea.psi.ValueTypeNameNode;
import org.ballerinalang.plugins.idea.psi.VariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.VariableReferenceListNode;
import org.ballerinalang.plugins.idea.psi.VariableReferenceNode;
import org.ballerinalang.plugins.idea.psi.WorkerDeclarationNode;
import org.ballerinalang.plugins.idea.psi.WorkerInterationStatementNode;
import org.ballerinalang.plugins.idea.psi.WorkerReplyNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.ballerinalang.plugins.idea.grammar.BallerinaLexer.*;

public class BallerinaParserDefinition implements ParserDefinition {

    private static final IFileElementType FILE = new IFileElementType(BallerinaLanguage.INSTANCE);

    public static TokenIElementType ID;

    static {
        PSIElementTypeFactory.defineLanguageIElementTypes(BallerinaLanguage.INSTANCE,
                BallerinaParser.tokenNames, BallerinaParser.ruleNames);
        List<TokenIElementType> tokenIElementTypes =
                PSIElementTypeFactory.getTokenIElementTypes(BallerinaLanguage.INSTANCE);
        ID = tokenIElementTypes.get(BallerinaLexer.Identifier);
    }

    public static final TokenSet IDENTIFIER = PSIElementTypeFactory.createTokenSet(BallerinaLanguage.INSTANCE,
            Identifier);

    public static final TokenSet COMMENTS = PSIElementTypeFactory.createTokenSet(BallerinaLanguage.INSTANCE,
            LINE_COMMENT);

    private static final TokenSet WHITESPACE = PSIElementTypeFactory.createTokenSet(BallerinaLanguage.INSTANCE, WS);

    public static final TokenSet STRING_LITERALS = PSIElementTypeFactory.createTokenSet(BallerinaLanguage.INSTANCE,
            QuotedStringLiteral, BacktickStringLiteral);

    public static final TokenSet NUMBER = PSIElementTypeFactory.createTokenSet(BallerinaLanguage.INSTANCE,
            IntegerLiteral, FloatingPointLiteral);

    public static final TokenSet KEYWORDS = PSIElementTypeFactory.createTokenSet(BallerinaLanguage.INSTANCE,
            ABORT, ABORTED, ACTION, ALL, ANNOTATION, ANY, AS, ATTACH, BREAK, CATCH, COMMITTED, CONNECTOR, CONST,
            CONTINUE, CREATE, ELSE, FINALLY, FORK, FUNCTION, IF, IMPORT, ITERATE, JOIN, NATIVE, PACKAGE, PARAMETER,
            REPLY, RESOURCE, RETURN, SERVICE, SOME, STRUCT, THROW, TIMEOUT, TRANSACTION, TRANSFORM, TRY, TYPEMAPPER,
            WHILE, WORKER, BOOLEAN, INT, FLOAT, STRING, BLOB, MESSAGE, MAP, XML, XML_DOCUMENT, JSON, DATATABLE,
            BooleanLiteral, NullLiteral);

    public static final TokenSet BRACES_AND_OPERATORS = PSIElementTypeFactory.createTokenSet(BallerinaLanguage.INSTANCE,
            SEMI, COMMA, SENDARROW, RECEIVEARROW, TILDE, COLON);

    public static final TokenSet BAD_CHARACTER = PSIElementTypeFactory.createTokenSet(BallerinaLanguage.INSTANCE,
            ERRCHAR);

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        BallerinaLexer lexer = new BallerinaLexer(null);
        return new ANTLRLexerAdaptor(BallerinaLanguage.INSTANCE, lexer);
    }

    @NotNull
    public PsiParser createParser(final Project project) {
        final BallerinaParser parser = new BallerinaParser(null);
        return new ANTLRParserAdaptor(BallerinaLanguage.INSTANCE, parser) {
            @Override
            protected ParseTree parse(Parser parser, IElementType root) {
                // Start rule depends on root passed in; sometimes we want to create an ID node etc...
                // Eg: if (root instanceof IFileElementType) { }
                return ((BallerinaParser) parser).compilationUnit();
            }
        };
    }

    @NotNull
    public TokenSet getWhitespaceTokens() {
        return WHITESPACE;
    }

    @NotNull
    public TokenSet getCommentTokens() {
        return COMMENTS;
    }

    @NotNull
    public TokenSet getStringLiteralElements() {
        return STRING_LITERALS;
    }

    @Override
    public IFileElementType getFileNodeType() {
        return FILE;
    }

    public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }

    public PsiFile createFile(FileViewProvider viewProvider) {
        return new BallerinaFile(viewProvider);
    }

    @NotNull
    public PsiElement createElement(ASTNode node) {
        IElementType elementType = node.getElementType();
        if (elementType instanceof TokenIElementType) {
            return new ANTLRPsiNode(node);
        }
        if (!(elementType instanceof RuleIElementType)) {
            return new ANTLRPsiNode(node);
        }

        RuleIElementType ruleElType = (RuleIElementType) elementType;
        switch (ruleElType.getRuleIndex()) {
            case BallerinaParser.RULE_functionDefinition:
                return new FunctionDefinitionNode(node);
            case BallerinaParser.RULE_callableUnitBody:
                return new CallableUnitBodyNode(node);
            case BallerinaParser.RULE_nameReference:
                return new NameReferenceNode(node);
            case BallerinaParser.RULE_variableReference:
                return new VariableReferenceNode(node);
            case BallerinaParser.RULE_variableDefinitionStatement:
                return new VariableDefinitionNode(node);
            case BallerinaParser.RULE_parameter:
                return new ParameterNode(node);
            case BallerinaParser.RULE_actionDefinition:
                return new ActionDefinitionNode(node);
            case BallerinaParser.RULE_connectorBody:
                return new ConnectorBodyNode(node);
            case BallerinaParser.RULE_connectorDefinition:
                return new ConnectorDefinitionNode(node);
            case BallerinaParser.RULE_resourceDefinition:
                return new ResourceDefinitionNode(node);
            case BallerinaParser.RULE_packageName:
                return new PackageNameNode(node);
            case BallerinaParser.RULE_packagePath:
                return new PackagePathNode(node);
            case BallerinaParser.RULE_expressionList:
                return new ExpressionListNode(node);
            case BallerinaParser.RULE_expression:
                return new ExpressionNode(node);
            case BallerinaParser.RULE_functionInvocationStatement:
                return new FunctionInvocationStatementNode(node);
            case BallerinaParser.RULE_compilationUnit:
                return new CompilationUnitNode(node);
            case BallerinaParser.RULE_packageDeclaration:
                return new PackageDeclarationNode(node);
            case BallerinaParser.RULE_annotationAttachment:
                return new AnnotationAttachmentNode(node);
            case BallerinaParser.RULE_serviceBody:
                return new ServiceBodyNode(node);
            case BallerinaParser.RULE_importDeclaration:
                return new ImportDeclarationNode(node);
            case BallerinaParser.RULE_statement:
                return new StatementNode(node);
            case BallerinaParser.RULE_typeName:
                return new TypeNameNode(node);
            case BallerinaParser.RULE_actionInvocation:
                return new ActionInvocationNode(node);
            case BallerinaParser.RULE_constantDefinition:
                return new ConstantDefinitionNode(node);
            case BallerinaParser.RULE_structDefinition:
                return new StructDefinitionNode(node);
            case BallerinaParser.RULE_simpleLiteral:
                return new SimpleLiteralNode(node);
            case BallerinaParser.RULE_alias:
                return new AliasNode(node);
            case BallerinaParser.RULE_parameterList:
                return new ParameterListNode(node);
            case BallerinaParser.RULE_typeMapperDefinition:
                return new TypeMapperNode(node);
            case BallerinaParser.RULE_typeMapperBody:
                return new TypeMapperBodyNode(node);
            case BallerinaParser.RULE_fieldDefinition:
                return new FieldDefinitionNode(node);
            case BallerinaParser.RULE_returnTypeList:
                return new ReturnTypeListNode(node);
            case BallerinaParser.RULE_connectorInitExpression:
                return new ConnectorInitExpressionNode(node);
            case BallerinaParser.RULE_serviceDefinition:
                return new ServiceDefinitionNode(node);
            case BallerinaParser.RULE_valueTypeName:
                return new ValueTypeNameNode(node);
            case BallerinaParser.RULE_annotationDefinition:
                return new AnnotationDefinitionNode(node);
            case BallerinaParser.RULE_annotationAttributeValue:
                return new AnnotationAttributeValueNode(node);
            case BallerinaParser.RULE_structBody:
                return new StructBodyNode(node);
            case BallerinaParser.RULE_returnParameters:
                return new ReturnParametersNode(node);
            case BallerinaParser.RULE_attachmentPoint:
                return new AttachmentPointNode(node);
            case BallerinaParser.RULE_definition:
                return new DefinitionNode(node);
            case BallerinaParser.RULE_ifElseStatement:
                return new IfElseStatementNode(node);
            case BallerinaParser.RULE_assignmentStatement:
                return new AssignmentStatementNode(node);
            case BallerinaParser.RULE_variableReferenceList:
                return new VariableReferenceListNode(node);
            case BallerinaParser.RULE_mapStructLiteral:
                return new MapStructLiteralNode(node);
            case BallerinaParser.RULE_globalVariableDefinition:
                return new GlobalVariableDefinitionNode(node);
            case BallerinaParser.RULE_mapStructKeyValue:
                return new MapStructKeyValueNode(node);
            case BallerinaParser.RULE_forkJoinStatement:
                return new ForkJoinStatementNode(node);
            case BallerinaParser.RULE_workerInteractionStatement:
                return new WorkerInterationStatementNode(node);
            case BallerinaParser.RULE_functionInvocation:
                return new FunctionInvocationNode(node);
            case BallerinaParser.RULE_returnStatement:
                return new ReturnStatementNode(node);
            case BallerinaParser.RULE_throwStatement:
                return new ThrowStatementNode(node);
            case BallerinaParser.RULE_replyStatement:
                return new ReplyStatementNode(node);
            case BallerinaParser.RULE_annotationAttribute:
                return new AnnotationAttributeNode(node);
            case BallerinaParser.RULE_transformStatement:
                return new TransformStatementNode(node);
            case BallerinaParser.RULE_transformStatementBody:
                return new TransformStatementBodyNode(node);
            case BallerinaParser.RULE_expressionAssignmentStatement:
                return new ExpressionAssignmentStatementNode(node);
            case BallerinaParser.RULE_expressionVariableDefinitionStatement:
                return new ExpressionVariableDefinitionStatementNode(node);
            case BallerinaParser.RULE_workerReply:
                return new WorkerReplyNode(node);
            case BallerinaParser.RULE_triggerWorker:
                return new TriggerWorkerNode(node);
            case BallerinaParser.RULE_workerDeclaration:
                return new WorkerDeclarationNode(node);
            case BallerinaParser.RULE_joinConditions:
                return new JoinConditionNode(node);
            default:
                return new ANTLRPsiNode(node);
        }
    }
}
