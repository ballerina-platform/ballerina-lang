import { getCodePoint } from "@ballerina/font";
import cn from "classnames";
import React, { Fragment, StatelessComponent } from "react";
import { SimplePoint } from "../../view-model/simple-point";

export interface SVGCircleButtonProps {
    text?: string;
    icon?: string;
    enabled?: boolean;
    radius?: number;
    position: SimplePoint;
    className?: string;
    onClick?: () => void;
}

export const SVGCircleButton: StatelessComponent<SVGCircleButtonProps> =
    ({ text, icon, enabled = true, radius = 10, position, className, onClick }) => {
    return <Fragment>
        <g className={cn("svg-button", "circle", "noselect", { enabled }, className)} onClick={onClick} >
            <circle cx={position.x} cy={position.y} r={radius} />
            {icon && <text x={position.x} y={position.y} >{getCodePoint(icon)}</text>}
            {text && <text {...position} >{text}</text>}
        </g>
    </Fragment>;
};
