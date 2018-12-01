import React, { StatelessComponent } from "react";
import { DiagramContext } from "../../diagram/diagram-context";
import { SimplePoint } from "../../view-model/simple-point";

export interface DropDownMenuTriggerProps {
    position: SimplePoint;
    className?: string;
}

export const DropDownMenuTrigger: StatelessComponent<DropDownMenuTriggerProps> =
    ({ position, className, children }) => {
    const { x, y } = position;
    const foreignObjectBBox = {
        height: 10,
        width: 10,
        x,
        y
    };
    return <DiagramContext.Consumer>
        {({ editingEnabled }) => {
            return editingEnabled && <foreignObject
                    {...foreignObjectBBox}
                    className={"dropdown-trigger"}
                >
                    <div
                        style={{ position: "fixed" }}
                    >
                        {children}
                    </div>
                </foreignObject>;
        }}
    </DiagramContext.Consumer>;
};
