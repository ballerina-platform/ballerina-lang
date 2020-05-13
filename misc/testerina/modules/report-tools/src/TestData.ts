export type TestData = {
    projectName: string;
    totalTests: number
    passed: number;
    failed: number;
    skipped: number;
    coveredLines: number;
    missedLines: number;
    coveragePercentage: number;
    moduleStatus: ModuleStatus[];
    moduleCoverage: ModuleCoverage[];
}

export type ModuleStatus = {
    name: string;
    totalTests: number;
    passed: number;
    failed: number;
    skipped: number;
    tests: Test[];
}

export type Test = {
    name: string;
    status: string;
    failureMessage?: string;
}

export type ModuleCoverage = {
    name: string;
    coveredLines: number;
    missedLines: number;
    coveragePercentage: number;
    sourceFiles: SourceFile[];
}

export type SourceFile = {
    name: string;
    coveredLines: number[];
    missedLines: number[];
    coveragePercentage: number;
    sourceCode: string;
}

