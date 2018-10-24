
import {
    DebugConfigurationProvider, WorkspaceFolder, DebugConfiguration,
    ProviderResult, debug, ExtensionContext, window
} from 'vscode';
import { ballerinaExtInstance, BallerinaExtension } from '../core/index';

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

        return config;
    }
};

export function activateDebugConfigProvider(ballerinaExtInstance: BallerinaExtension) {
    let context = <ExtensionContext>ballerinaExtInstance.context;
    context.subscriptions.push(debug.registerDebugConfigurationProvider('ballerina', debugConfigProvider));
}