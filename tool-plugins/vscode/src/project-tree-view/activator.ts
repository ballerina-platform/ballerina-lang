import * as vscode from 'vscode';

import { BallerinaExtension } from '../core';
import { ProjectTreeProvider } from './project-overview';

export function activate(ballerinaExtInstance: BallerinaExtension) {
    const projectTreeProvider = new ProjectTreeProvider(ballerinaExtInstance);
    vscode.window.registerTreeDataProvider('ballerinaProjectTree', projectTreeProvider);

    vscode.commands.registerCommand('ballerina.executeTreeElement',(moduleName, constructName) => {
        ballerinaExtInstance.projectTreeElementClicked({
            moduleName,
            constructName,
        });
    });
}