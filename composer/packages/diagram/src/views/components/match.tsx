
import { ASTUtil, Match as MatchNode } from "@ballerina/ast-model";
import * as React from "react";
import { DiagramConfig } from "../../config/default";
import { DiagramUtils } from "../../diagram/diagram-utils";
import { ViewState } from "../../view-model/index";
import { HiddenBlock } from "./hidden-block";

const config: DiagramConfig = DiagramUtils.getConfig();

export const Match: React.StatelessComponent<{
        model: MatchNode
    }> = ({
        model
    }) => {
        const viewState: ViewState = model.viewState;

        if (viewState.hiddenBlock) {
            return <HiddenBlock model={ model }/>;
        }

        const frameBorder = {x: 0, y: 0, width: 0, height: 0, className: "frame-border"};
        const p = { x1: 0, y1: 0 , x2: 0, y2: 0, x3: 0, y3: 0, x4: 0, y4: 0};

        frameBorder.x = viewState.bBox.x - viewState.bBox.leftMargin;
        frameBorder.y = viewState.bBox.y + config.frame.topMargin;
        frameBorder.width = viewState.bBox.w + viewState.bBox.leftMargin;
        frameBorder.height = viewState.bBox.h - config.frame.topMargin;

        const expression = DiagramUtils.getTextWidth("match " + ASTUtil.genSource(model.expression).trim());

        p.x1 = p.x2 = frameBorder.x;
        p.y1 = p.y4 = frameBorder.y;
        p.y2 = p.y3 = p.y1 + config.statement.height;
        p.x3 = p.x1 + expression.w + 10;
        p.x4 = p.x3 + config.statement.height;

        return (
            <g className="match">
                <rect {...frameBorder} />
                <polygon points={`${p.x1},${p.y1} ${p.x2},${p.y2} ${p.x3},${p.y3} ${p.x4},${p.y4}`} />
                <text x={p.x1 + 5} y={p.y1 + (config.statement.height / 2) }>{expression.text}</text>
                {model.patternClauses.map((element) => {
                    const cmp = DiagramUtils.getComponents(element.statement);

                    const lineProps = { x1: 0, y1: 0, x2: 0, y2: 0};
                    lineProps.x1 = frameBorder.x;
                    lineProps.y1 = lineProps.y2 = element.viewState.bBox.y + element.viewState.bBox.h;
                    lineProps.x2 = frameBorder.x + frameBorder.width;

                    const patternLabel = { x: 0, y: 0};
                    patternLabel.x = frameBorder.x + 5;
                    patternLabel.y = element.viewState.bBox.y + (config.statement.height / 2);

                    return [cmp , <line {...lineProps} />,
                    <text {...patternLabel}>{element.viewState.bBox.label}</text>];
                })}
            </g>);
    };
