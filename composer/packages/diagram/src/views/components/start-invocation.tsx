
import * as React from "react";
import { DiagramConfig } from "../../config/default";
import { DiagramUtils } from "../../diagram/diagram-utils";
import { ViewState } from "../../view-model/index";
import { ArrowHead } from "./arrow-head";

const config: DiagramConfig = DiagramUtils.getConfig();

export const StartInvocation: React.StatelessComponent<{
    client: ViewState, worker: ViewState, y: number, label?: string
}> = ({
    client, worker, y, label
}) => {
        const startLine = { x1: 0, y1: 0, x2: 0, y2: 0 };
        const labelProps = {x: 0, y: 0};

        startLine.x1 = client.bBox.x + (client.bBox.w / 2);
        startLine.y1 = startLine.y2 = y + config.statement.height;
        startLine.x2 = worker.bBox.x + (worker.bBox.w / 2);

        labelProps.x = startLine.x1 + config.statement.padding.left;
        labelProps.y = y + (config.statement.height / 2);
        return (
            <g className="start-invocation">
                <line {...startLine} />
                <ArrowHead direction="right" x={startLine.x2} y={startLine.y2} />
                <text {...labelProps}>{label}</text>
            </g>
        );
    };
