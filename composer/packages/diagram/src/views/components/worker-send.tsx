
import { WorkerSend as WorkerSendNode} from "@ballerina/ast-model";
import * as React from "react";
import { DiagramConfig } from "../../config/default";
import { DiagramUtils } from "../../diagram/diagram-utils";
import { WorkerSendViewState } from "../../view-model/worker-send";
import { ActionInvocation } from "./action-invocation";
import { ArrowHead } from "./arrow-head";

const config: DiagramConfig = DiagramUtils.getConfig();

export const WorkerSend: React.StatelessComponent<{
        model: WorkerSendNode
    }> = ({
        model
    }) => {
        const viewState: WorkerSendViewState = model.viewState;

        const mLine = { x1: 1, y1: 0, x2: 0, y2: 0 };
        mLine.x1 = viewState.bBox.x;
        mLine.y1 = mLine.y2 = viewState.bBox.y + config.statement.height;
        mLine.x2 = viewState.to.lifeline.bBox.x + (viewState.to.lifeline.bBox.w / 2);

        if (viewState.isAction) {
            mLine.y2 = mLine.y1 += (config.statement.height / 2);
        }

        const arrowDirection = (mLine.x2 > mLine.x1) ? "right" : "left";

        const statementProps = {
            className: "statement",
            x: mLine.x1 + config.statement.padding.left,
            y: viewState.bBox.y + (viewState.bBox.h / 2)
        };

        if (!viewState.isSynced) {
            // Don't draw the arrow if not syced
            return <g className="action-invocation">
                {!viewState.isAction && <text {...statementProps}>{viewState.bBox.label}</text>}
            </g>;
        }

        return (
            <g className="action-invocation">
                {viewState.isAction && <ActionInvocation
                    model={viewState}
                    action={viewState.bBox.label}
                    astModel={model} />}
                <ArrowHead direction={arrowDirection} x={mLine.x2} y={mLine.y2} />
                <line {...mLine} />
                {!viewState.isAction && <text {...statementProps}>{viewState.bBox.label}</text>}
            </g>);
    };
