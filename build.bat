@echo off
rem Build the widescreen-mod proxy DLL (needs w64devkit: https://github.com/skeeto/w64devkit)
rem The game folder copy step is optional - remove it if you only want the build.
set PATH=C:\w64devkit\bin;%PATH%
gcc -m32 -shared -O2 hghook.c proxy.def -o binkw32.dll
if errorlevel 1 exit /b 1
echo Build OK: binkw32.dll
if exist "C:\Program Files (x86)\Steam\steamapps\common\Haegemonia Legions of Iron\Game.exe" (
  copy /Y binkw32.dll "C:\Program Files (x86)\Steam\steamapps\common\Haegemonia Legions of Iron\binkw32.dll" >nul
  echo Copied to the game folder.
)
