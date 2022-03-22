@echo off
start /b /WAIT java -cp "%~dp0\..\lib\*" at.asit.pdfover.gui.Main %* 

