
import { Block as BlockNode } from "@ballerina/ast-model";
import * as React from "react";
import { DiagramUtils } from "../../diagram/diagram-utils";

export const Block: React.StatelessComponent<{
        model: BlockNode
    }> = ({
        model
    }) => {
        const statements: React.ReactNode[] = [];
        statements.push(DiagramUtils.getComponents(model.statements));
        return (
            <g className="panel">
                {statements}
            </g>);
    };
