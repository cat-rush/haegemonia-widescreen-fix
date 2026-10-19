@echo off
set JDK=C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot
set GHIDRA=C:\ghidra_12.1.4_PUBLIC
set PATH=%JDK%\bin;%PATH%
"%GHIDRA%\support\analyzeHeadless.bat" ghidra-proj Haegemonia -process Game.exe -noanalysis -scriptPath . -postScript MsgScreenCtx.java
