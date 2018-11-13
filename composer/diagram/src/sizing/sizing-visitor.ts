import { Visitor } from "../base-visitor";
import { Assignment, Block, CompilationUnit } from "../models";

export const visitor: Visitor = {
    beginVisitAssignment(node: Assignment) {
        node.viewState = {};
    },

    endVisitBlock(node: Block) {
        let height: number = 0;
        node.statements.forEach((statement) => {
            if (statement.viewState) {
                height += statement.viewState.height;
            }
        });

        node.viewState = {
            height,
        };
    },
};
