@echo off
set PATH=C:\w64devkit\bin;%PATH%
gcc -mwindows -O2 -D_CRT_SECURE_NO_WARNINGS ttcore.c invdata.c InvEditor.c -lgdi32 -lcomctl32 -lcomdlg32 -lshell32 -lole32 -o InventionEditor.exe
if errorlevel 1 exit /b 1
copy /Y InventionEditor.exe "C:\Program Files (x86)\Steam\steamapps\common\Haegemonia Legions of Iron\InventionEditor.exe"
if errorlevel 1 exit /b 1
echo Build OK: InventionEditor.exe (copied to game folder)
