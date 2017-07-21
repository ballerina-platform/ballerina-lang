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

package org.ballerinalang.composer.service.workspace.langserver.util.resolvers;

import org.ballerinalang.composer.service.workspace.langserver.SymbolInfo;
import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItem;
import org.ballerinalang.composer.service.workspace.langserver.util.resolvers.parsercontext.ParserRuleStatementContextResolver;
import org.ballerinalang.composer.service.workspace.langserver.util.resolvers.parsercontext.ParserRuleTriggerWorkerContext;
import org.ballerinalang.composer.service.workspace.langserver.util.resolvers.parsercontext.ParserRuleTypeNameContextResolver;
import org.ballerinalang.composer.service.workspace.langserver.util.resolvers.parsercontext.ParserRuleVariableDefinitionStatementContextResolver;
import org.ballerinalang.composer.service.workspace.langserver.util.resolvers.parsercontext.ParserRuleWorkerReplyContext;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;
import org.ballerinalang.model.AnnotationAttachment;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BallerinaAction;
import org.ballerinalang.model.BallerinaConnectorDef;
import org.ballerinalang.model.GlobalScope;
import org.ballerinalang.model.Resource;
import org.ballerinalang.model.Service;
import org.ballerinalang.model.statements.VariableDefStmt;
import org.ballerinalang.util.parser.BallerinaParser;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * ResolveCommandExecutor will accept the command to execute
 */
public class ResolveCommandExecutor {
    private static final HashMap<Class, AbstractItemResolver> resolvers = new HashMap<>();
    private static final DefaultResolver DEFAULT_RESOLVER = new DefaultResolver();

    /**
     * Constructor for the Resolve command executor
     */
    public ResolveCommandExecutor() {
        StatementContextResolver statementContextResolver = new StatementContextResolver();
        VariableDefinitionStatementContextResolver variableDefinitionStatementContextResolver =
                new VariableDefinitionStatementContextResolver();
        PackageNameContextResolver packageNameContextResolver = new PackageNameContextResolver();
        AnnotationAttachmentContextResolver annotationAttachmentContextResolver =
                new AnnotationAttachmentContextResolver();
        ParameterContextResolver parameterContextResolver = new ParameterContextResolver();
        ServiceContextResolver serviceContextResolver = new ServiceContextResolver();
        AnnotationAttachmentResolver annotationAttachmentResolver = new AnnotationAttachmentResolver();
        ConnectorDefinitionContextResolver connectorDefinitionContextResolver =
                new ConnectorDefinitionContextResolver();
        ConnectorActionContextResolver connectorActionContextResolver = new ConnectorActionContextResolver();
        ResourceContextResolver resourceContextResolver = new ResourceContextResolver();
        TopLevelResolver topLevelResolver = new TopLevelResolver();
        CallableUnitBodyContextResolver callableUnitBodyContextResolver = new CallableUnitBodyContextResolver();

        // Parser rule context based resolvers
        ParserRuleStatementContextResolver parserRuleStatementContextResolver =
                new ParserRuleStatementContextResolver();
        ParserRuleVariableDefinitionStatementContextResolver parserRuleVariableDefStatementContextResolver
                = new ParserRuleVariableDefinitionStatementContextResolver();
        ParserRuleTriggerWorkerContext parserRuleTriggerWorkerContext = new ParserRuleTriggerWorkerContext();
        ParserRuleWorkerReplyContext parserRuleWorkerReplyContext = new ParserRuleWorkerReplyContext();
        ParserRuleTypeNameContextResolver parserRuleTypeNameContextResolver = new ParserRuleTypeNameContextResolver();
        GlobalScopeResolver globalScopeResolver = new GlobalScopeResolver();

        // Here we use the resolver class as the key for statement context resolver. This is in order to simplify and
        // since there are many statements in Ballerina model which can be handled similarly
        resolvers.put(StatementContextResolver.class, statementContextResolver);
        resolvers.put(BallerinaParser.PackageNameContext.class, packageNameContextResolver);
        resolvers.put(BallerinaParser.ImportDeclarationContext.class, packageNameContextResolver);
        resolvers.put(BallerinaParser.AnnotationAttachmentContext.class, annotationAttachmentContextResolver);
        resolvers.put(BallerinaParser.GlobalVariableDefinitionContext.class, topLevelResolver);
        resolvers.put(BallerinaParser.ParameterContext.class, parameterContextResolver);
        resolvers.put(null, topLevelResolver);
        resolvers.put(Service.class, serviceContextResolver);
        resolvers.put(BallerinaConnectorDef.class, connectorDefinitionContextResolver);
        resolvers.put(BallerinaAction.class, connectorActionContextResolver);
        resolvers.put(VariableDefStmt.class, variableDefinitionStatementContextResolver);
        resolvers.put(AnnotationAttachment.class, annotationAttachmentResolver);
        resolvers.put(Resource.class, resourceContextResolver);
        resolvers.put(CallableUnitBodyContextResolver.class, callableUnitBodyContextResolver);
        resolvers.put(GlobalScope.class, globalScopeResolver);
        resolvers.put(BLangPackage.class, globalScopeResolver);

        // Parser Rule Context Resolvers
        resolvers.put(BallerinaParser.StatementContext.class, parserRuleStatementContextResolver);
        resolvers.put(BallerinaParser.VariableDefinitionStatementContext.class,
                parserRuleVariableDefStatementContextResolver);
        resolvers.put(BallerinaParser.TriggerWorkerContext.class, parserRuleTriggerWorkerContext);
        resolvers.put(BallerinaParser.WorkerReplyContext.class, parserRuleWorkerReplyContext);
        resolvers.put(BallerinaParser.TypeNameContext.class, parserRuleTypeNameContextResolver);
    }

    /**
     * Resolve the completion items based on the criteria
     * @param resolveCriteria - resolving criteria
     * @param dataModel - SuggestionsFilterDataModel
     * @param symbols - Symbols list
     * @return {@link ArrayList}
     */
    public ArrayList<CompletionItem> resolveCompletionItems
    (Class resolveCriteria, SuggestionsFilterDataModel dataModel, ArrayList<SymbolInfo> symbols) {
        AbstractItemResolver itemResolver = resolvers.get(resolveCriteria);
        if (itemResolver == null) {
            return DEFAULT_RESOLVER.resolveItems(dataModel, symbols, resolvers);
        } else {
            return itemResolver.resolveItems(dataModel, symbols , resolvers);
        }
    }
}
