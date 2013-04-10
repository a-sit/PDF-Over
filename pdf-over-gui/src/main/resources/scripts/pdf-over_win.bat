@echo off
pushd %~dp0\..
start /b javaw -jar lib\pdf-over-gui-4.0.0-SNAPSHOT.jar %*
popd