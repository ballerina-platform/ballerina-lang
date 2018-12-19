import * as Swagger from "openapi3-ts";
import * as React from "react";

import { Header, Segment } from "semantic-ui-react";

import InlineEdit from "../utils/inline-edit";
import OpenApiContact from "./contact";
import OpenApiLicense from "./license";

export interface OpenApiInfoProps {
    info: Swagger.InfoObject;
}

class OpenApiInfo extends React.Component<OpenApiInfoProps, any> {
    constructor(props: OpenApiInfoProps) {
        super(props);
    }

    public render() {
        const { info } = this.props;
        return(
            <Segment>
                <Header as="h1">
                    {info.title}
                    <Header.Subheader>{info.version}</Header.Subheader>
                </Header>
                <InlineEdit text={info.description} placeholderText="Please include an appropriate description."/>
                <a href={info.termsOfService} target="_blank">Terms of Service</a>
                <OpenApiContact contact={info.contact} />
                <OpenApiLicense license={info.license} />
            </Segment>
        );
    }
}

export default OpenApiInfo;
