'use strict';
/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

import { expect } from 'chai';
import * as path from 'path';
import { ExtendedLangClient } from "../../src/core/extended-language-client";
import { getServerOptions } from "../../src/server/server";
import { getBallerinaHome } from "../test-util";
import { Uri } from "vscode";

let langClient: ExtendedLangClient;

suite("Language Server Tests", function () {
    this.timeout(10000);

    suiteSetup((done: MochaDone): any => {
        langClient = new ExtendedLangClient(
            'ballerina-vscode',
            'Ballerina LS Client',
            getServerOptions(getBallerinaHome()),
            { documentSelector: [{ scheme: 'file', language: 'ballerina' }] },
            false
        );
        langClient.start();
        done();
    });


    test("Test Language Server Start", function (done): void {
        langClient.onReady().then(() => {
            done();
        }, () => {
            done(new Error("Language Server start failed"));
        }).catch((err) => {
            done(new Error("Language Server start failed"));
        });
    });

    test("Test getAST", function (done): void {
        langClient.onReady().then(() => {
            const filePath = path.join(getBallerinaHome(),'examples','hello-world','hello_world.bal');
            let uri = Uri.file(filePath.toString());
            langClient.getAST(uri).then((response) => {
                expect(response).to.contain.keys('ast','parseSuccess');
                done();
            }, (reason) => {
                done(reason);
            });
        });
    });

    test("Test Language Server Stop", function (done): void {
        langClient.stop().then(() => {
            done();
        }, () => {
            done(new Error("Language Server stop failed"));
        });
    });
});
