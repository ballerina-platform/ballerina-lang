import log from 'log';
import ReactUpdates from 'react-dom/lib/ReactUpdates';
import ReactDefaultBatchingStrategy from 'react-dom/lib/ReactDefaultBatchingStrategy';

// based on: https://engineering.classdojo.com/blog/2016/12/10/catching-react-errors/

let isHandlingError = false;
const ReactTryCatchBatchingStrategy = {
    // this is part of the BatchingStrategy API. simply pass along
    // what the default batching strategy would do.
    get isBatchingUpdates() { return ReactDefaultBatchingStrategy.isBatchingUpdates; },

    batchedUpdates(...args) {
        try {
            ReactDefaultBatchingStrategy.batchedUpdates(...args);
        } catch (e) {
            if (isHandlingError) {
                // our error handling code threw an error. just throw now
                throw e;
            }
            isHandlingError = true;
            try {
                log.error('Error while react render.', e);
            } finally {
                isHandlingError = false;
            }
        }
    },
};

ReactUpdates.injection.injectBatchingStrategy(ReactTryCatchBatchingStrategy);
