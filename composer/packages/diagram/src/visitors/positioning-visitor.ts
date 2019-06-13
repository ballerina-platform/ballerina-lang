import {
    ASTKindChecker, ASTUtil, Block, CompilationUnit, Foreach,
    Function as BalFunction, If, Lambda, Match, MatchStaticPatternClause, ObjectType, Service,
    TypeDefinition, Variable, VariableDef, VisibleEndpoint, Visitor, While
} from "@ballerina/ast-model";
import { DiagramConfig } from "../config/default";
import { DiagramUtils } from "../diagram/diagram-utils";
import { BlockViewState } from "../view-model/block";
import { CompilationUnitViewState, FunctionViewState, StmntViewState, ViewState } from "../view-model/index";
import { WorkerViewState } from "../view-model/worker";

const config: DiagramConfig = DiagramUtils.getConfig();

function positionWorkerLine(worker: WorkerViewState) {
    worker.lifeline.bBox.y = worker.bBox.y;
    worker.lifeline.bBox.x = worker.bBox.x + worker.bBox.leftMargin - (worker.lifeline.bBox.w / 2);
}

class PositioningVisitor implements Visitor {

    private epX: number = 0;
    private epY: number = 0;

    public beginVisitCompilationUnit(node: CompilationUnit) {

        this.epX = 0;
        this.epY = 0;
        const viewState: CompilationUnitViewState = node.viewState;

        // filter out visible children from top level nodes.
        const visibleChildren = node.topLevelNodes.filter((child) => {
            return ASTKindChecker.isFunction(child)
                || ASTKindChecker.isService(child)
                || ASTUtil.isValidObjectType(child);
        });

        let width = 0;
        // Set the height of the toplevel nodes so that the other nodes would be positioned relative to it
        let height = config.canvas.padding.top;
        visibleChildren.forEach((child, index) => {
            // we will get the maximum node's width.
            if (width <= child.viewState.bBox.w) {
                width = child.viewState.bBox.w;
            }
            // for each top level node set x and y.
            child.viewState.bBox.x = config.panel.gutter.h;
            // const hasTopMargin = (index > 0) ? 1 : 0; // this is to remove top margin of the first panel.
            child.viewState.bBox.y = height;
            height = config.panel.gutter.v + child.viewState.bBox.h + height;
        });
        // add the padding for the width
        width += (config.panel.gutter.h * 2);
        // add the bottom padding to the canvas height.
        height += (config.panel.gutter.v * 2);
        // if  the view port is taller we take the height of the view port.
        height = (height > viewState.container.h) ? height : viewState.container.h;

        // re-adjust the width of each node to fill the container.
        if (width < viewState.container.w) {
            width = viewState.container.w;
        }
        // set all the children to same width.
        visibleChildren.forEach((child) => {
            child.viewState.bBox.w = width - (config.panel.gutter.h * 2);
        });

        viewState.bBox.h = height;
        viewState.bBox.w = width;

    }

    // tslint:disable-next-line:ban-types
    public beginVisitFunction(node: BalFunction) {
        if (node.lambda || !node.body) { return; }
        const viewState: FunctionViewState = node.viewState;
        const defaultWorker: WorkerViewState = node.viewState.defaultWorker;
        const bodyViewState: ViewState = node.body.viewState;

        let workerX: number;
        let workerY: number;

        const workers = node.body.statements.filter((element: any) => ASTUtil.isWorker(element));
        let visibleEndpoints: VisibleEndpoint[] = [];
        if (node.VisibleEndpoints) {
            visibleEndpoints = node.VisibleEndpoints.filter((ep) => !ep.caller && ep.viewState.visible);
            if (!(node.viewState as FunctionViewState).isExpandedFunction && !node.resource) {
                node.VisibleEndpoints.forEach((ep) => {
                    // Find of one of the visible endpoints is actually a parameter to the function
                    if (node.parameters) {
                        node.parameters.forEach((p, i) => {
                            let variableName = "";
                            if (ASTKindChecker.isVariable(p)) {
                                variableName = p.name.value;
                            } else if (ASTKindChecker.isVariable(p.variable)) {
                                variableName = p.variable.name.value;
                            }

                            if (variableName === ep.name) {
                                // ep is a parameter to the function which is an endpoint
                                visibleEndpoints.push(ep);
                            }
                        });
                    }
                });
            }
        }

        // They way we position function components depends on whether this is an expanded function
        // or a regular functions
        if (viewState.isExpandedFunction) {
            bodyViewState.bBox.x = viewState.bBox.x + bodyViewState.bBox.leftMargin;
            bodyViewState.bBox.y = viewState.bBox.y + config.statement.expanded.header;

            viewState.client.bBox.x = viewState.bBox.x;
            viewState.client.bBox.w = 0;

            workerX = viewState.bBox.x + bodyViewState.bBox.w + config.lifeLine.gutter.h;
            workerY = bodyViewState.bBox.y;

            if (viewState.containsOtherLifelines) {
                bodyViewState.bBox.y += config.lifeLine.header.height +
                + config.statement.height; // leave room for start line.
            }
        } else {
            // Position the header
            viewState.header.x = viewState.bBox.x;
            viewState.header.y = viewState.bBox.y;
            // Position the body
            viewState.body.x = viewState.bBox.x;
            viewState.body.y = viewState.header.y + viewState.header.h;
            // Position client line
            viewState.client.bBox.x = viewState.bBox.x + config.panel.padding.left;
            viewState.client.bBox.y = viewState.body.y + config.panel.padding.top;
            // Position default worker
            defaultWorker.bBox.x = viewState.client.bBox.x + viewState.client.bBox.w + config.lifeLine.gutter.h;
            defaultWorker.bBox.y = viewState.client.bBox.y;
            // Position default worker lifeline.
            positionWorkerLine(defaultWorker);
                    // Position the body block node
            bodyViewState.bBox.x = defaultWorker.bBox.x + defaultWorker.bBox.leftMargin;
            bodyViewState.bBox.y = defaultWorker.bBox.y + config.lifeLine.header.height
                + config.statement.height; // leave room for start line.
            workerX = defaultWorker.bBox.x + defaultWorker.bBox.w + config.lifeLine.gutter.h;
            workerY = defaultWorker.bBox.y;
        }

        // Position the other workers
        workers.forEach((worker) => {
            const workerViewState: WorkerViewState = worker.viewState;
            const variable: Variable = ((worker as VariableDef).variable as Variable);
            const lambda: Lambda = (variable.initialExpression as Lambda);
            const functionNode = lambda.functionNode;
            // Position worker lifeline
            workerViewState.lifeline.bBox.y = workerY;
            workerViewState.lifeline.bBox.x = workerX;
            // Position worker body
            let leftMargin = functionNode.body!.viewState.bBox.leftMargin;
            leftMargin = (leftMargin === 0) ? 60 : leftMargin;
            functionNode.body!.viewState.bBox.x = workerX + leftMargin;
            workerX = workerX + functionNode.body!.viewState.bBox.w + leftMargin;
            functionNode.body!.viewState.bBox.y = workerY
            + functionNode.body!.viewState.paddingTop
            + config.lifeLine.header.height;
        });

        this.epX = workerX + config.lifeLine.gutter.h;
        this.epY = workerY;
        // Position endpoints
        visibleEndpoints.forEach((endpoint: VisibleEndpoint) => {
            endpoint.viewState.bBox.x = this.epX;
            endpoint.viewState.bBox.y = this.epY;
            this.epX = this.epX + endpoint.viewState.bBox.w + config.lifeLine.gutter.h;
        });

        // Position drop down menu for adding workers and endpoints
        viewState.menuTrigger.x = this.epX;
        viewState.menuTrigger.y = defaultWorker.bBox.y + config.lifeLine.header.height / 2;

        // Update the width of children
        viewState.body.w = viewState.bBox.w;
        viewState.header.w = viewState.bBox.w;
    }

    public beginVisitBlock(node: Block) {
        const viewState: BlockViewState = node.viewState;
        let height = 0;

        node.statements.forEach((element) => {
            if (ASTUtil.isWorker(element)) {
                height += config.statement.height;
                return;
            }

            const elViewState: StmntViewState = element.viewState;

            elViewState.bBox.x = viewState.bBox.x;
            elViewState.bBox.y = viewState.bBox.y + elViewState.bBox.paddingTop + height;
            height += elViewState.bBox.h + elViewState.bBox.paddingTop;
            if (elViewState.expandContext) {
                if (elViewState.expandContext.expandedSubTree) {
                    const expandedSubTree = elViewState.expandContext.expandedSubTree;
                    const expandedFunctionVS = expandedSubTree.viewState as FunctionViewState;

                    expandedFunctionVS.bBox.x = elViewState.bBox.x;
                    expandedFunctionVS.bBox.y = elViewState.bBox.y;

                    ASTUtil.traversNode(expandedSubTree, this);
                }
            }
        });

        if (node.parent && ASTKindChecker.isFunction(node.parent)) {
            const functionViewState = node.parent.viewState as FunctionViewState;
            // Position the implicit return if not hidden
            if (!functionViewState.implicitReturn.hidden) {
                functionViewState.implicitReturn.bBox.x = node.viewState.bBox.x;
                functionViewState.implicitReturn.bBox.y = node.viewState.bBox.y + (node.viewState.bBox.h / 2);

                if (node.statements.length > 0) {
                    const lastStatement = node.statements[node.statements.length - 1];
                    const lsvs: StmntViewState = lastStatement.viewState;
                    functionViewState.implicitReturn.bBox.y = lsvs.bBox.y + lsvs.bBox.h;
                }
            }
        }

        viewState.menuTrigger = {
            x: viewState.bBox.x,
            y: viewState.bBox.y + viewState.bBox.h - config.block.menuTriggerMargin
        };
        viewState.hoverRect.x = viewState.bBox.x - viewState.hoverRect.leftMargin;
        viewState.hoverRect.y = viewState.bBox.y;
    }

    public beginVisitVariableDef(node: VariableDef) {
        if (ASTUtil.isWorker(node)) {
            const variable = node.variable;
            const lambda: Lambda = variable.initialExpression as Lambda;
            const functionNode = lambda.functionNode;
            if (functionNode.VisibleEndpoints) {
                // Position endpoints
                functionNode.VisibleEndpoints.forEach((endpoint: VisibleEndpoint) => {
                    endpoint.viewState.bBox.x = this.epX;
                    endpoint.viewState.bBox.y = this.epY;
                    this.epX = this.epX + endpoint.viewState.bBox.w + config.lifeLine.gutter.h;
                });
            }
        }
    }

    public beginVisitWhile(node: While) {
        const viewState: ViewState = node.viewState;
        node.body.viewState.bBox.x = viewState.bBox.x;
        node.body.viewState.bBox.y = viewState.bBox.y + + config.flowCtrl.condition.bottomMargin
            + config.flowCtrl.condition.height;
        if (node.VisibleEndpoints) {
            // Position endpoints
            node.VisibleEndpoints.forEach((endpoint: VisibleEndpoint) => {
                endpoint.viewState.bBox.x = this.epX;
                endpoint.viewState.bBox.y = this.epY;
                this.epX = this.epX + endpoint.viewState.bBox.w + config.lifeLine.gutter.h;
            });
        }
    }

    public beginVisitForeach(node: Foreach) {
        const viewState: ViewState = node.viewState;
        node.body.viewState.bBox.x = viewState.bBox.x;
        node.body.viewState.bBox.y = viewState.bBox.y + config.flowCtrl.foreach.height;
        if (node.VisibleEndpoints) {
            // Position endpoints
            node.VisibleEndpoints.forEach((endpoint: VisibleEndpoint) => {
                endpoint.viewState.bBox.x = this.epX;
                endpoint.viewState.bBox.y = this.epY;
                this.epX = this.epX + endpoint.viewState.bBox.w + config.lifeLine.gutter.h;
            });
        }
    }

    public beginVisitIf(node: If) {
        const viewState: ViewState = node.viewState;
        node.body.viewState.bBox.x = viewState.bBox.x;
        node.body.viewState.bBox.y = viewState.bBox.y + config.flowCtrl.condition.bottomMargin
            + config.flowCtrl.condition.height;

        if (node.elseStatement) {
            node.elseStatement.viewState.bBox.x = viewState.bBox.x + node.body.viewState.bBox.w;
            node.elseStatement.viewState.bBox.y = viewState.bBox.y +
                config.flowCtrl.condition.height + node.body.viewState.bBox.h;
        }
        if (node.VisibleEndpoints) {
            // Position endpoints
            node.VisibleEndpoints.forEach((endpoint: VisibleEndpoint) => {
                endpoint.viewState.bBox.x = this.epX;
                endpoint.viewState.bBox.y = this.epY;
                this.epX = this.epX + endpoint.viewState.bBox.w + config.lifeLine.gutter.h;
            });
        }
    }

    public beginVisitService(node: Service) {
        const viewState: ViewState = node.viewState;
        let y = viewState.bBox.y + config.panelGroup.header.height;
        // tslint:disable-next-line:ban-types
        node.resources.forEach((element: BalFunction) => {
            element.viewState.bBox.x = viewState.bBox.x;
            element.viewState.bBox.y = y;
            y += element.viewState.bBox.h;
        });
    }

    public beginVisitTypeDefinition(node: TypeDefinition) {
        // If it is a service do nothing.
        if (node.service || !ASTUtil.isValidObjectType(node)) { return; }
        const viewState: ViewState = node.viewState;
        let y = viewState.bBox.y + config.panelGroup.header.height;
        // tslint:disable-next-line:ban-types
        (node.typeNode as ObjectType).functions.forEach((element: BalFunction) => {
            element.viewState.bBox.x = viewState.bBox.x;
            element.viewState.bBox.y = y;
            y += element.viewState.bBox.h;
        });
    }

    public beginVisitMatchStaticPatternClause(node: MatchStaticPatternClause) {
        const viewState: ViewState = node.viewState;
        node.statement.viewState.bBox.x = viewState.bBox.x;
        node.statement.viewState.bBox.y = viewState.bBox.y
        + config.statement.height; // To print literal;
    }

    public beginVisitMatch(node: Match) {
        const viewState: ViewState = node.viewState;
        let height = config.frame.topMargin + config.frame.header.height;
        node.patternClauses.forEach((element) => {
            element.viewState.bBox.x = viewState.bBox.x;
            element.viewState.bBox.y = viewState.bBox.y + height;
            height += element.viewState.bBox.h;
        });
    }

    public endVisitFunction(node: BalFunction) {
        const viewState: FunctionViewState = node.viewState;
        viewState.menuTrigger.x = this.epX;
    }
}

export const visitor = new PositioningVisitor();
