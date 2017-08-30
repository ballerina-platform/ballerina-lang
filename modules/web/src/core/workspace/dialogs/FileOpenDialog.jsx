import React from 'react';
import PropTypes from 'prop-types';
import { Button } from 'react-bootstrap';
import Dialog from './../../view/Dialog';
import FileTree from './../../view/FileTree';

/**
 * File Open Wizard Dialog
 * @extends React.Component
 */
class FileOpenDialog extends React.Component {

    /**
     * @inheritdoc
     */
    render() {
        return (
            <Dialog
                title="Open File"
                actions={
                    <Button />
                }
            >
                <FileTree />
            </Dialog>
        );
    }
}

FileOpenDialog.propTypes = {
    title: PropTypes.node,
};

export default FileOpenDialog;
