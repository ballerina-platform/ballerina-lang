import React from 'react';
import proptypes from 'prop-types';
import { Accordion, Button, Icon } from 'semantic-ui-react';

import OasOperation from './operation';
import OsaAddOperation from './add-operation';
import SwaggerAppContext from '../../context/app-context';

/* eslint-disable */
class OasOperations extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            activeIndex: -1,
            showAddOperation: false,
        };

        this.handleOperationExpand = this.handleOperationExpand.bind(this);
        this.handleShowAddOperation = this.handleShowAddOperation.bind(this);
    }

    /**
     * Click handler for accordion expand.
     * @param {Object} e event Object
     * @param {Object} titleProps accordion title props
     */
    handleOperationExpand(e, titleProps) {
        const { index } = titleProps;
        const { activeIndex } = this.state;
        const newIndex = activeIndex === index ? -1 : index;

        this.setState({ activeIndex: newIndex });
    }

    /**
     * Event handler for show add resource form
     */
    handleShowAddOperation() {
        const { showAddOperation } = this.state;
        this.setState({
            showAddOperation: !showAddOperation,
        });
    }

    render() {
        const { oasOperations, path } = this.props;
        const { activeIndex, showAddOperation } = this.state;
        return (
            <React.Fragment>
                <Button size='mini' icon labelPosition='left' onClick={this.handleShowAddOperation}>
                    <Icon name='plus' />
                    Add Operation
                </Button>
                {showAddOperation && 
                    <SwaggerAppContext.Consumer>
                        {(appContext) => {
                            return (
                                <OsaAddOperation
                                    onAddOperation={appContext.onAddOperation}
                                    oasJson={appContext.oasJson}
                                    path={path}
                                />
                            );
                        }}
                    </SwaggerAppContext.Consumer>
                }
                {Object.keys(oasOperations).length > 0 && 
                    <Accordion fluid>
                        {oasOperations && Object.keys(oasOperations).map((operation, index) => {
                            return (
                                <SwaggerAppContext.Consumer>
                                    {(appContext) => {
                                        return (
                                            <OasOperation
                                                path={path}
                                                opType={operation}
                                                oasOp={oasOperations[operation]}
                                                activeIndex={activeIndex}
                                                currIndex={index}
                                                handleExpand={this.handleOperationExpand}
                                                onDeleteOperation={appContext.onDeleteOperation}
                                            />
                                        );
                                    }}
                                </SwaggerAppContext.Consumer>
                            );
                        })}
                    </Accordion>
                }
            </React.Fragment>
        );
    }
}

OasOperations.prototypes = {
    oasOperations: proptypes.object.isRequired,
    path: proptypes.string.isRequired,
};

export default OasOperations;
