import * as vscode from 'vscode';

/**
 * This class is a container class for Tree data required for Ballerina Project Overview.
 */
export class ProjectTreeElement extends vscode.TreeItem {
    constructor(
		public readonly label: string,
        public readonly collapsibleState: vscode.TreeItemCollapsibleState,
        public readonly command?: vscode.Command
	) {
		super(label, collapsibleState);
	}
}