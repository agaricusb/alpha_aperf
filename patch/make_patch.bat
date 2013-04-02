@echo off

set thisdir=%CD%
call cleanup.bat"

set curdir=%CD%
set basedir=%curdir%\..\..\..

set srcFolder=%curdir%\src
set libFolder=%curdir%\class
set compilerFolder=%basedir%\compiler

set compilerReobfFolder=%compilerFolder%\reobf
set compilerSrcFolder=%compilerFolder%\src\minecraft

set reobfFolder=%srcFolder%\..\reobf

echo Copying files
xcopy "%srcFolder%\*" "%compilerSrcFolder%\" /s /q /i /y

echo Compiling
cd "%compilerFolder%"

runtime\bin\python\python_mcp runtime\recompile.py --client

echo Reobfuscating
runtime\bin\python\python_mcp runtime\reobfuscate.py --client

echo Copying reobf compiler/reobf
xcopy "%compilerReobfFolder%\minecraft\*" "%reobfFolder%\" /s /q /i /y

echo Running jar
cd "%reobfFolder%"
"C:\Program Files\Java\jdk1.7.0_09\bin\jar.exe" cfM "%curdir%\aPerf_network_patch.zip" *

cd "%thisdir%"
call cleanup.bat