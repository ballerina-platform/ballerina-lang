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

const { workspace, commands, window, ExtensionContext, debug } = require('vscode');
const { LanguageClient, LanguageClientOptions, ServerOptions } = require('vscode-languageclient');
const path = require('path');
const fs = require('fs');

let oldConfig;

const debugConfigResolver = {
	resolveDebugConfiguration(folder, config) {
		if (!config['ballerina.sdk']) {
			// If ballerina.sdk is not defined in in debug config get it from workspace configs
			const workspaceConfig = workspace.getConfiguration('ballerina');
			if (workspaceConfig.sdk) {
				config['ballerina.sdk'] = workspaceConfig.sdk;
			}
		}

		if (config['ballerina.sdk']) {
			if (fs.readdirSync(config['ballerina.sdk']).indexOf('bin') < 0) {
				const msg = "Couldn't find a bin directory inside the configured sdk path. Please set ballerina.sdk correctly."
				window.showErrorMessage(msg);
			}
		} else {
			const msg = "To start the debug server please set ballerina.sdk."
			window.showErrorMessage(msg);
		}

		return config;
	}
}

exports.activate = function(context) {
	// The server is implemented in java
	let serverModule = context.asAbsolutePath(path.join('server-build', 'langserver.jar'));
	const main = 'org.ballerinalang.langserver.launchers.stdio.Main';

	// Options to control the language client
	const clientOptions = {
		documentSelector: [{ scheme: 'file', language: 'ballerina' }],
	}

	const config = workspace.getConfiguration('ballerina');
	oldConfig = config;
	// in windows class path seperated by ';'
	const sep = process.platform === 'win32' ? ';' : ':';
	
	if (config.sdk) {
		// sdk path is set in configurations
		serverModule = context.asAbsolutePath(path.join('server-build', 'langserver-no-bal-deps.jar'));
		serverModule += (sep + path.join(config.sdk, 'bre', 'lib', '*'));
	}

	if (!config.showLSErrors) {
		clientOptions.outputChannel = dropOutputChannel;
	}

	const args = ['-cp', serverModule, main];
	// If the extension is launched in debug mode then the debug server options are used
	// Otherwise the run options are used
	const serverOptions = {
		run: { command: 'java', args },
		debug: {
			command: 'java', 
			args: [
				'-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005,quiet=y',
				...args,
			],
		},
	}

	const forceDebug = (process.env.LSDEBUG === "true");

	const langClientDisposable = new LanguageClient('ballerina-vscode', 'Ballerina vscode lanugage client',
		serverOptions, clientOptions, forceDebug).start();
	
	// Push the disposable to the context's subscriptions so that the 
	// client can be deactivated on extension deactivation
	context.subscriptions.push(langClientDisposable);
	context.subscriptions.push(debug.registerDebugConfigurationProvider(
		'ballerina', debugConfigResolver));
}

workspace.onDidChangeConfiguration(params => {
	const newConfig = workspace.getConfiguration('ballerina');
	if (newConfig.sdk != oldConfig.sdk) {
		const msg = 'Ballerina sdk path configuration changed. Please restart vscode for changes to take effect.';
		const action = 'Restart Now';
		window.showWarningMessage(msg, action).then((selection) => {
			if (action === selection) {
				commands.executeCommand('workbench.action.reloadWindow');
			}
		});
	}

	if (newConfig.showLSErrors != oldConfig.showLSErrors) {
		const msg = 'Configuration for displaying output from language server changed. Please restart vscode for changes to take effect.';
		const action = 'Restart Now';
		window.showWarningMessage(msg, action).then((selection) => {
			if (action === selection) {
				commands.executeCommand('workbench.action.reloadWindow');
			}
		});
	}

	oldConfig = newConfig;
});

// This channel ignores(drops) all requests it receives.
// So the user won't see any output sent through this channel
const dropOutputChannel = {
	name: 'dropOutputChannel',
	append: () => {},
	appendLine: () => {},
	clear: () => {},
	show: () => {},
	show: () => {},
	hide: () => {},
	dispose: () => {},
}
