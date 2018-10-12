
import { DebugConfigurationProvider, WorkspaceFolder, DebugConfiguration, ProviderResult, debug, ExtensionContext } from 'vscode';
import { getPluginConfig, BallerinaPluginConfig } from '../utils/index';
import { BallerinaExtInstance } from '../core/index';

const debugConfigProvider: DebugConfigurationProvider = {
	resolveDebugConfiguration(folder: WorkspaceFolder, config: DebugConfiguration)
		: ProviderResult<DebugConfiguration> {
		if (!config['ballerina.home']) {
			// If ballerina.home is not defined in in debug config get it from workspace configs
			const workspaceConfig: BallerinaPluginConfig = getPluginConfig();
			if (workspaceConfig.home) {
				config['ballerina.home'] = workspaceConfig.home;
			} else {
				config['ballerina.home'] = BallerinaExtInstance.getBallerinaHome();
			}
		}
		return config;
	}
};

export function activateDebugConfigProvider(context: ExtensionContext) {
	context.subscriptions.push(debug.registerDebugConfigurationProvider('ballerina', debugConfigProvider));
}