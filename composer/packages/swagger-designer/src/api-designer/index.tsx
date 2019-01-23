import * as Swagger from "openapi3-ts";
import * as React from "react";
import { Button, Icon } from "semantic-ui-react";

import { OpenApiContext, OpenApiContextProvider } from "./components/context/open-api-context";

import { EVENTS } from "./components/utils/constants";

import OpenApiInfo from "./components/info/info";
import AddOpenApiPath from "./paths/add-path";
import OpenApiPathList from "./paths/paths-list";

export interface OpenApiProps {
    openApiJson: Swagger.OpenAPIObject;
    onDidAddResource?: (resourse: Swagger.PathItemObject | string, swagger: Swagger.OpenAPIObject) => void;
    onDidAddOperation?: (operation: Swagger.OperationObject, swagger: Swagger.OpenAPIObject) => void;
    onDidAddParameter?: (parameter: Swagger.ParameterObject, swagger: Swagger.OpenAPIObject) => void;
    onDidChange?: (event: string, swaggerJson: Swagger.OpenAPIObject) => void;
}

export interface OpenApiState {
    showOpenApiAddPath: boolean;
    openApiJson: Swagger.OpenAPIObject;
}

class OpenApiVisualizer extends React.Component<OpenApiProps, OpenApiState> {
    constructor(props: OpenApiProps) {
        super(props);

        this.state = {
            openApiJson: {
                info: {
                    title: this.props.openApiJson.info.title,
                    version: this.props.openApiJson.info.version
                },
                openapi: this.props.openApiJson.openapi,
                paths: this.props.openApiJson.paths
            },
            showOpenApiAddPath: false,
        };

        this.handleShowOpenApiAddPath = this.handleShowOpenApiAddPath.bind(this);
        this.onAddOpenApiPath = this.onAddOpenApiPath.bind(this);
        this.onAddOpenApiOperation = this.onAddOpenApiOperation.bind(this);
        this.onInlineValueChange = this.onInlineValueChange.bind(this);
    }

    public componentDidMount() {
        let { openApiJson } = this.props;
        openApiJson = typeof openApiJson !== "object" ? JSON.parse(openApiJson) : openApiJson;

        this.setState({
            openApiJson
        });
    }

    public componentWillReceiveProps(nextProps: OpenApiProps) {
        let { openApiJson } = nextProps;
        openApiJson = typeof openApiJson !== "object" ? JSON.parse(openApiJson) : openApiJson;

        if (openApiJson !== this.state.openApiJson) {
            this.setState({
                openApiJson
            });
        }
    }

    public render() {
        const { openApiJson } = this.props;
        const { showOpenApiAddPath } = this.state;

        const appContext: OpenApiContext = {
            onAddOpenApiOperation: this.onAddOpenApiOperation,
            onAddOpenApiParameter: this.onAddOpenApiParameter,
            onAddOpenApiPath: this.onAddOpenApiPath,
            onAddOpenApiResponse: this.onAddOpenApiResponse,
            onInlineValueChange: this.onInlineValueChange,
            openApiJson,
        };

        debugger;

        return (
            <OpenApiContextProvider value={appContext}>
                <OpenApiInfo info={openApiJson.info} />
                <Button size="mini" primary icon labelPosition="left" onClick={this.handleShowOpenApiAddPath}>
                    <Icon name="plus" />
                    Add Resource
                </Button>
                {showOpenApiAddPath &&
                    <AddOpenApiPath onAddOpenApiPath={appContext.onAddOpenApiPath} />
                }
                <OpenApiPathList paths={openApiJson.paths} />
            </OpenApiContextProvider>
        );
    }

    private onAddOpenApiPath(path: Swagger.PathItemObject) {
        const { onDidAddResource, onDidChange } = this.props;
        const resourceName = path.name.replace(" ", "");
        const operations: { [index: string]: Swagger.OperationObject } = {};

        path.methods.forEach((method: string, index: number) => {
            operations[method.toLowerCase()] = {
                operationId: index === 0 ? resourceName : "resource" + index,
                responses : {},
            };
        });

        this.setState((prevState) => ({
            ...prevState,
            openApiJson: {
                ...prevState.openApiJson,
                paths: {
                    ...prevState.openApiJson.paths,
                    ["/" + resourceName]: operations
                }
            }
        }), () => {
            if (this.state.openApiJson.paths["/" + resourceName]) {

                if (onDidAddResource) {
                    onDidAddResource(resourceName, this.state.openApiJson);
                }

                if (onDidChange) {
                    onDidChange(EVENTS.ADD_RESOURCE, this.state.openApiJson);
                }

            }
        });
    }

    private onAddOpenApiOperation(operation: Swagger.OperationObject) {
        const { onDidAddOperation, onDidChange } = this.props;
        const path = operation.path;
        const operations = this.state.openApiJson.paths[path];
        const paths = this.state.openApiJson.paths;
        let resourceIndex: number = 0;

        Object.keys(paths).forEach((e) => {
            const op = paths[e];
            if (Object.keys(op).length > 0) {
                Object.keys(op).forEach((ea) => {
                    const lastValue: string = op[ea].operationId.split("-").pop();
                    if (lastValue.match(/\b(s|resource\w)\b/g) !== null) {
                        if (resourceIndex <= Number(lastValue.split("resource")[1])) {
                            resourceIndex = Number(lastValue.split("resource")[1]) + 1;
                        }
                    }
                });
            }
        });

        operation.method.forEach((method: string, index: number) => {
            operations[method.toLowerCase()] = {
                description: "",
                operationId: Object.keys(operations).length === 0 ? path  : "resource" + resourceIndex,
                parameters: [],
                responses : {
                        200: {
                            description: "OK"
                        }
                    },
                security: [],
                summary: "",
                tags: []
            };
            resourceIndex++;
        });

        this.setState((prevState) => ({
            ...prevState,
            openApiJson: {
                ...prevState.openApiJson,
                paths: {
                    ...prevState.openApiJson.paths,
                    [path] : operations
                }
            }
        }), () => {

            if (onDidAddOperation) {
                onDidAddOperation(operation, this.state.openApiJson);
            }

            if (onDidChange) {
                onDidChange(EVENTS.ADD_OPERATION, this.state.openApiJson);
            }

        });
    }

    private onAddOpenApiParameter(parameter: Swagger.ParameterObject) {
        // console.log('add-parameter');
    }

    private onAddOpenApiResponse(response: Swagger.ResponseObject) {
        // console.log('add-response');
    }

    private onInlineValueChange(openApiJson: Swagger.OpenAPIObject) {
        const { onDidChange } = this.props;

        this.setState({
            openApiJson
        }, () => {
            if (onDidChange) {
                onDidChange(EVENTS.ON_INLINE_CHANGE, this.state.openApiJson);
            }
        });
    }

    private handleShowOpenApiAddPath() {
        this.setState({
            showOpenApiAddPath: !this.state.showOpenApiAddPath
        });
    }
}

export default OpenApiVisualizer;
