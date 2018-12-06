import React, { StatelessComponent } from "react";
import { DiagramContext } from "../../diagram/diagram-context";
import { SimplePoint } from "../../view-model/simple-point";
import { SVGCircleButton } from "./svg-circle-button";
import { SVGOverlayComponent } from "./svg-overlay-component";

export interface SVGDropDownMenuTriggerProps {
    position: SimplePoint;
    className?: string;
    onClick?: () => void;
    radius?: number;
    icon?: string;
    onMouseOut?: () => void;
    onMouseOver?: () => void;
}

export const SVGDropDownMenuTrigger: StatelessComponent<SVGDropDownMenuTriggerProps> =
    ({ position, onClick, children, radius = 10, icon = "add", className, onMouseOut, onMouseOver }) => {
    const btnProps = {
        icon, onClick, position, radius
    };
    return <DiagramContext.Consumer>
        {({ editingEnabled, hasSyntaxErrors }) => {
            return (editingEnabled && !hasSyntaxErrors) &&
                <SVGOverlayComponent>
                    <g className={className} onMouseOut={onMouseOut} onMouseOver={onMouseOver} >
                        {children}
                        <g className="trigger">
                            <SVGCircleButton {...btnProps} />
                        </g>
                   </g>
                </SVGOverlayComponent>;
        }}
    </DiagramContext.Consumer>;
};
