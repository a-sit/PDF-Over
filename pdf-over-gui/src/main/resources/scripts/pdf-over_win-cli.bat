@echo off
REM start /b javaw -jar "%~dp0\..\lib\pdf-over-install-helper-1.0.0.jar"
start /b /WAIT java -cp "%~dp0\..\lib\*" at.asit.pdfover.gui.Main %* 

