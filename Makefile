JAV = target/swingy.tar
MVN = mvn

all: build

main:
	javac src/main/java/com/yzioual/Main.java && java src/main/java/com/yzioual/Main.java

build:
	$(MVN) clean package

$(JAR):
	$(MVN) clean package

console: $(JAR)
	java -jar $(JAR) console

gui: $(JAR)
	java -jar $(JAR) gui

clean:
	$(MVN) clean

softclean:
	rm src/main/java/com/yzioual/Main.class

.PHONY: all build console gui clean re main softclean
