TOP_DIR="$(cd "$(dirname "$0")"/../"" && pwd)"
cd $TOP_DIR
BIN_FOLDER=${BIN_FOLDER:-$TOP_DIR/bin}
SRC_PATH="org/ucx/jucx/examples"
mkdir -p $BIN_FOLDER/$SRC_PATH
SRC_JAVA_FOLDER=${SRC_JAVA_FOLDER:-$TOP_DIR/examples/}
SRC_JAVA_FILES="$SRC_JAVA_FOLDER/$SRC_PATH/*.java"
DEPENDENCY="$BIN_FOLDER/jucp.jar"

javac -cp $DEPENDENCY -d $BIN_FOLDER $SRC_JAVA_FILES
