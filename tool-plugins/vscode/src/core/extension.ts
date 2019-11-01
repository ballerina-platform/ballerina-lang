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
    Extension, ExtensionContext, IndentAction, WebviewPanel, OutputChannel,
} from "vscode";
import {
    INVALID_HOME_MSG, INSTALL_BALLERINA, DOWNLOAD_BALLERINA, MISSING_SERVER_CAPABILITY,
    CONFIG_CHANGED, OLD_BALLERINA_VERSION, OLD_PLUGIN_VERSION, UNKNOWN_ERROR, INVALID_FILE, INSTALL_NEW_BALLERINA,
} from "./messages";
import * as path from 'path';
import * as fs from 'fs';
import { exec, execSync } from 'child_process';
import { LanguageClientOptions, State as LS_STATE, RevealOutputChannelOn, DidChangeConfigurationParams } from "vscode-languageclient";
import { getServerOptions, getOldServerOptions } from '../server/server';
import { ExtendedLangClient } from './extended-language-client';
import { log, getOutputChannel } from '../utils/index';
import { AssertionError } from "assert";
import { OVERRIDE_BALLERINA_HOME, BALLERINA_HOME, ALLOW_EXPERIMENTAL, ENABLE_DEBUG_LOG, ENABLE_TRACE_LOG } from "./preferences";
import TelemetryReporter from "vscode-extension-telemetry";
import { createTelemetryReporter, TM_EVENT_ERROR_INVALID_BAL_HOME_CONFIGURED, TM_EVENT_ERROR_INVALID_BAL_HOME_DETECTED, TM_EVENT_OLD_BAL_HOME, TM_EVENT_OLD_BAL_PLUGIN, TM_EVENT_ERROR_OLD_BAL_HOME_DETECTED } from "../telemetry";

export const EXTENSION_ID = 'ballerina.ballerina';

export interface ConstructIdentifier {
    sourceRoot?: string;
    filePath?: string;
    moduleName: string;
    constructName: string;
    subConstructName?: string;
}

export class BallerinaExtension {
    public telemetryReporter: TelemetryReporter;
    public ballerinaHome: string;
    public isNewCLICmdSupported: boolean = true;
    public extension: Extension<any>;
    private clientOptions: LanguageClientOptions;
    public langClient?: ExtendedLangClient;
    public context?: ExtensionContext;
    private projectTreeElementClickedCallbacks: Array<(construct: ConstructIdentifier) => void> = [];
    private webviewPanels: {
        [name: string]: WebviewPanel;
    };

    constructor() {
        this.ballerinaHome = '';
        this.webviewPanels = {};
        // Load the extension
        this.extension = extensions.getExtension(EXTENSION_ID)!;
        this.clientOptions = {
            documentSelector: [{ scheme: 'file', language: 'ballerina' }],
            outputChannel: getOutputChannel(),
            revealOutputChannelOn: RevealOutputChannelOn.Never,
        };
        this.telemetryReporter = createTelemetryReporter(this);
    }

    setContext(context: ExtensionContext) {
        this.context = context;
    }

    init(onBeforeInit: Function): Promise<void> {
        try {
            // Register pre init handlers.
            this.registerPreInitHandlers();

            // Check if ballerina home is set.
            if (this.overrideBallerinaHome()) {
                log("Ballerina home is configured in settings.");
                this.ballerinaHome = this.getConfiguredBallerinaHome();
                // Lets check if ballerina home is valid.
                if (!this.isValidBallerinaHome(this.ballerinaHome)) {
                    const msg = "Configured Ballerina home is not valid.";
                    log(msg);
                    // Ballerina home in setting is invalid show message and quit.
                    // Prompt to correct the home. // TODO add auto detection.
                    this.showMessageInvalidBallerinaHome();
                    this.telemetryReporter.sendTelemetryEvent(TM_EVENT_ERROR_INVALID_BAL_HOME_CONFIGURED, { error: msg });
                    return Promise.reject(msg);
                }
            } else {
                log("Auto detecting Ballerina home.");
                // If ballerina home is not set try to auto detect ballerina home.
                const { isBallerinaNotFound, isOldBallerinaDist, home } = this.autoDetectBallerinaHome();
                this.ballerinaHome = home;

                if (isBallerinaNotFound) {
                    this.showMessageInstallBallerina();
                    const msg = "Unable to auto detect Ballerina home.";
                    log(msg);
                    this.telemetryReporter.sendTelemetryEvent(TM_EVENT_ERROR_INVALID_BAL_HOME_DETECTED, { error: msg });
                    return Promise.reject(msg);
                } else if (isOldBallerinaDist) {
                    this.showMessageInstallLatestBallerina();
                    const msg = "Found an incompatible Ballerina installation.";
                    log(msg);
                    this.telemetryReporter.sendTelemetryEvent(TM_EVENT_ERROR_OLD_BAL_HOME_DETECTED, { error: msg });
                    return Promise.reject(msg);
                }
            }
            log("Using " + this.ballerinaHome + " as the Ballerina home.");
            // Validate the ballerina version.
            const pluginVersion = this.extension.packageJSON.version.split('-')[0];
            return this.getBallerinaVersion(this.ballerinaHome, this.overrideBallerinaHome()).then(ballerinaVersion => {
                ballerinaVersion = ballerinaVersion.split('-')[0];
                log(`Plugin version: ${pluginVersion}\nBallerina version: ${ballerinaVersion}`);
                this.checkCompatibleVersion(pluginVersion, ballerinaVersion);

                // versions less than 1.0.2 are incapable of handling cli commands for langserver and debug-adapter
                this.isNewCLICmdSupported = this.compareVersions(ballerinaVersion, "1.0.3", true) >= 0;

                // if Home is found load Language Server.
                let serverOptions = getServerOptions(this.getBallerinaHome(), this.isExperimental(), this.isDebugLogsEnabled(), this.isTraceLogsEnabled());
                if (!this.isNewCLICmdSupported) {
                    serverOptions = getOldServerOptions(this.getBallerinaHome(), this.isExperimental(), this.isDebugLogsEnabled(), this.isTraceLogsEnabled());
                }
                this.langClient = new ExtendedLangClient('ballerina-vscode', 'Ballerina LS Client', serverOptions, this.clientOptions, false);

                // 0.983.0 and 0.982.0 versions are incapable of handling client capabilities 
                if (ballerinaVersion !== "0.983.0" && ballerinaVersion !== "0.982.0") {
                    onBeforeInit(this.langClient);
                }

                // Following was put in to handle server startup failures.
                const disposeDidChange = this.langClient.onDidChangeState(stateChangeEvent => {
                    if (stateChangeEvent.newState === LS_STATE.Stopped) {
                        log("Couldn't establish language server connection.");
                        this.showPluginActivationError();
                    }
                });

                let disposable = this.langClient.start();

                this.langClient.onReady().then(fulfilled => {
                    disposeDidChange.dispose();
                    this.context!.subscriptions.push(disposable);
                });
            }, (reason) => {
                throw new Error(reason);
            }).catch(e => {
                const msg = `Error when checking ballerina version. ${e.message}`;
                this.telemetryReporter.sendTelemetryException(e, { error: msg });
                throw new Error(msg);
            });
        } catch (ex) {
            const msg = "Error while activating plugin. " + (ex.message ? ex.message : ex);
            // If any failure occurs while initializing show an error message
            this.showPluginActivationError();
            this.telemetryReporter.sendTelemetryException(ex, { error: msg });
            return Promise.reject(msg);
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
                params.affectsConfiguration(ENABLE_DEBUG_LOG) ||
                params.affectsConfiguration(ENABLE_TRACE_LOG)) {
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
    compareVersions(pluginVersion: string, ballerinaVersion: string, comparePatchVer: boolean = false): number {
        const toInt = (i: string) => {
            return parseInt(i, 10);
        };
        const numMatchRegexp = /\d+/g;

        const [pluginMajor, pluginMinor, pluginPatch] = pluginVersion.match(numMatchRegexp)!.map(toInt);
        const [ballerinaMajor, ballerinaMinor, ballerinaPatch] = ballerinaVersion.match(numMatchRegexp)!.map(toInt);

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

        if (comparePatchVer) {
            if (pluginPatch > ballerinaPatch) {
                return 1;
            }

            if (pluginPatch < ballerinaPatch) {
                return -1;
            }
        }

        return 0;
    }

    checkCompatibleVersion(pluginVersion: string, ballerinaVersion: string): void {
        const versionCheck = this.compareVersions(pluginVersion, ballerinaVersion);

        if (versionCheck > 0) {
            // Plugin version is greater
            this.showMessageOldBallerina();
            this.telemetryReporter.sendTelemetryEvent(TM_EVENT_OLD_BAL_HOME);
            return;
        }

        if (versionCheck < 0) {
            // Ballerina version is greater
            this.showMessageOldPlugin();
            this.telemetryReporter.sendTelemetryEvent(TM_EVENT_OLD_BAL_PLUGIN);
        }
    }

    getBallerinaVersion(ballerinaHome: string, overrideBallerinaHome: boolean): Promise<string> {
        if (overrideBallerinaHome && !ballerinaHome) {
            throw new AssertionError({
                message: "Trying to get ballerina version without setting ballerina home."
            });
        }
        // if ballerina home is overridden, use ballerina cmd inside distribution
        // otherwise use wrapper command
        const balCmd = this.getBallerinaCmd(overrideBallerinaHome ? ballerinaHome : "");
        return new Promise((resolve, reject) => {
            exec(balCmd + ' version', (err, stdout, stderr) => {
                const cmdOutput = stdout.length > 0 ? stdout : stderr;
                if (cmdOutput.startsWith("Error:")) {
                    reject(cmdOutput);
                    return;
                }
                try {
                    const parsedVersion = cmdOutput.split('\n')[0].replace(/Ballerina /, '').replace(/[\n\t\r]/g, '');
                    resolve(parsedVersion);
                } catch (error) {
                    reject(error);
                }
            });
        });
    }

    showMessageInstallBallerina(): any {
        const download: string = 'Download';
        const openSettings: string = 'Open Settings';
        const viewLogs: string = 'View Logs';
        window.showWarningMessage(INSTALL_BALLERINA, download, openSettings, viewLogs).then((selection) => {
            if (openSettings === selection) {
                commands.executeCommand('workbench.action.openGlobalSettings');
            } else if (download === selection) {
                commands.executeCommand('vscode.open', Uri.parse(DOWNLOAD_BALLERINA));
            } else if (viewLogs === selection) {
                const balOutput = ballerinaExtInstance.getOutPutChannel();
                if (balOutput) {
                    balOutput.show();
                }
            }

        });
    }

    showMessageInstallLatestBallerina(): any {
        const download: string = 'Download';
        const openSettings: string = 'Open Settings';
        const viewLogs: string = 'View Logs';
        window.showWarningMessage(ballerinaExtInstance.getVersion() + INSTALL_NEW_BALLERINA, download, openSettings, viewLogs).then((selection) => {
            if (openSettings === selection) {
                commands.executeCommand('workbench.action.openGlobalSettings');
            }
            if (download === selection) {
                commands.executeCommand('vscode.open', Uri.parse(DOWNLOAD_BALLERINA));
            } else if (viewLogs === selection) {
                const balOutput = ballerinaExtInstance.getOutPutChannel();
                if (balOutput) {
                    balOutput.show();
                }
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
        const ballerinaCmd = this.getBallerinaCmd(homePath);
        if (fs.existsSync(ballerinaCmd)) {
            return true;
        }
        return false;
    }

    getBallerinaCmd(ballerinaDistribution: string = "") {
        const prefix = ballerinaDistribution ? (path.join(ballerinaDistribution, "bin") + path.sep) : "";
        return prefix + (process.platform === 'win32' ? 'ballerina.bat' : 'ballerina');
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

    isDebugLogsEnabled(): boolean {
        return <boolean>workspace.getConfiguration().get(ENABLE_DEBUG_LOG);
    }

    isTraceLogsEnabled(): boolean {
        return <boolean>workspace.getConfiguration().get(ENABLE_TRACE_LOG);
    }

    autoDetectBallerinaHome(): { home: string, isOldBallerinaDist: boolean, isBallerinaNotFound: boolean } {
        let balHomeOutput = "",
            isBallerinaNotFound = false,
            isOldBallerinaDist = false;
        try {
            balHomeOutput = execSync(this.getBallerinaCmd() + ' home').toString().trim();
            // specially handle unknown ballerina command scenario for windows
            if (balHomeOutput === "" && process.platform === "win32") {
                isOldBallerinaDist = true;
            }
        } catch ({ message }) {
            // ballerina is installed, but ballerina home command is not found
            isOldBallerinaDist = message.includes("ballerina: unknown command 'home'");
            // ballerina is not installed
            isBallerinaNotFound = message.includes('command not found')
                || message.includes('unknown command')
                || message.includes('is not recognized as an internal or external command');
            log("Error executing `ballerina home`. " + "\n<---- cmd output ---->\n"
                + message + "<---- cmd output ---->\n");
        }

        return {
            home: isBallerinaNotFound || isOldBallerinaDist ? '' : balHomeOutput,
            isBallerinaNotFound,
            isOldBallerinaDist
        };
    }

    public overrideBallerinaHome(): boolean {
        return <boolean>workspace.getConfiguration().get(OVERRIDE_BALLERINA_HOME);
    }

    public projectTreeElementClicked(construct: ConstructIdentifier): void {
        this.projectTreeElementClickedCallbacks.forEach((callback) => {
            callback(construct);
        });
    }

    public onProjectTreeElementClicked(callback: (construct: ConstructIdentifier) => void) {
        this.projectTreeElementClickedCallbacks.push(callback);
    }

    public addWebviewPanel(name: string, panel: WebviewPanel) {
        this.webviewPanels[name] = panel;

        panel.onDidDispose(() => {
            delete this.webviewPanels[name];
        });
    }

    public getWebviewPanels() {
        return this.webviewPanels;
    }

    public getID(): string {
        return this.extension.id;
    }

    public getVersion(): string {
        return this.extension.packageJSON.version;
    }

    public getOutPutChannel(): OutputChannel | undefined {
        return getOutputChannel();
    }
}

export const ballerinaExtInstance = new BallerinaExtension();
