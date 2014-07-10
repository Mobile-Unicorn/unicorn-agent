@ echo off
set ANDROID_HOME=D:\Software\android-sdk-windows
set ANDROID_JAR=%ANDROID_HOME%\platforms\android-10\android.jar
set SUPPORT_JAR=%ANDROID_HOME%\tools\support\annotations.jar

rd /s /q bin
md bin\classes
javac -encoding utf-8 -bootclasspath %ANDROID_JAR% -d bin\classes -sourcepath src src\com\cyou\cma\statistics\UnicornAgent.java
jar cf bin\statistic-common.jar -C bin\classes .