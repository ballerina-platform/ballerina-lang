/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
define(['./ballerina-file-editor', './ballerina-view', './message-manager', './canvas',
        './connector-declaration-view', './function-definition-view', './reply-statement-view',
        './resource-definition-view', './return-statement-view', './service-definition-view', './throw-statement-view',
        './while-statement-view', './worker-declaration-view', './source-view', './action-invocation-statement-view',
        './service-preview-view', './simple-statement-view', './block-statement-view', './compound-statement-view', './backend'],
    function (BallerinaFileEditor, BallerinaView, Canvas,MessageManager,
              ConnectorDeclarationView, FunctionDefinitionView, ReplyStatementView,
              ResourceDefinitionView, ReturnStatementView, ServiceDefinitionView, ThrowStatementView,
              WhileStatementView, WorkerDeclarationView, SourceView, ActionInvocationStatementView,
              ServicePreviewView, SimpleStatementView, BlockStatementView, CompoundStatementView, Backend) {
        return  {
            BallerinaView: BallerinaView,
            BallerinaFileEditor: BallerinaFileEditor,
            MessageManager: MessageManager,
            Canvas: Canvas,
            ConnectionDeclarationView: ConnectorDeclarationView,
            FunctionDefinitionView: FunctionDefinitionView,
            ReplyStatementView: ReplyStatementView,
            ResourceDefinitionView: ResourceDefinitionView,
            ReturnStatementView: ReturnStatementView,
            ServiceDefinitionView: ServiceDefinitionView,
            ThrowStatementView: ThrowStatementView,
            WhileStatementView: WhileStatementView,
            WorkerDeclarationView: WorkerDeclarationView,
            SourceView: SourceView,
            ActionInvocationStatementView: ActionInvocationStatementView,
            ServicePreviewView: ServicePreviewView,
            SimpleStatementView: SimpleStatementView,
            BlockStatementView: BlockStatementView,
            CompoundStatementView: CompoundStatementView,
            Backend : Backend
        }
    });

