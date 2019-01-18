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
import ReactMarkdown from "react-markdown";
import { Form, Icon, Input } from "semantic-ui-react";

export interface InlineEditProps {
    inlineEditString: string;
    classDefinition?: string;
    placeholderString?: string;
    isURL?: URL;
    isParagraph?: boolean;
    changeModel: any;
    changeAttribute: AttributeObject;
    isMarkdown?: boolean;
    characterLimit?: number;
    onInlineValueChange: (openApiJson: any) => void;
}

export interface URL {
    urlLink: string;
    urlString: string;
}

export interface AttributeObject {
    key: string;
    path?: string;
    changeValue: string;
}

export interface InlineEditState {
    stateText: string;
    initialTextValue: string;
    initialURLStringValue: string;
    isEditing: boolean;
    changed?: boolean;
    urlString?: string;
}

class InlineEdit extends React.Component<InlineEditProps, InlineEditState> {
    constructor(props: InlineEditProps) {
        super(props);

        let urlLink = "";
        let urlText = "";

        if (this.props.isURL) {
            urlLink = this.props.isURL.urlLink &&
                this.props.isURL.urlLink !== "" ? this.props.isURL.urlLink : "";
            urlText = this.props.isURL.urlString &&
                this.props.isURL.urlString !== "" ? this.props.isURL.urlString : "";
        }

        this.state = {
            initialTextValue: this.props.inlineEditString,
            initialURLStringValue: this.props.isURL ? urlText : "",
            isEditing: false,
            stateText: this.props.isURL ? urlLink : this.props.inlineEditString,
            urlString: this.props.isURL ? urlText : ""
        };

        this.handleDoneEditing = this.handleDoneEditing.bind(this);
        this.enableEditing = this.enableEditing.bind(this);
        this.handleOnTextChange = this.handleOnTextChange.bind(this);
        this.handleCancelEdit = this.handleCancelEdit.bind(this);
    }

    public componentWillReceiveProps(nextProps: InlineEditProps) {
        this.setState({
            stateText: nextProps.inlineEditString
        });
    }

    public enableEditing(e: React.MouseEvent<HTMLSpanElement>) {
        e.stopPropagation();
        this.setState({
            isEditing: true,
        });
    }

    public render() {
        const { stateText, isEditing, urlString } = this.state;
        const { isParagraph, isURL, classDefinition, placeholderString, isMarkdown } = this.props;

        if (isEditing) {
            if (isURL) {
                return (
                    <div className={`inline-editor editing  ${classDefinition}}`}>
                        <Form>
                            <Form.Group>
                                <Form.Input
                                    id="url-link"
                                    compact
                                    size="mini"
                                    placeholder={placeholderString}
                                    value={stateText}
                                    onChange={this.handleOnTextChange}
                                />
                                <Form.Input
                                    id="url-text"
                                    compact
                                    size="mini"
                                    placeholder="Add a meaningful link text"
                                    value={urlString}
                                    onChange={this.handleOnTextChange}
                                />
                                <Form.Button
                                    inverted
                                    color="black"
                                    size="mini"
                                    onClick={this.handleDoneEditing}
                                ><i className="fw fw-check"></i></Form.Button>
                                <Form.Button
                                    inverted
                                    color="black"
                                    size="mini"
                                    onClick={this.handleCancelEdit}
                                ><i className="fw fw-delete"></i></Form.Button>
                            </Form.Group>
                        </Form>
                    </div>
                );
            } else if (isParagraph) {
                return (
                    <div className={"inline-editor editing " + classDefinition}>
                        <textarea
                            autoFocus
                            placeholder={placeholderString}
                            onBlur={this.handleDoneEditing}
                            onChange={this.handleOnTextChange}
                        >
                            {stateText}
                        </textarea>
                    </div>
                );
            } else {
                return (
                    <div className={"inline-editor editing " + classDefinition}>
                        <Form>
                            <Input
                                transparent
                                autoFocus
                                placeholder={placeholderString}
                                value={stateText}
                                onBlur={this.handleDoneEditing}
                                onChange={this.handleOnTextChange}
                                onClick={(e: any) => {e.stopPropagation(); }}
                            />
                        </Form>
                    </div>
                );
            }
        } else {
            if (isURL && urlString && stateText) {
                return (
                    <div className={"inline-editor url " + classDefinition}>
                        <span onClick={this.enableEditing}>{urlString === "" ? stateText : urlString}</span>
                        <a className="activate-edit" href={stateText === "" ? "#" : stateText} target="_blank">
                            <Icon className="fw fw-uri" />
                        </a>
                    </div>
                );
            } else if (stateText && stateText !== "") {
                return (
                    <div className={"inline-editor with-text " + classDefinition} onClick={this.enableEditing}>
                        {isMarkdown ? <ReactMarkdown source={stateText} /> :
                            <span>
                                {stateText}
                            </span>
                        }
                    </div>
                );
            } else {
                return (
                    <div className={"inline-editor no-text " + classDefinition} onClick={this.enableEditing}>
                        <span>
                            {placeholderString}
                        </span>
                    </div>
                );
            }
        }
    }

    private handleOnTextChange(e: React.ChangeEvent<HTMLInputElement> | React.ChangeEvent<HTMLTextAreaElement>) {
        const { isURL, isParagraph, characterLimit } = this.props;

        if (isURL) {
            switch (e.target.id) {
                case "url-link":
                    this.setState({
                        stateText: e.target.value
                    });
                    break;
                case "url-text":
                    this.setState({
                        urlString: e.target.value
                    });
                    break;
            }
        } else {
            if (characterLimit) {
                if (!isParagraph && e.target.value.length <= characterLimit) {
                    this.setState({
                        stateText: e.target.value
                    });
                }
            } else {
                this.setState({
                    stateText: e.target.value
                });
            }
        }

    }

    private handleDoneEditing() {
        this.setState({
            isEditing: false
        }, () => {
            const { changeModel, changeAttribute } = this.props;
            this.handleChangeEvent(changeModel, changeAttribute);
        });
    }

    private handleChangeEvent(model: any, attribute: AttributeObject) {
        const { stateText, urlString } = this.state;

        switch (attribute.key) {
            case "info.description":
                model.info.description = stateText;
                break;
            case "info.termsOfService":
                model.info.termsOfService = stateText;
                break;
            case "info.license":
                model.info.license = {
                    name: urlString,
                    url: stateText
                };
                break;
            case "info.contact":
                model.info.contact = {
                    name: urlString,
                    url: stateText
                };
                break;
            case "resource.name":
                if (stateText !== "" && model.paths) {
                    model.paths[stateText] = model.paths[attribute.changeValue];
                    delete model.paths[attribute.changeValue];
                }
                break;
            case "operation.description":
                if (stateText !== "" && model.paths && attribute.path) {
                    model.paths[attribute.path][attribute.changeValue].description = stateText;
                }
                break;
            case "operation.summary":
                if (stateText !== "" && model.paths && attribute.path) {
                    model.paths[attribute.path][attribute.changeValue].summary = stateText;
                }
                break;
            default:
                break;
        }

        this.props.onInlineValueChange(model);
    }

    private handleCancelEdit() {
        const { isURL } = this.props;

        if (isURL) {
            this.setState({
                isEditing: false,
                stateText: this.state.initialTextValue,
                urlString: this.state.initialURLStringValue
            });
        }
    }

}

export default InlineEdit;
