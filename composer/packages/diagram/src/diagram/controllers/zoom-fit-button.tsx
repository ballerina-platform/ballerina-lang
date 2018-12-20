import React, { StatelessComponent } from "react";
import { Button, Icon, Popup } from "semantic-ui-react";
import { DiagramContext } from "../diagram-context";

export const ZoomFitButton: StatelessComponent<{}> = () => {
    return  (
        <DiagramContext.Consumer>
            {({ zoomFit, zoomLevel, hasSyntaxErrors }) => {
                return (
                    <Popup
                        trigger={
                            <Button
                                icon
                                onClick={zoomFit}
                                disabled={hasSyntaxErrors || (zoomLevel <= 1)}
                            >
                                <Icon className="fw fw-fit" />
                            </Button>
                        }
                        content="Zoom Fit"
                        size="mini"
                        inverted
                    />
                );
            }}
        </DiagramContext.Consumer>
    );
};
