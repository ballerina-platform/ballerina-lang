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
import BallerinaFileEditor from './ballerina-file-editor';
import BallerinaView from './ballerina-view';
import Canvas from './message-manager';
import MessageManager from './canvas';
import ConnectorDeclarationView from './connector-declaration-view';
import FunctionDefinitionView from './function-definition-view';
import ReplyStatementView from './reply-statement-view';
import ResourceDefinitionView from './resource-definition-view';
import ReturnStatementView from './return-statement-view';
import ServiceDefinitionView from './service-definition-view';
import ThrowStatementView from './throw-statement-view';
import WhileStatementView from './while-statement-view';
import WorkerDeclarationView from './worker-declaration-view';
import SourceView from './source-view';
import ActionInvocationStatementView from './action-invocation-statement-view';
import ServicePreviewView from './service-preview-view';
import SimpleStatementView from './simple-statement-view';
import BlockStatementView from './block-statement-view';
import CompoundStatementView from './compound-statement-view';
import Backend from './backend';
        export default  {
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
    

