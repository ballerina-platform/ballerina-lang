
import {
    DebugConfigurationProvider, WorkspaceFolder, DebugConfiguration,
    debug, ExtensionContext, window,
    DebugSession,
    DebugAdapterExecutable, DebugAdapterDescriptor, DebugAdapterDescriptorFactory, DebugAdapterServer
} from 'vscode';
import { RunningInfo } from './model';
import * as toml from 'toml';
import * as child_process from "child_process";
import { getPortPromise } from 'portfinder';
import * as path from "path";
import * as fs from 'fs';
import * as os from 'os';
import { ballerinaExtInstance, BallerinaExtension } from '../core/index';
import { ExtendedLangClient } from '../core/extended-language-client';
import { BALLERINA_HOME } from '../core/preferences';
import { isUnix } from "./osUtils";
import { TM_EVENT_START_DEBUG_SESSION } from '../telemetry';

const debugConfigProvider: DebugConfigurationProvider = {
    resolveDebugConfiguration(folder: WorkspaceFolder, config: DebugConfiguration)
        : Thenable<DebugConfiguration> {
            return getPortPromise({ port: 5010, stopPort: 10000}).then((debuggeePort) => {
                const ballerinaHome = ballerinaExtInstance.getBallerinaHome();
                if (!ballerinaHome) {
                    ballerinaExtInstance.showMessageInstallBallerina();
                    return config;
                } else {
                    config[BALLERINA_HOME] = ballerinaHome;
                }
    
                if (!config.type) {
                    config.type = 'ballerina';
                }
    
                if (!config.request) {
                    config.request = 'launch';
                }
    
                if (!config.script || config.script == "${file}") {
                    if (!window.activeTextEditor) {
                        ballerinaExtInstance.showMessageInvalidFile();
                        return config;
                    }
    
                    const activeDoc = window.activeTextEditor.document;
    
                    if (!activeDoc.fileName.endsWith('.bal')) {
                        ballerinaExtInstance.showMessageInvalidFile();
                        return config;;
                    }
    
                    config.script = window.activeTextEditor.document.uri.fsPath
                }
    
                let cwd: string | undefined = path.dirname(config.script);
                let { sourceRoot, ballerinaPackage } = getRunningInfo(cwd, path.parse(config.script).root);
    
                if (ballerinaPackage && sourceRoot) {
                    try {
                        const balConfigString = fs.readFileSync(path.join(<string>sourceRoot, 'Ballerina.toml'));
                        var projectConfig = toml.parse(balConfigString.toString()).project;
                        config.orgName = projectConfig['org-name'];
                        config.package = ballerinaPackage;
                        config.sourceRoot = sourceRoot;
                    } catch (e) {
                        console.log(e);
                        // no log file
                    }
                } else {
                    config.sourceRoot = cwd;
                }
    
                let langClient = <ExtendedLangClient>ballerinaExtInstance.langClient;
                if (langClient.initializeResult) {
                    const { experimental } = langClient.initializeResult!.capabilities;
                    if (experimental && experimental.introspection && experimental.introspection.port > 0) {
                        config.networkLogsPort = experimental.introspection.port;
                        if (config.networkLogs === undefined) {
                            config.networkLogs = true;
                        }
                    }
                }
    
                config.debuggeePort = debuggeePort.toString();
                return config;
            });
    }

};

function getRunningInfo(currentPath: string, root: string, ballerinaPackage: string | undefined = undefined): RunningInfo {
    if (fs.existsSync(path.join(currentPath, '.ballerina'))) {
        if (currentPath !== os.homedir()) {
            return {
                sourceRoot: currentPath,
                ballerinaPackage,
            };
        }
    }

    if (currentPath === root) {
        return {};
    }

    return getRunningInfo(
        path.dirname(currentPath), root, path.basename(currentPath));
}

export function activateDebugConfigProvider(ballerinaExtInstance: BallerinaExtension) {
    let context = <ExtensionContext>ballerinaExtInstance.context;

    context.subscriptions.push(debug.registerDebugConfigurationProvider('ballerina', debugConfigProvider));

    const factory = new BallerinaDebugAdapterDescriptorFactory();
    context.subscriptions.push(debug.registerDebugAdapterDescriptorFactory('ballerina', factory));
}

class BallerinaDebugAdapterDescriptorFactory implements DebugAdapterDescriptorFactory {
    createDebugAdapterDescriptor(session: DebugSession, executable: DebugAdapterExecutable | undefined): Thenable<DebugAdapterDescriptor> {
        return new Promise(function (resolve, reject) {
            if (session.configuration && session.configuration.debugServer) {
                new DebugAdapterServer(session.configuration.debugServer)
            } else {
                BallerinaDebugAdapterDescriptorFactory.launchAdapter(resolve, reject);
            }
        });
    }
    static launchAdapter(resolve: (arg0: DebugAdapterServer) => void, reject: (arg0: string | Buffer) => void) {
        const ballerinaPath = ballerinaExtInstance.getBallerinaHome();

        let startScriptPath = path.resolve(ballerinaPath, "lib", "tools", "debug-adapter", "launcher", "debug-adapter-launcher.sh");
        // Ensure that start script can be executed
        if (isUnix()) {
            child_process.exec("chmod +x " + startScriptPath);
        } else {
            startScriptPath = path.resolve(ballerinaPath, "lib", "tools", "debug-adapter", "launcher", "debug-adapter-launcher.bat");
        }
        getPortPromise({port: 10001, stopPort: 20000}).then((port)=>{
            ballerinaExtInstance.telemetryReporter.sendTelemetryEvent(TM_EVENT_START_DEBUG_SESSION);
            const serverProcess = child_process.spawn(startScriptPath, [
                port.toString()
            ]);
    
            console.info("Found debug adapter {} with args {}", startScriptPath);
            serverProcess.stdout.on('data', (data) => {
                if (data.toString().includes('Debug server started')) {
                    resolve(new DebugAdapterServer(port));
                }
            });
    
            serverProcess.stderr.on('data', (data) => {
                console.log(`stderr: ${data}`);
                reject(data);
            });
        });
    }
}