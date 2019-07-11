import * as vscode from 'vscode';

import { BallerinaExtension } from '../core';
import { ProjectTreeProvider } from './project-overview';

export function activate(ballerinaExtInstance: BallerinaExtension) {
    ballerinaExtInstance.onReady().then(() => {
        const projectTreeProvider = new ProjectTreeProvider(ballerinaExtInstance);
        vscode.window.registerTreeDataProvider('ballerinaProjectTree', projectTreeProvider);

        vscode.commands.registerCommand('extension.openPackageOnNpm',(testing) => {
            //TODO Command Execute Logic
            console.log(testing);
        });
    });
}