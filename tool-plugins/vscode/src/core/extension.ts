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
    workspace, window, commands, languages, Uri,
    ConfigurationChangeEvent, extensions,
    Extension, ExtensionContext, IndentAction,
} from "vscode";
import {
    INVALID_HOME_MSG, INSTALL_BALLERINA, DOWNLOAD_BALLERINA, MISSING_SERVER_CAPABILITY,
    CONFIG_CHANGED, OLD_BALLERINA_VERSION, OLD_PLUGIN_VERSION, UNKNOWN_ERROR, INVALID_FILE,
} from "./messages";
import * as path from 'path';
import * as fs from 'fs';
import { exec, execSync } from 'child_process';
import { LanguageClientOptions, State as LS_STATE, RevealOutputChannelOn, DidChangeConfigurationParams } from "vscode-languageclient";
import { getServerOptions } from '../server/server';
import { ExtendedLangClient } from './extended-language-client';
import { info, getOutputChannel } from '../utils/index';
import { AssertionError } from "assert";
import { OVERRIDE_BALLERINA_HOME, BALLERINA_HOME, ALLOW_EXPERIMENTAL, ENABLE_DEBUG_LOG } from "./preferences";
export class BallerinaExtension {

    public ballerinaHome: string;
    public extension: Extension<any>;
    private clientOptions: LanguageClientOptions;
    public langClient?: ExtendedLangClient;
    public context?: ExtensionContext;

    constructor() {
        this.ballerinaHome = '';
        // Load the extension
        this.extension = extensions.getExtension('ballerina.ballerina')!;
        this.clientOptions = {
            documentSelector: [{ scheme: 'file', language: 'ballerina' }],
            outputChannel: getOutputChannel(),
            revealOutputChannelOn: RevealOutputChannelOn.Never,
        };
    }

    setContext(context: ExtensionContext) {
        this.context = context;
    }

    init(onBeforeInit: Function): Promise<any> {
        try {
            // Register pre init handlers.
            this.registerPreInitHandlers();

            // Check if ballerina home is set.
            if (this.overrideBallerinaHome()) {
                info("Ballerina home is configured in settings.");
                this.ballerinaHome = this.getConfiguredBallerinaHome();
                // Lets check if ballerina home is valid.
                if (!this.isValidBallerinaHome(this.ballerinaHome)) {
                    info("Configured Ballerina home is not valid.");
                    // Ballerina home in setting is invalid show message and quit.
                    // Prompt to correct the home. // TODO add auto detection.
                    this.showMessageInvalidBallerinaHome();
                    return Promise.resolve();
                }
            } else {
                info("Auto detecting Ballerina home.");
                // If ballerina home is not set try to auto detect ballerina home.
                // TODO If possible try to update the setting page.
                this.ballerinaHome = this.autoDetectBallerinaHome();
                if (!this.ballerinaHome) {
                    this.showMessageInstallBallerina();
                    info("Unable to auto detect Ballerina home.");
                    return Promise.resolve();
                }
            }
            info("Using " + this.ballerinaHome + " as the Ballerina home.");
            // Validate the ballerina version.
            const pluginVersion = this.extension.packageJSON.version.split('-')[0];
            return this.getBallerinaVersion(this.ballerinaHome).then(ballerinaVersion => {
                ballerinaVersion = ballerinaVersion.split('-')[0];
                info(`Plugin version: ${pluginVersion}\nBallerina version: ${ballerinaVersion}`);
                this.checkCompatibleVersion(pluginVersion, ballerinaVersion);
                // if Home is found load Language Server.
                this.langClient = new ExtendedLangClient('ballerina-vscode', 'Ballerina LS Client',
                    getServerOptions(this.getBallerinaHome(), this.isExperimental()), this.clientOptions, false);

                // 0.983.0 and 0.982.0 versions are incapable of handling client capabilities 
                if (ballerinaVersion !== "0.983.0" && ballerinaVersion !== "0.982.0") {
                    onBeforeInit(this.langClient);
                }

                // Following was put in to handle server startup failures.
                const disposeDidChange = this.langClient.onDidChangeState(stateChangeEvent => {
                    if (stateChangeEvent.newState === LS_STATE.Stopped) {
                        info("Couldn't establish language server connection.");
                        this.showPluginActivationError();
                    }
                });

                let disposable = this.langClient.start();

                this.langClient.onReady().then(fulfilled => {
                    disposeDidChange.dispose();
                    this.context!.subscriptions.push(disposable);
                });
            }).catch(e => {
                info(`Error when checking ballerina version. Got ${e}`);
            });

        } catch (ex) {
            info("Error while activating plugin: " + (ex.message ? ex.message : ex));
            // If any failure occurs while initializing show an error message
            this.showPluginActivationError();
            return Promise.resolve();
        }
    }

    onReady(): Promise<void> {
        if (!this.langClient) {
            return Promise.reject('BallerinaExtension is not initialized');
        }

        return this.langClient.onReady();
    }

    showPluginActivationError(): any {
        // message to display on Unknown errors.
        // ask to enable debug logs.
        // we can ask the user to report the issue.

        window.showErrorMessage(UNKNOWN_ERROR);
    }

    registerPreInitHandlers(): any {
        // We need to restart VSCode if we change plugin configurations.
        workspace.onDidChangeConfiguration((params: ConfigurationChangeEvent) => {
            if (params.affectsConfiguration(BALLERINA_HOME) ||
                params.affectsConfiguration(OVERRIDE_BALLERINA_HOME) ||
                params.affectsConfiguration(ALLOW_EXPERIMENTAL) ||
                params.affectsConfiguration(ENABLE_DEBUG_LOG)) {
                this.showMsgAndRestart(CONFIG_CHANGED);
            }
            if (params.affectsConfiguration('ballerina')) {
                const args: DidChangeConfigurationParams = {
                    settings: workspace.getConfiguration('ballerina'),
                };
                this.langClient!.sendNotification("workspace/didChangeConfiguration", args);
            }
        });

        languages.setLanguageConfiguration('ballerina', {
            onEnterRules: [
                {
                    beforeText: new RegExp('^\\s*#'),
                    action: {
                        appendText: '# ',
                        indentAction: IndentAction.None,
                    }
                }
            ]
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

    /**
     * Compares plugin's versions with the used ballerina distribution's version
     * Uses only the major and minor versions according to the semver spec.
     * First two numbers will be used when version string is not semver (eg. 0.990-r1)
     * Returns 1 if plugin version is higher than ballerina's; -1 if plugin version is lower
     * than ballerina's; 0 if the versions match.
     *
     * @returns {number}
     */
    compareVersions(pluginVersion: string, ballerinaVersion: string): number {
        const toInt = (i: string) => {
            return parseInt(i, 10);
        };
        const numMatchRegexp = /\d+/g;
        
        const [pluginMajor, pluginMinor] = pluginVersion.match(numMatchRegexp)!.map(toInt);
        const [ballerinaMajor, ballerinaMinor] = ballerinaVersion.match(numMatchRegexp)!.map(toInt);

        if (pluginMajor > ballerinaMajor) {
            return 1;
        }

        if (pluginMajor < ballerinaMajor) {
            return -1;
        }

        if (pluginMinor > ballerinaMinor) {
            return 1;
        }

        if (pluginMinor < ballerinaMinor) {
            return -1;
        }

        return 0;
    }

    checkCompatibleVersion(pluginVersion: string, ballerinaVersion: string): void {
        const versionCheck = this.compareVersions(pluginVersion, ballerinaVersion);

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
        if (!ballerinaHome) {
            throw new AssertionError({
                message: "Trying to get ballerina version without setting ballerina home."
            });
        }
        let command = `${path.join(ballerinaHome, 'bin', 'jballerina')} version`;
        if (process.platform === 'win32') {
            command = `"${path.join(ballerinaHome, 'bin', 'jballerina.bat')}" version`;
        }
        return new Promise((resolve, reject) => {
            exec(command, (err, stdout, stderr) => {
                const version = stdout.length > 0 ? stdout : stderr;
                if (version.startsWith("Error:")) {
                    reject(version);
                    return;
                }
                
                resolve(version.replace(/Ballerina /, '').replace(/[\n\t\r]/g, ''));
            });
        });
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

    showMessageInvalidFile(): any {
        window.showErrorMessage(INVALID_FILE);
    }


    isValidBallerinaHome(homePath: string = this.ballerinaHome): boolean {
        const ballerinaCmd = process.platform === 'win32' ? 'jballerina.bat' : 'jballerina';
        if (fs.existsSync(path.join(homePath, 'bin', ballerinaCmd))) {
            return true;
        }
        return false;
    }

    /**
     * Get ballerina home path.
     *
     * @returns {string}
     * @memberof BallerinaExtension
     */
    getBallerinaHome(): string {
        return this.ballerinaHome;
    }

    /**
     * Get ballerina home path configured in preferences.
     *
     * @returns {string}
     * @memberof BallerinaExtension
     */
    getConfiguredBallerinaHome(): string {
        return <string>workspace.getConfiguration().get(BALLERINA_HOME);
    }

    isExperimental(): boolean {
        return <boolean>workspace.getConfiguration().get(ALLOW_EXPERIMENTAL);
    }

    autoDetectBallerinaHome(): string {
        // try to detect the environment.
        const platform: string = process.platform;
        let ballerinaPath = '';
        switch (platform) {
            case 'win32': // Windows
                if (process.env.BALLERINA_HOME) {
                    return process.env.BALLERINA_HOME;
                }
                try {
                    ballerinaPath = execSync('where jballerina').toString().trim();
                } catch (error) {
                    return ballerinaPath;
                }
                if (ballerinaPath) {
                    ballerinaPath = ballerinaPath.replace(/bin\\jballerina.bat$/, '');
                }
                break;
            case 'darwin': // Mac OS
                try {
                    const output = execSync('which ballerina');
                    ballerinaPath = fs.realpathSync(output.toString().trim());
                    // remove ballerina bin from ballerinaPath
                    if (ballerinaPath) {
                        ballerinaPath = ballerinaPath.replace(/bin\/jballerina$/, '');
                        // For homebrew installations ballerina executable is in libexcec
                        const homebrewBallerinaPath = path.join(ballerinaPath, 'libexec');
                        if (fs.existsSync(homebrewBallerinaPath)) {
                            ballerinaPath = homebrewBallerinaPath;
                        }
                    }
                } catch {
                    return ballerinaPath;
                }
                break;
            case 'linux': // Linux
                // lets see where the ballerina command is.
                try {
                    const output = execSync('which jballerina');
                    ballerinaPath = fs.realpathSync(output.toString().trim());
                    // remove ballerina bin from path
                    if (ballerinaPath) {
                        ballerinaPath = ballerinaPath.replace(/bin\/jballerina$/, '');
                    }
                } catch {
                    return ballerinaPath;
                }
                break;
        }

        // If we cannot find ballerina home return empty.
        return ballerinaPath;
    }

    private overrideBallerinaHome(): boolean {
        return <boolean>workspace.getConfiguration().get(OVERRIDE_BALLERINA_HOME);
    }
}

export const ballerinaExtInstance = new BallerinaExtension();