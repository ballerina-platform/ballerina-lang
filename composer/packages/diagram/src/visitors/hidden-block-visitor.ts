
import { ASTKindChecker, ASTNode, ASTUtil, Block, Visitor } from "@ballerina/ast-model";
import * as _ from "lodash";
import { HiddenBlockContext } from "../view-model/hidden-block-context";
import { StmntViewState } from "../view-model/index";

export const visitor: Visitor = {

    endVisitBlock(node: Block) {
        let hiddenSet = false;
        let hiddenSetNodes: ASTNode[] = [];
        let resetBlock = false;

        node.viewState.hidden = false;

        node.statements.forEach((stmt) => {
            if (ASTUtil.isWorker(stmt) || ASTUtil.isWorkerFuture(stmt)) {
                return;
            }

            const statementViewState = stmt.viewState as StmntViewState;

            if (statementViewState.expandContext && statementViewState.expandContext.expandedSubTree
                && !statementViewState.hidden && !statementViewState.hiddenBlock) {
                ASTUtil.traversNode(statementViewState.expandContext.expandedSubTree, visitor);
            }

            if (stmt.viewState.hidden || stmt.viewState.hiddenBlock) {
                if (!hiddenSet) {
                    const hiddenSetViewState = stmt.viewState as StmntViewState;
                    hiddenSet = true;
                    let hiddenSetNode;
                    if (hiddenSetViewState.hiddenBlockContext === undefined) {
                        resetBlock = true;
                        hiddenSetNode = _.cloneDeep(stmt);
                    }
                    hiddenSetViewState.hidden = false;
                    hiddenSetViewState.hiddenBlock = true;

                    if (hiddenSetNode) {
                        const hiddenSetNodeViewState = hiddenSetNode.viewState as StmntViewState;
                        hiddenSetViewState.hiddenBlockContext = new HiddenBlockContext();
                        hiddenSetNodeViewState.hidden = false;
                        if (ASTKindChecker.isMatch(hiddenSetNode)) {
                            hiddenSetNode.patternClauses.forEach((clause) => {
                                clause.viewState.hidden = false;
                                clause.statement.viewState.hidden = false;
                            });
                        }

                        hiddenSetNodeViewState.hiddenBlock = false;
                        hiddenSetNodeViewState.isInHiddenBlock = true;
                        hiddenSetNodeViewState.hiddenBlockContext = undefined;
                        hiddenSetNodes = hiddenSetViewState.hiddenBlockContext.otherHiddenNodes = [hiddenSetNode];
                    }
                } else if (hiddenSetNodes) {
                    if (resetBlock) {
                        const hiddenSetNode = _.cloneDeep(stmt);
                        hiddenSetNode.viewState.hidden = false;
                        if (ASTKindChecker.isMatch(hiddenSetNode)) {
                            hiddenSetNode.patternClauses.forEach((clause) => {
                                clause.viewState.hidden = false;
                                clause.statement.viewState.hidden = false;
                            });
                        }

                        (hiddenSetNode.viewState as StmntViewState).isInHiddenBlock = true;
                        hiddenSetNodes.push(hiddenSetNode);
                    }
                }
            } else {
                // one set is complete
                hiddenSet = false;
                if (hiddenSetNodes) {
                    hiddenSetNodes.forEach((hiddenNode) => {
                        ASTUtil.traversNode(hiddenNode, visitor);
                        hiddenNode.viewState.hidden = false;
                        const hiddenNodeViewstate = hiddenNode.viewState as StmntViewState;
                        if (hiddenNodeViewstate.expandContext && hiddenNodeViewstate.expandContext.expandedSubTree) {
                            hiddenNodeViewstate.expandContext.expandedSubTree.viewState.hidden = false;
                            ASTUtil.traversNode(hiddenNodeViewstate.expandContext.expandedSubTree, visitor);
                        }
                    });
                    hiddenSetNodes = [];
                }
            }
        });

        hiddenSetNodes.forEach((hiddenNode) => {
            ASTUtil.traversNode(hiddenNode, visitor);
            hiddenNode.viewState.hidden = false;
            const hiddenNodeViewstate = hiddenNode.viewState as StmntViewState;
            if (hiddenNodeViewstate.expandContext && hiddenNodeViewstate.expandContext.expandedSubTree) {
                hiddenNodeViewstate.expandContext.expandedSubTree.viewState.hidden = false;
                ASTUtil.traversNode(hiddenNodeViewstate.expandContext.expandedSubTree, visitor);
            }
        });
    },
};
