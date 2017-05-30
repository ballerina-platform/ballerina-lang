
class ActiveArbiter {
    readyToDeactivate(statement) {
        clearTimeout(this.timeout);
        if (statement.state.active === 'visible') {
            setTimeout(() => {
                if (statement.state.active === 'fade') {
                    statement.setState({active: 'hidden'});
                }
            }, 500);
            statement.setState({active: 'fade'});
        }
    }

    readyToDelayedActivate(statement) {
        this.timeout = setTimeout(() => {
            this.readyToActivate(statement);
        }, 500);
    }

    readyToActivate(statement) {
        clearTimeout(this.timeout);
        if (this.active && this.active !== statement) {
            if (this.active.active !== 'hidden') {
                this.active.setState({active: 'hidden'});
            }
        }
        this.active = statement;
        if (statement.state.active !== 'visible') {
            statement.setState({active: 'visible'});
        }
    }
}
export default ActiveArbiter;
