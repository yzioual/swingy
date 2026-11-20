JAR = target/swingy.jar
MVN = mvn
SRCS = $(shell find src -type f 2>/dev/null)

all: build

main:
	javac src/main/java/com/yzioual/Main.java && java src/main/java/com/yzioual/Main.java

build: $(JAR)

$(JAR): $(SRCS) pom.xml
	$(MVN) clean package

console: $(JAR)
	java -jar $(JAR) console

gui: $(JAR)
	java -jar $(JAR) gui

clean:
	$(MVN) clean

re: clean all

softclean:
	rm -f src/main/java/com/yzioual/Main.class

.PHONY: all build console gui clean re main softclean
