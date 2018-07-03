@echo off
start /b javaw -jar "%~dp0\..\lib\pdf-over-install-helper.jar"
start /b javaw -cp "%~dp0\..\lib\*" at.asit.pdfover.gui.Main %*
