export interface BallerinaExample {
    title?: string;
    url: string;
    name: string;
}

export interface BallerinaExampleCategory {
    title: string;
    column: number;
    samples: BallerinaExample[];
    category?: string;
}
