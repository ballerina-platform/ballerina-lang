
// import { getCodePoint } from "@ballerina/font";
import * as React from "react";
import { DiagramConfig } from "../../config/default";
import { DiagramUtils } from "../../diagram/diagram-utils";
import { SimpleBBox } from "../../view-model/index";

const config: DiagramConfig = DiagramUtils.getConfig();

export const LifeLine: React.StatelessComponent<{
    model: SimpleBBox,
    title: string,
    icon: string
}> = ({
    model,
    title,
    children,
    icon
}) => {

        const topLabel = { x: 0, y: 0 };
        const bottomLabel = { x: 0, y: 0 };
        const topBox = { x: 0, y: 0 , width: 0, height: 0};
        const bottomBox = { x: 0, y: 0 , width: 0, height: 0};
        const lifeLine = { x1: 0 , y1: 0 , x2: 0, y2: 0 };

        // Position the labels
        topLabel.x = bottomLabel.x = model.x + (model.w / 2);
        topLabel.y = model.y + (config.lifeLine.header.height / 2);
        bottomLabel.y = model.y + model.h - (config.lifeLine.footer.height / 2);
        // Position the Boxes
        topBox.x = bottomBox.x = model.x;
        topBox.y = model.y;
        bottomBox.y = model.y + model.h - config.lifeLine.footer.height;
        topBox.height = config.lifeLine.header.height;
        bottomBox.height = config.lifeLine.footer.height;
        topBox.width = model.w;
        bottomBox.width = model.w;
        // Position Line
        lifeLine.x1 = lifeLine.x2 = model.x + (model.w / 2);
        lifeLine.y1 = model.y + config.lifeLine.header.height;
        lifeLine.y2 = model.y + model.h - config.lifeLine.footer.height;

        return (
            <g className="life-line">
                <line {...lifeLine} />
                <rect {...topBox} />
                <rect {...bottomBox} />
                <text {...topLabel}>{title}</text>
                <text {...bottomLabel}>{title}</text>
            </g>);
    };
