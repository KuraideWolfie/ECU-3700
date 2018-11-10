@echo off

mkdir out

cd src
javac -d "../out" -cp ".;../postgresql-42.2.5.jar" Main.java
cd ..
pause

jar cfm DBMSApp.jar ./src/manifest.txt -C out .
rmdir out /s /q
java -jar DBMSApp.jar in ./input.txt