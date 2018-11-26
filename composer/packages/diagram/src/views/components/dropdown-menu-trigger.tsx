import React, { StatelessComponent } from "react";
import { DiagramContext } from "../../diagram/diagram-context";
import { SimpleBBox } from "../../view-model/index";

export interface DropDownMenuTriggerProps {
    bBox: SimpleBBox;
    className?: string;
}

export const DropDownMenuTrigger: StatelessComponent<DropDownMenuTriggerProps> =
    ({ bBox, className, children }) => {
    const { x, y, w, h } = bBox;
    const foreignObjectBBox = {
        height: h,
        width: w,
        x,
        y
    };
    return <DiagramContext.Consumer>
        {({ editingEnabled }) => {
            return editingEnabled && <foreignObject
                    {...foreignObjectBBox}
                    className={"dropdown-trigger"}
                >
                    {children}
                </foreignObject>;
        }}
    </DiagramContext.Consumer>;
};
