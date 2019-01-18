import * as Swagger from "openapi3-ts";
import * as React from "react";
import ReactMarkdown from "react-markdown";
import { Form, Icon } from "semantic-ui-react";

export interface InlineEditProps {
    text?: string | Swagger.LicenseObject | Swagger.ContactObject | URL ;
    isMarkdown?: boolean;
    placeholderText?: string;
    isParagraph?: boolean;
}

export interface URL {
    link: string;
    urlText?: string;
}

export interface InlineEditState {
    text: string | Swagger.LicenseObject | Swagger.ContactObject | URL;
    isEditing: boolean;
}

class InlineEdit extends React.Component<InlineEditProps, InlineEditState> {
    constructor(props: InlineEditProps) {
        super(props);

        this.state = {
            isEditing: false,
            text: this.props.text ? this.props.text : ""
        };

        this.enableEditing = this.enableEditing.bind(this);
        this.cancelEditing = this.cancelEditing.bind(this);
    }

    public render() {
        const { text, isEditing } = this.state;
        const { isMarkdown, isParagraph, placeholderText } = this.props;

        if (this.isContactObj(text)) {
            if (isEditing) {
                return (
                    <div></div>
                );
            } else {
                return (
                    <div></div>
                );
            }
        }

        if (this.isLicenseObj(text)) {
            if (isEditing) {
                return (
                    <div className="inline-editor editing">
                        <Form>
                            <Form.Group>
                                <Form.Input id="url-link" size="mini" />
                                <Form.Input
                                    id="url-text"
                                    size="mini"
                                    placeholder="Add a meaningful link text"
                                />
                                <Form.Button inverted color="black" icon="check" size="mini" />
                                <Form.Button inverted color="black" icon="close" size="mini"
                                    onClick={this.cancelEditing} />
                            </Form.Group>
                        </Form>
                    </div>
                );
            } else {
                return (
                    <div className="inline-editor url">
                        <span onClick={this.enableEditing}>{text.name === "" ? "" : text.name}</span>
                        <a className="activate-edit" href={text.url === "" ? "#" : text.url} target="_blank">
                            <Icon name="linkify" />
                        </a>
                    </div>
                );
            }
        }

        if (this.isURLObj(text)) {
            if (isEditing) {
                return (
                    <div className="inline-editor editing">
                        <Form>
                            <Form.Group>
                                <Form.Input id="url-link" size="mini" />
                                <Form.Button inverted color="black" icon="check" size="mini" />
                                <Form.Button inverted color="black" icon="close" size="mini"
                                    onClick={this.cancelEditing}/>
                            </Form.Group>
                        </Form>
                    </div>
                );
            }

            if (text.urlText && text.urlText === "") {
                return (
                    <div className="inline-editor url">
                        <span onClick={this.enableEditing}>{text.link}</span>
                        <a className="activate-edit" href={text.link} target="_blank">
                            <Icon name="linkify" />
                        </a>
                    </div>
                );
            } else {
                return (
                    <div className="inline-editor url">
                        <span onClick={this.enableEditing}>{text.urlText}</span>
                        <a className="activate-edit" href={text.link === "" ? "#" : text.link} target="_blank">
                            <Icon name="linkify" />
                        </a>
                    </div>
                );
            }
        }

        if (typeof text === "string") {

            if (text === "") {
                return (
                    <div>
                        <span>{placeholderText}</span>
                    </div>
                );
            }

            if (isParagraph && isMarkdown) {
                if (isEditing) {
                    return(
                        <div className="inline-editor editing">
                            <textarea
                                autoFocus
                                placeholder={placeholderText}
                                onBlur={this.cancelEditing}
                            >
                                {text}
                            </textarea>
                        </div>
                    );
                }
                return (
                    <div className="inline-editor markdown" onClick={this.enableEditing}>
                        <ReactMarkdown source={text} />
                    </div>
                );
            } else if (isParagraph) {
                return (
                    <div className="inline-editor markdown">
                        <p>{text}</p>
                    </div>
                );
            } else {
                return (
                    <div>
                        <span>{text}</span>
                    </div>
                );
            }
        }

        return(
            <div></div>
        );
    }

    private enableEditing(e: React.MouseEvent<HTMLSpanElement>) {
        e.stopPropagation();
        this.setState({
            isEditing: true,
        });
    }

    private cancelEditing() {
        this.setState({
            isEditing: false
        });
    }

    private isContactObj(arg: any): arg is Swagger.ContactObject {
        return arg && arg.name && arg.url && arg.email;
    }

    private isLicenseObj(arg: any): arg is Swagger.LicenseObject {
        return arg && arg.name && arg.url;
    }

    private isURLObj(arg: any): arg is URL {
        return arg && arg.link && arg.urlText;
    }
}

export default InlineEdit;
