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
    ({ text, icon, enabled = true, radius = 8, position, className, onClick }) => {
    const iconText = (icon && getCodePoint(icon)) ? getCodePoint(icon) : icon;
    return <Fragment>
        <g className={cn("svg-button", "circle", "noselect", { enabled }, className)} onClick={onClick} >
            <circle cx={position.x} cy={position.y} r={radius} />
            {icon && <text className="btn-icon" x={position.x} y={position.y} >{iconText}</text>}
            {text && <text className="btn-title" {...position} >{text}</text>}
        </g>
    </Fragment>;
};
