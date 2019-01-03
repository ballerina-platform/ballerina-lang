import { ASTNode, ASTUtil } from "@ballerina/ast-model";
import * as React from "react";
import { Popup } from "semantic-ui-react";
import { DiagramConfig } from "../../config/default";
import { DiagramUtils } from "../../diagram/diagram-utils";
import { StmntViewState } from "../../view-model";
import { ActionInvocation } from "./action-invocation";
import { ArrowHead } from "./arrow-head";

const config: DiagramConfig = DiagramUtils.getConfig();

export const Return: React.StatelessComponent<{
        model: ASTNode
    }> = ({
        model
    }) => {
        const viewState: StmntViewState = model.viewState;
        if (viewState.hidden) {
            return null;
        }

        const returnLine = { x1: 1, y1: 0, x2: 0, y2: 0 };
        returnLine.x1 = model.viewState.bBox.x;
        returnLine.y1 = returnLine.y2 = model.viewState.bBox.y + config.statement.height;
        // TODO: Following is a hack to fix object return need to revisit the logic.
        returnLine.x2 = 90; // model.viewState.client.bBox.x + (model.viewState.client.bBox.w / 2);

        if (viewState.isAction) {
            returnLine.y2 = returnLine.y1 += (config.statement.height / 2);
        }

        const statementProps = {
            className: "statement",
            x: returnLine.x2 + config.statement.padding.left,
            y: viewState.bBox.y + (viewState.bBox.h / 2)
        };

        const fullExpression = (model) ? ASTUtil.genSource(model) : viewState.bBox.label;

        return (
            <g className="action-invocation">
                {viewState.isAction && <ActionInvocation
                    model={viewState}
                    action={viewState.bBox.label}
                    astModel={model} />}
                <ArrowHead direction="left" x={returnLine.x2} y={returnLine.y2} />
                <line {...returnLine} />
                <Popup
                    trigger={!viewState.isAction && <text {...statementProps}>{viewState.bBox.label}</text>}
                    content={fullExpression}
                    size="mini"
                    inverted
                />
            </g>
        );
    };
