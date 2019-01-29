import React, { StatelessComponent } from "react";
import { Button, Menu } from "semantic-ui-react";
import { ZoomFitButton } from "./zoom-fit-button";
import { ZoomInButton } from "./zoom-in-button";
import { ZoomOutButton } from "./zoom-out-button";

export const ZoomControllers: StatelessComponent<{}> = () => {
    return (
        <Menu.Item>
            <Button.Group className="zoom-controllers" size="tiny">
                <ZoomInButton />
                <ZoomOutButton />
                <ZoomFitButton />
            </Button.Group>
        </Menu.Item>
    );
};
