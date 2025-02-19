#!/bin/env fish
git clean -fdx
chmod +x gradlew

 if test -f foo.txt
       
    else
       echo "local.properties file doesnt exists try to copy from ../Xed-Editor"
	cp -r ../Xed-Editor/local.properties .
   end


./gradlew clean
./gradlew assembleRelease

