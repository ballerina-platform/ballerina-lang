import React, { StatelessComponent } from "react";
import { Icon, Label, Menu, Popup } from "semantic-ui-react";
import { DiagramContext } from "../diagram-context";

export const UpdateErrorIndicator: StatelessComponent<{}> = () => {
    return  (
        <DiagramContext.Consumer>
            {({ hasSyntaxErrors }) => {
                return (hasSyntaxErrors &&
                    <Menu.Item>
                        <Popup
                            trigger={
                                <Label as="a" basic color="red">
                                    <Icon className="fw fw-warning" />
                                    <span>Update Failed</span>
                                </Label>
                            }
                            content="Please correct syntax errors"
                            size="mini"
                            inverted
                        />
                    </Menu.Item>
                );
            }}
        </DiagramContext.Consumer>
    );
};
