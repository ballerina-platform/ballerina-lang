import { CompilationUnit } from "@ballerina/ast-model";
import { IBallerinaLangClient, ProjectAST } from "@ballerina/lang-service";
import React, { createRef } from "react";
import { Dropdown, DropdownItemProps, List, ListItemProps, Menu } from "semantic-ui-react";
import { CommonDiagramProps, Diagram } from "./diagram";
import { DiagramMode } from "./diagram-context";

export interface OverviewProps extends CommonDiagramProps {
    langClient: IBallerinaLangClient;
    sourceRoot: string;
    initialCollapsedModules: {
        [name: string]: {
            collapsed: boolean,
            collapsedFunctions: string[]
        }
    };
}
export interface OverviewState {
    modules: ProjectAST;
    collapsedModules: {
        [name: string]: {
            collapsed: boolean,
            collapsedFunctions: string[]
        }
    };
    mode: DiagramMode;
    modeText: string;
}

export class Overview extends React.Component<OverviewProps, OverviewState> {
    private containerRef = createRef<HTMLDivElement>();

    constructor(props: OverviewProps) {
        super(props);
        this.handleModuleClick = this.handleModuleClick.bind(this);
        this.handleModeChange = this.handleModeChange.bind(this);
        this.state = {
            collapsedModules: props.initialCollapsedModules,
            mode: DiagramMode.INTERACTION,
            modeText: "Interaction",
            modules: {},
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
        return (
            <div>
                <Menu fixed={"top"}>
                    <Menu.Item>
                    <Dropdown text={this.state.modeText}>
                        <Dropdown.Menu>
                            <Dropdown.Item
                                text="Interaction"
                                value={DiagramMode.INTERACTION}
                                onClick={this.handleModeChange}/>
                            <Dropdown.Item
                                text="Statement"
                                value={DiagramMode.STATEMENT}
                                onClick={this.handleModeChange}/>
                        </Dropdown.Menu>
                    </Dropdown>
                    </Menu.Item>
                </Menu>
                <div style={{margin: "50px 15px 0px"}} ref={this.containerRef}>
                    <List relaxed divided>
                        {Object.keys(modules).map((moduleName) => {
                            const module = modules[moduleName];
                            return (
                                <List.Item key={module.name} onClick={this.handleModuleClick}
                                    data={{name: module.name}}>
                                    <List.Content>
                                        <List.Header as="a">{module.name}</List.Header>
                                        {this.state.collapsedModules[module.name] &&
                                        this.state.collapsedModules[module.name].collapsed &&
                                        Object.keys(module.compilationUnits).map((cUnitName) => {
                                            const cUnit = module.compilationUnits[cUnitName];
                                            const ast = cUnit.ast;
                                            return <Diagram ast={ast as CompilationUnit}
                                                zoom={1} height={0} width={1000}
                                                mode={this.state.mode} projectAst={modules}></Diagram>;
                                        })}
                                    </List.Content>
                                </List.Item>
                            );
                        })}
                    </List>
                </div>
            </div>
        );
    }

    private handleModuleClick(e: React.MouseEvent<HTMLAnchorElement, MouseEvent>, props: ListItemProps) {
        this.setState((state) => {
            let collapsedModule = state.collapsedModules[props.data.name];

            if (!collapsedModule) {
                collapsedModule = {
                    collapsed: true,
                    collapsedFunctions: [],
                };
            } else {
                collapsedModule.collapsed = !collapsedModule.collapsed;
            }

            return {
                collapsedModules: {
                    ...state.collapsedModules,
                    [props.data.name]: collapsedModule,
                }
            };
        });
    }

    private handleModeChange(e: React.MouseEvent<HTMLDivElement, MouseEvent>, props: DropdownItemProps) {
        if (props.value === DiagramMode.INTERACTION) {
            this.setState({
                mode: DiagramMode.INTERACTION,
                modeText: "Interaction",
            });
            return;
        }

        if (props.value === DiagramMode.STATEMENT) {
            this.setState({
                mode: DiagramMode.STATEMENT,
                modeText: "Statement",
            });
            return;
        }
    }
}
