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
    INVALID_HOME_MSG, INSTALL_BALLERINA, DOWNLOAD_BALLERINA,
    CONFIG_CHANGED, UNKNOWN_ERROR
} from "./messages";
import * as path from 'path';
import * as fs from 'fs';
import { execSync } from "child_process";
import { LanguageClientOptions, State as LS_STATE } from "vscode-languageclient";
import { getServerOptions } from '../server';
import { ExtendedLangClient } from "../lang-client";
import { log } from '../logger';
import { AssertionError } from "assert";

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

    init(): Promise<BallerinaExtension> {
        return new Promise((resolve, reject) => {
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
                    // Return error
                    reject();
                }
            } else {
                // If ballerina home is not set try to auto ditect ballerina home.
                // TODO If possible try to update the setting page.
                this.ballerinaHome = this.autoDitectBallerinaHome();
                if (!this.ballerinaHome) {
                    this.showMessageInstallBallerina();
                    log("Unable to auto ditect ballerina home.");
                    reject();
                }
            }

            // Validate the ballerina version.
            const pluginVersion = this.extention.packageJSON.version;
            const ballerinaVersion = this.getBallerinaVersion(this.ballerinaHome);
            if (!this.isCompatibleVersion(pluginVersion, ballerinaVersion)) {
                reject();
            }

            // if Home is found load Language Server.
            this.langClient = new ExtendedLangClient('ballerina-vscode', 'Ballerina LS Client',
                getServerOptions(this.getBallerinaHome()), this.clientOptions, false);

            // Following was put in to handle server startup failiers.
            const disposeDidChange = this.langClient.onDidChangeState(stateChangeEvent => {
                if (stateChangeEvent.newState === LS_STATE.Stopped) {
                    this.showPluginActivationError();
                    reject();
                }
            });

            let disposable = this.langClient.start();

            this.langClient.onReady().then(fullfilled => {
                disposeDidChange.dispose();
                this.context!.subscriptions.push(disposable);
                resolve();
            });
        });
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

    isCompatibleVersion(pluginVersion: string, ballerinaVersion: string): boolean {
        // If both minor versions are equal return true.
        // If home version > plugin version.
        // Inform the user to update the plugin.
        // If home version < plugin version.
        // Inform the user to update ballerina.
        // TODO put a dummy class for ExtendedLanguageClient.
        return true;
    }

    getBallerinaVersion(ballerinaHome: string): string {
        if (!this.ballerinaHome) {
            throw new AssertionError({
                message: "Trying to get ballerina version without setting ballerina home."
            });
        }
        let command = `${path.join(ballerinaHome, 'bin', 'ballerina')} version`;
        if (process.platform === 'win32') {
            command = `"${path.join(ballerinaHome, 'bin', 'ballerina.bat')}" version`;
        }
        let version = execSync(command).toString();
        version = version.replace(/Ballerina /, '').replace(/[\n\t\r]/g, '');
        return version;
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



    isValidBallerinaHome(homePath: string = this.ballerinaHome): boolean {
        if (fs.existsSync(path.join(homePath, 'lib', 'resources', 'composer'))) {
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