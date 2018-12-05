
import * as React from "react";
import { DiagramConfig } from "../../config/default";
import { DiagramUtils } from "../../diagram/diagram-utils";

const config: DiagramConfig = DiagramUtils.getConfig();

export const Condition: React.StatelessComponent<{
        x: number,
        y: number,
        label: string | React.ReactNode,
        expression: string,
        width?: number
    }> = ({
        x, y, label, expression, width
    }) => {
        const controlWidth = (width) ? width : config.statement.width;
        const hHeight = (config.flowCtrl.condition.height / 2);
        const conditionLables = { true: "true", false: "false" };
        const labelProps = { x , y };
        const conditionProps = { x, y };
        const p1 = { x: 0, y: 0 };
        const p2 = { x: 0, y: 0 };
        const p3 = { x: 0, y: 0 };
        const p4 = { x: 0, y: 0 };
        const trueProps = {
            height: config.statement.height,
            width: DiagramUtils.getTextWidth(conditionLables.true, 0, 1000,
                config.condition.caseLabel.padding.left, config.condition.caseLabel.padding.right).w,
            x: 0,
            y: 0
        };
        const falseProps = {
            height: config.statement.height,
            width: DiagramUtils.getTextWidth(conditionLables.false, 0, 1000,
                config.condition.caseLabel.padding.left, config.condition.caseLabel.padding.right).w,
            x: 0,
            y: 0
        };
        const trueTextProps = { x: 0, y: 0 };
        const falseTextProps = { x: 0, y: 0 };

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

        trueProps.x = p3.x - (trueProps.width / 2);
        trueProps.y = p3.y + config.condition.caseLabel.margin.top;

        falseProps.x = p2.x + config.condition.caseLabel.margin.left;
        falseProps.y = p2.y - (falseProps.height / 2);

        trueTextProps.x = trueProps.x + config.condition.caseLabel.margin.left;
        trueTextProps.y = trueProps.y + (config.condition.caseLabel.margin.top * 2) - 1;

        falseTextProps.x = falseProps.x + config.condition.caseLabel.margin.left;
        falseTextProps.y = falseProps.y + (config.condition.caseLabel.margin.top * 2) - 1;

        return (
            <g className="condition">
                <text {...conditionProps} className="expression">
                    {DiagramUtils.getTextWidth(expression, 0, controlWidth).text}
                </text>
                <polyline
                    points={`${p1.x},${p1.y} ${p2.x},${p2.y} ${p3.x},${p3.y} ${p4.x},${p4.y} ${p1.x},${p1.y}`}
                />
                {(typeof label === "string") && <text {...labelProps} className="label">{label}</text>}
                {(typeof label === "object") && label}
                <rect {...trueProps} className="condition-case-background condition-case-background-true"></rect>
                <text {...trueTextProps} className="condition-case condition-case-true">{conditionLables.true}</text>
                <rect {...falseProps} className="condition-case-background condition-case-background-false"></rect>
                <text {...falseTextProps} className="condition-case condition-case-false">{conditionLables.false}</text>
            </g>);
    };
