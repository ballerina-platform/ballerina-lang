
import {
    DebugConfigurationProvider, WorkspaceFolder, DebugConfiguration,
    ProviderResult, debug, ExtensionContext, window,
    DebugSession, 
    DebugAdapterExecutable, DebugAdapterDescriptor, DebugAdapterDescriptorFactory, DebugAdapterServer
} from 'vscode';

import * as child_process from "child_process";
import * as path from "path";
import { ballerinaExtInstance, BallerinaExtension } from '../core/index';
import { ExtendedLangClient } from 'src/core/extended-language-client';
import { isUnix } from "./osUtils";

const debugConfigProvider: DebugConfigurationProvider = {
    resolveDebugConfiguration(folder: WorkspaceFolder, config: DebugConfiguration)
        : ProviderResult<DebugConfiguration> {
        const ballerinaHome = ballerinaExtInstance.getBallerinaHome();
        if (!ballerinaHome) {
            ballerinaExtInstance.showMessageInstallBallerina();
            return;
        } else {
            config['ballerina.home'] = ballerinaHome;
        }

        if (!config.type) {
            config.type = 'ballerina';
        }

        if (!config.request) {
            config.request = 'launch';
        }

        if (!config.script) {
            if (!window.activeTextEditor) {
                ballerinaExtInstance.showMessageInvalidFile();
                return
            }

            const activeDoc = window.activeTextEditor.document;

            if (!activeDoc.fileName.endsWith('.bal')) {
                ballerinaExtInstance.showMessageInvalidFile();
                return;
            }

            config.script = window.activeTextEditor.document.uri.path;
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

        return config;
    }
    
};

export function activateDebugConfigProvider(ballerinaExtInstance: BallerinaExtension) {
    let context = <ExtensionContext>ballerinaExtInstance.context;

    context.subscriptions.push(debug.registerDebugConfigurationProvider('ballerina', debugConfigProvider));

    const factory = new BallerinaDebugAdapterDescriptorFactory();
    context.subscriptions.push(debug.registerDebugAdapterDescriptorFactory('ballerina', factory));
}

class BallerinaDebugAdapterDescriptorFactory implements DebugAdapterDescriptorFactory {
    createDebugAdapterDescriptor(session: DebugSession, executable: DebugAdapterExecutable | undefined): Thenable <DebugAdapterDescriptor> {
        return new Promise(function(resolve, reject) {
            // launch debugger
            const ballerinaPath = ballerinaExtInstance.getBallerinaHome();

            let startScriptPath = path.resolve(ballerinaPath, "lib", "tools", "debug-adapter", "launcher", "debug-adapter-launcher.sh");
            // Ensure that start script can be executed
            if (isUnix()) {
                child_process.exec("chmod +x " + startScriptPath);
            } else {
                startScriptPath = path.resolve(ballerinaPath, "lib", "tools", "debug-adapter", "launcher", "debug-adapter-launcher.bat");
            }

            console.info("Found debug adapter {} with args {}", startScriptPath);
            const serverProcess = child_process.spawn(startScriptPath);

            serverProcess.stdout.on('data', (data) => {
                if (data.toString().includes('Debug server started')) {
                    resolve(new DebugAdapterServer(4711));
                }
            });
            
            serverProcess.stderr.on('data', (data) => {
                console.log(`stderr: ${data}`);
                reject(data);
            });
        });
    }
}