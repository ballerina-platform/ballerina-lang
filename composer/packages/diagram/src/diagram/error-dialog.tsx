import React, { StatelessComponent } from "react";
import { Header, Modal } from "semantic-ui-react";

export const DiagramErrorDialog: StatelessComponent<{ error: Error }> = ({
        error
    }) => (
    <Modal
        open={true}
        basic
        size="small"
      >
        <Header icon="warning" content="Oops! Something went wrong. " />
        <Modal.Content>
          <h3>{(error ? error.message : "")}</h3>
          <p>{(error ? error.stack : "")}</p>
        </Modal.Content>
      </Modal>
);
