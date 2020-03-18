
import { ASTNode } from "@ballerina/ast-model";
import * as React from "react";
import { DiagramConfig } from "../../config/default";
import { DiagramUtils } from "../../diagram/diagram-utils";
import { DiagramContext } from "../../diagram/index";
import { getCodePoint } from "../../utils";
import { StmntViewState } from "../../view-model";

const boxWidth = 25;
const config: DiagramConfig = DiagramUtils.getConfig();

export const HiddenBlock: React.StatelessComponent<{
    model: ASTNode
}> = ({
    model
}) => {
        const viewState: StmntViewState = model.viewState;
        const { bBox } = viewState;

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
            <DiagramContext.Consumer>{
                ({update}) => {
                    if (viewState.hiddenBlockContext && viewState.hiddenBlockContext.expanded) {
                        return (<g className="expanded-hidden-block">
                            <rect className="frame"
                                x={bBox.x - viewState.bBox.leftMargin}
                                y={bBox.y + config.statement.expanded.topMargin}
                                width={bBox.w + viewState.bBox.leftMargin - config.statement.expanded.rightMargin}
                                height={bBox.h - (2 * config.statement.expanded.bottomMargin)}/>
                            <text className="collapser"
                                x={bBox.x + bBox.w - config.statement.expanded.collapserWidth
                                    - config.statement.expanded.rightMargin}
                                y={bBox.y + config.statement.expanded.topMargin
                                    + config.statement.expanded.collapserHeight}
                                onClick={(e) => {
                                    if (viewState.hiddenBlockContext) {
                                        e.stopPropagation();
                                        viewState.hiddenBlockContext.expanded = false;
                                        update();
                                    }}}>
                                {getCodePoint("up")}
                            </text>
                            {DiagramUtils.getComponents(
                                viewState.hiddenBlockContext.otherHiddenNodes)}
                        </g>);
                    }

                    return (<g className="hidden-block" onClick={(e) => {
                        if (viewState.hiddenBlockContext) {
                            e.stopPropagation();
                            viewState.hiddenBlockContext.expanded = true;
                            update();
                        }}}>
                        <rect {...blockProps} />
                        <line {...labelProps} />
                    </g>);
                }
            }
            </DiagramContext.Consumer>
        );
    };
