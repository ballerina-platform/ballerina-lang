import React, { StatelessComponent } from "react";
import { Button, Icon, Popup } from "semantic-ui-react";
import { DiagramContext } from "../diagram-context";

export const ZoomInButton: StatelessComponent<{}> = () => {
    return  (
        <DiagramContext.Consumer>
            {({ zoomIn, hasSyntaxErrors }) => {
                return (
                    <Popup
                        trigger={
                            <Button
                                icon
                                onClick={zoomIn}
                                disabled={hasSyntaxErrors}
                            >
                                <Icon className="fw fw-zoom-in" />
                            </Button>
                        }
                        content="Zoom In"
                        size="mini"
                        inverted
                    />
                );
            }}
        </DiagramContext.Consumer>
    );
};
