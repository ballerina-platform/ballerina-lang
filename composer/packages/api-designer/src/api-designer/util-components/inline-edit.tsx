/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

import * as React from "react";
import { Form, Icon, Input } from "semantic-ui-react";

export interface InlineEditProps {
    text: string;
    customClass?: string;
    activeStateClass?: string;
    isEditable: boolean;
    placeholderText?: string;
    isTextArea?: boolean;
    isUrl?: boolean;
    urlLink?: string;
    model: any;
    attribute: string;
}

export interface InlineEditState {
    stateText: string;
    isEditing: boolean;
    elementType: string;
    urlLinkState?: string;
    changed?: boolean;
}

class InlineEdit extends React.Component<InlineEditProps, InlineEditState> {
    constructor(props: InlineEditProps) {
        super(props);

        this.state = {
            elementType: "input",
            isEditing: false,
            stateText: this.props.text,
            urlLinkState: this.props.urlLink,
        };

        this.enableEditing = this.enableEditing.bind(this);
        this.handleFocusOut = this.handleFocusOut.bind(this);
        this.handleTextChange = this.handleTextChange.bind(this);
    }

    public componentWillReceiveProps(nextProps: InlineEditProps) {
        this.setState({
            stateText: nextProps.text,
            urlLinkState: nextProps.urlLink,
        });
    }

    public enableEditing(e: React.MouseEvent<HTMLSpanElement>) {
        e.stopPropagation();
        this.setState({
            isEditing: true,
        });
    }

    public handleFocusOut() {
        this.setState({
            isEditing: false,
            stateText: this.state.stateText
        });
    }

    public handleTextChange(e: React.ChangeEvent<HTMLInputElement>) {
        this.setState({
            stateText: e.target.value
        }, () => {
            const { model, attribute } = this.props;
            this.handleChangeEvent(model, attribute);
        });
    }

    public handleChangeEvent(model: any, attribute: string) {
        this.setState({
            changed: false
        });
    }

    public validateURL(url: string): boolean {
        return /^(ftp|http|https):\/\/[^ "]+$/.test(url);
    }

    public render() {
        const { isEditable, placeholderText, customClass, isTextArea, isUrl, urlLink } = this.props;
        const { isEditing, stateText, urlLinkState } = this.state;

        if (!isEditable) {
            if (isUrl && urlLink && this.validateURL(urlLink)) {
                return <a href={urlLink} target="_blank">{stateText}</a>;
            } else {
                return <span className="inline-editor disabled">{stateText}</span>;
            }
        } else if (!isEditing && stateText !== undefined && stateText !== "") {
            if (isUrl && urlLink && this.validateURL(urlLink)) {
                if (stateText !== "") {
                    return (
                        <div className={"inline-editor url " + customClass}>
                            <span onClick={this.enableEditing}>{stateText}</span>
                            <a className="activate-edit" href={urlLink} target="_blank">
                                <Icon name="linkify" />
                            </a>
                        </div>
                    );
                } else {
                    return (
                        <div className={"inline-editor url " + customClass}>
                            <span onClick={this.enableEditing}>{urlLink}</span>
                            <a className="activate-edit" href={urlLink} target="_blank">
                                <Icon name="linkify" />
                            </a>
                        </div>
                    );
                }
            } else {
                return (
                    <div className={"inline-editor non-editing " + customClass} onClick={this.enableEditing}>
                        <span>
                            {stateText}
                        </span>
                    </div>
                );
            }
        } else if (!isEditing && (stateText === undefined || stateText === "")) {
            if (isUrl && urlLink && this.validateURL(urlLink)) {
                if (stateText !== "") {
                    return (
                        <div className={"inline-editor url " + customClass}>
                            <span onClick={this.enableEditing}>{stateText}</span>
                            <a className="activate-edit" href={urlLink} target="_blank">
                                <Icon name="linkify" />
                            </a>
                        </div>
                    );
                } else {
                    return (
                        <div className={"inline-editor url " + customClass}>
                            <span onClick={this.enableEditing}>{urlLink}</span>
                            <a className="activate-edit" href={urlLink} target="_blank">
                                <Icon name="linkify" />
                            </a>
                        </div>
                    );
                }
            } else {
                return (
                    <div className={"inline-editor no-text " + customClass} onClick={this.enableEditing}>
                        <span>
                            {placeholderText}
                        </span>
                    </div>
                );
            }
        } else {
            if (isTextArea) {
                return (
                    <div className={"inline-editor editing " + customClass}>
                        <Form >
                            <Form.TextArea
                                autoFocus
                                onBlur={this.handleFocusOut}
                                placeholder={placeholderText}
                            >{stateText}</Form.TextArea>
                        </Form>
                    </div>
                );
            } else if (isUrl) {
                return (
                    <div className={"inline-editor editing " + customClass}>
                        <Form>
                            <Form.Group widths="5" inline>
                                <Form.Input
                                    transparent
                                    fluid
                                    placeholder={placeholderText}
                                    onBlur={this.handleFocusOut}
                                    value={urlLinkState}
                                />
                                <Form.Input
                                    transparent
                                    fluid
                                    placeholder="Write a link name"
                                    onBlur={this.handleFocusOut}
                                    value={stateText}
                                />
                                <Form.Button width={1} inverted color="black" icon="check" />
                                <Form.Button
                                    width={1}
                                    inverted
                                    color="black"
                                    icon="close"
                                    onClick={this.handleFocusOut}/>
                            </Form.Group>
                        </Form>
                    </div>
                );
            } else {
                return (
                    <div className={"inline-editor editing " + customClass}>
                        <Form>
                            <Input
                                transparent
                                autoFocus
                                placeholder={placeholderText}
                                value={stateText}
                                onBlur={this.handleFocusOut}
                                onChange={this.handleTextChange}
                                onClick={(e: any) => {e.stopPropagation(); }}
                            />
                        </Form>
                    </div>
                );
            }
        }
    }
}

export default InlineEdit;
