#!/bin/bash

# Configuring Running Directory
TOP_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $TOP_DIR
echo -e "\nThe JUCP top directory is $TOP_DIR\n"

##GIT_VERSION=`git describe --long --tags --always --dirty`
##echo "JUCP git version: $GIT_VERSION"

TARGET=${TARGET:-jucp.jar}
BIN_FOLDER=${BIN_FOLDER:-$TOP_DIR/bin}
LIB_FOLDER=${LIB_FOLDER:-$TOP_DIR/src/lib}
DOCS_FOLDER=${DOCS_FOLDER:-$TOP_DIR/docs}
SRC_JAVA_FOLDER=${SRC_JAVA_FOLDER:-$TOP_DIR/src/java}
SRC_JAVA_FILES="$SRC_JAVA_FOLDER/org/ucx/jucx/*.java"
NATIVE_LIBS="libjucp.so libucp.so libucs.so libuct.so"

if [ -z "$DONT_STRIP" ]; then
	STRIP_COMMAND="strip -s"
else
	STRIP_COMMAND="touch" #do not strip libraries from symbols
fi

## Clean
rm -fr $BIN_FOLDER
mkdir -p $BIN_FOLDER

## Build UCX
echo "Build UCX... libuc* C code"
cd $TOP_DIR
git submodule update --init
GIT_VERSION_UCX=`cd src/ucx; git describe --long --tags --always --dirty`
cd src/ucx/ && make distclean -si > /dev/null 2>&1;
./autogen.sh && ./contrib/configure-release --prefix=$PWD/install/ --silent && make -j install --quiet \
		&& cp -f install/lib/libuc*.so $BIN_FOLDER  && $STRIP_COMMAND $BIN_FOLDER/libuc*.so 
if [[ $? != 0 ]] ; then
    echo "FAILURE! stopped JUCP build"
    exit 1
fi


######################

## Build Java UCP

######################

## Build JUCP C code
echo "Build JUCP C code"
cd $TOP_DIR
cd src/c/ && ./autogen.sh && ./configure --silent && make clean -s
status=$?
make -s
if [[ $? != 0 ]] || [[ $status != 0 ]]; then
    echo "FAILURE! stopped JUCP build"
    exit 1
fi
cp -f src/.libs/libjucp.so $BIN_FOLDER && $STRIP_COMMAND $BIN_FOLDER/libjucp.so

## Build JXIO JAVA code
echo "Build JUCP Java code"
cd $TOP_DIR
javac -cp $LIB_FOLDER/commons-logging.jar -d $BIN_FOLDER $SRC_JAVA_FILES
if [[ $? != 0 ]] ; then
    echo "FAILURE! stopped JUCP build"
    exit 1
fi

##############

#exit 0

##############

### Create JUCP Java docs
#echo "Creating JUCP Java docs"
#javadoc -quiet -classpath $LIB_FOLDER/commons-logging.jar -d $DOCS_FOLDER -sourcepath src/java/ org.accelio.jxio
#if [[ $? != 0 ]] ; then
#    echo "FAILURE! stopped JUCP build"
#    exit 1
#fi

## Prepare jar MANIFEST file
cd $TOP_DIR
cp manifest.template ${TOP_DIR}/manifest.txt
sed -i "s/Implementation-Version: .*/Implementation-Version: $GIT_VERSION/" ${TOP_DIR}/manifest.txt
echo "Implementation-Version-AccelIO: $GIT_VERSION_XIO" >> ${TOP_DIR}/manifest.txt

## Create JUCP Jar
echo "Creating JUCP jar..."
cd $BIN_FOLDER && jar -cvfm $TARGET ${TOP_DIR}/manifest.txt org $NATIVE_LIBS
if [[ $? != 0 ]] ; then
    echo "FAILURE! stopped JUCP build"
    exit 1
fi

## Print Version details
cd $TOP_DIR
echo "UCX git version: $GIT_VERSION_UCX" > version
echo ""; cat $TOP_DIR/version

echo -e "\nJUCP Build completed SUCCESSFULLY!\n"
