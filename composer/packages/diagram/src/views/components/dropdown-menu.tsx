import React, { StatelessComponent } from "react";
import { Dropdown } from "semantic-ui-react";
import { DropDownMenuTrigger, DropDownMenuTriggerProps } from "./dropdown-menu-trigger";

export interface DropDownMenuProps extends DropDownMenuTriggerProps {
    items: DropDownItem[];
    triggerIcon: string;
}

export interface DropDownItem {
    title: string;
    onSelect: () => void;
    icon?: string;
}

export const DropDownMenu: StatelessComponent<DropDownMenuProps> =
    ({ bBox, className, items, triggerIcon }) => {
    return <DropDownMenuTrigger bBox={bBox}>
            <Dropdown icon={triggerIcon} style={{ position: "fixed" }} className={className} >
                <Dropdown.Menu>
                    {items.map((item) => (
                         <Dropdown.Item
                            text={item.title}
                            onClick={item.onSelect}
                            icon={item.icon ? item.icon : undefined}
                            key={item.title}
                        />
                    ))}
                </Dropdown.Menu>
            </Dropdown>
    </DropDownMenuTrigger>;
};
