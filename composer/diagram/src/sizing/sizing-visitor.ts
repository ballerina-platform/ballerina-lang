import { Assignment, Block, Visitor } from "@ballerina/ast-model";

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
