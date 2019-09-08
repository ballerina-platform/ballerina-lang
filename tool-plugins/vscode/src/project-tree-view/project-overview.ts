import * as vscode from 'vscode';
import * as path from 'path';
import * as fs from 'fs';
import * as os from 'os';

import { BallerinaExtension, ExtendedLangClient } from '../core/index';
import { ProjectTreeElement } from './project-tree';

const errorNode: ProjectTreeElement = {
    name: "Couldn't create the project overview. Please make sure your code compiles successfully and refresh.",
    kind: 'Info',
};

const noBalFileNode: ProjectTreeElement = {
    name: "Please open a ballerina project to see the ballerina project overview",
    kind: 'Info',
};

interface ProjectAST {
    [moduleName: string]: {
        name: string;
        compilationUnits: {
            [compilationUnitName: string]: {
                name: string;
                ast: any;
                uri: string;
            };
        };
    };
}

const treeItemKinds = {
    PROJECT_ROOT: 'ProjectRoot',
    MODULE: 'Module',
    FUNCTION: 'Function',
    SERVICE: 'Service',
    RESOURCE: 'Resource',
};

const itemKindsWithIcons = [
    treeItemKinds.SERVICE,
    treeItemKinds.FUNCTION,
    treeItemKinds.RESOURCE
];

const collapsibleKinds = [
    treeItemKinds.PROJECT_ROOT,
    treeItemKinds.MODULE,
    treeItemKinds.SERVICE,
];

/**
 * This class will provide Tree Data required to draw the Ballerina Project Overview 
 * on the explorer panel. 
 */
export class ProjectTreeProvider implements vscode.TreeDataProvider<ProjectTreeElement> {

    private _onDidChangeTreeData: vscode.EventEmitter<ProjectTreeElement | undefined> = new vscode.EventEmitter<ProjectTreeElement | undefined>();
    readonly onDidChangeTreeData: vscode.Event<ProjectTreeElement | undefined> = this._onDidChangeTreeData.event;
    private langClient?: ExtendedLangClient;
    private ballerinaExtInstance!: BallerinaExtension;

    constructor(balExt: BallerinaExtension) {
        this.ballerinaExtInstance = balExt;
        this.langClient = balExt.langClient;

        vscode.window.onDidChangeActiveTextEditor((activatedTextEditor) => {
            if (activatedTextEditor && activatedTextEditor.document.languageId === "ballerina") {
                this.refresh();
            }
        });

        vscode.commands.registerCommand("ballerina.refreshProjectTree", () => {
            this.refresh();
        });
    }

    private refresh(): void {
        this._onDidChangeTreeData.fire();
	}

    getChildren(element?: ProjectTreeElement | undefined): vscode.ProviderResult<ProjectTreeElement[]> {
        if (!element) {
            return this.getRoots();
        } else {
            if (element.path) {
                return this.getProjectStructure(element.path);
            }
            if (element.topLevelNodes) {
                return element.topLevelNodes;
            }
            if (element.resources) {
                return element.resources;
            }
        }
    }

    getTreeItem(element: ProjectTreeElement): vscode.TreeItem | Thenable<vscode.TreeItem> {
        const {
            Expanded, None
        } = vscode.TreeItemCollapsibleState;
        const collapsibleState = (collapsibleKinds.indexOf(element.kind) > -1) ? Expanded : None;
        const { extensionPath } = this.ballerinaExtInstance.extension;

        const treeItem: vscode.TreeItem = {
            label: element.name,
            collapsibleState,
        };

        const sourceRoot = element.sourceRoot? vscode.Uri.file(element.sourceRoot).toString(): undefined;

        if (itemKindsWithIcons.indexOf(element.kind) > -1) {
            treeItem.iconPath = {
                light: vscode.Uri.file(
                    path.join(extensionPath, `resources/images/icons/${element.kind.toLowerCase()}.svg`)),
                dark: vscode.Uri.file(
                    path.join(extensionPath, `resources/images/icons/${element.kind.toLowerCase()}-inverse.svg`))
            };

            treeItem.command = {
                command: "ballerina.executeTreeElement",
                title: "Execute Tree Command",
                arguments: [
                    sourceRoot,
                    undefined,
                    element.moduleName, element.name]
            };

            if (element.kind === treeItemKinds.RESOURCE) {
                treeItem.command.arguments = [
                    sourceRoot,
                    undefined,
                    element.moduleName, element.serviceName, element.name ];
            }
        }

        if (element.kind === treeItemKinds.PROJECT_ROOT) {
            treeItem.tooltip = element.path;
        }

        return treeItem;
    }

    public getRoots(): ProjectTreeElement[] {
        const openFolders = vscode.workspace.workspaceFolders;
        let sourceRoots: ProjectTreeElement[] = [];
        if (openFolders) {
            sourceRoots = openFolders.filter((openFolder) => {
                return fs.existsSync(path.join(openFolder.uri.fsPath, "Ballerina.toml"));
            }).map((root) => {
                return {
                    kind: 'ProjectRoot',
                    name: root.name,
                    path: root.uri.fsPath,
                };
            });
        }

        const activeEditor = vscode.window.activeTextEditor;
        if ((activeEditor && activeEditor.document && activeEditor.document.languageId === "ballerina")) {
            const activePath = activeEditor.document.uri.path;
            const activeSourceRoot = this.getSourceRoot(activePath, path.parse(activePath).root);

            if (activeSourceRoot && (sourceRoots.filter((root) => (root.path === activeSourceRoot)).length === 0)) {
                sourceRoots.push({
                    kind: 'ProjectRoot',
                    name: path.parse(activeSourceRoot).base,
                    path: activeSourceRoot,
                });
            }
        }

        if (sourceRoots.length === 0) {
            sourceRoots.push(noBalFileNode);
        }

        return sourceRoots;
    }

    private getProjectStructure(sourceRoot: string): Promise<any> {
        return new Promise<any>((resolve) => {
            this.ballerinaExtInstance.onReady().then(() => {
                if (!this.langClient) {
                    resolve();
                    return;
                }

                this.langClient.getProjectAST(vscode.Uri.file(sourceRoot).toString()).then((result: any) => {
                    if (result.modules && (Object.keys(result.modules).length > 0)) {
                        const balProjectTree = this.buildProjectTree(result.modules, sourceRoot);
                        resolve(balProjectTree);
                    } else {
                        resolve([errorNode]);
                    }
                }, () => {
                    resolve([errorNode]);
                });

                return;
            });
        });
    }

    private buildProjectTree(modules: ProjectAST, sourceRoot: string): ProjectTreeElement[] {
        const moduleElementList: ProjectTreeElement[] = [];
        Object.keys(modules).forEach((moduleName) => {
            const module = modules[moduleName];
            const newModuleElement: ProjectTreeElement = {
                sourceRoot,
                name: moduleName,
                kind: 'Module',
                topLevelNodes: [],
            };

            const newModuleTopLevelNodes: ProjectTreeElement[] = [];

            Object.keys(module.compilationUnits).forEach((cUnitName) => {
                const cUnit = module.compilationUnits[cUnitName];
                cUnit.ast.topLevelNodes.forEach((topLevelNode: any) => {
                    if (topLevelNode.kind === 'Function') {
                        newModuleTopLevelNodes.push({
                            sourceRoot,
                            name: topLevelNode.name.value,
                            kind: 'Function',
                            moduleName,
                        });
                        return;
                    }

                    if (topLevelNode.kind === 'Service') {
                        const serviceElement: ProjectTreeElement = {
                            sourceRoot,
                            name: topLevelNode.name.value,
                            kind: 'Service',
                            moduleName,
                        };

                        serviceElement.resources = topLevelNode.resources.map((resource: any) => {
                            return {
                                sourceRoot,
                                name: resource.name.value,
                                kind: 'Resource',
                                moduleName,
                                serviceName: topLevelNode.name.value,
                            };
                        });

                        newModuleTopLevelNodes.push(serviceElement);
                        return;
                    }
                });
            });
            newModuleTopLevelNodes.sort((node1, node2) => node1.name.localeCompare(node2.name));
            newModuleElement.topLevelNodes = newModuleTopLevelNodes;

            moduleElementList.push(newModuleElement);
        });

        moduleElementList.sort((node1, node2) => node1.name.localeCompare(node2.name));
        return moduleElementList;
    }

    /**
     * Util method to get Ballerina project root.
     * 
     * @param currentPath - current active path
     * @param root - root path
     */
    private getSourceRoot(currentPath: string, root: string): string|undefined {
        if (fs.existsSync(path.join(currentPath, 'Ballerina.toml'))) {
            if (currentPath !== os.homedir()) {
                return currentPath;
            }
        }

        if (currentPath === root) {
            return;
        }

        return this.getSourceRoot(path.dirname(currentPath), root);
    }

    getParent(element: ProjectTreeElement): vscode.ProviderResult<ProjectTreeElement> {
        // This is implemented in-order to make treeView#reveal api work.
        // returns undefined for the moment, indicates no parent. It won't be a problem
        // as we only use reveal api to reveal root element
        return undefined;
    }
}
