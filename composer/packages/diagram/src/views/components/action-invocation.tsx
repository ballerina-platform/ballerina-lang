import { ASTNode, ASTUtil } from "@ballerina/ast-model";
import _ from "lodash";
import * as React from "react";
import { Popup } from "semantic-ui-react";
import { DiagramConfig } from "../../config/default";
import { DiagramUtils } from "../../diagram/diagram-utils";
// import { getCodePoint } from "../../utils";
import { StmntViewState } from "../../view-model/index";
import { ArrowHead } from "./arrow-head";
import { SourceLinkedLabel } from "./source-linked-label";

const config: DiagramConfig = DiagramUtils.getConfig();

export const ActionInvocation: React.StatelessComponent<{
    model: StmntViewState,
    action: string,
    astModel?: ASTNode,
    // icon: string,
}> = ({
    model, action, astModel,
}) => {
        // const topIcon = { x: 0, y: 0, className: "alert" };
        const sendLine = { x1: 0, y1: 0, x2: 0, y2: 0 };
        const receiveLine = { x1: 0, y1: 0, x2: 0, y2: 0 };
        const actionProps = { x: 0, y: 0 };
        const statusRect = { x: 0, y: 0 , width: 55, height: 20, rx: 5, ry: 5 };
        const succesText = { x: 0, y: 0 , width: 0, height: 0 };
        const errorText = { x: 0, y: 0 , width: 0, height: 0 };
        const msText = { x: 0, y: 0 , width: 0, height: 0 };

        sendLine.x1 = model.bBox.x;
        sendLine.y1 = sendLine.y2 = model.bBox.y + config.statement.height ;
        sendLine.x2 = model.endpoint.bBox.x + (model.endpoint.bBox.w / 2) - 3;

        receiveLine.x1 = sendLine.x1;
        receiveLine.x2 = sendLine.x2;
        receiveLine.y1 = receiveLine.y2 = sendLine.y1 + config.statement.height;

        actionProps.x = model.bBox.x + config.statement.padding.left;
        actionProps.y = model.bBox.y + (config.statement.height / 2);

        statusRect.x = model.bBox.x + (config.statement.expanded.rightMargin * 7);
        statusRect.y = sendLine.y1 - (config.statement.expanded.topMargin * 2) - 5 ;

        succesText.y = statusRect.y + (config.statement.height / 2);
        succesText.x = statusRect.x + 5;
        errorText.y = statusRect.y + (config.statement.height / 2);
        errorText.x = succesText.x + config.statement.expanded.rightMargin - 5;
        msText.y = errorText.y + config.statement.height + (config.statement.expanded.topMargin / 2) ;
        msText.x = errorText.x - config.statement.expanded.leftMargin - 5;

        const metrics = _.get(astModel, "variable.initialExpression.metrics");

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
                        <text className= {"action-status-text-sucess"} {...succesText}>{successRate}%</text>
                        <text className= {"action-status-text-error"}  {...errorText}>{errorRate}%</text>
                        <text className= {"action-status-text-ms"}  {...msText}>{meanTimeMS}ms</text>
                   </React.Fragment>
                );
            }
        };

        const fullExpression = (astModel) ? ASTUtil.genSource(astModel) : action;

        return (
            <g className="action-invocation">
                {metrics && renderMetrics()}
                <line {...sendLine} className="invoke-line" />
                <ArrowHead direction="right" x={sendLine.x2} y={sendLine.y2} className="invoke-arrowhead" />
                <line {...receiveLine} strokeDasharray={5} className="invoke-line" />
                <ArrowHead direction="left" x={receiveLine.x1} y={receiveLine.y1} className="invoke-arrowhead"/>
                <rect x={sendLine.x2} y={sendLine.y2} width="6" rx="3" ry="3" height={(config.statement.height)}
                    className="life-line-endpoint-activity" />
                <Popup
                    trigger={
                        <g>
                            {!astModel && <text {...actionProps}>{action}</text>}
                            {astModel && <SourceLinkedLabel {...actionProps} text={action} target={astModel} />}
                        </g>
                    }
                    content={fullExpression}
                    size="mini"
                    inverted
                />
            </g>
        );
    };
