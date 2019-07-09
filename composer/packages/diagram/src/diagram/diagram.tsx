import { ASTNode, ASTUtil } from "@ballerina/ast-model";
import { IBallerinaLangClient, ProjectAST } from "@ballerina/lang-service";
import React from "react";
import { DefaultConfig } from "../config/default";
import { CompilationUnitViewState, ViewState } from "../view-model/index";
import { SvgCanvas } from "../views";
import { visitor as initVisitor } from "../visitors/init-visitor";
import { setProjectAST, visitor as invocationVisitor } from "../visitors/invocation-expanding-visitor";
import { visitor as interactionModeVisitor } from "../visitors/mode-visitors/interaction-mode-visitor";
import { visitor as statementModeVisitor } from "../visitors/mode-visitors/statement-mode-visitor";
import { visitor as positioningVisitor } from "../visitors/positioning-visitor";
import { visitor as sizingVisitor } from "../visitors/sizing-visitor";
import { DiagramContext, DiagramMode, IDiagramContext } from "./diagram-context";
import { DiagramUtils } from "./diagram-utils";

export interface CommonDiagramProps {
    height?: number;
    width?: number;
    zoom: number;
    fitToWidthOrHeight: boolean;
    mode: DiagramMode;
    langClient: IBallerinaLangClient;
}
export interface DiagramProps extends CommonDiagramProps {
    ast?: ASTNode;
    projectAst?: ProjectAST;
    docUri: string;
}

export interface DiagramState {
    currentMode: DiagramMode;
    currentZoom: number;
}

export class Diagram extends React.Component<DiagramProps, DiagramState> {

    // get default context or provided context from a parent (if any)
    public static contextType = DiagramContext;

    public state = {
        currentMode: this.props.mode,
        currentZoom: this.props.zoom
    };

    private containerRef = React.createRef<HTMLDivElement>();

    public render() {
        const { ast, width, height, projectAst } = this.props;

        if (!ast || !projectAst || !DiagramUtils.isDrawable(ast)) {
            return null;
        }

        const children: React.ReactNode[] = [];

        // use default width/height if not provided
        const diagramWidth = width !== undefined ? width : DefaultConfig.canvas.width;
        const diagramHeight = height !== undefined ? height : DefaultConfig.canvas.height;

        const cuViewState: CompilationUnitViewState = new CompilationUnitViewState();
        cuViewState.container.w = diagramWidth;
        cuViewState.container.h = diagramHeight;

        // Initialize AST node view state
        ASTUtil.traversNode(ast, initVisitor);
        setProjectAST(projectAst);
        ASTUtil.traversNode(ast, invocationVisitor);
        if (this.props.mode === DiagramMode.INTERACTION) {
            ASTUtil.traversNode(ast, interactionModeVisitor);
        } else {
            ASTUtil.traversNode(ast, statementModeVisitor);
        }
        // Calculate dimention of AST Nodes.
        ASTUtil.traversNode(ast, sizingVisitor);
        // Calculate positions of the AST Nodes.
        ASTUtil.traversNode(ast, positioningVisitor);
        // Get React components for AST Nodes.
        children.push(DiagramUtils.getComponents(ast));

        return <DiagramContext.Provider value={this.createContext({
            h: (ast.viewState as ViewState).bBox.h,
            w: (ast.viewState as ViewState).bBox.w
        })}>
            <div className="diagram-container" ref={this.containerRef}>
                <SvgCanvas
                    width = {(ast.viewState as ViewState).bBox.w}
                    height = {(ast.viewState as ViewState).bBox.h}
                    zoom = {this.props.zoom}
                    fitToWidthOrHeight = {this.props.fitToWidthOrHeight}
                >
                    {children}
                </SvgCanvas>
            </div>
        </DiagramContext.Provider>;
    }

    private createContext(diagramSize: { w: number, h: number }): IDiagramContext {
        const { currentMode, currentZoom } = this.state;
        // create context contributions
        const contextContributions: Partial<IDiagramContext> = {
            ast: this.props.ast,
            containerRef: this.containerRef,
            docUri: this.props.docUri,
            langClient: this.props.langClient,
            mode: currentMode,
            update: () => {
                this.forceUpdate();
            },
            zoomLevel: currentZoom,
        };

        // merge with parent (if any) or with default context
        return { ...this.context, ...contextContributions };
    }
}
