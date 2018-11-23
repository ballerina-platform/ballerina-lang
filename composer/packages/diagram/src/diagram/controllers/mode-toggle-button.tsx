import React, { StatelessComponent } from "react";
import { Button } from "semantic-ui-react";
import { DiagramContext, DiagramMode } from "../diagram-context";

export const ModeToggleButton: StatelessComponent<{}> = (
        { children }
    ) => {
    return  (
        <DiagramContext.Consumer>
            {(diagContext) => {
                const currentMode = diagContext.mode;
                const icon = currentMode === DiagramMode.ACTION
                    ? "fw-expand"
                    : "fw-collapse";
                const toolTip = currentMode === DiagramMode.ACTION
                    ? "Expand Code"
                    : "Collapse Code";
                return (
                    <Button.Group>
                        <Button
                            icon
                            onClick={() => {
                                if (currentMode === DiagramMode.ACTION) {
                                    diagContext.changeMode(DiagramMode.DEFAULT);
                                } else {
                                    diagContext.changeMode(DiagramMode.ACTION);
                                }
                            }}
                        >
                            <i
                                className={`fw ${icon}`}
                                title={toolTip}
                            />
                        </Button>
                        {children}
                    </Button.Group>
                );
            }}
        </DiagramContext.Consumer>
    );
};
