
import {
    DebugConfigurationProvider, WorkspaceFolder, DebugConfiguration,
    ProviderResult, debug, ExtensionContext, window
} from 'vscode';
import { ballerinaExtInstance, BallerinaExtension } from '../core/index';
import { ExtendedLangClient } from 'src/core/extended-language-client';

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
}