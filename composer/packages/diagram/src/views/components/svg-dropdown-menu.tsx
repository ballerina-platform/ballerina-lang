import React from "react";
import { SimplePoint } from "../../view-model/simple-point";
import { SVGDropDownMenuTrigger } from "./svg-dropdown-menu-trigger";

export interface SVGDropDownMenuProps {
    triggerPosition: SimplePoint;
    className?: string;
    items: SVGDropDownItem[];
    triggerIcon: string;
}

export interface SVGDropDownMenuState {
    active: boolean;
}

export interface SVGDropDownItem {
    title: string;
    onSelect?: () => void;
    icon?: string;
}

export class SVGDropDownMenu extends React.Component<SVGDropDownMenuProps, SVGDropDownMenuState> {

    public state = {
        active: false
    };

    public render() {
        const { triggerPosition } = this.props;
        return <SVGDropDownMenuTrigger position={triggerPosition} onClick={() => {
            this.setState({
                active: !this.state.active
            });
        }}>
                {this.state.active &&
                    // TODO
                    // draw menu from svg elements here
                    <rect
                        x={triggerPosition.x + 5}
                        y={triggerPosition.y + 5}
                        width={100}
                        height={100}
                    />
                }
        </SVGDropDownMenuTrigger>;
    }
}
