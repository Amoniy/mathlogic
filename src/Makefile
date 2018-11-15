all: compile 

compile:
	javac hw6/* -d ./out

run:
	cd out && java hw6.Main

clean:
	rm -rf out/* *zip
	mkdir out

pack:
	zip hw6.zip -r hw6 Makefile out