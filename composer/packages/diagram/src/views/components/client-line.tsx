import classNames from "classnames";
import * as React from "react";
import { DiagramConfig } from "../../config/default";
import { DiagramUtils } from "../../diagram/diagram-utils";
import { SimpleBBox } from "../../view-model/index";

const config: DiagramConfig = DiagramUtils.getConfig();

export const ClientLine: React.StatelessComponent<{
    model: SimpleBBox,
    activeRange?: [number, number]
}> = ({
    model,
    activeRange
}) => {

        const clientLine = { x1: 0 , y1: 0 , x2: 0, y2: 0 };

        clientLine.y1 = model.y + config.clientLine.header.height - 40;
        clientLine.y2 = model.y + model.h;

        return (
            <g className={classNames("client-line")}>
                <line className="client-line" {...clientLine} strokeDasharray={(activeRange ? 4 : 0)} />
                {activeRange && <line
                    x1={clientLine.x1} x2={clientLine.x2}
                    y1={activeRange[0]}
                    y2={activeRange[1]}
                /> }
            </g>);
    };
