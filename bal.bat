@echo off
echo Checking local Ballerina...
"C:\Program Files\Ballerina\jballerina-2201.13.0-m3\bin\bal.bat" --version

echo.
echo Checking which ballerina is in PATH...
where ballerina

echo.
echo Current Ballerina version in PATH:
ballerina --version