import * as vscode from 'vscode';

import { BallerinaExtension } from '../core';
import { ProjectTreeProvider } from './project-overview';
import { TM_EVENT_OPEN_PROJECT_OVERVIEW_VIA_TREE_VIEW, CMP_PROJECT_OVERVIEW } from '../telemetry';

export function activate(ballerinaExtInstance: BallerinaExtension) {
    const reporter = ballerinaExtInstance.telemetryReporter;
    const projectTreeProvider = new ProjectTreeProvider(ballerinaExtInstance);
    vscode.window.registerTreeDataProvider('ballerinaProjectTree', projectTreeProvider);

    vscode.commands.registerCommand('ballerina.executeTreeElement',(moduleName, constructName, subConstructName) => {
        reporter.sendTelemetryEvent(TM_EVENT_OPEN_PROJECT_OVERVIEW_VIA_TREE_VIEW, { component: CMP_PROJECT_OVERVIEW });
        ballerinaExtInstance.projectTreeElementClicked({
            moduleName,
            constructName,
            subConstructName,
        });
    });
}