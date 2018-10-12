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
import { ExtensionContext } from 'vscode';
import { activate as activateDiagram } from './diagram'; 
import { activate as activateBBE } from './bbe';
import { ballerinaExtInstance } from './core';
import { activateDebugConfigProvider } from './debugger';

export function activate(context: ExtensionContext): void {
	ballerinaExtInstance.setContext(context);
	ballerinaExtInstance.init();
	// start the features.
	// Enable Ballerina diagram
	activateDiagram(ballerinaExtInstance);
	// Enable Ballerina by examples
	activateBBE(ballerinaExtInstance);
	// Enable Ballerina Debug Config Provider
	activateDebugConfigProvider(ballerinaExtInstance);
}