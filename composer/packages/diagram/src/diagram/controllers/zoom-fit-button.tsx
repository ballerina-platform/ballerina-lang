import React, { StatelessComponent } from "react";
import { Button, Icon } from "semantic-ui-react";
import { DiagramContext } from "../diagram-context";

export const ZoomFitButton: StatelessComponent<{}> = () => {
    return  (
        <DiagramContext.Consumer>
            {({ zoomFit, zoomLevel, hasSyntaxErrors }) => {
                return (
                    <Button
                        icon
                        onClick={zoomFit}
                        disabled={hasSyntaxErrors || (zoomLevel <= 1)}
                    >
                        <Icon className="fw fw-fit" title="Zoom Fit" />
                    </Button>
                );
            }}
        </DiagramContext.Consumer>
    );
};
