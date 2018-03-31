import fs = require('fs');
import path = require('path');
import yaml = require('js-yaml');
import plist = require('plist');

enum Language {
    TypeScript = "ballerina",
    TypeScriptReact = "ballerina"
}

enum Extension {
    TmLanguage = "tmLanguage",
    TmTheme = "tmTheme",
    YamlTmLangauge = "YAML-tmLanguage",
    YamlTmTheme = "YAML-tmTheme"
}

function file(language: Language, extension: Extension) {
    return path.join(__dirname, '..', `${language}.${extension}`);
}

function writePlistFile(grammar: any, fileName: string) {
    const text = plist.build(grammar);
    fs.writeFileSync(fileName, text);
}

function readYaml(fileName: string) {
    const text = fs.readFileSync(fileName, "utf8");
    return yaml.safeLoad(text);
}

function changeTsToTsx(str: string) {
    return str.replace(/\.ts/g, '.tsx');
}

function transformGrammarRule(rule: any, propertyNames: string[], transformProperty: (ruleProperty: string) => string) {
    for (const propertyName of propertyNames) {
        if (typeof rule[propertyName] === 'string') {
            rule[propertyName] = transformProperty(rule[propertyName]);
        }
    }

    for (var propertyName in rule) {
        var value = rule[propertyName];
        if (typeof value === 'object') {
            transformGrammarRule(value, propertyNames, transformProperty);
        }
    }
}

function transformGrammarRepository(grammar: any, propertyNames: string[], transformProperty: (ruleProperty: string) => string) {
    const repository = grammar.repository;
    for (let key in repository) {
        transformGrammarRule(repository[key], propertyNames, transformProperty);
    }
}

function changeTsToTsxGrammar(grammar: any, variables: any) {
    const tsxUpdates = updateGrammarVariables(readYaml(file(Language.TypeScriptReact, Extension.YamlTmLangauge)), variables);

    // Update name, file types, scope name and uuid
    for (let key in tsxUpdates) {
        if (key !== "repository") {
            grammar[key] = tsxUpdates[key];
        }
    }

    // Update scope names to .tsx
    transformGrammarRepository(grammar, ["name", "contentName"], changeTsToTsx);

    // Add repository items
    const repository = grammar.repository;
    const updatesRepository = tsxUpdates.repository;
    for (let key in updatesRepository) {
        switch(key) {
            case "expressionWithoutIdentifiers":
                // Update expression
                repository[key].patterns.unshift(updatesRepository[key].patterns[0]);
                break;
            default:
                // Add jsx
                repository[key] = updatesRepository[key];
        }
    }

    return grammar;
}

function replacePatternVariables(pattern: string, variableReplacers: VariableReplacer[]) {
    let result = pattern;
    for (const [variableName, value] of variableReplacers) {
        result = result.replace(variableName, value);
    }
    return result;
}

type VariableReplacer = [RegExp, string];
function updateGrammarVariables(grammar: any, variables: any) {
    delete grammar.variables;
    const variableReplacers: VariableReplacer[] = [];
    for (const variableName in variables) {
        // Replace the pattern with earlier variables
        const pattern = replacePatternVariables(variables[variableName], variableReplacers);
        variableReplacers.push([new RegExp(`{{${variableName}}}`, "gim"), pattern]);
    }
    transformGrammarRepository(
        grammar,
        ["begin", "end", "match"],
        pattern => replacePatternVariables(pattern, variableReplacers)
    );
    return grammar;
}

function buildGrammar() {
    const tsGrammarBeforeTransformation = readYaml(file(Language.TypeScript, Extension.YamlTmLangauge));
    const variables = tsGrammarBeforeTransformation.variables;
    const tsGrammar = updateGrammarVariables(tsGrammarBeforeTransformation, variables);

    // Write TypeScript.tmLanguage
    writePlistFile(tsGrammar, file(Language.TypeScript, Extension.TmLanguage));

    // Write TypeScriptReact.tmLangauge
    // const tsxGrammar = changeTsToTsxGrammar(tsGrammar, variables);
    // writePlistFile(tsxGrammar, file(Language.TypeScriptReact, Extension.TmLanguage));
}

function changeTsToTsxTheme(theme: any) {
    const tsxUpdates = readYaml(file(Language.TypeScriptReact, Extension.YamlTmTheme));

    // Update name, uuid
    for (let key in tsxUpdates) {
        if (key !== "settings") {
            theme[key] = tsxUpdates[key];
        }
    }

    // Update scope names to .tsx
    const settings = theme.settings;
    for (let i = 0; i < settings.length; i++) {
        settings[i].scope = changeTsToTsx(settings[i].scope);
    }

    // Add additional setting items
    theme.settings = theme.settings.concat(tsxUpdates.settings);

    return theme;
}

function buildTheme() {
    const tsTheme = readYaml(file(Language.TypeScript, Extension.YamlTmTheme));

    // Write TypeScript.tmTheme
    writePlistFile(tsTheme, file(Language.TypeScript, Extension.TmTheme));

    // Write TypeScriptReact.thTheme
    // const tsxTheme = changeTsToTsxTheme(tsTheme);
    // writePlistFile(tsxTheme, file(Language.TypeScriptReact, Extension.TmTheme));
}

buildGrammar();
//buildTheme();