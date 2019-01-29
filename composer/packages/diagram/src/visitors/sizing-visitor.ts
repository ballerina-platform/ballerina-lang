import {
    Assignment, ASTKindChecker,
    ASTNode, ASTUtil, Block, Break, CompoundAssignment, Constant, ExpressionStatement,
    Foreach, Function, If, Invocation, Lambda, Literal, Match,
    MatchStaticPatternClause, ObjectType, Panic, Return, Service,
    TypeDefinition, UnionTypeNode, UserDefinedType, ValueType, Variable, VariableDef, VisibleEndpoint,
    Visitor, While, WorkerSend
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
    if (node.viewState.hidden) {
        viewState.bBox.h = 0;
        viewState.bBox.w = 0;
        return;
    }

    const label = DiagramUtils.getTextWidth(ASTUtil.genSource(node));
    viewState.bBox.h = config.statement.height;
    viewState.bBox.w = (config.statement.width > label.w) ? config.statement.width : label.w;
    viewState.bBox.label = label.text;
    // Check if statement is action invocation.
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

    if (node.viewState.hiddenBlock) {
        viewState.bBox.w = 60;
    }
}

function sizeWorker(node: VariableDef, preWorkerHeight = 0, workerHolder: WorkerTuple[]) {
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
    // tslint:disable-next-line:prefer-conditional-expression
    if (functionNode.body!.viewState.bBox.leftMargin) {
        viewState.bBox.leftMargin = functionNode.body!.viewState.bBox.leftMargin;
    } else {
        viewState.bBox.leftMargin = config.lifeLine.leftMargin;
    }
    viewState.name = variable.name.value.replace("0", "");
    workerHolder.push({ block: functionNode.body!, view: viewState });
}

function calcPreWorkerHeight(body: Block) {
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

interface WorkerTuple { block: Block; view: WorkerViewState; }
function syncWorkerInvocations(workerBlocks: WorkerTuple[]) {
    const workerMap: { [s: string]: Block; } = {};
    workerBlocks.forEach((workerBlock) => {
        workerMap[workerBlock.view.name] = workerBlock.block;
    });
    // Clear out previous calculations.
    setZeroPadding(workerBlocks);
    // traverse default identify interaction
    workerBlocks.forEach((workerBlock) => {
        workerBlock.block.statements.forEach((statement, index) => {
            if (ASTKindChecker.isWorkerSend(statement)) {
                const workerViewState = workerBlocks.find((worker) => {
                    return worker.view.name === (statement as WorkerSend).workerName.value;
                });
                if (!workerViewState) { return; }
                (statement as WorkerSend).viewState.to = workerViewState!.view;
                // if statement is not synced call balance.
                balanceWorkerInvocation(index,
                    workerBlock.view.name,
                    (statement as WorkerSend).workerName.value,
                    workerMap);
            }
        });
    });
}

function setZeroPadding(workerBlocks: WorkerTuple[]) {
    workerBlocks.forEach((workerBlock) => {
        workerBlock.block.statements.forEach((statement) => {
            statement.viewState.synced = false;
            statement.viewState.paddingTop = 0;
        });
    });
}

function balanceWorkerInvocation(index: number, from: string, to: string, workerMap: { [s: string]: Block; }) {
    // Iterate the specifc worker till you find the index.
    const fromWorker: Block = workerMap[from];
    const toWorker: Block = workerMap[to];
    let toHeight = 0;
    for (let i = 0; i < index; i++) {
        // Add height while you iterate.
        toHeight += fromWorker.statements[i].viewState.bBox.h
            + fromWorker.statements[i].viewState.bBox.paddingTop;
    }
    const sendStatement = fromWorker.statements[index];

    // Iterate the target worker till you find a compatible receive.
    let receiveHeight = 0;
    // tslint:disable-next-line:prefer-for-of
    for (let j = 0; j < toWorker.statements.length; j++) {
        // If you find another send or receive call recursive
        const stmt = toWorker.statements[j];
        // Add height while iterating.
        if (ASTKindChecker.isWorkerSend(stmt) && !stmt.viewState.synced) {
            balanceWorkerInvocation(j, to, (stmt as WorkerSend).workerName.value, workerMap);
        }
        const receiveName = ASTUtil.isWorkerReceive(stmt);
        if (receiveName) {
            if (!stmt.viewState.synced) {
                // Check if compatible
                if (from === (receiveName as string)) {
                    if (receiveHeight > toHeight) {
                        sendStatement.viewState.bBox.paddingTop = receiveHeight - toHeight;
                    } else {
                        stmt.viewState.bBox.paddingTop = toHeight - receiveHeight;
                    }
                    sendStatement.viewState.synced = true;
                    stmt.viewState.synced = true;
                } else {
                    // ToDo bug need to find other send.
                    // balanceWorkerInvocation(j, to, (receiveName as string), workerMap);
                }
            }
        }
        receiveHeight += stmt.viewState.bBox.h + stmt.viewState.bBox.paddingTop;
    }
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

        // make endpoints, which are defined in function, visible
        if (node.VisibleEndpoints && node.body) {
            const varDefStmts = node.body.statements.filter(ASTKindChecker.isVariableDef);
            node.VisibleEndpoints.forEach((visibleEndpoint) => {
                const epDef = varDefStmts.find((varDefStmt) => {
                    const varDef = varDefStmt as VariableDef;
                    const variable = varDef.variable as Variable;
                    const variableTypeNode = variable.typeNode as UserDefinedType;
                    return variable.name.value === visibleEndpoint.name
                        && variableTypeNode.packageAlias.value === visibleEndpoint.pkgAlias
                        && variableTypeNode.typeName.value === visibleEndpoint.typeName;
                });
                if (epDef) {
                    (visibleEndpoint.viewState as EndpointViewState).visible = true;
                    // link position info of var def stmt to make revealPosition work
                    visibleEndpoint.position = epDef.position;
                }
            });
        }
    },

    beginVisitIf(node: If) {
        node.viewState.bBox.paddingTop = config.flowCtrl.paddingTop;
    },

    beginVisitWhile(node: While) {
        node.viewState.bBox.paddingTop = config.flowCtrl.paddingTop;
    },

    beginVisitForeach(node: Foreach) {
        node.viewState.bBox.paddingTop = config.flowCtrl.paddingTop;
    },

    // tslint:disable-next-line:ban-types
    endVisitFunction(node: Function) {
        if (node.lambda || !node.body) { return; }
        const viewState: FunctionViewState = node.viewState;
        const body = viewState.body;
        const header = viewState.header;
        const client = viewState.client;
        const defaultWorker = viewState.defaultWorker;
        const workerHolder: WorkerTuple[] = [];

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
        defaultWorker.name = "default";
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
        defaultWorker.initHeight = calcPreWorkerHeight(node.body);
        node.body!.statements.filter((element) => ASTUtil.isWorker(element)).forEach((worker) => {
            sizeWorker(worker as VariableDef, defaultWorker.initHeight, workerHolder);
            if (lineHeight < worker.viewState.bBox.h) {
                lineHeight = worker.viewState.bBox.h;
            }
            workerWidth += worker.viewState.bBox.w;
        });
        // Set Worker Arrows
        syncWorkerInvocations(workerHolder);
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

        // Update return statement view-states.
        returnStatements.forEach((returnStmt) => {
            const returnViewState: ReturnViewState = returnStmt.viewState;
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
        if (!node.resource && returnStatements.length === 0) {
            const isNilType = (target: ASTNode) => ASTKindChecker.isValueType(target)
                && (target as ValueType).typeKind === "nil";

            // case one: returns () or no return type declaration
            viewState.implicitReturn.hidden = !(isNilType(node.returnTypeNode)
                // case two: returns a union type which wraps nil
                || (ASTKindChecker.isUnionTypeNode(node.returnTypeNode)
                    && (node.returnTypeNode as UnionTypeNode).memberTypeNodes.find(isNilType) !== undefined));
            viewState.implicitReturn.client = client;
            viewState.implicitReturn.bBox.h = config.statement.height;
            viewState.implicitReturn.bBox.w = config.statement.width;
        }
    },

    endVisitBlock(node: Block) {
        const viewState: BlockViewState = node.viewState;
        let height = 0;
        viewState.bBox.w = config.statement.width;
        node.statements.forEach((element) => {
            if (ASTUtil.isWorker(element) ||
                ASTKindChecker.isReturn(element)
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
        node.viewState.bBox.label = DiagramUtils
            .getTextWidth(ASTUtil.genSource(node.expression)).text;
        returnStatements.push(node);
    },

    endVisitCompoundAssignment(node: CompoundAssignment) {
        sizeStatement(node);
    },

    endVisitWorkerSend(node: WorkerSend) {
        sizeStatement(node);
    },

    endVisitPanic(node: Panic) {
        sizeStatement(node);
    },

    endVisitBreak(node: Break) {
        sizeStatement(node);
    },

    endVisitConstant(node: Constant) {
        sizeStatement(node);
    },

    endVisitService(node: Service) {
        const viewState: ViewState = node.viewState;
        let height = config.panelGroup.header.height;
        // tslint:disable-next-line:ban-types
        node.resources.forEach((element: Function) => {
            viewState.bBox.w = (viewState.bBox.w > element.viewState.bBox.w)
                ? viewState.bBox.w : element.viewState.bBox.w;
            height += element.viewState.bBox.h;
            element.viewState.icon = "resource";
        });
        viewState.bBox.h = height;
    },

    endVisitTypeDefinition(node: TypeDefinition) {
        // If it is a service do nothing.
        if (node.service || !ASTUtil.isValidObjectType(node)) { return; }
        const viewState: ViewState = node.viewState;
        let height = config.panelGroup.header.height;
        // tslint:disable-next-line:ban-types
        (node.typeNode as ObjectType).functions.forEach((element: Function) => {
            viewState.bBox.w = (viewState.bBox.w > element.viewState.bBox.w)
                ? viewState.bBox.w : element.viewState.bBox.w;
            height += element.viewState.bBox.h;
            element.viewState.icon = "function";
        });
        viewState.bBox.h = height;
    },

    endVisitMatchStaticPatternClause(node: MatchStaticPatternClause) {
        const viewState: ViewState = node.viewState;
        viewState.bBox.w = node.statement.viewState.bBox.w;
        viewState.bBox.h = node.statement.viewState.bBox.h
            + config.statement.height; // To print literal
        viewState.bBox.label = DiagramUtils.getTextWidth(ASTUtil.genSource(node.literal)).text;
    },

    endVisitMatch(node: Match) {
        const viewState: ViewState = node.viewState;
        let height = config.frame.topMargin + config.frame.header.height;
        let width = 0;
        node.patternClauses.forEach((element) => {
            height += element.viewState.bBox.h;
            width = (width > element.viewState.bBox.w) ?
                width : element.viewState.bBox.w;
        });
        viewState.bBox.h = height;
        viewState.bBox.w = width;
        viewState.bBox.leftMargin = 60;
    }
};
