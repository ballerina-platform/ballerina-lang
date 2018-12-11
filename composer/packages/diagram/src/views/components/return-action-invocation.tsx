import { ASTNode, ASTUtil } from "@ballerina/ast-model";
import * as React from "react";
import { Popup } from "semantic-ui-react";
import { DiagramConfig } from "../../config/default";
import { DiagramUtils } from "../../diagram/diagram-utils";
import { StmntViewState } from "../../view-model/index";
import { ArrowHead } from "./arrow-head";
import { SourceLinkedLabel } from "./source-linked-label";

const config: DiagramConfig = DiagramUtils.getConfig();

export const ReturnActionInvocation: React.StatelessComponent<{
    model: StmntViewState, action: string, astModel?: ASTNode
}> = ({
    model, action, astModel
}) => {
        const returnLine = { x1: 0, y1: 0, x2: 0, y2: 0 };
        const actionProps = { x: 0, y: 0 };

        returnLine.x1 = model.bBox.x;
        returnLine.y1 = returnLine.y2 = model.bBox.y + config.statement.height;
        returnLine.x2 = model.endpoint.bBox.x + (model.endpoint.bBox.w / 2);

        actionProps.x = returnLine.x2 + config.statement.padding.left;
        actionProps.y = returnLine.y2 - (config.statement.height / 2);

        const fullExpression = (astModel) ? ASTUtil.genSource(astModel) : action;

        return (
            <g className="action-invocation">
                <line {...returnLine} />
                <ArrowHead direction="left" x={returnLine.x2} y={returnLine.y2} />
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
