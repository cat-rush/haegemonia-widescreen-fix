@echo off
setlocal
cd /d "%~dp0"
set CC=C:\w64devkit\bin\g++
set RC=C:\w64devkit\bin\windres.exe
%RC% app.rc -O coff -o app.res || exit /b 1
%CC% -O2 -std=c++17 -municode -mwindows -DUNICODE -D_UNICODE -DWIN32_LEAN_AND_MEAN ^
  src\main.cpp src\gui.cpp src\model.cpp src\inflate.cpp src\catalog.cpp app.res ^
  -o scpeditor.exe ^
  -static -lcomctl32 -lcomdlg32 -lgdi32 -luser32 -lkernel32 -lshell32 -lole32 -luuid || exit /b 1
echo OK: scpeditor.exe
