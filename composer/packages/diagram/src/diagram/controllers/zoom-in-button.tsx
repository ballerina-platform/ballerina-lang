import React, { StatelessComponent } from "react";
import { Button, Icon } from "semantic-ui-react";
import { DiagramContext } from "../diagram-context";

export const ZoomInButton: StatelessComponent<{}> = () => {
    return  (
        <DiagramContext.Consumer>
            {({ zoomIn, hasSyntaxErrors }) => {
                return (
                    <Button
                        icon
                        onClick={zoomIn}
                        disabled={hasSyntaxErrors}
                    >
                        <Icon className="fw fw-zoom-in" title="Zoom In" />
                    </Button>
                );
            }}
        </DiagramContext.Consumer>
    );
};
