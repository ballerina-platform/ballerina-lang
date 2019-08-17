import { ASTNode, ASTUtil } from "@ballerina/ast-model";
import * as React from "react";
import { DiagramConfig } from "../../config/default";
import { DiagramUtils } from "../../diagram/diagram-utils";
import { StmntViewState, ViewState } from "../../view-model";
import { ReturnViewState } from "../../view-model/return";
import { ActionInvocation } from "./action-invocation";
import { ArrowHead } from "./arrow-head";
import { SourceLinkedLabel } from "./source-linked-label";

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

        let returnLines = [];
        const returnViewState = viewState as any as ReturnViewState;
        if (returnViewState.callerViewStates && Object.keys(returnViewState.callerViewStates).length > 0) {
            returnLines = Object.keys(returnViewState.callerViewStates).map((callerVSName) => {
                return getReturnLine(returnViewState, returnViewState.callerViewStates[callerVSName]);
            });
        } else {
            returnLines = [getReturnLine(viewState, returnViewState.client)];
        }

        return <>() => {
            returnLines.map((returnLine) => {
            const statementProps = {
                className: "statement",
                fullText: (model) ? ASTUtil.genSource(model) : undefined,
                target: model,
                text: viewState.bBox.label,
                x: returnLine.x2 + config.statement.padding.left,
                y: viewState.bBox.y + (viewState.bBox.h / 2)
            };

            const direction = returnLine.x1 > returnLine.x2 ? "left" : "right";

            return <g className="action-invocation">
                {viewState.isAction && <ActionInvocation
                    model={viewState}
                    action={viewState.bBox.label}
                    astModel={model} />}
                <ArrowHead direction={direction} x={returnLine.x2} y={returnLine.y2} />
                <line {...returnLine} />
                <SourceLinkedLabel {...statementProps}  />
            </g>;
            })
        }</>;
    };

function getReturnLine(modelVS: ReturnViewState | StmntViewState, callerVS: ViewState) {
    const returnLine = { x1: 1, y1: 0, x2: 0, y2: 0 };
    returnLine.x1 = modelVS.bBox.x;
    returnLine.y1 = returnLine.y2 = modelVS.bBox.y + config.statement.height;
    returnLine.x2 = callerVS.bBox.x + (callerVS.bBox.w / 2);

    if (modelVS.isAction) {
        returnLine.y2 = returnLine.y1 += (config.statement.height / 2);
    }

    return returnLine;
}
