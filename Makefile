
JDK = "/opt/jdk27"
JAVAC = "$(JDK)/bin/javac"
JAVA = "$(JDK)/bin/java"
JAR = "$(JDK)/bin/jar"

peg.jar: src/mouse/Main.java
	mkdir -p bin
	$(JAVAC) -d bin -sourcepath src $^
	$(JAR) cvf $@ -C bin mouse
	$(JAR) uvf $@ -C src mouse/peg/grammar.template

PEG.java: peg.jar
	$(JAVA) -cp $< bootstrap/Bootstrap.java < bootstrap/peg.peg > $@

clean:
	rm -fr bin
	rm -f PEG.java peg.jar
 
