
import * as React from "react";
import { DiagramConfig } from "../../config/default";
import { DiagramUtils } from "../../diagram/diagram-utils";

const config: DiagramConfig = DiagramUtils.getConfig();

export const Condition: React.StatelessComponent<{
        x: number,
        y: number,
        label: string,
        expression: string,
    }> = ({
        x, y, label, expression
    }) => {
        const hHeight = (config.flowCtrl.header.height / 2);

        const labelProps = { x , y};
        const conditionProps = {x, y};
        const p1 = { x: 0, y: 0};
        const p2 = { x: 0, y: 0};
        const p3 = { x: 0, y: 0};
        const p4 = { x: 0, y: 0};
        const trueProps = { x: 0, y: 0};
        const falseProps = { x: 0, y: 0};
        //     p1
        //     /\
        // p4 /  \ p2
        //    \  /
        //     \/
        //     p3
        p1.x = x;
        p1.y = y - hHeight;

        p2.x = x + hHeight;
        p2.y = y;

        p3.x = x;
        p3.y = y + hHeight;

        p4.x = x - hHeight;
        p4.y = y;

        conditionProps.x = x + (hHeight / 2) + 5;
        conditionProps.y = y + (hHeight / 2) + 5;

        trueProps.x = p3.x - 30;
        trueProps.y = p3.y + 5;

        falseProps.x = p2.x + 5;
        falseProps.y = p2.y - 10;

        return (
            <g className="condition">
                <text {...conditionProps} className="expression">{expression}</text>
                <polyline
                    points={`${p1.x},${p1.y} ${p2.x},${p2.y} ${p3.x},${p3.y} ${p4.x},${p4.y} ${p1.x},${p1.y}`}
                />
                <text {...labelProps} className="label">{label}</text>
                <text {...trueProps} className="condition-case">true</text>
                <text {...falseProps} className="condition-case">false</text>
            </g>);
    };
