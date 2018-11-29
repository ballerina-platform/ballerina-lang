import React from "react";
import { DiagramErrorDialog } from "./error-dialog";

interface DiagramErrorBoundaryState {
    hasError: boolean;
    error?: Error;
}
export class DiagramErrorBoundary extends React.Component<{},
    DiagramErrorBoundaryState> {

    public static getDerivedStateFromError(error: Error) {
        // Update state so the next render will show the fallback UI.
        return { hasError: true, error };
    }

    public state: DiagramErrorBoundaryState = {
        hasError: false
    };

    public componentsDidCatch(error: Error, errorInfo: React.ErrorInfo) {
        // tslint:disable-next-line:no-console
        console.log("Error occurred: ", error);
    }

    public render() {
        const { hasError, error } = this.state;
        if (hasError) {
            return <div>
                <DiagramErrorDialog error={error as Error} />
            </div>;
        }
        return this.props.children;
    }
}
