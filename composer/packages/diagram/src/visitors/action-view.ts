import { ASTKindChecker, ASTNode, ASTUtil, Block, Visitor } from "@ballerina/ast-model";
import { StmntViewState } from "../view-model/index";

let actionView = false;

function isOrdinaryStatement(node: ASTNode): boolean {
    return !ASTKindChecker.isIf(node)
        && !ASTKindChecker.isMatch(node)
        && !ASTKindChecker.isReturn(node)
        && !ASTKindChecker.isWhile(node)
        && !ASTKindChecker.isForeach(node)
        && !ASTKindChecker.isWorkerSend(node)
        && !ASTKindChecker.isWorkerSyncSend(node)
        && !ASTUtil.isWorker(node)
        && !ASTUtil.isWorkerFuture(node)
        && !ASTUtil.isWorkerReceive(node)
        && !ASTUtil.isActionInvocation(node);
}

function beginVisitBlock(node: Block) {
    let hiddenSet = false;
    node.statements.forEach((element) => {
        if (isOrdinaryStatement(element)) {
            if (element.viewState) {
                element.viewState.hidden = actionView && hiddenSet;
                element.viewState.hiddenBlock = !hiddenSet && actionView;
                hiddenSet = true;
            }
        } else {
            if (!ASTUtil.isWorker(element)) {
                hiddenSet = false;
            }
        }
        const viewState = element.viewState as StmntViewState;
        if (viewState.expandContext && actionView) {
            viewState.expandContext = undefined;
        }
    });
}

export const visitor: Visitor = {
    beginVisitBlock(node: Block) {
        beginVisitBlock(node);
    },
    beginVisitBlockFunctionBody(node) {
        beginVisitBlock(node);
    }
};

export function setActionViewStatus(enabled = false) {
    actionView = enabled;
}
