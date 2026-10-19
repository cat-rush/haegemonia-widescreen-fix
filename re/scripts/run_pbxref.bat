@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d C:\Users\Nova\haegemonia-mod
"C:\ghidra_12.1.4_PUBLIC\support\analyzeHeadless.bat" "ghidra-proj" Haegemonia -process Game.exe -noanalysis -scriptPath "C:\Users\Nova\haegemonia-mod" -postScript PbXrefs.java

