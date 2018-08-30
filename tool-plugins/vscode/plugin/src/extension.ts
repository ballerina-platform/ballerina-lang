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
import { window, commands, ExtensionContext, extensions, workspace, Extension, debug,
	DebugConfigurationProvider, WorkspaceFolder, DebugConfiguration, ProviderResult } from 'vscode';
import { } from 'vscode-debugadapter';
import { LanguageClientOptions } from 'vscode-languageclient';
import { exec } from 'child_process';
import * as path from 'path';
import * as fs from 'fs';

import { getServerOptions } from './server';
import { Messages } from './messages';
import { BallerinaPluginConfig, getPluginConfig } from './config';
import { activate as activateRenderer, errored as rendererErrored } from './renderer';
import { ExtendedLangClient } from './lang-client';

const { showWarningMessage } = window;
const { executeCommand } = commands;

function openGlobalSettings(): void {
    executeCommand('workbench.action.openGlobalSettings');
}

function reloadWindow(): void {
    executeCommand('workbench.action.reloadWindow');
}

function showMsgAndRestart(msg: string): void {
    const action = 'Restart Now';
    showWarningMessage(msg, action).then((selection) => {
        if (action === selection) {
            reloadWindow();
        }
    });
}

function showMsgAndOpenSettings(msg: string): void {
    const action = 'Open Settings';
    showWarningMessage(msg, action).then((selection) => {
        if (action === selection) {
            openGlobalSettings();
        }
    });
}

function getExtension(): Extension<any> | undefined {
    return extensions.getExtension('ballerina.ballerina');
}

let oldConfig: BallerinaPluginConfig;

const debugConfigResolver: DebugConfigurationProvider = {
	resolveDebugConfiguration(folder: WorkspaceFolder, config: DebugConfiguration)
								: ProviderResult<DebugConfiguration> {
		if (!config.has('ballerina.home')) {
			// If ballerina.home is not defined in in debug config get it from workspace configs
			const workspaceConfig: BallerinaPluginConfig = getPluginConfig();
			if (workspaceConfig.home) {
				config.update('ballerina.home', workspaceConfig.home);
			}
		}

		if (config.has('ballerina.home')) {
			if (fs.readdirSync(<string> config.get('ballerina.home')).indexOf('bin') < 0) {
				showMsgAndOpenSettings(Messages.NO_BIN_IN_HOME);
			}
		} else {
			showMsgAndOpenSettings(Messages.DEBUG_HOME_NOT_SET);
		}
		return config;
	}
};


function checkHome(homePath: string) : boolean {
	try {
		if (fs.readdirSync(path.join(homePath, 'lib', 'resources')).indexOf('composer') > -1) {
			return true;
		}
	} catch(e) {
		// could not read the home path. Let the warning show
	}

	const homeSetWarning = Messages.HOME_INCORRECT;
	const action = 'Open Settings';	
	showWarningMessage(homeSetWarning, action).then((selection) => {
		if (action === selection) {
			openGlobalSettings();
		}
	});
	return false;
}

function checkVersion(homePath: string) : void {
	if (!fs.existsSync(path.join(homePath, 'bin', 'ballerina'))) {
		return;
	}

	exec(`${path.join(homePath, 'bin', 'ballerina')} version`, (err, stdout, stderr) => {
		const platformVersion = stderr.toString().trim();
		const extension = getExtension();
        const extensionVersion = 'Ballerina ' + (extension ? extension.packageJSON.version : '');

		if (platformVersion !== extensionVersion) {
            const msg  = `Version mismatch. Platform version: ${platformVersion} Extension version: ${extensionVersion}`;
			console.log(msg);
			showWarningMessage(Messages.VERSION_MISMATCH);
		}
	});
}

export function activate(context: ExtensionContext) : void {
	// Options to control the language client
	const clientOptions: LanguageClientOptions = {
		documentSelector: [{ scheme: 'file', language: 'ballerina' }],
	};

	const config = getPluginConfig();
	oldConfig = config;
	// in windows class path seperated by ';'
	//const sep = process.platform === 'win32' ? ';' : ':';

	if (!config.home) {
		// home path is not set. The plugin can't activate. Show warning and exit.
		rendererErrored(context);
		showMsgAndOpenSettings(Messages.HOME_NOT_SET);
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

	const langClient = new ExtendedLangClient('ballerina-vscode', 'Ballerina vscode lanugage client',
		getServerOptions(), clientOptions);
	const langClientDisposable = langClient.start();	
	activateRenderer(context, langClient);

	// Push the disposable to the context's subscriptions so that the 
	// client can be deactivated on extension deactivation
	context.subscriptions.push(langClientDisposable);
	context.subscriptions.push(debug.registerDebugConfigurationProvider('ballerina', debugConfigResolver));
}

workspace.onDidChangeConfiguration(params => {
	const newConfig = getPluginConfig();
	if (newConfig.home !== oldConfig.home) {
		showMsgAndRestart(Messages.HOME_CHANGED);
	}

	if (newConfig.showLSErrors !== oldConfig.showLSErrors) {
		showMsgAndRestart(Messages.SHOW_LSERRORS_CHANGED);
	}

	if (newConfig.debugLog !== oldConfig.debugLog) {
		showMsgAndRestart(Messages.DEBUG_LOG_CHANGED);
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
	hide: () => {},
	dispose: () => {},
};
