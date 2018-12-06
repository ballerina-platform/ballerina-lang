import {
    Assignment, ASTNode, ASTUtil,
    Block, ExpressionStatement, Foreach, Function, If, Invocation,
    Lambda, Return, Service, Variable, VariableDef, VisibleEndpoint, Visitor, While
} from "@ballerina/ast-model";
import { DiagramConfig } from "../config/default";
import { DiagramUtils } from "../diagram/diagram-utils";
import { EndpointViewState, FunctionViewState, SimpleBBox, StmntViewState, ViewState } from "../view-model";
import { BlockViewState } from "../view-model/block";
import { ReturnViewState } from "../view-model/return";
import { WorkerViewState } from "../view-model/worker";

const config: DiagramConfig = DiagramUtils.getConfig();

function sizeStatement(node: ASTNode) {
    const viewState: StmntViewState = node.viewState;
    // If hidden do nothing.
    if (node.viewState.hidden) { return; }

    const label = DiagramUtils.getTextWidth(ASTUtil.genSource(node));
    viewState.bBox.h = config.statement.height;
    viewState.bBox.w = (config.statement.width > label.w) ? config.statement.width : label.w;
    viewState.bBox.label = label.text;
    // Check if statement is action invocation
    const action = ASTUtil.isActionInvocation(node);
    if (action) {
        // find the endpoint view state
        const epName = ASTUtil.getEndpointName(action as Invocation);
        endpointHolder.forEach((element: VisibleEndpoint) => {
            if (element.name === epName) {
                viewState.endpoint = element.viewState;
                viewState.isAction = true;
                viewState.bBox.h = config.statement.actionHeight;
                let actionName = ASTUtil.genSource(action as Invocation).split("->").pop();
                actionName = (actionName) ? actionName : "";
                viewState.bBox.label = DiagramUtils.getTextWidth(actionName).text;
                // Set visible to true so we can only draw used endpoints.
                (element.viewState as EndpointViewState).visible = true;
                viewState.isReturn = (element.viewState as EndpointViewState).usedAsClient;
            }
        });
    }
}

function sizeWorker(node: VariableDef) {
    const variable: Variable = (node.variable as Variable);
    const lambda: Lambda = (variable.initialExpression as Lambda);
    const functionNode = lambda.functionNode;
    const viewState: WorkerViewState = node.viewState;

    viewState.bBox.h = functionNode.body!.viewState.bBox.h + (config.lifeLine.header.height * 2)
        + config.statement.height  // leave room for start call.
        + config.statement.height; // for bottom plus
    viewState.bBox.w = (functionNode.body!.viewState.bBox.w) ? functionNode.body!.viewState.bBox.w :
        config.lifeLine.width;
    viewState.lifeline.bBox.w = config.lifeLine.width;
    // tslint:disable-next-line:prefer-conditional-expression
    if (functionNode.body!.viewState.bBox.leftMargin) {
        viewState.bBox.leftMargin = functionNode.body!.viewState.bBox.leftMargin;
    } else {
        viewState.bBox.leftMargin = config.lifeLine.leftMargin;
    }
    viewState.name = variable.name.value.replace("0", "");
}

let endpointHolder: VisibleEndpoint[] = [];
let returnStatements: Return[] = [];

export const visitor: Visitor = {

    // tslint:disable-next-line:ban-types
    beginVisitFunction(node: Function) {
        const viewState: FunctionViewState = node.viewState;
        if (node.VisibleEndpoints && !node.lambda) {
            endpointHolder = node.VisibleEndpoints;
            // clear return statements.
            returnStatements = [];
        }
        // If resource set the caller as first param.
        if (node.resource && node.VisibleEndpoints !== undefined) {
            const caller = node.VisibleEndpoints.find((element: VisibleEndpoint) => {
                return element.caller;
            });
            if (caller) {
                viewState.client = caller.viewState;
                (caller.viewState as EndpointViewState).visible = true;
                (caller.viewState as EndpointViewState).usedAsClient = true;
            } else {
                viewState.client = new ViewState();
            }
        }
    },

    // tslint:disable-next-line:ban-types
    endVisitFunction(node: Function) {
        if (node.lambda) {return; }
        const viewState: FunctionViewState = node.viewState;
        const body = viewState.body;
        const header = viewState.header;
        const client = viewState.client;
        const defaultWorker = viewState.defaultWorker;

        // Initialize the client width and height to default.
        client.bBox.h = config.lifeLine.line.height + (config.lifeLine.header.height * 2);
        client.bBox.w = config.lifeLine.width;

        // Size default worker
        defaultWorker.bBox.h = node.body!.viewState.bBox.h + (config.lifeLine.header.height * 2)
            + config.statement.height  // leave room for start call.
            + config.statement.height; // for bottom plus
        defaultWorker.bBox.w = (node.body!.viewState.bBox.w) ? node.body!.viewState.bBox.w :
            config.lifeLine.width;
        defaultWorker.lifeline.bBox.w = config.lifeLine.width;
        // tslint:disable-next-line:prefer-conditional-expression
        if (node.body!.viewState.bBox.leftMargin) {
            defaultWorker.bBox.leftMargin = node.body!.viewState.bBox.leftMargin;
        } else {
            defaultWorker.bBox.leftMargin = config.lifeLine.leftMargin;
        }
        // Size the other workers
        let lineHeight = (client.bBox.h > defaultWorker.bBox.h) ? client.bBox.h : defaultWorker.bBox.h;
        let workerWidth = 0;
        node.body!.statements.filter((element) => ASTUtil.isWorker(element)).forEach((worker) => {
            sizeWorker(worker as VariableDef);
            if (lineHeight < worker.viewState.bBox.h) {
                lineHeight = worker.viewState.bBox.h;
            }
            workerWidth += worker.viewState.bBox.w;
        });

        // Sync up the heights of lifelines
        client.bBox.h = defaultWorker.bBox.h = lineHeight;
        defaultWorker.lifeline.bBox.h = defaultWorker.bBox.h; // Set the height of lifeline.
        // Sync height of workers
        node.body!.statements.filter((element) => ASTUtil.isWorker(element)).forEach((worker) => {
            const workerViewState: WorkerViewState = worker.viewState;
            workerViewState.bBox.h = lineHeight;
            workerViewState.lifeline.bBox.h = lineHeight;
        });

        // Size endpoints
        let endpointWidth = 0;
        if (node.VisibleEndpoints) {
            node.VisibleEndpoints.forEach((endpoint: VisibleEndpoint) => {
                if (!endpoint.caller && endpoint.viewState.visible) {
                    endpoint.viewState.bBox.w = config.lifeLine.width;
                    endpoint.viewState.bBox.h = client.bBox.h;
                    endpointWidth += endpoint.viewState.bBox.w + config.lifeLine.gutter.h;
                }
            });
        }

        const lifeLinesWidth = client.bBox.w + config.lifeLine.gutter.h
            + defaultWorker.bBox.w + endpointWidth + workerWidth;
        body.w = config.panel.padding.left + lifeLinesWidth + config.panel.padding.right;
        body.h = config.panel.padding.top + lineHeight + config.panel.padding.bottom;

        header.w = config.panelHeading.padding.left + config.panelHeading.padding.right;
        header.h = config.panelHeading.height;

        viewState.bBox.w = (body.w > header.w) ? body.w : header.w;
        viewState.bBox.h = body.h + header.h;

        // Update return statement with client.
        returnStatements.forEach((element) => {
            const returnViewState: ReturnViewState = element.viewState;
            returnViewState.client = client;
        });
    },

    endVisitBlock(node: Block) {
        const viewState: BlockViewState = node.viewState;
        let height = 0;
        viewState.bBox.w = config.statement.width;
        node.statements.forEach((element) => {
            viewState.bBox.w = (viewState.bBox.w < element.viewState.bBox.w)
                ? element.viewState.bBox.w : viewState.bBox.w;
            viewState.bBox.leftMargin = (viewState.bBox.leftMargin < element.viewState.bBox.leftMargin)
                ? element.viewState.bBox.leftMargin : viewState.bBox.leftMargin;
            height += element.viewState.bBox.h;
        });
        viewState.bBox.h = ((height === 0) ? config.statement.height : height) + config.block.bottomMargin;
        const hoverRectLeftMargin = viewState.bBox.leftMargin === 0
                                    ? config.block.hoverRect.leftMargin
                                    : viewState.bBox.leftMargin;

        viewState.hoverRect.h = viewState.bBox.h;
        viewState.hoverRect.w = viewState.bBox.w + hoverRectLeftMargin;
        viewState.hoverRect.leftMargin = hoverRectLeftMargin;
    },

    endVisitWhile(node: While) {
        const viewState: ViewState = node.viewState;
        const bodyBBox: SimpleBBox = node.body.viewState.bBox;

        viewState.bBox.w = node.body.viewState.bBox.w + config.flowCtrl.rightMargin;
        viewState.bBox.h = node.body.viewState.bBox.h + config.flowCtrl.condition.height
            + config.flowCtrl.condition.bottomMargin
            + config.flowCtrl.whileGap + config.flowCtrl.bottomMargin;
        // If body has a left margin assign to while
        // tslint:disable-next-line:prefer-conditional-expression
        if (bodyBBox.leftMargin) {
            viewState.bBox.leftMargin = bodyBBox.leftMargin + config.flowCtrl.leftMargin;
        } else {
            viewState.bBox.leftMargin = config.flowCtrl.leftMarginDefault;
        }
    },

    endVisitForeach(node: Foreach) {
        const viewState: ViewState = node.viewState;
        const bodyBBox: SimpleBBox = node.body.viewState.bBox;

        viewState.bBox.w = node.body.viewState.bBox.w + config.flowCtrl.rightMargin;
        viewState.bBox.h = node.body.viewState.bBox.h + config.flowCtrl.foreach.height
            + config.flowCtrl.whileGap + config.flowCtrl.bottomMargin;
        // If body has a left margin assign to while
        // tslint:disable-next-line:prefer-conditional-expression
        if (bodyBBox.leftMargin) {
            viewState.bBox.leftMargin = bodyBBox.leftMargin + config.flowCtrl.leftMargin;
        } else {
            viewState.bBox.leftMargin = config.flowCtrl.leftMarginDefault;
        }
    },

    endVisitIf(node: If) {
        const viewState: ViewState = node.viewState;
        const bodyBBox: SimpleBBox = node.body.viewState.bBox;

        viewState.bBox.w = node.body.viewState.bBox.w;
        viewState.bBox.h = node.body.viewState.bBox.h + config.flowCtrl.condition.height
            + config.flowCtrl.condition.bottomMargin
            + config.flowCtrl.bottomMargin;
        // If body has a left margin assign to while
        // tslint:disable-next-line:prefer-conditional-expression
        if (bodyBBox.leftMargin) {
            viewState.bBox.leftMargin = bodyBBox.leftMargin + config.flowCtrl.leftMargin;
        } else {
            viewState.bBox.leftMargin = config.flowCtrl.leftMarginDefault;
        }

        // Add Else block
        if (node.elseStatement) {
            viewState.bBox.h += node.elseStatement.viewState.bBox.h;
            viewState.bBox.w += node.elseStatement.viewState.bBox.w;
        }
    },

    endVisitExpressionStatement(node: ExpressionStatement) {
        sizeStatement(node);
    },

    endVisitVariableDef(node: VariableDef) {
        sizeStatement(node);
    },

    endVisitAssignment(node: Assignment) {
        sizeStatement(node);
    },

    endVisitReturn(node: Return) {
        sizeStatement(node);
        returnStatements.push(node);
    },

    endVisitService(node: Service) {
        const viewState: ViewState = node.viewState;
        let height = config.panelGroup.header.height;
        // tslint:disable-next-line:ban-types
        node.resources.forEach((element: Function) => {
            viewState.bBox.w = (viewState.bBox.w > element.viewState.bBox.w)
                ? viewState.bBox.w : element.viewState.bBox.w;
            height = viewState.bBox.h;
            element.viewState.icon = "resource";
        });
        viewState.bBox.h = height;
    }
};
