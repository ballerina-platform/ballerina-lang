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
import ServiceDefinitionVisitor from './service-definition-visitor';
import BallerinaASTRootVisitor from './ballerina-ast-root-visitor';
import ResourceDefinitionVisitor from './resource-definition-visitor';
import FunctionDefinitionVisitor from './function-definition-visitor';
import TryCatchStatementVisitor from './try-catch-statement-visitor';
        export default  {
            ServiceDefinitionVisitor: ServiceDefinitionVisitor,
            ResourceDefinitionVisitor: ResourceDefinitionVisitor,
            BallerinaASTRootVisitor: BallerinaASTRootVisitor,
            FunctionDefinitionVisitor: FunctionDefinitionVisitor,
            TryCatchStatementVisitor: TryCatchStatementVisitor
        }
    

