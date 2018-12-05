import { Block } from "@ballerina/ast-model";
import cn from "classnames";
import React from "react";
import { DiagramContext, IDiagramContext } from "../../diagram/diagram-context";
import { SimplePoint } from "../../view-model/simple-point";
import { SVGDropDownMenu } from "./svg-dropdown-menu";

export interface BlockDropdownProps {
    active: boolean;
    triggerPosition: SimplePoint;
    model: Block;
}

export class BlockDropdown extends React.Component<BlockDropdownProps, { isHovered: boolean }> {

    public static contextType = DiagramContext;

    public state = {
        isHovered: this.props.active
    };

    public componentWillReceiveProps(nextProps: BlockDropdownProps) {
        this.setState({
            isHovered: nextProps.active
        });
    }

    public render() {
        const { triggerPosition } = this.props;
        const { isHovered } = this.state;
        const context = this.context as IDiagramContext;
        return <SVGDropDownMenu
                    className={cn("block-dropdown", { active: isHovered })}
                    triggerPosition={triggerPosition}
                    triggerIcon="add"
                    items={[
                        {
                            icon: "if-else",
                            name: "If",
                            onClick: () => {
                                // tslint:disable-next-line:no-console
                                console.log(context.ast);
                            }
                        },
                        {
                            icon: "while",
                            name: "While",
                            onClick: () => {
                                // todo
                            }
                        },
                        {
                            icon: "foreach",
                            name: "For-Each",
                            onClick: () => {
                                // todo
                            }
                        },
                        {
                            icon: "action",
                            name: "Action",
                            onClick: () => {
                                // todo
                            }
                        }
                    ]}
                    onMouseOver={() => {
                        this.setState({
                            isHovered: true
                        });
                    }}
                    onMouseOut={() => {
                        this.setState({
                            isHovered: false
                        });
                    }}
                />;
    }
}
