import { BallerinaEndpoint } from "@ballerina/lang-service";
import React, { Fragment } from "react";
import { SimplePoint } from "../../view-model/simple-point";
import { EndpointSearchDialog } from "./endpoint-search-dialog";
import { SVGDropDownMenu } from "./svg-dropdown-menu";

export class AddWorkerOrEndpointMenu extends React.Component<{
        triggerPosition: SimplePoint;
        onAddEndpoint?: (endpointDef: BallerinaEndpoint) => void;
        onAddWorker?: () => void;
    }, {
        showEndpointDialog: boolean
    }> {

    public state = {
        showEndpointDialog: false
    };

    public render() {
        const { triggerPosition, onAddWorker, onAddEndpoint } = this.props;

        return <Fragment>
            <SVGDropDownMenu
                    triggerPosition={triggerPosition}
                    triggerIcon="add"
                    items={[
                        {
                            icon: "worker",
                            name: "Worker",
                            onClick: onAddWorker,
                        },
                        {
                            icon: "endpoint",
                            name: "Endpoint",
                            onClick: this.openEpSearchDialog
                        }
                    ]}
                />
            <EndpointSearchDialog
                show={this.state.showEndpointDialog}
                onClose={this.closeEpSearchDialog}
                onEndpointSelect={onAddEndpoint}
            />
        </Fragment>;
    }

    private openEpSearchDialog = () => this.setState({ showEndpointDialog: true });
    private closeEpSearchDialog = () => this.setState({ showEndpointDialog: false });
}
