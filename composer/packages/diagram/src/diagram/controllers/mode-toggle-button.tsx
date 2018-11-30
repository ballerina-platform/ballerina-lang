import React, { StatelessComponent } from "react";
import { Button, Icon, Menu } from "semantic-ui-react";
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
                    <Menu.Item>
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
                                <Icon className={`fw ${icon}`} title={toolTip} />
                            </Button>
                            {children}
                        </Button.Group>
                    </Menu.Item>
                );
            }}
        </DiagramContext.Consumer>
    );
};
