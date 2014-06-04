ANDROID_HOME=/usr/local/android-sdk-linux
ANDROID_JAR=$ANDROID_HOME/platforms/android-10/android.jar
SUPPORT_JAR=$ANDROID_HOME/tools/support/annotations.jar

rm -rf bin
mkdir -p bin/classes
javac -encoding utf-8 -bootclasspath $ANDROID_JAR:$SUPPORT_JAR -d bin/classes -sourcepath src src/com/cyou/cma/statistics/CyouAgent.java
jar cf statistic-common.jar -C bin/classes/ .