import { WorkspaceConfiguration, workspace } from 'vscode';

export interface BallerinaPluginConfig extends WorkspaceConfiguration {
    home?: string;
    showLSErrors?: boolean;
    debugLog?: boolean;
    classpath?: string;
}

export function getPluginConfig() : BallerinaPluginConfig {
    return workspace.getConfiguration('ballerina');
}