@echo off
mkdir out
javac -d out Main.java
jar cfe Generator.jar Main -C out .
pause
rmdir out /s /q
pause
java -jar Generator.jar -d