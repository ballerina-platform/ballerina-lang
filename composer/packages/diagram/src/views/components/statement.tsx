
import { ASTNode} from "@ballerina/ast-model";
import * as React from "react";
import { DiagramConfig } from "../../config/default";
import { DiagramUtils } from "../../diagram/diagram-utils";
import { ViewState } from "../../view-model";

const config: DiagramConfig = DiagramUtils.getConfig();

export const Statement: React.StatelessComponent<{
        model: ASTNode
    }> = ({
        model
    }) => {
        const viewState: ViewState = model.viewState;

        const statementProps = {
            className: "statement",
            x: viewState.bBox.x + config.statement.padding.left,
            y: viewState.bBox.y + (viewState.bBox.h / 2)
        };

        return (
            <g className="panel">
                <text {...statementProps}>{viewState.bBox.label}</text>
            </g>);
    };
