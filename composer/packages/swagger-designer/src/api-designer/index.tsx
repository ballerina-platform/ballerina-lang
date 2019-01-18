import * as Swagger from "openapi3-ts";
import * as React from "react";
import { Button, Icon } from "semantic-ui-react";

import OpenApiInfo from "./components/info/info";
import AddOpenApiPath from "./paths/add-path";
import OpenApiPathList from "./paths/paths-list";

export interface OpenApiProps {
    openApiJson: Swagger.OpenAPIObject;
}

export interface OpenApiState {
    showOpenApiAddPath: boolean;
}

class OpenApiVisualizer extends React.Component<OpenApiProps, OpenApiState> {
    constructor(props: OpenApiProps) {
        super(props);

        this.state = {
            showOpenApiAddPath: false
        };

        this.handleShowOpenApiAddPath = this.handleShowOpenApiAddPath.bind(this);
    }

    public render() {
        const { openApiJson } = this.props;
        const { showOpenApiAddPath } = this.state;
        return (
            <React.Fragment>
                <OpenApiInfo info={openApiJson.info} />
                <Button size="mini" primary icon labelPosition="left" onClick={this.handleShowOpenApiAddPath}>
                    <Icon name="plus" />
                    Add Resource
                </Button>
                {showOpenApiAddPath &&
                    <AddOpenApiPath path={"asdas"} />
                }
                <OpenApiPathList paths={openApiJson.paths} />
            </React.Fragment>
        );
    }

    private handleShowOpenApiAddPath() {
        this.setState({
            showOpenApiAddPath: !this.state.showOpenApiAddPath
        });
    }
}

export default OpenApiVisualizer;
