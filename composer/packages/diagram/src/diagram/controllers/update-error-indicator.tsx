import React, { StatelessComponent } from "react";
import { Button, Icon, Label, Menu } from "semantic-ui-react";
import { DiagramContext } from "../diagram-context";

export const UpdateErrorIndicator: StatelessComponent<{}> = () => {
    return  (
        <DiagramContext.Consumer>
            {({ hasSyntaxErrors }) => {
                return (hasSyntaxErrors &&
                    <Menu.Item>
                        <Button.Group className="update-error-indicator" size="tiny">
                            <Label as="a">
                                <span title="please correct syntax errors.">Update Failed</span>
                                <Icon name="warning" />
                            </Label>
                        </Button.Group>
                    </Menu.Item>
                );
            }}
        </DiagramContext.Consumer>
    );
};
