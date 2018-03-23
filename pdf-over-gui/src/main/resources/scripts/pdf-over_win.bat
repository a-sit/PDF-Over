@echo off
start /b javaw -jar "%~dp0\..\lib\pdf-over-install-helper-1.0.0.jar"
start /b javaw -cp "%~dp0\..\lib\*" at.asit.pdfover.gui.Main %*
