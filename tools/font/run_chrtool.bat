@echo off
set PATH=C:\w64devkit\bin;%PATH%
gcc -mwindows -O2 -D_CRT_SECURE_NO_WARNINGS chrtool_chr.c chrtool_gen.c chrtool_ui.c -lgdi32 -lmsimg32 -lcomdlg32 -o chrtool.exe
if errorlevel 1 exit /b 1
copy /Y chrtool.exe "C:\Program Files (x86)\Steam\steamapps\common\Haegemonia Legions of Iron\chrtool.exe"
if errorlevel 1 exit /b 1
echo Build OK: chrtool.exe (copied to game folder)
