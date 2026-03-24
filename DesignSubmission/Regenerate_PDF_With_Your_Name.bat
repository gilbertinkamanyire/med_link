@echo off
echo =======================================================
echo     MediLink PDF Design Documentation Generator
echo =======================================================
echo.
set /p name="1. Enter your Full Name: "
set /p reg="2. Enter your Registration Number: "
echo.
echo Generating PDF with your details... Please wait a few seconds...
node generate.js "%name%" "%reg%"
echo.
echo Done! Please check MediLink_Design_Documentation.pdf
pause
