import { CompilationUnit } from "@ballerina/ast-model";
import { IBallerinaLangClient, ProjectAST } from "@ballerina/lang-service";
import React from "react";
import { CommonDiagramProps, Diagram } from "./diagram";
import { DiagramMode } from "./diagram-context";

export interface OverviewProps extends CommonDiagramProps {
    langClient: IBallerinaLangClient;
    sourceRoot: string;
}
export interface OverviewState {
    modules: ProjectAST;
}

export class Overview extends React.Component<OverviewProps, OverviewState> {
    constructor(props: OverviewProps) {
        super(props);

        this.state = {
            modules: {}
        };
    }

    public updateAST() {
        const { langClient } = this.props;
        langClient.getProjectAST({ sourceRoot: this.props.sourceRoot}).then((result) => {
            console.log("<<<------", result);
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
            <div className="ui text container" style={{padding: "1em 0em"}}>
            <div className="ui relaxed divided list">
                {Object.keys(modules).map((moduleName) => {
                    const module = modules[moduleName];
                    return (
                        <div className="item" key={module.name}>
                            <div className="content">
                                <a className="header">{module.name}</a>
                                {Object.keys(module.compilationUnits).map((cUnitName) => {
                                    const cUnit = module.compilationUnits[cUnitName];
                                    const ast = cUnit.ast;
                                    return <Diagram ast={ast as CompilationUnit} zoom={1} height={100} width={1000}
                                        mode={DiagramMode.DEFAULT} projectAst={modules}></Diagram>;
                                })}
                            </div>
                        </div>
                    );
                })}
            </div>
            </div>
        );
    }
}
