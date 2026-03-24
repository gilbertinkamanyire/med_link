@echo off
set /p name="Enter Student Name (Tinka Gilbert): "
set /p reg="Enter Registration Number: "
echo Generating Smart Campus Design Documentation...
node generate.js "%name%" "%reg%" smart_campus_design.html
pause
