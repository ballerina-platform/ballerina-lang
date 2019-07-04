import { ASTKindChecker, ASTNode, ASTUtil } from "@ballerina/ast-model";
import { IBallerinaLangClient, ProjectAST } from "@ballerina/lang-service";
import React from "react";
import { DropdownItemProps, List, ListItemProps, Loader } from "semantic-ui-react";
import { CommonDiagramProps, Diagram } from "./diagram";
import { DiagramMode } from "./diagram-context";
import { DiagramUtils } from "./diagram-utils";
import { TopMenu } from "./top-menu";

const modes = [
    {
        text: "Interaction",
        type: DiagramMode.INTERACTION
    },
    {
        text: "Statements",
        type: DiagramMode.STATEMENT
    },
];

export interface OverviewProps extends CommonDiagramProps {
    langClient: IBallerinaLangClient;
    sourceRoot: string;
}
export interface OverviewState {
    modules: ProjectAST;
    selectedConstruct?: {
        moduleName: string;
        constructName: string;
    } | undefined;
    mode: DiagramMode;
    modeText: string;
    fitToWidthOrHeight: boolean;
    zoom: number;
}

export class Overview extends React.Component<OverviewProps, OverviewState> {
    constructor(props: OverviewProps) {
        super(props);
        this.handleModeChange = this.handleModeChange.bind(this);
        this.handleConstructClick = this.handleConstructClick.bind(this);
        this.handleBackClick = this.handleBackClick.bind(this);
        this.handleFitClick = this.handleFitClick.bind(this);
        this.handleZoomIn = this.handleZoomIn.bind(this);
        this.handleZoomOut = this.handleZoomOut.bind(this);
        this.handleConstructNameSelect = this.handleConstructNameSelect.bind(this);
        this.handleModuleNameSelect = this.handleModuleNameSelect.bind(this);
        this.state = {
            fitToWidthOrHeight: true,
            mode: DiagramMode.INTERACTION,
            modeText: "Interaction",
            modules: {},
            zoom: 0,
        };
    }

    public updateAST() {
        const { langClient } = this.props;
        langClient.getProjectAST({ sourceRoot: this.props.sourceRoot}).then((result) => {
            this.setState({
                modules: result.modules,
            });
        });
    }

    public componentDidMount() {
        this.updateAST();
    }

    public render() {
        const { modules } = this.state;

        if (!this.state.selectedConstruct) {
            return this.renderModulesList();
        }

        const selectedModule = this.state.selectedConstruct.moduleName;
        const selectedConstruct = this.state.selectedConstruct.constructName;
        const moduleList = this.getModuleList();

        const moduleNames: string[] = [];
        const constructNames: string[] = [];
        let selectedAST;
        let selectedUri = "";

        moduleList.forEach((module) => {
            moduleNames.push(module.name);

            if (selectedModule === module.name) {
                module.nodeInfo.forEach((nodeI) => {
                    const nodeName = (nodeI.node as any).name.value;
                    constructNames.push(nodeName);

                    if (selectedConstruct && (nodeName === selectedConstruct)) {
                        selectedAST = nodeI.node;
                        selectedUri = nodeI.uri;
                    }
                });
            }
        });

        return (
            <div>
                <TopMenu
                    modes={modes}
                    moduleNames={moduleNames}
                    constructNames={constructNames}
                    handleModeChange={this.handleModeChange}
                    selectedModeText={this.state.modeText}
                    handleBackClick={this.handleBackClick}
                    handleFitClick={this.handleFitClick}
                    handleZoomIn={this.handleZoomIn}
                    handleZoomOut={this.handleZoomOut}
                    fitActive={this.state.fitToWidthOrHeight}
                    selectedModuleName={selectedModule}
                    selectedConstructName={selectedConstruct}
                    handleConstructNameSelect={this.handleConstructNameSelect}
                    handleModuleNameSelect={this.handleModuleNameSelect}
                />
                <div>
                <Diagram ast={selectedAST}
                    langClient={this.props.langClient}
                    projectAst={modules}
                    docUri={selectedUri}
                    zoom={this.state.zoom} height={0} width={1000}
                    fitToWidthOrHeight={this.state.fitToWidthOrHeight}
                    mode={this.state.mode}>
                </Diagram>
                </div>
            </div>
        );
    }

    private renderModulesList() {
        const modules = this.getModuleList().map((module) => {
            return {
                name: module.name,
                nodes: module.nodeInfo.map((nodeInfo) => (nodeInfo.node))
            };
        });

        if (!(modules.length > 0)) {
            return <Loader active/>;
        }

        return <div className="overview">
            <List relaxed>
                {modules.map((module) => (
                    <List.Item className="item-wrapper" key={module.name}>
                        <List.Content>
                            <List.Header className="list-item-header" >{module.name}</List.Header>
                            <div>
                                { this.renderConstructsList(module) }
                            </div>
                        </List.Content>
                    </List.Item>)
                )}
            </List>
        </div>;
    }

    private renderConstructsList(module: { name: string; nodes: ASTNode[]; }) {
        return (
            <List>
                {module.nodes.filter((node) => (DiagramUtils.isDrawable(node)))
                    .map((node) => {
                        const nodeName = (node as any).name.value;
                        return (
                            <List.Item
                                key={nodeName}
                                data={{ moduleName: module.name, constructName: nodeName }}
                                onClick={this.handleConstructClick}
                            >
                                <List.Content>
                                    <List.Header as="a">{(node as any).name.value}</List.Header>
                                </List.Content>
                            </List.Item>
                        );
                    })}
            </List>
        );
    }

    private getModuleList(): Array<{name: string, nodeInfo: Array<{node: ASTNode, uri: string}>}> {
        const { modules } = this.state;
        const moduleList: Array<{name: string, nodeInfo: Array<{node: ASTNode, uri: string}>}>  = [];

        Object.keys(modules).map((moduleName) => {
            const module = modules[moduleName];
            const newModule: {name: string, nodeInfo: Array<{node: ASTNode, uri: string}>}
                = { name: module.name, nodeInfo: [] };

            Object.keys(module.compilationUnits).forEach((cUnitName) => {
                const cUnit = module.compilationUnits[cUnitName];

                cUnit.ast.topLevelNodes.forEach((topLevelNode) => {
                    const node = topLevelNode as ASTNode;
                    if (node.ws && DiagramUtils.isDrawable(node)) {
                        if (ASTKindChecker.isTypeDefinition(node)
                            && (node.service || !ASTUtil.isValidObjectType(node))) {
                            return;
                        }
                        newModule.nodeInfo.push({uri: cUnit.uri, node: (node as ASTNode)});
                    }
                });
            });

            moduleList.push(newModule);
        });

        return moduleList;
    }

    private handleConstructClick(e: React.MouseEvent<HTMLAnchorElement, MouseEvent>, props: ListItemProps) {
        this.setState({
            selectedConstruct: {
                constructName: props.data.constructName,
                moduleName: props.data.moduleName,
            }
        });
    }

    private handleModeChange(e: React.MouseEvent<HTMLDivElement, MouseEvent>, props: DropdownItemProps) {
        this.setState({
            mode: props.data.type,
            modeText: props.data.text,
        });
    }

    private handleBackClick() {
        this.setState({
            selectedConstruct: undefined,
        });
    }

    private handleFitClick() {
        this.setState((state) => ({
            fitToWidthOrHeight: !state.fitToWidthOrHeight,
            zoom: 0,
        }));
    }

    private handleZoomIn() {
        this.setState((state) => ({
            fitToWidthOrHeight: false,
            zoom: state.zoom + 1,
        }));
    }

    private handleZoomOut() {
        this.setState((state) => ({
            fitToWidthOrHeight: false,
            zoom: state.zoom - 1,
        }));
    }

    private handleModuleNameSelect(e: React.MouseEvent<HTMLDivElement, MouseEvent>, props: DropdownItemProps) {
        const moduleList = this.getModuleList();
        const selectedModule = moduleList.find((module) => (module.name === props.data.name));
        if (!selectedModule) {
            return;
        }

        this.setState({
            selectedConstruct: {
                constructName: (selectedModule.nodeInfo[0].node as any).name.value,
                moduleName: props.data.name,
            }
        });
    }

    private handleConstructNameSelect(e: React.MouseEvent<HTMLDivElement, MouseEvent>, props: DropdownItemProps) {
        this.setState((state) => ({
            selectedConstruct: {
                ...state.selectedConstruct!,
                constructName: props.data.name,
            }
        }));
    }
}
