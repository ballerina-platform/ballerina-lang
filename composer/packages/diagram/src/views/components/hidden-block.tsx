
import { ASTNode } from "@ballerina/ast-model";
import * as React from "react";
import { StmntViewState } from "../../view-model";

const boxWidth = 25;

export const HiddenBlock: React.StatelessComponent<{
    model: ASTNode
}> = ({
    model
}) => {
        const viewState: StmntViewState = model.viewState;

        const blockProps = {
            className: "hidden-block",
            height: 10,
            width: boxWidth,
            x: viewState.bBox.x - (boxWidth / 2),
            y: viewState.bBox.y + (viewState.bBox.h / 2),
        };

        const labelProps = {
            className: "hidden-block",
            strokeDasharray: "4 3",
            x1: viewState.bBox.x - (boxWidth / 2) + 4,
            x2: viewState.bBox.x - (boxWidth / 2) + 21,
            y1: viewState.bBox.y + (viewState.bBox.h / 2) + 5,
            y2: viewState.bBox.y + (viewState.bBox.h / 2) + 5,
        };

        return (
            <g className="hidden-block">
                <rect {...blockProps} />
                <line {...labelProps} />
            </g>);
    };
