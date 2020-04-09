import {
    Assignment, ASTKindChecker,
    ASTNode, ASTUtil, Block, BlockFunctionBody, Break, CompoundAssignment,
    Constant, ExpressionStatement, Foreach, Function as BalFunction, If, Invocation,
    Lambda, Literal, Match, MatchStaticPatternClause, MatchStructuredPatternClause,
    ObjectType, Panic, Return, Service, TypeDefinition, Variable, VariableDef,
    VisibleEndpoint, Visitor, WaitExpr, While, WorkerReceive, WorkerSend
} from "@ballerina/ast-model";
import { DiagramConfig } from "../config/default";
import { DiagramUtils } from "../diagram/diagram-utils";
import { EndpointViewState, FunctionViewState, SimpleBBox, StmntViewState, ViewState } from "../view-model";
import { BlockViewState } from "../view-model/block";
import { ReturnViewState } from "../view-model/return";
import { WorkerViewState } from "../view-model/worker";
import { WorkerSendViewState } from "../view-model/worker-send";

const config: DiagramConfig = DiagramUtils.getConfig();
interface WorkerTuple { block: Block; view: WorkerViewState; }

class SizingVisitor implements Visitor {
    private endpointHolder: VisibleEndpoint[] = [];
    private returnStatements: Return[] = [];
    private envEndpoints: VisibleEndpoint[] = [];
    private soroundingEndpoints: VisibleEndpoint[] = [];

    public beginVisitFunction(node: BalFunction) {
        const viewState: FunctionViewState = node.viewState;
        if (!node.lambda) {
            this.endpointHolder = [];
            this.soroundingEndpoints = viewState.soroundingVisibleEndpoints;
            // clear return statements.
            this.returnStatements = [];
        }
        // If resource set the caller as first param.
        if (node.resource &&  node.body && node.body.VisibleEndpoints !== undefined) {
            const caller = node.body.VisibleEndpoints.find((element: VisibleEndpoint) => {
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
    }

    public beginVisitIf(node: If) {
        if (!node.viewState.hidden && !node.viewState.hiddenBlock) {
            node.viewState.bBox.paddingTop = config.flowCtrl.paddingTop;
        }
    }

    public beginVisitWhile(node: While) {
        if (!node.viewState.hidden && !node.viewState.hiddenBlock) {
            node.viewState.bBox.paddingTop = config.flowCtrl.paddingTop;
        }
    }

    public beginVisitForeach(node: Foreach) {
        node.viewState.bBox.paddingTop = config.flowCtrl.paddingTop;
    }

    public endVisitFunction(node: BalFunction) {
        if (node.lambda || !node.body || !ASTKindChecker.isBlockFunctionBody(node.body)) { return; }
        const viewState: FunctionViewState = node.viewState;
        const body = viewState.body;
        const header = viewState.header;
        const client = viewState.client;
        const defaultWorker = viewState.defaultWorker;
        const workerHolder: WorkerTuple[] = [];

        // Initialize the client width and height to default.
        client.bBox.h = config.lifeLine.line.height + (config.lifeLine.header.height * 2);
        client.bBox.w = 4;

        // Size default worker
        defaultWorker.bBox.h = node.body!.viewState.bBox.h + (config.lifeLine.header.height * 2)
            + config.statement.height  // leave room for start call.
            + config.statement.height; // for bottom plus
        defaultWorker.bBox.w = (node.body!.viewState.bBox.w) ? node.body!.viewState.bBox.w :
            config.lifeLine.width;
        defaultWorker.lifeline.bBox.w = config.lifeLine.width;
        workerHolder.push({ block: node.body, view: defaultWorker });

        // tslint:disable-next-line:prefer-conditional-expression
        if (node.body!.viewState.bBox.leftMargin) {
            defaultWorker.bBox.leftMargin = node.body!.viewState.bBox.leftMargin;
        } else {
            defaultWorker.bBox.leftMargin = config.lifeLine.leftMargin;
        }
        // Size the other workers
        let lineHeight = (client.bBox.h > defaultWorker.bBox.h) ? client.bBox.h : defaultWorker.bBox.h;
        let workerWidth = 0;
        defaultWorker.initHeight = this.calcPreWorkerHeight(node.body);
        const workers = node.body!.statements.filter((element) => ASTUtil.isWorker(element));

        workers.forEach((workerEl) => {
            const functionNode = ((workerEl as VariableDef).variable.initialExpression as Lambda).functionNode;
            const workerViewState: WorkerViewState = workerEl.viewState;
            workerHolder.push({ block: functionNode.body as BlockFunctionBody, view: workerViewState });
            workerViewState.name = ((workerEl as VariableDef).variable as Variable).name.value.replace("0", "");
        });

        // Set Worker Arrows
        this.syncWorkerInteractions(workerHolder);

        // need to resize after syncing the interactions
        workers.forEach((workerEl) => {
            const worker = workerEl as VariableDef;
            const functionNode = ((workerEl as VariableDef).variable.initialExpression as Lambda).functionNode;
            this.sizeBlock(functionNode.body as BlockFunctionBody);
            this.sizeWorker(worker, defaultWorker.initHeight);
            if (lineHeight < worker.viewState.bBox.h) {
                lineHeight = worker.viewState.bBox.h;
            }
            workerWidth += worker.viewState.bBox.w + worker.viewState.bBox.leftMargin;
        });

        this.sizeBlock(node.body);
        defaultWorker.bBox.h = node.body!.viewState.bBox.h + (config.lifeLine.header.height * 2)
        + config.statement.height  // leave room for start call.
        + config.statement.height;
        if (lineHeight < defaultWorker.bBox.h) {
            lineHeight = defaultWorker.bBox.h;
        }

        // Sync up the heights of lifelines
        client.bBox.h = defaultWorker.bBox.h = lineHeight;
        defaultWorker.lifeline.bBox.h = defaultWorker.bBox.h; // Set the height of lifeline.
        // Sync height of workers
        workers.forEach((worker) => {
            const workerViewState: WorkerViewState = worker.viewState;
            workerViewState.bBox.h = lineHeight;
            workerViewState.lifeline.bBox.h = lineHeight;
        });

        // Size endpoints
        let endpointWidth = 0;
        if (this.endpointHolder) {
            this.endpointHolder.forEach((endpoint: VisibleEndpoint) => {
                if (endpoint.viewState.visible) {
                    endpoint.viewState.bBox.w = config.lifeLine.width;
                    endpoint.viewState.bBox.h = client.bBox.h;
                    endpointWidth += endpoint.viewState.bBox.w + config.lifeLine.gutter.h;
                }
            });
        }
        const lifeLinesWidth = client.bBox.w + config.lifeLine.gutter.h + defaultWorker.bBox.leftMargin
            + defaultWorker.bBox.w + endpointWidth + workerWidth;
        body.w = config.panel.padding.left + lifeLinesWidth;
        body.h = config.panel.padding.top + lineHeight + config.panel.padding.bottom;

        header.w = config.panelHeading.padding.left + config.panelHeading.padding.right;
        header.h = config.panelHeading.height;

        viewState.bBox.w = (body.w > header.w) ? body.w : header.w;
        viewState.bBox.h = body.h + header.h;
        viewState.endpointsWidth = endpointWidth;
        viewState.workerWidth = workerWidth;
        viewState.containsOtherLifelines = workers.length > 0 || this.endpointHolder.length > 0;
        defaultWorker.returnStatements = [...this.returnStatements];

        // Update return statement view-states.
        this.returnStatements.forEach((returnStmt) => {
            const returnViewState: ReturnViewState = returnStmt.viewState;
            if (returnViewState.containingWokerViewState) {
                // this return is inside a worker
                return;
            }

            returnViewState.client = client;
            // hide empty return stmts in resources
            if (node.resource) {
                returnViewState.hidden =
                    returnStmt.noExpressionAvailable
                    || (ASTKindChecker.isLiteral(returnStmt.expression)
                        && (returnStmt.expression as Literal).emptyParantheses === true);
            }
        });

        // show an implicit return line for functions with return type nil
        // and doesn't have any return statements
        if (!node.resource && this.returnStatements.length === 0) {
            viewState.implicitReturn.client = client;
            viewState.implicitReturn.bBox.h = config.statement.height;
            viewState.implicitReturn.bBox.w = config.statement.width;
            viewState.implicitReturn.hidden = false;
        } else {
            viewState.implicitReturn.hidden = true;
        }

        this.soroundingEndpoints = [];
    }

    public beginVisitBlockFunctionBody(node: BlockFunctionBody, parent: ASTNode) {
        this.beginVisitBlock(node, parent);
    }

    public beginVisitBlock(node: Block, parent: ASTNode) {
        if (!node.parent) {
            return;
        }
        if (node.VisibleEndpoints) {
            this.envEndpoints = [...this.envEndpoints, ...node.VisibleEndpoints];
            this.endpointHolder = [...node.VisibleEndpoints, ...this.endpointHolder];
        }
    }

    public endVisitBlockFunctionBody(node: BlockFunctionBody, parent: ASTNode) {
        this.endVisitBlock(node, parent);
    }

    public endVisitBlock(node: Block, parent: ASTNode) {
        this.sizeBlock(node);
    }

    public endVisitWhile(node: While) {
        const viewState: ViewState = node.viewState;

        if (node.viewState.hidden || node.viewState.hiddenBlock) {
            this.checkHiddenState(node.viewState);
            return;
        }

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
    }

    public endVisitForeach(node: Foreach) {
        const viewState: ViewState = node.viewState;

        if (node.viewState.hidden || node.viewState.hiddenBlock) {
            this.checkHiddenState(node.viewState);
            return;
        }

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
    }

    public endVisitIf(node: If) {
        const viewState: ViewState = node.viewState;
        const bodyBBox: SimpleBBox = node.body.viewState.bBox;

        if (node.viewState.hidden || node.viewState.hiddenBlock) {
            this.checkHiddenState(node.viewState);
            return;
        }

        viewState.bBox.w = node.body.viewState.bBox.w + config.flowCtrl.rightMargin;
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
    }

    public endVisitExpressionStatement(node: ExpressionStatement) {
        this.sizeStatement(node);
    }

    public endVisitVariableDef(node: VariableDef) {
        this.sizeStatement(node);
    }

    public endVisitAssignment(node: Assignment) {
        this.sizeStatement(node);
    }

    public endVisitReturn(node: Return) {
        this.sizeStatement(node);
        node.viewState.bBox.label = DiagramUtils
            .getTextWidth(ASTUtil.genSource(node.expression)).text;
        this.returnStatements.push(node);
    }

    public endVisitCompoundAssignment(node: CompoundAssignment) {
        this.sizeStatement(node);
    }

    public endVisitWorkerSend(node: WorkerSend) {
        this.sizeStatement(node);
    }

    public endVisitPanic(node: Panic) {
        this.sizeStatement(node);
    }

    public endVisitBreak(node: Break) {
        this.sizeStatement(node);
    }

    public endVisitConstant(node: Constant) {
        this.sizeStatement(node);
    }

    public endVisitService(node: Service) {
        const viewState: ViewState = node.viewState;
        let height = config.panelGroup.header.height;
        // tslint:disable-next-line:ban-types
        node.resources.forEach((element: BalFunction) => {
            viewState.bBox.w = (viewState.bBox.w > element.viewState.bBox.w)
                ? viewState.bBox.w : element.viewState.bBox.w;
            height += element.viewState.bBox.h;
            element.viewState.icon = "resource";
        });
        viewState.bBox.h = height;
    }

    public endVisitTypeDefinition(node: TypeDefinition) {
        // If it is a service do nothing.
        if (node.service || !ASTUtil.isValidObjectType(node)) { return; }
        const viewState: ViewState = node.viewState;
        let height = config.panelGroup.header.height;
        // tslint:disable-next-line:ban-types
        (node.typeNode as ObjectType).functions.forEach((element: BalFunction) => {
            viewState.bBox.w = (viewState.bBox.w > element.viewState.bBox.w)
                ? viewState.bBox.w : element.viewState.bBox.w;
            height += element.viewState.bBox.h;
            element.viewState.icon = "function";
        });
        viewState.bBox.h = height;
    }

    public endVisitMatchStaticPatternClause(node: MatchStaticPatternClause) {
        const viewState: ViewState = node.viewState;

        if (node.viewState.hidden || node.viewState.hiddenBlock) {
            this.checkHiddenState(node.viewState);
            return;
        }

        viewState.bBox.w = node.statement.viewState.bBox.w;
        viewState.bBox.h = node.statement.viewState.bBox.h
            + config.statement.height; // To print literal
        viewState.bBox.label = DiagramUtils.getTextWidth(ASTUtil.genSource(node.literal)).text;
    }

    public endVisitMatchStructuredPatternClause(node: MatchStructuredPatternClause) {
        const viewState: ViewState = node.viewState;

        if (node.viewState.hidden || node.viewState.hiddenBlock) {
            this.checkHiddenState(node.viewState);
            return;
        }

        viewState.bBox.w = node.statement.viewState.bBox.w;
        viewState.bBox.h = node.statement.viewState.bBox.h
            + config.statement.height; // To print literal
        viewState.bBox.label = DiagramUtils.getTextWidth(ASTUtil.genSource(node.variableNode)).text;
    }

    public endVisitMatch(node: Match) {
        const viewState: ViewState = node.viewState;

        if (node.viewState.hidden || node.viewState.hiddenBlock) {
            this.checkHiddenState(node.viewState);
            return;
        }

        let height = config.frame.topMargin + config.frame.header.height;
        let width = 0;
        node.patternClauses.forEach((element) => {
            height += element.viewState.bBox.h;
            width = (width > element.viewState.bBox.w) ?
                width : element.viewState.bBox.w;
        });
        viewState.bBox.h = height;
        viewState.bBox.w = width + config.flowCtrl.rightMargin;
        viewState.bBox.leftMargin = 60;
    }

    private findActualEP(endpoint?: VisibleEndpoint): VisibleEndpoint | undefined {
        if (!endpoint) {
            return;
        }

        const actualEpName = (endpoint.viewState as EndpointViewState).actualEpName;
        if (actualEpName) {
            const actualEP = this.soroundingEndpoints.find((el: VisibleEndpoint) => el.name === actualEpName);
            if (actualEP === endpoint) {
                return endpoint;
            }
            return this.findActualEP(actualEP);
        } else {
            return endpoint;
        }
    }

    private sizeStatement(node: ASTNode) {
        const viewState: StmntViewState = node.viewState;
        // If hidden do nothing.
        if (viewState.hidden && !viewState.isInHiddenBlock) {
            viewState.bBox.h = 0;
            viewState.bBox.w = 0;
            viewState.bBox.leftMargin = 0;
            viewState.bBox.paddingTop = 0;
            return;
        }

        const source = ASTUtil.genSource(node);
        const label = DiagramUtils.getTextWidth(source);
        viewState.bBox.h = config.statement.height;
        viewState.bBox.w = (config.statement.width > label.w) ? config.statement.width : label.w;
        viewState.bBox.leftMargin = 0;
        viewState.bBox.paddingTop = 0;
        viewState.bBox.label = label.text;
        viewState.bBox.labelWidth = label.labelWidth;
        // Check if statement is action invocation.
        const action = ASTUtil.isActionInvocation(node);
        if (action) {
            // find the endpoint view state
            const epName = ASTUtil.getEndpointName(action as Invocation);

            const endpoint = this.findActualEP(this.endpointHolder.find((el: VisibleEndpoint) => el.name === epName));

            if (endpoint) {
                viewState.endpoint = endpoint.viewState;
                viewState.isAction = true;
                viewState.bBox.h = config.statement.actionHeight;
                let actionName = ASTUtil.genSource(action as Invocation).split("->").pop();
                actionName = (actionName) ? actionName : "";
                viewState.bBox.label = DiagramUtils.getTextWidth(actionName).text;
                // Set visible to true so we can only draw used endpoints.
                (endpoint.viewState as EndpointViewState).visible = true;
                viewState.isReturn = (endpoint.viewState as EndpointViewState).usedAsClient;
            }
        }

        if (viewState.hiddenBlockContext) {
            this.sizeHiddenBlock(viewState);
        }

        if (viewState.expandContext) {
            // add space for the expander
            viewState.bBox.w += 10;
            if (!viewState.expandContext.collapsed && !viewState.hidden && !viewState.hiddenBlock) {
                const labelText = viewState.expandContext.expandedSubTree.name.value;
                viewState.expandContext.labelText = labelText;
                viewState.expandContext.statementText = ASTUtil.genSource(node);
                viewState.expandContext.labelWidth = DiagramUtils.calcTextLength(labelText, {bold: true});
                this.handleExpandedFn(viewState.expandContext.expandedSubTree, viewState);
            }
        }
    }

    private checkHiddenState(viewState: ViewState) {
        if (viewState.hidden) {
            viewState.bBox.w = 0;
            viewState.bBox.h = 0;
            return;
        }

        if (viewState.hiddenBlock) {
            this.sizeHiddenBlock(viewState as StmntViewState);
            return;
        }
    }

    private sizeHiddenBlock(viewState: StmntViewState) {
        if (!viewState.hiddenBlockContext) {
            return;
        }

        if (viewState.hiddenBlockContext.expanded) {
            let hiddenBlockHeight = 2 * config.statement.expanded.topMargin;
            let hiddenBlockWidth = 0;
            let hiddenBlockLeftMargin = config.statement.expanded.margin;
            viewState.hiddenBlockContext.otherHiddenNodes.forEach((hiddenNode) => {
                ASTUtil.traversNode(hiddenNode, visitor);
                hiddenBlockHeight += (hiddenNode.viewState.bBox.h + hiddenNode.viewState.bBox.paddingTop);
                if (hiddenBlockWidth < hiddenNode.viewState.bBox.w) {
                    hiddenBlockWidth = hiddenNode.viewState.bBox.w;
                }
                if (hiddenBlockLeftMargin < hiddenNode.viewState.bBox.leftMargin) {
                    hiddenBlockLeftMargin = hiddenNode.viewState.bBox.leftMargin + config.statement.expanded.margin;
                }
            });
            viewState.bBox.h = hiddenBlockHeight + 2 * config.statement.expanded.bottomMargin;
            viewState.bBox.w = hiddenBlockWidth + config.statement.expanded.collapserWidth +
                config.statement.expanded.rightMargin;
            viewState.bBox.leftMargin = hiddenBlockLeftMargin;
        } else {
            viewState.bBox.w = 60;
            viewState.bBox.h = config.statement.height;
            viewState.bBox.leftMargin = 0;
        }
    }

    private handleExpandedFn(expandedFn: BalFunction, viewState: StmntViewState) {
        if (!expandedFn.body) {
            return;
        }

        const expandedBody = (expandedFn.body.viewState as BlockViewState).bBox;
        const expandedFnViewState = expandedFn.viewState as FunctionViewState;
        const expandedDefaultWorker = expandedFnViewState.defaultWorker.bBox;
        ASTUtil.traversNode(expandedFn, new SizingVisitor());
        const sizes = config.statement.expanded;

        if ((config.lifeLine.width / 2) > expandedBody.leftMargin) {
            expandedBody.leftMargin = config.lifeLine.width / 2;
        }

        let expandedFnWidth = expandedBody.w + expandedBody.leftMargin;

        expandedFnWidth += expandedFnViewState.workerWidth;

        expandedFnWidth += expandedFnViewState.endpointsWidth;
        expandedFnWidth += sizes.rightMargin + sizes.margin;

        viewState.bBox.h = expandedDefaultWorker.h + sizes.header + sizes.footer + sizes.bottomMargin;
        const fullLabelWidth = config.statement.padding.left + viewState.expandContext!.labelWidth
            + sizes.rightMargin + (2 * sizes.labelGutter);

        viewState.bBox.w = expandedFnWidth > fullLabelWidth ? expandedFnWidth : fullLabelWidth;
        viewState.bBox.leftMargin = 40;
    }

    private sizeWorker(node: VariableDef, preWorkerHeight = 0) {
        const variable: Variable = (node.variable as Variable);
        const lambda: Lambda = (variable.initialExpression as Lambda);
        const functionNode = lambda.functionNode;
        const viewState: WorkerViewState = node.viewState;
        // set top pad
        functionNode.body!.viewState.paddingTop = preWorkerHeight;
        viewState.bBox.h = functionNode.body!.viewState.bBox.h + (config.lifeLine.header.height * 2)
            + functionNode.body!.viewState.paddingTop
            + config.statement.height  // leave room for start call.
            + config.statement.height; // for bottom plus
        viewState.bBox.w = (functionNode.body!.viewState.bBox.w) ? functionNode.body!.viewState.bBox.w :
            config.lifeLine.width;
        viewState.lifeline.bBox.w = config.lifeLine.width;
        // set minimum left margin as 60
        functionNode.body!.viewState.bBox.leftMargin =
            functionNode.body!.viewState.bBox.leftMargin > 60 ? functionNode.body!.viewState.bBox.leftMargin : 60;

        // tslint:disable-next-line:prefer-conditional-expression
        if (functionNode.body!.viewState.bBox.leftMargin) {
            viewState.bBox.leftMargin = functionNode.body!.viewState.bBox.leftMargin;
        } else {
            viewState.bBox.leftMargin = config.lifeLine.leftMargin;
        }
    }

    private calcPreWorkerHeight(body: Block) {
        let height = config.statement.height * 2;
        // tslint:disable-next-line:prefer-for-of
        for (let i = 0; i < body!.statements.length; i++) {
            const statement = body!.statements[i];
            if (ASTUtil.isWorker(statement)) {
                break;
            }
            height += statement.viewState.bBox.h;
        }
        return height;
    }

    private syncWorkerInteractions(workers: WorkerTuple[]) {
        const sendReceivePairs: Array<{
            sendingWorkerName: string,
            sendIndex: number,
            sendHolder: ASTNode,
            receivingWorkerName: string,
            receiveIndex: number,
            receiveHolder: ASTNode}> = [];
        const sends: {[fromWorker: string]: {[toWorker: string]:
            Array<{statement: WorkerSend, index: number,  holder: ASTNode}>}} = {};
        const receives: {[toWorker: string]: {[fromWorker: string]:
            Array<{statement: WorkerReceive, index: number, holder: ASTNode}>}} = {};
        const waits: Array<{wait: WaitExpr, index: number, holder: ASTNode, containingWorkerName: string}> = [];

        const workersMap: {[workerName: string]: WorkerTuple} = {};
        const workerHeightInfo: {[workerName: string]: {currentHeight: number, currentIndex: number}} = {};

        // 1. Collect all sends and receives
        workers.forEach((worker) => {
            sends[worker.view.name] = {};
            receives[worker.view.name] = {};
            workersMap[worker.view.name] = worker;
            workerHeightInfo[worker.view.name] = {
                currentHeight: 0,
                currentIndex: 0
            };
            worker.block.statements.forEach((statement, index) => {
                // Check if the statement is a send
                if (ASTKindChecker.isWorkerSend(statement)) {
                    if (sends[worker.view.name][statement.workerName.value] === undefined) {
                        sends[worker.view.name][statement.workerName.value] = [];
                    }
                    sends[worker.view.name][statement.workerName.value].push({statement, index, holder: statement});
                    return;
                }

                // Check if the statement is a sync send
                if (ASTKindChecker.isVariableDef(statement) && statement.variable.initialExpression &&
                        ASTKindChecker.isWorkerSyncSend(statement.variable.initialExpression)) {
                    const sendExp = statement.variable.initialExpression;
                    if (sends[worker.view.name][sendExp.workerName.value] === undefined) {
                        sends[worker.view.name][sendExp.workerName.value] = [];
                    }
                    sends[worker.view.name][sendExp.workerName.value].push({
                        holder: statement,
                        index,
                        statement: sendExp,
                    });
                    return;
                }

                // Check if the statement is a receive
                const receiveStatement = ASTUtil.extractWorkerReceive(statement);
                if (receiveStatement) {
                    if (receives[worker.view.name][receiveStatement.workerName.value] === undefined) {
                        receives[worker.view.name][receiveStatement.workerName.value] = [];
                    }
                    receives[worker.view.name][receiveStatement.workerName.value].push(
                        {statement: receiveStatement, index, holder: statement});
                    return;
                }

                const waitExpr = extractWaitExpr(statement);

                if (waitExpr) {
                    waits.push({
                        containingWorkerName: worker.view.name,
                        holder: statement,
                        index,
                        wait: waitExpr,
                    });
                }
            });
        });

        waits.forEach((waitinfo) => {
            const {
                wait: waitExpr,
                index,
                holder: statement,
                containingWorkerName,
            } = waitinfo;

            let workerNames: string[] = [];
            if (waitExpr.expression) {
                workerNames = extractWorkerNames(waitExpr.expression);
            }

            if (waitExpr.keyValuePairs) {
                waitExpr.keyValuePairs.forEach((pair) => {
                    if (pair) {
                        workerNames.push(pair.key.value);
                    }
                });
            }

            workerNames.forEach((workerName) => {
                if (workersMap[workerName]) {
                    workersMap[workerName].view.returnStatements.forEach((returnStmt) => {
                        (returnStmt.viewState as ReturnViewState).callerViewStates[containingWorkerName] =
                            workersMap[containingWorkerName].view.lifeline;

                        sendReceivePairs.push({
                            receiveHolder: statement,
                            receiveIndex: index,
                            receivingWorkerName: containingWorkerName,
                            sendHolder: returnStmt,
                            sendIndex: workersMap[workerName].block.statements.length,
                            // all returns should be after sends and receives
                            sendingWorkerName: workerName,
                        });
                    });
                }
            });
        });

        // 2. Pair up sends and receives
        workers.forEach((fromWorker) => {
            workers.forEach((toWorker) => {
                if (fromWorker === toWorker) {
                    return;
                }
                if (!sends[fromWorker.view.name][toWorker.view.name]
                    || !receives[toWorker.view.name][fromWorker.view.name]) {
                    return;
                }
                sends[fromWorker.view.name][toWorker.view.name].forEach((sendInfo) => {
                    const { statement: send, index: sendIndex, holder: sendHolder } = sendInfo;
                    (send.viewState as WorkerSendViewState).to = toWorker.view;
                    // Since sends and receives are added to the list on order, the first available receive in the
                    // array is the matching receive to this send.
                    const r = receives[toWorker.view.name][fromWorker.view.name].shift();
                    if (!r) {
                        return;
                    }

                    const { statement: receive, index: receiveIndex, holder: receiveHolder } = r;
                    const sendingWorkerName = receive.workerName.value;
                    const receivingWorkerName = send.workerName.value;
                    (send.viewState as WorkerSendViewState).isSynced = true;
                    sendReceivePairs.push({
                        receiveHolder,
                        receiveIndex,
                        receivingWorkerName,
                        sendHolder,
                        sendIndex,
                        sendingWorkerName,
                    });
                });
            });
        });

        // 3. Sort the pairs in the order they should appear top to bottom
        sendReceivePairs.sort((p1, p2) => {
            if (p1.receivingWorkerName === p2.receivingWorkerName) {
                // If two pairs has the same receiver (send.workerName is the receiver name) one with
                // higher lower receiver index (one defined heigher up in the receiver) should be rendered
                // higher in the list of pairs. Same logic is used for all the cases following.
                return p1.receiveIndex - p2.receiveIndex;
            }
            if (p1.receivingWorkerName === p2.sendingWorkerName) {
                return p1.receiveIndex - p2.sendIndex;
            }
            if (p1.sendingWorkerName === p2.receivingWorkerName) {
                return p1.sendIndex - p2.receiveIndex;
            }
            if (p1.sendingWorkerName === p2.sendingWorkerName) {
                return p1.sendIndex - p2.sendIndex;
            }
            return 0;
        });

        // 4. Calculate heights
        sendReceivePairs.forEach((pair) => {
            pair.sendHolder.viewState.bBox.paddingTop = 0;
            pair.receiveHolder.viewState.bBox.paddingTop = 0;
        });

        if (workerHeightInfo.Default) {
            workerHeightInfo.Default.currentHeight =
                -(workersMap.Default.view.initHeight - (2 * config.statement.height));
        }

        sendReceivePairs.forEach((pair) => {
            const sendWorker = workersMap[pair.sendingWorkerName];
            for (let index = workerHeightInfo[sendWorker.view.name].currentIndex; index < pair.sendIndex; index++) {
                workerHeightInfo[sendWorker.view.name].currentHeight +=
                    sendWorker.block.statements[index].viewState.bBox.h
                    + sendWorker.block.statements[index].viewState.bBox.paddingTop;
            }
            workerHeightInfo[sendWorker.view.name].currentIndex = pair.sendIndex;

            const receiveWorker = workersMap[pair.receivingWorkerName];
            for (let index = workerHeightInfo[receiveWorker.view.name].currentIndex;
                index < pair.receiveIndex; index++) {
                workerHeightInfo[receiveWorker.view.name].currentHeight +=
                    receiveWorker.block.statements[index].viewState.bBox.h
                    + receiveWorker.block.statements[index].viewState.bBox.paddingTop;
            }
            workerHeightInfo[receiveWorker.view.name].currentIndex = pair.receiveIndex;
            const sendHeight = workerHeightInfo[sendWorker.view.name].currentHeight;
            const receiveHeight = workerHeightInfo[receiveWorker.view.name].currentHeight;
            (pair.sendHolder.viewState as WorkerSendViewState).isSynced = true;
            if (sendHeight > receiveHeight) {
                if (pair.receiveHolder.viewState.bBox.paddingTop < (sendHeight - receiveHeight)) {
                    pair.receiveHolder.viewState.bBox.paddingTop = sendHeight - receiveHeight;
                }
            } else {
                if (pair.sendHolder.viewState.bBox.paddingTop < (receiveHeight - sendHeight)) {
                    pair.sendHolder.viewState.bBox.paddingTop = receiveHeight - sendHeight;
                }
            }
        });
    }

    private sizeBlock(node: Block) {
        const viewState: BlockViewState = node.viewState;
        let height = 0;
        viewState.bBox.w = config.statement.width;
        node.statements.forEach((element) => {
            if (ASTUtil.isWorker(element)
            ) { return; }
            viewState.bBox.w = (viewState.bBox.w < element.viewState.bBox.w)
                ? element.viewState.bBox.w : viewState.bBox.w;
            viewState.bBox.leftMargin = (viewState.bBox.leftMargin < element.viewState.bBox.leftMargin)
                ? element.viewState.bBox.leftMargin : viewState.bBox.leftMargin;
            height += element.viewState.bBox.h + element.viewState.bBox.paddingTop;
        });
        viewState.bBox.h = ((height === 0) ? config.statement.height : height) + config.block.bottomMargin;
        const hoverRectLeftMargin = viewState.bBox.leftMargin === 0
            ? config.block.hoverRect.leftMargin
            : viewState.bBox.leftMargin;

        viewState.hoverRect.h = viewState.bBox.h;
        viewState.hoverRect.w = viewState.bBox.w + hoverRectLeftMargin;
        viewState.hoverRect.leftMargin = hoverRectLeftMargin;

        if (node.VisibleEndpoints) {
            const visibleEndpoints = node.VisibleEndpoints;
            this.envEndpoints = this.envEndpoints.filter((ep) => (!visibleEndpoints.includes(ep)));
        }
    }
}

function extractWaitExpr(statement: ASTNode): WaitExpr | undefined {
    if (ASTKindChecker.isVariableDef(statement) && statement.variable.initialExpression &&
        ASTKindChecker.isWaitExpr(statement.variable.initialExpression)) {

        return statement.variable.initialExpression;
    }

    if (ASTKindChecker.isExpressionStatement(statement) &&
        ASTKindChecker.isWaitExpr(statement.expression)) {

        return statement.expression;
    }

    return;
}

function extractWorkerNames(expression: ASTNode, workerNames: string[] = []) {
    if (ASTKindChecker.isSimpleVariableRef(expression)) {
        workerNames.push(expression.variableName.value);
    }

    if (ASTKindChecker.isBinaryExpr(expression)) {
        extractWorkerNames(expression.rightExpression, workerNames);
        extractWorkerNames(expression.leftExpression, workerNames);
    }

    return workerNames;
}

export const visitor = new SizingVisitor();
