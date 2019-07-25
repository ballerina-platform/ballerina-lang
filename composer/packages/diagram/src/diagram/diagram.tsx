import { ASTNode, ASTUtil } from "@ballerina/ast-model";
import { IBallerinaLangClient, ProjectAST } from "@ballerina/lang-service";
import panzoom, { PanZoom } from "panzoom";
import React from "react";
import { DefaultConfig } from "../config/default";
import { CompilationUnitViewState, ViewState } from "../view-model/index";
import { SvgCanvas } from "../views";
import { visitor as hiddenBlockVisitor } from "../visitors/hidden-block-visitor";
import { visitor as initVisitor } from "../visitors/init-visitor";
import { setMaxInvocationDepth, setProjectAST, visitor as invocationVisitor
    } from "../visitors/invocation-expanding-visitor";
import { visitor as interactionModeVisitor } from "../visitors/mode-visitors/interaction-mode-visitor";
import { visitor as statementModeVisitor } from "../visitors/mode-visitors/statement-mode-visitor";
import { visitor as positioningVisitor } from "../visitors/positioning-visitor";
import { visitor as sizingVisitor } from "../visitors/sizing-visitor";
import { DiagramContext, DiagramMode, IDiagramContext } from "./diagram-context";
import { DiagramUtils } from "./diagram-utils";
import { ConstructIdentifier } from "./overview";

export interface CommonDiagramProps {
    height?: number;
    width?: number;
    zoom: number;
    mode: DiagramMode;
    langClient: IBallerinaLangClient;
    maxInvocationDepth?: number;
    sourceRootUri?: string;
    initialSelectedConstruct?: ConstructIdentifier;
    docUri: string;
}
export interface DiagramProps extends CommonDiagramProps {
    ast?: ASTNode;
    projectAst?: ProjectAST;
    setPanZoomComp?: (comp: PanZoom | undefined, element: SVGGElement | undefined) => void;
}

export interface DiagramState {
    currentMode: DiagramMode;
    currentZoom: number;
}

export class Diagram extends React.Component<DiagramProps, DiagramState> {

    // get default context or provided context from a parent (if any)
    public static contextType = DiagramContext;

    private containerRef = React.createRef<HTMLDivElement>();
    private panZoomRootRef: React.RefObject<SVGAElement>;
    private panZoomRootRefCurrent: SVGAElement | null | undefined;
    private panZoomComp: PanZoom | undefined;

    constructor(props: DiagramProps) {
        super(props);
        this.state = {
            currentMode: this.props.mode,
            currentZoom: this.props.zoom
        };
        this.panZoomRootRef = React.createRef<SVGAElement>();
    }

    public componentDidMount() {
        this.createPanZoom();
    }

    public componentWillUnmount() {
        this.disposePanZoom();
    }

    public componentDidUpdate() {
        if (this.panZoomRootRef.current === this.panZoomRootRefCurrent) {
            // pan-zoom root component is not updated
            return;
        }

        if (!this.panZoomRootRef.current) {
            // pan-zoom root component is unmounted
            this.disposePanZoom();
        }

        this.createPanZoom();
    }

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
        setMaxInvocationDepth(this.props.maxInvocationDepth === undefined ? -1 : this.props.maxInvocationDepth);
        ASTUtil.traversNode(ast, invocationVisitor);
        if (this.props.mode === DiagramMode.INTERACTION) {
            ASTUtil.traversNode(ast, interactionModeVisitor);
        } else {
            ASTUtil.traversNode(ast, statementModeVisitor);
        }
        // Mark hidden blocks
        ASTUtil.traversNode(ast, hiddenBlockVisitor);
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
                    panZoomRootRef = {this.panZoomRootRef}
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

    private createPanZoom() {
        if (!this.panZoomRootRef.current) {
            return;
        }

        this.panZoomRootRefCurrent = this.panZoomRootRef.current;
        this.panZoomComp = panzoom(this.panZoomRootRef.current, {
            beforeWheel: (e) => {
                // allow wheel-zoom only if ctrl is down.
                return !e.ctrlKey;
            },
            smoothScroll: false,
        });
        if (this.props.setPanZoomComp) {
            this.props.setPanZoomComp(this.panZoomComp, this.panZoomRootRef.current);
        }
    }

    private disposePanZoom() {
        if (this.panZoomComp) {
            this.panZoomComp.dispose();
            if (this.props.setPanZoomComp) {
                this.props.setPanZoomComp(undefined, undefined);
            }
        }
        return;
    }
}
