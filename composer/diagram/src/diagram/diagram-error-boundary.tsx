import React from "react";

export class DiagramErrorBoundary extends React.Component<{},
        { hasError: boolean }> {

    public static getDerivedStateFromError(error: Error) {
        // Update state so the next render will show the fallback UI.
        return { hasError: true };
    }

    public state = {
        hasError: false,
    };

    public componentsDidCatch(error: Error, errorInfo: React.ErrorInfo) {
        // tslint:disable-next-line:no-console
        console.log("Error");
    }

    public render() {
        if (this.state.hasError) {
            return <div>
                {"Oops! Something went wrong."}
            </div>;
        }
        return this.props.children;
    }
}
