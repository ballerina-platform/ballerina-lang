import classNames from "classnames";
import React, { Fragment, StatelessComponent } from "react";
import { getCodePoint } from "../../utils";
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
        <g className={classNames("ui", "button", "svg", "circle", "primary", { enabled }, className)}
            onClick={onClick}>
            <circle cx={position.x} cy={position.y} r={radius} />
            {icon && <text className="icon" x={position.x} y={position.y} >{iconText}</text>}
            {text && <text className="title" {...position} >{text}</text>}
        </g>
    </Fragment>;
};
