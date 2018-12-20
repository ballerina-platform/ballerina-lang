
import { Block as BlockNode } from "@ballerina/ast-model";
import * as React from "react";
import { DiagramUtils } from "../../diagram/diagram-utils";
import { BlockViewState } from "../../view-model/block";
import { BlockDropdown } from "./block-dropdown";

export class Block extends React.Component<{ model: BlockNode }, { isHovered: boolean }> {

    public state = {
        isHovered: false
    };

    public render() {
        const { model }  = this.props;
        const { isHovered } = this.state;
        const statements: React.ReactNode[] = [];
        const viewState: BlockViewState = model.viewState;
        const triggerPosition = viewState.menuTrigger;
        const { x, y, w: width, h: height } = viewState.hoverRect;
        const hoverRectProps = {
            height,
            width,
            x,
            y
        };
        statements.push(DiagramUtils.getComponents(model.statements));
        const dropDownProps = { model, isHovered, triggerPosition };
        return (
            <g className="worker-block-container">
                <rect
                    className="hover-rect"
                    {...hoverRectProps}
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
                    visibility="hidden"
                />
                {statements}
                {<BlockDropdown {...dropDownProps} />}
            </g>);
    }
}
