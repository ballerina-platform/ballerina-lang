/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import * as NodeTypes from '../js/ballerina/utils/fragment-utils/node-types';

export default {
    createHTTPServiceDef: () => {
        return NodeTypes.createTopLevelNodeFragment(
            `
                service<http> service1 {
                    resource echo1 (http:Request req, http:Response res) {

                    }
                }
            `);
    },
    createWSServiceDef: () => {
        return NodeTypes.createTopLevelNodeFragment(
            `
                service<ws> service1 {
                    resource onOpen(ws:Connection conn) {
            
                    }
                    resource onTextMessage(ws:Connection conn, ws:TextFrame frame) {
            
                    }
                    resource onClose(ws:Connection conn, ws:CloseFrame frame) {
            
                    }
                }
            `);
    },
    createJMSServiceDef: () => {
        return NodeTypes.createTopLevelNodeFragment(
            `
                service<jms> service1 {
                    resource echo1 (jms:JMSMessage request) {

                    }
                }
            `);
    },
    createFSServiceDef: () => {
        return NodeTypes.createTopLevelNodeFragment(
            `
            service<fs> service1 {
                resource echo1 (fs:FileSystemEvent m) {

                }
            }
            `);
    },
    createFTPServiceDef: () => {
        return NodeTypes.createTopLevelNodeFragment(
            `
            service<ftp> service1 {
                resource echo1 (ftp:FTPServerEvent m) {

                }
            }
            `);
    },
    createMainFunction: () => {
        return NodeTypes.createTopLevelNodeFragment(`
            function main(string[] args) {

            }
        `);
    },
    createFunction: () => {
        return NodeTypes.createTopLevelNodeFragment(`
                    function function1() {

                    }
                `);
    },
    createConnector: () => {
        return NodeTypes.createTopLevelNodeFragment(`
            connector ClientConnector(string url) {
                action action1(){

                }
            }
        `);
    },
    createConnectorAction: () => {
        return NodeTypes.createConnectorActionFragment(`
            action action1(){

            }
        `);
    },
    createHTTPResource: () => {
        return NodeTypes.createServiceResourceFragment(`
            resource echo1 (http:Request req, http:Response res) {

            }
        `);
    },
    createFSResource: () => {
        return NodeTypes.createServiceResourceFragment(`
            resource echo1 (fs:FileSystemEvent m) {

            }
        `);
    },
    createFTPResource: () => {
        return NodeTypes.createServiceResourceFragment(`
            resource echo1 (ftp:FTPServerEvent m) {

            }
        `);
    },
    createJMSResource: () => {
        return NodeTypes.createServiceResourceFragment(`
            resource echo1 (jms:JMSMessage request) {

            }
        `);
    },
    createStruct: () => {
        return NodeTypes.createTopLevelNodeFragment(`
            struct struct1 {

            }
        `);
    },
    createTransformer: () => {
        return NodeTypes.createTopLevelNodeFragment(`
            transformer <Source a, Target b> newTransformer (){

            }
        `);
    },
    createWorkerFragment: () => {
        return NodeTypes.createWorkerFragment(`
            worker worker1 {
            }
        `);
    },
    createDefaultWorkerFragment: () => {
        return NodeTypes.createWorkerFragment(`
            worker default {
            }
        `);
    },
    createAnnotation: () => {
        return NodeTypes.createTopLevelNodeFragment(`
            annotation annotation1 {
            }
        `);
    },
    createAssignmentStmt: () => {
        return NodeTypes.createStatementFragment('var a = 1;');
    },
    createBindStmt: () => {
        return NodeTypes.createStatementFragment('bind _connector with _endpoint;');
    },
    createVarDefStmt: () => {
        return NodeTypes.createStatementFragment('int a = 1;');
    },
    createIf: () => {
        return NodeTypes.createStatementFragment(`
            if (true) {

            } else {
            
            }
        `);
    },
    createIfElse: () => {
        return NodeTypes.createStatementFragment(`
            if (true) {

            } else if (true) {
            
            } else {
            
            }
        `);
    },
    createInvocation: () => {
        return NodeTypes.createStatementFragment(`
            invokeFunction(arg1);
        `);
    },
    createWhile: () => {
        return NodeTypes.createStatementFragment(`
            while(true) {

            }
        `);
    },
    createBreak: () => {
        return NodeTypes.createStatementFragment(`
            break;
        `);
    },
    createNext: () => {
        return NodeTypes.createStatementFragment(`
            next;
        `);
    },
    createTry: () => {
        return NodeTypes.createStatementFragment(`
            try {

            } catch (error err) {

            } finally {
            
            }
        `);
    },
    createThrow: () => {
        return NodeTypes.createStatementFragment(`
            throw e;
        `);
    },
    createReturn: () => {
        return NodeTypes.createStatementFragment(`
            return m;
        `);
    },
    createWorkerInvocation: () => {
        return NodeTypes.createStatementFragment(`
            m -> worker1 ;
        `);
    },
    createWorkerReceive: () => {
        return NodeTypes.createStatementFragment(`
            m <- worker1 ;
        `);
    },
    createTransaction: () => {
        return NodeTypes.createStatementFragment(`
            transaction {

            } failed {

            } aborted {
            
            } committed {
            
            }
        `);
    },
    createAbort: () => {
        return NodeTypes.createStatementFragment(`
            abort;
        `);
    },
    createRetry: () => {
        return NodeTypes.createTransactionFailedFragment(`
            retry 3;
        `);
    },
    createForkJoin: () => {
        return NodeTypes.createStatementFragment(`
            fork {
                worker worker1 {
                }
                worker worker2 {
                }
            } join(all)(map results) {
            
            } timeout(100)(map results1) {
            
            }
        `);
    },
    createXmlns: () => {
        return NodeTypes.createStatementFragment(`
            xmlns "namespace.uri" as xn;
        `);
    },
    createEnum: () => {
        return NodeTypes.createTopLevelNodeFragment(`
            enum name {
                ENUMERATOR
            }
        `);
    },
};
