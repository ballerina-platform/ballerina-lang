import React, { StatelessComponent } from "react";
import { Button, Icon, Popup } from "semantic-ui-react";
import { DiagramContext } from "../diagram-context";

export const ZoomOutButton: StatelessComponent<{}> = () => {
    return  (
        <DiagramContext.Consumer>
            {({ zoomOut, zoomLevel, hasSyntaxErrors }) => {
                return (
                    <Popup
                        trigger={
                            <Button
                                icon
                                onClick={zoomOut}
                                disabled={hasSyntaxErrors || (zoomLevel <= 1)}>
                                <Icon className="fw fw-zoom-out" />
                            </Button>
                        }
                        content="Zoom Out"
                        size="mini"
                        inverted
                    />
                );
            }}
        </DiagramContext.Consumer>
    );
};
