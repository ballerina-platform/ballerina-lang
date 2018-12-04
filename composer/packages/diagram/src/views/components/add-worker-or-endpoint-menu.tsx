import React, { Fragment } from "react";
import { SimplePoint } from "../../view-model/simple-point";
import { EndpointSearchDialog } from "./endpoint-search-dialog";
import { SVGDropDownMenu } from "./svg-dropdown-menu";

export class AddWorkerOrEndpointMenu extends React.Component<{
        triggerPosition: SimplePoint;
        onAddEndpoint?: (endpointDef: any) => void;
        onAddWorker?: () => void;
    }, {
        showEndpoitDialog: boolean
    }> {

    public state = {
        showEndpoitDialog: false
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
                show={this.state.showEndpoitDialog}
                onClose={this.closeEpSearchDialog}
                onEndpointSelect={onAddEndpoint}
            />
        </Fragment>;
    }

    private openEpSearchDialog = () => this.setState({ showEndpoitDialog: true });
    private closeEpSearchDialog = () => this.setState({ showEndpoitDialog: false });
}
