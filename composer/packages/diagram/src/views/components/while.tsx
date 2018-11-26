
import { ASTUtil, While as WhileNode} from "@ballerina/ast-model";
import * as React from "react";
import { DiagramConfig } from "../../config/default";
import { DiagramUtils } from "../../diagram/diagram-utils";
import { ViewState } from "../../view-model";
import { ArrowHead } from "./arrow-head";
import { Condition } from "./condition";

const config: DiagramConfig = DiagramUtils.getConfig();

export const While: React.StatelessComponent<{
        model: WhileNode
    }> = ({
        model
    }) => {
        const viewState: ViewState = model.viewState;
        const body = [];

        const conditionProps = {
            expression: ASTUtil.genSource(model.condition),
            label: "while",
            x: viewState.bBox.x,
            y: viewState.bBox.y + (config.flowCtrl.header.height / 2),
        };

        // Continue Line
        const p1 = { x: 0, y: 0};
        const p2 = { x: 0, y: 0};
        const p3 = { x: 0, y: 0};
        const p4 = { x: 0, y: 0};

        p1.x = model.body.viewState.bBox.x;
        p1.y = model.body.viewState.bBox.y + model.body.viewState.bBox.h;

        p2.x = p1.x - viewState.bBox.leftMargin;
        p2.y = p1.y;

        p3.x = p2.x;
        p3.y = conditionProps.y;

        p4.x = p1.x - (config.flowCtrl.header.height / 2);
        p4.y = conditionProps.y;

        body.push(DiagramUtils.getComponents(model.body));

        // Return Line
        const r1 = { x: 0, y: 0};
        const r2 = { x: 0, y: 0};
        const r3 = { x: 0, y: 0};
        const r4 = { x: 0, y: 0};

        r1.x = conditionProps.x + (config.flowCtrl.header.height / 2);
        r1.y = conditionProps.y;

        r2.x = conditionProps.x +  model.body.viewState.bBox.w;
        r2.y = r1.y;

        r3.x = r2.x;
        r3.y = p1.y + config.flowCtrl.whileGap;

        r4.x =  conditionProps.x;
        r4.y = r3.y;

        return (
            <g className="panel">
                <Condition {...conditionProps}/>
                {body}
                <polyline className="condition-line"
                    points={`${p1.x},${p1.y} ${p2.x},${p2.y} ${p3.x},${p3.y} ${p4.x},${p4.y}`}
                />
                <polyline className="condition-line"
                    points={`${r1.x},${r1.y} ${r2.x},${r2.y} ${r3.x},${r3.y} ${r4.x},${r4.y}`}
                />
                <line className="hide-line" x1={p1.x} y1={p1.y + 1} x2={r4.x} y2={r4.y - 1} strokeLinecap="round" />
                <ArrowHead direction={"right"} {...p4} />
            </g>);
    };
