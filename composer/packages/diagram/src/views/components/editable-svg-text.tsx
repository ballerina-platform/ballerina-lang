import classNames from "classnames";
import React from "react";
import { DiagramContext, IDiagramContext } from "../../diagram/diagram-context";
import { SimpleBBox } from "../../view-model/index";

export interface EditableSVGTextProps {
    value: string;
    onChange?: (newValue: string) => void;
    bBox: SimpleBBox;
    className?: string;
}

export interface EditableSVGTextState {
    textEditingEnabled: boolean;
    currentValue: string;
}

export class EditableSVGText extends React.Component<EditableSVGTextProps, EditableSVGTextState> {

    public static contextType = DiagramContext;

    public state = {
        currentValue: this.props.value,
        textEditingEnabled: false
    };

    private inputRef = React.createRef<HTMLInputElement>();

    public componentDidUpdate(_: EditableSVGTextProps, prevState: EditableSVGTextState) {
        if (!prevState.textEditingEnabled && this.inputRef.current) {
            this.inputRef.current.select();
        }
    }

    public componentWillReceiveProps(newProps: EditableSVGTextProps) {
        this.setState({
            currentValue: newProps.value
        });
    }

    public render(): React.ReactNode {
        const { bBox, className, onChange } = this.props;
        const { x, y, w, h } = bBox;
        const { textEditingEnabled, currentValue } = this.state;
        const { editingEnabled, hasSyntaxErrors } = this.context as IDiagramContext;
        const foreignObjectBBox = {
            height: h,
            width: w,
            x,
            y: y + (h / 4)
        };
        return <g className="editable-text">
            {(hasSyntaxErrors || !textEditingEnabled) &&
                <text
                    x={x}
                    y={y + (h / 2)}
                    className={classNames(className, { editable: editingEnabled })}
                    onClick={() => {
                        if (editingEnabled && !textEditingEnabled) {
                            this.setState({
                                textEditingEnabled: true
                            });
                        }
                    }}
                >
                    {currentValue}
                </text>
            }
            {(!hasSyntaxErrors && textEditingEnabled) &&
                <foreignObject
                    {...foreignObjectBBox}
                    className={className}
                >
                    <input
                        value={currentValue}
                        ref={this.inputRef}
                        onBlur={() => {
                            this.setState({
                                textEditingEnabled: false
                            });
                            if (onChange) {
                                onChange(this.state.currentValue);
                            }
                        }}
                        onChange={(evt) => {
                            this.setState({
                                currentValue: evt.target.value
                            });
                        }}
                    />
                </foreignObject>
            }
        </g>;
    }
}
