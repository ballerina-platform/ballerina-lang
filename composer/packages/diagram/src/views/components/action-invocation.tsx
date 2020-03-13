import { ASTNode, ASTUtil } from "@ballerina/ast-model";
import * as React from "react";
import { Popup } from "semantic-ui-react";
import { DiagramConfig } from "../../config/default";
import { DiagramUtils } from "../../diagram/diagram-utils";
import { StmntViewState } from "../../view-model/index";
import { ArrowHead } from "./arrow-head";
import { SourceLinkedLabel } from "./source-linked-label";

const config: DiagramConfig = DiagramUtils.getConfig();

export const ActionInvocation: React.StatelessComponent<{
    model: StmntViewState, action: string, astModel?: ASTNode
}> = ({
    model, action, astModel
}) => {
        const sendLine = { x1: 0, y1: 0, x2: 0, y2: 0 };
        const receiveLine = { x1: 0, y1: 0, x2: 0, y2: 0 };
        const actionProps = { x: 0, y: 0 };

        sendLine.x1 = model.bBox.x;
        sendLine.y1 = sendLine.y2 = model.bBox.y + config.statement.height ;
        sendLine.x2 = model.endpoint.bBox.x + (model.endpoint.bBox.w / 2) - 3;

        receiveLine.x1 = sendLine.x1;
        receiveLine.x2 = sendLine.x2;
        receiveLine.y1 = receiveLine.y2 = sendLine.y1 + config.statement.height;

        actionProps.x = model.bBox.x + config.statement.padding.left;
        actionProps.y = model.bBox.y + (config.statement.height / 2);

        const fullExpression = (astModel) ? ASTUtil.genSource(astModel) : action;

        return (
            <g className="action-invocation">
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
