import React from 'react';
import PropTypes from 'prop-types';
import { Button, Form, FormGroup, FormControl, InputGroup } from 'react-bootstrap';
import Dialog from 'core/view/Dialog';
import './LauncherConfigDialog.scss';
/**
 *
 * @extends React.Component
 */
class LauncherConfigDialog extends React.Component {

    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
        this.state = {
            error: '',
            showDialog: true,
            configArguments: this.props.configArguments || [],
        };
        this.onDialogHide = this.onDialogHide.bind(this);
        this.onAddArgument = this.onAddArgument.bind(this);
        this.onChangeArgument = this.onChangeArgument.bind(this);
        this.saveConfigs = this.saveConfigs.bind(this);
        this.onDeleteArgument = this.onDeleteArgument.bind(this);
    }
    /**
     * Called when user hides the dialog
     */
    onDialogHide() {
        this.setState({
            showDialog: false,
        });
    }

    /**
     * Add command argument text field
     * @memberof LauncherConfigDialog
     */
    onAddArgument() {
        this.setState({
            configArguments: [...this.state.configArguments, ''],
        });
    }
    /**
     * on change argument text field
     * @param {int} idx - Array Index
     * @param {Object} evt - Text field change event
     * @memberof LauncherConfigDialog
     */
    onChangeArgument(idx, evt) {
        const newConfigArguments = [...this.state.configArguments];
        newConfigArguments[idx] = evt.target.value;
        this.setState({ configArguments: newConfigArguments });
    }

    /**
     * Save launcher configs
     * @memberof LauncherConfigDialog
     */
    saveConfigs() {
        this.props.onSaveConfigs(this.state.configArguments);
        this.setState({
            showDialog: false,
        });
    }
    /**
     * Remove command argument text field
     * @param {int} deleteIndex - Array index
     * @memberof LauncherConfigDialog
     */
    onDeleteArgument(deleteIndex) {
        const newConfigArguments = this.state.configArguments.filter((config, index) => {
            return index !== deleteIndex;
        });
        this.setState({
            configArguments: newConfigArguments,
        });
    }

    /**
     * @inheritdoc
     */
    render() {
        return (
            <Dialog
                show={this.state.showDialog}
                title="Configure Application Arguments"
                actions={
                    <Button
                        bsStyle="primary"
                        disabled={this.state.filePath === '' && this.state.fileName === ''}
                        onClick={this.saveConfigs}
                    >
                        Save
                    </Button>
                }
                closeAction
                onHide={this.onDialogHide}
                error={this.state.error}
            >
                <Form horizontal>

                    {this.state.configArguments.map((config, idx) => {
                        return (
                            <FormGroup key={idx}>
                                <InputGroup>
                                    <FormControl
                                        value={config}
                                        onChange={event => this.onChangeArgument(idx, event)}
                                        type="text"
                                    />
                                    <InputGroup.Button>
                                        <Button
                                            bsStyle="primary"
                                            onClick={() => this.onDeleteArgument(idx)}
                                            className="launcher-config-delete"
                                            disabled={this.state.filePath === '' && this.state.fileName === ''}
                                        >
                                            <i className="fw fw-delete" />
                                        </Button>
                                    </InputGroup.Button>
                                </InputGroup>
                            </FormGroup>
                        );
                    })}

                    <FormGroup controlId="filePath">
                        <Button
                            bsStyle="primary"
                            onClick={this.onAddArgument}
                            disabled={this.state.filePath === '' && this.state.fileName === ''}
                        >
                            <i className="fw fw-add" />
                            &nbsp; Add argument
                        </Button>


                    </FormGroup>
                </Form>
            </Dialog>
        );
    }
}

LauncherConfigDialog.propTypes = {

};

export default LauncherConfigDialog;
