
import { ASTNode } from "@ballerina/ast-model";
import * as React from "react";
import { DiagramConfig } from "../../config/default";
import { DiagramUtils } from "../../diagram/diagram-utils";
import { StmntViewState } from "../../view-model";
import { ActionInvocation } from "./action-invocation";
import { ReturnActionInvocation } from "./return-action-invocation";

const config: DiagramConfig = DiagramUtils.getConfig();

export const Statement: React.StatelessComponent<{
    model: ASTNode
}> = ({
    model
}) => {
        const viewState: StmntViewState = model.viewState;

        const statementProps = {
            className: "statement",
            x: viewState.bBox.x + config.statement.padding.left,
            y: viewState.bBox.y + (viewState.bBox.h / 2)
        };

        return (
            <g className="statement">
                {viewState.isAction && !viewState.isReturn
                    && <ActionInvocation model={viewState} action={viewState.bBox.label} />}
                {viewState.isAction && viewState.isReturn
                    && <ReturnActionInvocation model={viewState} action={viewState.bBox.label} />}
                {!viewState.isAction && <text {...statementProps}>{viewState.bBox.label}</text>}
            </g>);
    };
