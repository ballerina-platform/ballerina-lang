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
    workspace, window, commands, Uri,
    ConfigurationChangeEvent, extensions,
    Extension, ExtensionContext
} from "vscode";
import {
    INVALID_HOME_MSG, INSTALL_BALLERINA, DOWNLOAD_BALLERINA, MISSING_SERVER_CAPABILITY,
    CONFIG_CHANGED, OLD_BALLERINA_VERSION, OLD_PLUGIN_VERSION, UNKNOWN_ERROR,
} from "./messages";
import * as path from 'path';
import * as fs from 'fs';
import { exec, execSync } from 'child_process';
import { LanguageClientOptions, State as LS_STATE } from "vscode-languageclient";
import { getServerOptions } from '../server';
import { ExtendedLangClient } from "../lang-client";
import { log } from '../logger';
import { AssertionError } from "assert";
import * as compareVersions from 'compare-versions';
class BallerinaExtension {

    public ballerinaHome: string;
    public extention: Extension<any>;
    private clientOptions: LanguageClientOptions;
    public langClient?: ExtendedLangClient;
    public context?: ExtensionContext;

    constructor() {
        this.ballerinaHome = '';
        // Load the extention
        this.extention = extensions.getExtension('ballerina.ballerina')!;
        this.clientOptions = {
            documentSelector: [{ scheme: 'file', language: 'ballerina' }],
            // TODO set debug channel 
        };
    }

    setContext(context: ExtensionContext) {
        this.context = context;
    }

    init(): void {
        try {
            // Register pre init handlers.
            this.registerPreInitHandlers();

            // Check if ballerina home is set.
            if (this.hasBallerinaHomeSetting()) {
                this.ballerinaHome = this.getBallerinaHome();
                // Lets check if ballerina home is valid.
                if (!this.isValidBallerinaHome(this.ballerinaHome)) {
                    // Ballerina home in setting is invalid show message and quit.
                    // Prompt to correct the home. // TODO add auto ditection.
                    this.showMessageInvalidBallerinaHome();
                    return;
                }
            } else {
                // If ballerina home is not set try to auto ditect ballerina home.
                // TODO If possible try to update the setting page.
                this.ballerinaHome = this.autoDitectBallerinaHome();
                if (!this.ballerinaHome) {
                    this.showMessageInstallBallerina();
                    log("Unable to auto ditect ballerina home.");
                    return;
                }
            }

            // Validate the ballerina version.
            const pluginVersion = this.extention.packageJSON.version.split('-')[0];
            this.getBallerinaVersion(this.ballerinaHome).then(ballerinaVersion => {
                ballerinaVersion = ballerinaVersion.split('-')[0];
                this.checkCompatibleVersion(pluginVersion, ballerinaVersion);
            })

            // if Home is found load Language Server.
            this.langClient = new ExtendedLangClient('ballerina-vscode', 'Ballerina LS Client',
                getServerOptions(this.getBallerinaHome()), this.clientOptions, false);

            // Following was put in to handle server startup failiers.
            const disposeDidChange = this.langClient.onDidChangeState(stateChangeEvent => {
                if (stateChangeEvent.newState === LS_STATE.Stopped) {
                    this.showPluginActivationError();
                }
            });

            let disposable = this.langClient.start();

            this.langClient.onReady().then(fullfilled => {
                disposeDidChange.dispose();
                this.context!.subscriptions.push(disposable);
            });
        } catch {
            // If any failure occurs while intializing show an error messege
            this.showPluginActivationError();
        }
    }

    onReady(): Promise<void> {
        if (!this.langClient) {
            return Promise.reject('BallerinaExtension is not initialized');
        }

        return this.langClient.onReady();
    }

    showPluginActivationError(): any {
        // message to display on Unknoen errors.
        // ask to enable debuglogs.
        // we can ask the user to report the issue.

        window.showErrorMessage(UNKNOWN_ERROR);
    }

    registerPreInitHandlers(): any {
        // We need to restart VSCode if we change plugin configurations.
        workspace.onDidChangeConfiguration((params: ConfigurationChangeEvent) => {
            if (params.affectsConfiguration('ballerina.home') ||
                params.affectsConfiguration('ballerina.debugLog')) {
                this.showMsgAndRestart(CONFIG_CHANGED);
            }
        });
    }

    showMsgAndRestart(msg: string): void {
        const action = 'Restart Now';
        window.showInformationMessage(msg, action).then((selection) => {
            if (action === selection) {
                commands.executeCommand('workbench.action.reloadWindow');
            }
        });
    }

    checkCompatibleVersion(pluginVersion: string, ballerinaVersion: string): void {
        const versionCheck = compareVersions(pluginVersion, ballerinaVersion);

        if (versionCheck > 0) {
            // Plugin version is greater
            this.showMessageOldBallerina();
            return;
        }

        if (versionCheck < 0) {
            // Ballerina version is greater
            this.showMessageOldPlugin();
        }
    }

    getBallerinaVersion(ballerinaHome: string): Promise<string> {
        if (!this.ballerinaHome) {
            throw new AssertionError({
                message: "Trying to get ballerina version without setting ballerina home."
            });
        }
        let command = `${path.join(ballerinaHome, 'bin', 'ballerina')} version`;
        if (process.platform === 'win32') {
            command = `"${path.join(ballerinaHome, 'bin', 'ballerina.bat')}" version`;
        }
        return new Promise((resolve, reject) => {
            exec(command, (err, stdout, stderr) => {
                const version = stdout.length > 0 ? stdout : stderr;
                resolve(version.replace(/Ballerina /, '').replace(/[\n\t\r]/g, ''));
            })
        })
    }

    showMessageInstallBallerina(): any {
        const download: string = 'Download';
        const openSettings: string = 'Open Settings';
        window.showWarningMessage(INSTALL_BALLERINA, download, openSettings).then((selection) => {
            if (openSettings === selection) {
                commands.executeCommand('workbench.action.openGlobalSettings');
            }
            if (download === selection) {
                commands.executeCommand('vscode.open', Uri.parse(DOWNLOAD_BALLERINA));
            }
        });
    }

    showMessageInvalidBallerinaHome(): void {
        const action = 'Open Settings';
        window.showWarningMessage(INVALID_HOME_MSG, action).then((selection) => {
            if (action === selection) {
                commands.executeCommand('workbench.action.openGlobalSettings');
            }
        });
    }

    showMessageOldBallerina(): any {
        const download: string = 'Download';
        window.showWarningMessage(OLD_BALLERINA_VERSION, download).then((selection) => {
            if (download === selection) {
                commands.executeCommand('vscode.open', Uri.parse(DOWNLOAD_BALLERINA));
            }
        });
    }

    showMessageOldPlugin(): any {
        const download: string = 'Download';
        window.showWarningMessage(OLD_PLUGIN_VERSION, download).then((selection) => {
            if (download === selection) {
                commands.executeCommand('vscode.open', Uri.parse(DOWNLOAD_BALLERINA));
            }
        });
    }

    showMessageServerMissingCapability(): any {
        const download: string = 'Download';
        window.showErrorMessage(MISSING_SERVER_CAPABILITY, download).then((selection) => {
            if (download === selection) {
                commands.executeCommand('vscode.open', Uri.parse(DOWNLOAD_BALLERINA));
            }
        });
    }


    isValidBallerinaHome(homePath: string = this.ballerinaHome): boolean {
        if (fs.existsSync(path.join(homePath, 'lib', 'tools', 'lang-server'))) {
            return true;
        }
        return false;
    }

    /**
     * Get ballerina home path.
     *
     * @returns {string}
     * @memberof BallerinaExtention
     */
    getBallerinaHome(): string {
        if (this.ballerinaHome) {
            return this.ballerinaHome;
        } else {
            return <string>workspace.getConfiguration().get('ballerina.home');
        }
    }

    autoDitectBallerinaHome(): string {
        // try to ditect the environment.
        const platform: string = process.platform;
        let path = '';
        switch (platform) {
            case 'win32': // Windows
                path = execSync('where ballerina').toString().trim();
                if (path) {
                    path = path.replace(/bin\\ballerina.bat$/, '');
                }
                break;
            case 'darwin': // Mac OS
            case 'linux': // Linux
                // lets see where the ballerina command is.
                const output = execSync('which ballerina');
                path = fs.realpathSync(output.toString().trim());
                // remove ballerina bin from path
                if (path) {
                    path = path.replace(/bin\/ballerina$/, '');
                }
                break;
        }

        // If we cannot find ballerina home return empty.
        return path;
    }

    private hasBallerinaHomeSetting(): boolean {
        const home: string = this.getBallerinaHome();
        if (home) {
            return true;
        }
        return false;
    }

}

export default new BallerinaExtension();