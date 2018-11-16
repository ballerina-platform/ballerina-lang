import { CompilationUnit, traversNode } from "@ballerina/ast-model";
import React from "react";
import { SvgCanvas } from "../views";
import { visitor as positioningVisitor } from "../visitors/positioning-visitor";
import { visitor as sizingVisitor } from "../visitors/sizing-visitor";
import { DiagramContext, DiagramMode, IDiagramContext } from "./diagram-context";
import { DiagramErrorBoundary } from "./diagram-error-boundary";
import { DiagramUtils } from "./diagram-utils";
import { EditToggleButton } from "./edit-toggle-button";
import { ModeToggleButton } from "./mode-toggle-button";

export interface CommonDiagramProps {
    height: number;
    width: number;
    zoom: number;
    mode: DiagramMode;
}
export interface DiagramProps extends CommonDiagramProps {
    ast?: CompilationUnit;
}

export interface DiagramState {
    currentMode: DiagramMode;
}

export class Diagram extends React.Component<DiagramProps, DiagramState> {

    // get default context or provided context from a parent (if any)
    public static contextType = DiagramContext;

    public state = {
        currentMode: DiagramMode.ACTION,
    };

    public render() {
        const { ast } = this.props;
        const { currentMode } = this.state;
        const children: any = [];

        if (ast) {
            // Calculate dimention of AST Nodes.
            traversNode(ast, sizingVisitor);
            // Calculate positions of the AST Nodes.
            traversNode(ast, positioningVisitor);
            // Get React components for AST Nodes.
            children.push(DiagramUtils.getComponents(ast.topLevelNodes));
        }

        return <DiagramContext.Provider value={this.createContext()}>
            <DiagramErrorBoundary>
                <div>
                    <div>{"Current Diagram Mode: " + DiagramMode[currentMode]}</div>
                    <div>{"Editing Enabled: " + this.context.editingEnabled}</div>
                    <EditToggleButton />
                    <ModeToggleButton />
                </div>
                <SvgCanvas>
                    {children}
                </SvgCanvas>
            </DiagramErrorBoundary>
        </DiagramContext.Provider>;
    }

    private createContext(): IDiagramContext {
        const { ast } = this.props;
        const { currentMode } = this.state;
        // create context contributions
        const contextContributions = {
            ast,
            changeMode: (newMode: DiagramMode) => {
                this.setState({
                    currentMode: newMode,
                });
            },
            mode: currentMode,
        };

        // merge with parent (if any) or with default context
        return { ...this.context, ...contextContributions };
    }
}
