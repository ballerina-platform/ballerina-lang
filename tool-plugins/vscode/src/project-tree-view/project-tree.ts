import * as vscode from 'vscode';

/**
 * This class is a container class for Tree data required for Ballerina Project Overview.
 */
export interface ProjectTreeElement {
	kind: string;
	name: string;

	// only for project roots
	path?: string;

	// source root of constructs
	sourceRoot?: string;

	moduleName?: string;

	serviceName?: string;

	//only for services
	resources?: ProjectTreeElement[];

	// only for modules
	topLevelNodes?: ProjectTreeElement[];
}