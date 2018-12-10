
import * as React from "react";
import { DiagramConfig } from "../../config/default";
import { DiagramUtils } from "../../diagram/diagram-utils";

const config: DiagramConfig = DiagramUtils.getConfig();

export const ForeachBox: React.StatelessComponent<{
        x: number,
        y: number,
        label: string,
        expression: string,
    }> = ({
        x, y, label, expression
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
                <text {...labelProps} className="label">{label}</text>
            </g>);
    };
