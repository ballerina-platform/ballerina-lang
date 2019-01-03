
import { ASTNode } from "@ballerina/ast-model";
import * as React from "react";
import { DiagramConfig } from "../../config/default";
import { DiagramUtils } from "../../diagram/diagram-utils";
import { SourceLinkedLabel } from "./source-linked-label";

const config: DiagramConfig = DiagramUtils.getConfig();

export const ForeachBox: React.StatelessComponent<{
        x: number,
        y: number,
        label: string,
        expression: string,
        astModel?: ASTNode
    }> = ({
        x, y, label, expression, astModel
    }) => {
        const hHeight = (config.flowCtrl.foreach.height / 2) - 5;
        const hWidth = (config.flowCtrl.foreach.width / 2);

        const labelProps = { x , y };
        const conditionProps = {x, y};
        const p1 = { x: 0, y: 0};
        const p2 = { x: 0, y: 0};
        const p3 = { x: 0, y: 0};
        const p4 = { x: 0, y: 0};
        //    p1___________p2
        //     |           |
        //     |           |
        //     |___________|
        //    p3           p4
        p1.x = x - hWidth;
        p1.y = y - hHeight;

        p2.x = x + hWidth;
        p2.y = y - hHeight;

        p3.x = p2.x;
        p3.y = y + hHeight;

        p4.x = p1.x;
        p4.y = y + hHeight;

        conditionProps.x = x ;
        conditionProps.y = y + (hHeight / 2);

        return (
            <g className="condition">
                <polyline
                    points={`${p1.x},${p1.y} ${p2.x},${p2.y} ${p3.x},${p3.y} ${p4.x},${p4.y} ${p1.x},${p1.y}`}
                />
                {!astModel && <text {...labelProps} className="label">{label}</text>}
                {astModel && <SourceLinkedLabel
                                    {...labelProps} target={astModel}
                                    text={label} className="label"
                                />
                }
            </g>);
    };
