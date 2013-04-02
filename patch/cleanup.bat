@echo off

set curdir=%CD%
set basedir=%curdir%\..\..\..

set srcFolder=%curdir%\src
set bckFolder=%curdir%\backup
set libFolder=%curdir%\class
set compilerFolder=%basedir%\compiler

set compilerReobfFolder=%compilerFolder%\reobf
set compilerSrcFolder=%compilerFolder%\src\minecraft

set reobfFolder=%srcFolder%\..\reobf

echo Cleaning org/..-ru-ee
del "%compilerSrcFolder%\org\apache\*" /s /f /q
del "%compilerSrcFolder%\org\bukkit\*" /s /f /q
del "%compilerSrcFolder%\org\yaml\*" /s /f /q
del "%compilerSrcFolder%\ru\*" /s /f /q
del "%compilerSrcFolder%\javassist\*" /s /f /q
del "%compilerSrcFolder%\ee\*" /s /f /q
del "%compilerSrcFolder%\railcraft\*" /s /f /q
del "%compilerSrcFolder%\net\minecraft\network\packet\IPacketHandler.java" /f /q
xcopy "%bckFolder%\*" "%compilerSrcFolder%\" /s /q /i /y

echo Cleaning compiler reobf client-server, mod-reobf
del "%compilerReobfFolder%\minecraft\*" /s /f /q

echo Deleting compiler src folders
rd "%compilerSrcFolder%\org\apache" /s /q
rd "%compilerSrcFolder%\org\bukkit" /s /q
rd "%compilerSrcFolder%\org\yaml" /s /q
rd "%compilerSrcFolder%\ru" /s /q
rd "%compilerSrcFolder%\ee" /s /q
rd "%compilerSrcFolder%\javassist" /s /q
rd "%compilerSrcFolder%\railcraft" /s /q

echo Deleting client-server folders
rd "%compilerReobfFolder%\minecraft" /s /q

