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
define(['./action-definition-view', './ballerina-file-editor', './ballerina-view', './canvas',
        './connector-declaration-view', './function-definition-view', './reply-statement-view',
        './resource-definition-view', './return-statement-view', './service-definition-view', './throw-statement-view',
        './while-statement-view', './worker-declaration-view', './source-view','./get-action-statement-view' ],
    function (ActionDefinitionView, BallerinaFileEditor, BallerinaView, Canvas,
              ConnectorDeclarationView, FunctionDefinitionView, ReplyStatementView,
              ResourceDefinitionView, ReturnStatementView, ServiceDefinitionView, ThrowStatementView,
              WhileStatementView, WorkerDeclarationView, SourceView,GetActionStatementView) {
        return  {
            BallerinaView: BallerinaView,
            ActionDefinitionView: ActionDefinitionView,
            BallerinaFileEditor: BallerinaFileEditor,
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
            GetActionStatementView: GetActionStatementView
        }
    });

