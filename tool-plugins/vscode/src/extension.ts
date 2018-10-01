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
import {
	ExtensionContext, debug,
	DebugConfigurationProvider, WorkspaceFolder, DebugConfiguration, ProviderResult
} from 'vscode';
import { } from 'vscode-debugadapter';
import { BallerinaPluginConfig, getPluginConfig } from './config';
import { activate as activateRenderer, errored as rendererErrored } from './renderer';
import { activate as activateSamples } from './examples';
import BallerinaExtension from './core/ballerina-extension';

const debugConfigResolver: DebugConfigurationProvider = {
	resolveDebugConfiguration(folder: WorkspaceFolder, config: DebugConfiguration)
		: ProviderResult<DebugConfiguration> {
		if (!config['ballerina.home']) {
			// If ballerina.home is not defined in in debug config get it from workspace configs
			const workspaceConfig: BallerinaPluginConfig = getPluginConfig();
			if (workspaceConfig.home) {
				config['ballerina.home'] = workspaceConfig.home;
			}
		}
		return config;
	}
};

export function activate(context: ExtensionContext): void {

	BallerinaExtension.setContext(context);
	BallerinaExtension.init()
		.then(success => {
			// start the features.
			activateRenderer(context, BallerinaExtension.langClient!);
			activateSamples(context, BallerinaExtension.langClient!);
		}, () => {
			// If home is not valid show error on design page.
			if (!BallerinaExtension.isValidBallerinaHome()) {
				rendererErrored(context);
			}
			BallerinaExtension.showPluginActivationError();
		})
		.catch(error => {
			// unknown error
			BallerinaExtension.showPluginActivationError();
		});

	/*if (!config.debugLog) {
		clientOptions.outputChannel = dropOutputChannel;
	}*/

	context.subscriptions.push(debug.registerDebugConfigurationProvider('ballerina', debugConfigResolver));
}