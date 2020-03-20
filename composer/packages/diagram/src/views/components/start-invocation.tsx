
import { ASTNode } from "@ballerina/ast-model";
import _ from "lodash";
import * as React from "react";
import { DiagramConfig } from "../../config/default";
import { DiagramUtils } from "../../diagram/diagram-utils";
import { ViewState } from "../../view-model/index";
import { ArrowHead } from "./arrow-head";

const config: DiagramConfig = DiagramUtils.getConfig();

export const StartInvocation: React.StatelessComponent<{
    client: ViewState, worker: ViewState, y: number, label?: string, model: ASTNode
}> = ({
    client, worker, y, label, model
}) => {
        const startLine = { x1: 0, y1: 0, x2: 0, y2: 0 };
        const labelProps = {x: 0, y: 0};
        const statusRect = { x: 0, y: 0 , width: 55, height: 20, rx: 5, ry: 5 };
        const succesText = { x: 0, y: 0 , width: 0, height: 0 };
        const errorText = { x: 0, y: 0 , width: 0, height: 0 };
        const msText = { x: 0, y: 0 , width: 0, height: 0 };

        startLine.x1 = client.bBox.x + (client.bBox.w / 2);
        startLine.y1 = startLine.y2 = y + config.statement.height;
        startLine.x2 = worker.bBox.x + (worker.bBox.w / 2);

        labelProps.x = startLine.x1 + config.statement.padding.left;
        labelProps.y = y + (config.statement.height / 2);

        statusRect.x = startLine.x1 + config.statement.expanded.leftMargin - 2;
        statusRect.y = startLine.y1 + (config.statement.height / 3);

        succesText.y = statusRect.y + (config.statement.height / 2);
        succesText.x = statusRect.x + 5;
        errorText.y = statusRect.y + (config.statement.height / 2);
        errorText.x = succesText.x + config.statement.expanded.rightMargin - 5;
        msText.y = errorText.y ;
        msText.x = errorText.x + (config.statement.expanded.leftMargin * 3);

        const metrics = _.get(model, "variable.inititalExpression.metrics");

        const renderMetrics = () => {
            if (!metrics) {
                return (<g/>);
            } else {
                const { meanExecSuccessCount = 0, meanExecFailCount = 0, meanExecTime = 0 } = metrics;
                const totalCount = meanExecSuccessCount + meanExecFailCount;
                const successRate = (meanExecSuccessCount / totalCount) * 100;
                const errorRate = (meanExecFailCount / totalCount) * 100;
                const meanTimeMS = (meanExecTime * 1000).toFixed(2);
                return (
                   <React.Fragment>
                        <rect className = {"action-status"} {...statusRect} />
                        <text className= {"action-status-text-sucess"} {...succesText}>${successRate}%</text>
                        <text className= {"action-status-text-error"}  {...errorText}>${errorRate}%</text>
                        <text className= {"action-status-text-ms"}  {...msText}>${meanTimeMS}ms</text>
                   </React.Fragment>
                );
            }
        };

        return (
            <g className="start-invocation">
                {metrics && renderMetrics()}
                <line {...startLine} />
                <ArrowHead direction="right" x={startLine.x2} y={startLine.y2} />
                <text {...labelProps}>{label}</text>
            </g>
        );
    };
