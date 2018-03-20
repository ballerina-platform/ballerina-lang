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

import FragmentUtils from '../../src/plugins/ballerina/utils/fragment-utils';

export default {
    createHTTPServiceDef: () => {
        return FragmentUtils.createTopLevelNodeFragment(
            `
                service<http> service1 {
                    resource echo1 (http:Connection conn, http:Request req) {

                    }
                }
            `);
    },
    createWSServiceDef: () => {
        return FragmentUtils.createTopLevelNodeFragment(
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
        return FragmentUtils.createTopLevelNodeFragment(
            `
                service<jms> service1 {
                    resource echo1 (jms:JMSMessage request) {

                    }
                }
            `);
    },
    createFSServiceDef: () => {
        return FragmentUtils.createTopLevelNodeFragment(
            `
            service<fs> service1 {
                resource echo1 (fs:FileSystemEvent m) {

                }
            }
            `);
    },
    createFTPServiceDef: () => {
        return FragmentUtils.createTopLevelNodeFragment(
            `
            service<ftp> service1 {
                resource echo1 (ftp:FTPServerEvent m) {

                }
            }
            `);
    },
    createMainFunction: () => {
        return FragmentUtils.createTopLevelNodeFragment(`
            function main(string[] args) {

            }
        `);
    },
    createFunction: () => {
        return FragmentUtils.createTopLevelNodeFragment(`
                    function function1() {

                    }
                `);
    },
    createConnector: () => {
        return FragmentUtils.createTopLevelNodeFragment(`
            connector ClientConnector(string url) {
                action action1(){

                }
            }
        `);
    },
    createConnectorAction: () => {
        return FragmentUtils.createConnectorActionFragment(`
            action action1(){

            }
        `);
    },
    createHTTPResource: () => {
        return FragmentUtils.createServiceResourceFragment(`
            resource echo1 (http:Connection conn, http:Request req) {

            }
        `);
    },
    createFSResource: () => {
        return FragmentUtils.createServiceResourceFragment(`
            resource echo1 (fs:FileSystemEvent m) {

            }
        `);
    },
    createFTPResource: () => {
        return FragmentUtils.createServiceResourceFragment(`
            resource echo1 (ftp:FTPServerEvent m) {

            }
        `);
    },
    createJMSResource: () => {
        return FragmentUtils.createServiceResourceFragment(`
            resource echo1 (jms:JMSMessage request) {

            }
        `);
    },
    createStruct: () => {
        return FragmentUtils.createTopLevelNodeFragment(`
            struct struct1 {

            }
        `);
    },
    createTransformer: () => {
        return FragmentUtils.createTopLevelNodeFragment(`
            transformer <Source a, Target b> newTransformer (){

            }
        `);
    },
    createWorkerFragment: () => {
        return FragmentUtils.createWorkerFragment(`
            worker worker1 {
            }
        `);
    },
    createDefaultWorkerFragment: () => {
        return FragmentUtils.createWorkerFragment(`
            worker default {
            }
        `);
    },
    createAnnotation: () => {
        return FragmentUtils.createTopLevelNodeFragment(`
            annotation annotation1 {
            }
        `);
    },
    createAssignmentStmt: () => {
        return FragmentUtils.createStatementFragment('var a = 1;');
    },
    createBindStmt: () => {
        return FragmentUtils.createStatementFragment('bind _connector with _endpoint;');
    },
    createVarDefStmt: () => {
        return FragmentUtils.createStatementFragment('int a = 1;');
    },
    createIf: () => {
        return FragmentUtils.createStatementFragment(`
            if (true) {

            } else {
            
            }
        `);
    },
    createIfElse: () => {
        return FragmentUtils.createStatementFragment(`
            if (true) {

            } else if (true) {
            
            } else {
            
            }
        `);
    },
    createInvocation: () => {
        return FragmentUtils.createStatementFragment(`
            invokeFunction(arg1);
        `);
    },
    createWhile: () => {
        return FragmentUtils.createStatementFragment(`
            while(true) {

            }
        `);
    },
    createBreak: () => {
        return FragmentUtils.createStatementFragment(`
            break;
        `);
    },
    createNext: () => {
        return FragmentUtils.createStatementFragment(`
            next;
        `);
    },
    createTry: () => {
        return FragmentUtils.createStatementFragment(`
            try {

            } catch (error err) {

            } finally {
            
            }
        `);
    },
    createThrow: () => {
        return FragmentUtils.createStatementFragment(`
            throw e;
        `);
    },
    createReturn: () => {
        return FragmentUtils.createStatementFragment(`
            return m;
        `);
    },
    createWorkerInvocation: () => {
        return FragmentUtils.createStatementFragment(`
            m -> worker1 ;
        `);
    },
    createWorkerReceive: () => {
        return FragmentUtils.createStatementFragment(`
            m <- worker1 ;
        `);
    },
    createTransaction: () => {
        return FragmentUtils.createStatementFragment(`
            transaction {

            } failed {

            }
        `);
    },
    createAbort: () => {
        return FragmentUtils.createStatementFragment(`
            abort;
        `);
    },
    createForkJoin: () => {
        return FragmentUtils.createStatementFragment(`
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
        return FragmentUtils.createStatementFragment(`
            xmlns "namespace.uri" as xn;
        `);
    },
    createEnum: () => {
        return FragmentUtils.createTopLevelNodeFragment(`
            enum name {
                ENUMERATOR
            }
        `);
    },
};
