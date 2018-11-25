import { ASTUtil, CompilationUnit } from "@ballerina/ast-model";
import React from "react";
import { DefaultConfig } from "../config/default";
import { CompilationUnitViewState } from "../view-model/index";
import { SvgCanvas } from "../views";
import { visitor as initVisitor } from "../visitors/init-visitor";
import { visitor as positioningVisitor } from "../visitors/positioning-visitor";
import { visitor as sizingVisitor } from "../visitors/sizing-visitor";
import { ControllerPanel } from "./controllers/controller-panel";
import { DiagramContext, DiagramMode, IDiagramContext } from "./diagram-context";
import { DiagramErrorBoundary } from "./diagram-error-boundary";
import { DiagramUtils } from "./diagram-utils";

const zoomFactor = 0.1;

export interface CommonDiagramProps {
    height?: number;
    width?: number;
    zoom: number;
    mode: DiagramMode;
}
export interface DiagramProps extends CommonDiagramProps {
    ast?: CompilationUnit;
}

export interface DiagramState {
    currentMode: DiagramMode;
    currentZoom: number;
}

export class Diagram extends React.Component<DiagramProps, DiagramState> {

    // get default context or provided context from a parent (if any)
    public static contextType = DiagramContext;

    public state = {
        currentMode: DiagramMode.ACTION,
        currentZoom: this.props.zoom
    };

    private containerRef = React.createRef<HTMLDivElement>();

    public componentDidMount() {
        ASTUtil.onTreeModified(() => {
            this.forceUpdate();
        });
    }

    public render() {
        const { ast, width, height } = this.props;
        const children: React.ReactNode[] = [];

        // use default width/height if not provided
        const diagramWidth = width ? width : DefaultConfig.canvas.width;
        const diagramHeight = height ? height : DefaultConfig.canvas.height;

        const cuViewState: CompilationUnitViewState = new CompilationUnitViewState();
        cuViewState.container.w = diagramWidth;
        cuViewState.container.h = diagramHeight;

        if (ast) {
            // Initialize AST node view state
            ASTUtil.traversNode(ast, initVisitor);
            // Set width and height to toplevel node.
            ast.viewState = cuViewState;
            // Calculate dimention of AST Nodes.
            ASTUtil.traversNode(ast, sizingVisitor);
            // Calculate positions of the AST Nodes.
            ASTUtil.traversNode(ast, positioningVisitor);
            // Get React components for AST Nodes.
            children.push(DiagramUtils.getComponents(ast.topLevelNodes));
        }

        return <DiagramContext.Provider value={this.createContext()}>
            <DiagramErrorBoundary>
                <div className="diagram-container" ref={this.containerRef}>
                    <ControllerPanel stickTo={this.containerRef} width={diagramWidth} />
                    <SvgCanvas model={cuViewState} zoom={this.state.currentZoom}>
                        {children}
                    </SvgCanvas>
                </div>
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
            zoomFit: () => {
                this.setState({
                    currentZoom: 1
                });
            },
            zoomIn: () => {
               this.setState({
                   currentZoom: this.state.currentZoom + zoomFactor
               });
            },
            zoomOut: () => {
                if ((this.state.currentZoom - zoomFactor) >= 1) {
                    this.setState({
                        currentZoom: this.state.currentZoom - zoomFactor
                    });
                }
            },
        };

        // merge with parent (if any) or with default context
        return { ...this.context, ...contextContributions };
    }
}
