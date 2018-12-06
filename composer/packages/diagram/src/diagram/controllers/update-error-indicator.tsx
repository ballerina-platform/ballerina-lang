import React, { StatelessComponent } from "react";
import { Icon, Label, Menu } from "semantic-ui-react";
import { DiagramContext } from "../diagram-context";

export const UpdateErrorIndicator: StatelessComponent<{}> = () => {
    return  (
        <DiagramContext.Consumer>
            {({ hasSyntaxErrors }) => {
                return (hasSyntaxErrors &&
                    <Menu.Item>
                        <Label as="a" basic color="red">
                            <Icon name="warning" />
                            <span title="Please correct syntax errors.">Update Failed</span>
                        </Label>
                    </Menu.Item>
                );
            }}
        </DiagramContext.Consumer>
    );
};
