import React from 'react';
import PropTypes from 'prop-types';
import { Accordion } from 'semantic-ui-react';

import OasOperations from '../operations/operations';

/**
 * Resource Component
 */
/* eslint-disable */
class OasResource extends React.Component {
    /**
     * @returns {React.ReactElement} React Element
     */
    render() {
        const {
            oasOps, handleExpand, activeIndex, resPath, currIndex,
        } = this.props;
        return (
            <div className='resource'>
                <Accordion.Title className='res-title' index={currIndex} onClick={handleExpand}>
                    <span>{resPath}</span>
                </Accordion.Title>
                <Accordion.Content active={activeIndex === currIndex}>
                    <OasOperations oasOperations={oasOps} path={resPath} />
                </Accordion.Content>
            </div>
        );
    }
}

OasResource.propTypes = {
    oasOps: PropTypes.object.isRequired,
    activeIndex: PropTypes.number.isRequired,
    handleExpand: PropTypes.func.isRequired,
    currIndex: PropTypes.number.isRequired,
    resPath: PropTypes.string.isRequired,
};
export default OasResource;
