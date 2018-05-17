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

const { workspace, commands, window, ExtensionContext, debug, extensions } = require('vscode');
const { LanguageClient, LanguageClientOptions, ServerOptions } = require('vscode-languageclient');
const { exec } = require('child_process');
const path = require('path');
const fs = require('fs');
const { getLSService } = require('./serverStarter');
const { activate:activateRenderer, errored:rendererErrored } = require('./renderer');
const msgs = require('./messages');

let oldConfig;

const debugConfigResolver = {
	resolveDebugConfiguration(folder, config) {
		if (!config['ballerina.home']) {
			// If ballerina.home is not defined in in debug config get it from workspace configs
			const workspaceConfig = workspace.getConfiguration('ballerina');
			if (workspaceConfig.home) {
				config['ballerina.home'] = workspaceConfig.home;
			}
		}

		if (config['ballerina.home']) {
			if (fs.readdirSync(config['ballerina.home']).indexOf('bin') < 0) {
				window.showWarningMessage(msgs.NO_BIN_IN_HOME, action).then((selection) => {
					if (action === selection) {
						commands.executeCommand('workbench.action.openGlobalSettings');
					}
				});
			}
		} else {
			window.showWarningMessage(msgs.DEBUG_HOME_NOT_SET, action).then((selection) => {
				if (action === selection) {
					commands.executeCommand('workbench.action.openGlobalSettings');
				}
			});
			window.showErrorMessage(msg);
		}

		return config;
	}
}

function showHomeNotSetWarning() {
	const homeSetWarning = msgs.HOME_NOT_SET;
	const action = 'Open Settings';

	window.showWarningMessage(homeSetWarning, action).then((selection) => {
		if (action === selection) {
			commands.executeCommand('workbench.action.openGlobalSettings');
		}
	});
}

function checkHome(homePath) {
	try {
		if (fs.readdirSync(path.join(homePath, 'lib', 'resources')).indexOf('composer') > -1) {
			return true;
		}
	} catch(e) {
		// could not read the home path. Let the warning show
	}

	const homeSetWarning = msgs.HOME_INCORRECT;
	const action = 'Open Settings';	
	window.showWarningMessage(homeSetWarning, action).then((selection) => {
		if (action === selection) {
			commands.executeCommand('workbench.action.openGlobalSettings');
		}
	});
	return false;
}

function checkVersion(homePath) {
	if (!fs.existsSync(path.join(homePath, 'bin', 'ballerina'))) {
		return;
	}

	exec(`${path.join(homePath, 'bin', 'ballerina')} version`, (err, stdout, stderr) => {
		const platformVersion = stderr.toString().trim();
		const extensionVersion = 'Ballerina ' + extensions.getExtension('ballerina.ballerina').packageJSON.version

		if (platformVersion != extensionVersion) {
			console.log(`Version mismatch. Platform version: ${platformVersion} Extension version: ${extensionVersion}`);
			window.showWarningMessage(msgs.VERSION_MISMATCH);
		}
	});
}

exports.activate = function(context) {
	// Options to control the language client
	const clientOptions = {
		documentSelector: [{ scheme: 'file', language: 'ballerina' }],
	}

	const config = workspace.getConfiguration('ballerina');
	oldConfig = config;
	// in windows class path seperated by ';'
	//const sep = process.platform === 'win32' ? ';' : ':';

	if (!config.home) {
		// home path is not set. The plugin can't activate. Show warning and exit.
		rendererErrored(context);
		showHomeNotSetWarning();
		return;
	}

	checkVersion(config.home);

	if (!checkHome(config.home)) {
		rendererErrored(context);
		return;
	}

	if (!config.showLSErrors) {
		clientOptions.outputChannel = dropOutputChannel;
	}

	const serverOptions = getLSService;

	const langClientDisposable = new LanguageClient('ballerina-vscode', 'Ballerina vscode lanugage client',
		serverOptions, clientOptions).start();
	
	activateRenderer(context);

	// Push the disposable to the context's subscriptions so that the 
	// client can be deactivated on extension deactivation
	context.subscriptions.push(langClientDisposable);
	context.subscriptions.push(debug.registerDebugConfigurationProvider(
		'ballerina', debugConfigResolver));
}

workspace.onDidChangeConfiguration(params => {
	const newConfig = workspace.getConfiguration('ballerina');
	if (newConfig.home != oldConfig.home) {
		const msg = msgs.HOME_CHANGED;
		const action = 'Restart Now';
		window.showWarningMessage(msg, action).then((selection) => {
			if (action === selection) {
				commands.executeCommand('workbench.action.reloadWindow');
			}
		});
	}

	if (newConfig.showLSErrors != oldConfig.showLSErrors) {
		const msg = msgs.SHOW_LSERRORS_CHANGED;
		const action = 'Restart Now';
		window.showWarningMessage(msg, action).then((selection) => {
			if (action === selection) {
				commands.executeCommand('workbench.action.reloadWindow');
			}
		});
	}

	if (newConfig.debugLog != oldConfig.debugLog) {
		const msg = msgs.DEBUG_LOG_CHANGED;
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
