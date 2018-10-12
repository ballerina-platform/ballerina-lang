
import { DebugConfigurationProvider, WorkspaceFolder, DebugConfiguration, ProviderResult, debug, ExtensionContext } from 'vscode';
import { ballerinaExtInstance, BallerinaExtension } from '../core/index';

const debugConfigProvider: DebugConfigurationProvider = {
	resolveDebugConfiguration(folder: WorkspaceFolder, config: DebugConfiguration)
		: ProviderResult<DebugConfiguration> {
		const ballerinaHome = ballerinaExtInstance.getBallerinaHome();
		if (!ballerinaHome) {
			ballerinaExtInstance.showMessageInstallBallerina();
		} else {
			config['ballerina.home'] = ballerinaHome;
		}
		return config;
	}
};

export function activateDebugConfigProvider(ballerinaExtInstance: BallerinaExtension) {
    let context = <ExtensionContext> ballerinaExtInstance.context;
	context.subscriptions.push(debug.registerDebugConfigurationProvider('ballerina', debugConfigProvider));
}