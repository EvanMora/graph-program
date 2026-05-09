javac -cp "./lib/*" -d bin src/*.java src/domain/*.java
java -cp "bin:./lib/*" Main
