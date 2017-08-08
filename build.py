#!/usr/bin/env python

import os
import sys
import getopt
import subprocess
import shutil

try:
        f=os.popen('which python')
        pythonpath=f.readline()
        print "PYTHONPATH is %s" % pythonpath
except:
        print "\nPYTHON PATH not found!"
        sys.exit(1)

sys.path.append(pythonpath)

# Checks to see if JAVA path is valid
cmd = "[ ! -e ${JAVA_HOME} ]"
if(os.system(cmd) == 0):
        print "ERROR: JAVA not found!"
        sys.exit(1)


# Get Top Directory
dir =  os.path.dirname(os.path.realpath(__file__))
os.chdir(dir);

target = 'jucp.jar'
bin_dir = dir+'/bin'
lib_dir = dir+'/src/lib'
docs_dir = dir+'/docs'
src_java_dir = dir+'/src/java'
src_java_files = dir+'/java_files.txt'
find_cmd = 'find {} -name \"*.java\" > {}'
os.system(find_cmd.format(src_java_dir, src_java_files))
native_libs = 'libjucp.so libucp.so libucs.so libuct.so'


def usage():
    print "Usage: ./build.py [OPTION]"
    print "\t-h  | --help                       print help"
    print "\t-s  | --strip                      don't strip debug symbols"
    print "\t-e  | --examples                   don't build example code"
    print "\t-u  | --ucx                        don't build UCX library"
    print "\t-t  | --tests                      don't build test code"

options, remainder = getopt.gnu_getopt(sys.argv[1:], '?hseut', ['help', 'strip', 'examples', 'ucx', 'tests'])

strip_cmd = "strip -s"
examples = True
ucx = True
tests = True
git_version = 0
status1 = 0

for opt, arg in options:
	if opt in ('-?', '-h', '--help'):
		usage()
		sys.exit(0)
	elif opt in ('-s', '--strip'):
		strip_cmd = "touch"
	elif opt in ('-e', '--examples'):
		examples = False
	elif opt in ('-u', '--ucx'):
		ucx = False
	elif opt in ('-t', '--tests'):
		tests = False
	else:
		usage()
		assert False, "unhandled option"

print('\nThe JUCP top directory is ' + dir)

### CLEAN
shutil.rmtree(bin_dir)
if not os.path.exists(bin_dir):
    os.makedirs(bin_dir)
	
os.chdir(dir)

if ucx:
	print('Build UCX... libuc* C code')
	os.system('git submodule update --init')
	os.chdir('src/ucx')
	git_version = os.system('git describe --long --tags --always --dirty')
	cmd = 'make distclean -si > /dev/null 2>&1;'
	os.system(cmd)
	os.system('./autogen.sh')
	os.system('./contrib/configure-release --prefix=$PWD/install/ --silent')
	cmd = 'make -j install --quiet'
	status1 = os.system(cmd)
else:
	os.chdir('src/ucx')	

cmd = 'cp -f install/lib/libuc*.so ' +  bin_dir +  ' && ' + strip_cmd + ' ' + bin_dir + '/libuc*.so'
status2 = os.system(cmd)
if status1 != 0 or status2 != 0:
	print('FAILURE! stopped JUCP build')
	sys.exit(1)
		
######################

## Build Java UCP

######################

## Build JUCP C code
print('Build JUCP C code')
os.chdir(dir + '/src/c')
cmd = './autogen.sh && ./configure --silent && make clean -s'
status1 = os.system(cmd)
cmd = 'make -s'
status2 = os.system(cmd)
if status2 != 0 or status1 != 0:
    print('FAILURE! stopped JUCP build: Native')
    sys.exit(1)
	
cmd = 'cp -f src/.libs/libjucp.so '+ bin_dir + ' && ' + strip_cmd + ' ' + bin_dir + '/libjucp.so'
os.system(cmd)

## Build JUCP JAVA code
print('Build JUCP Java code')
os.chdir(dir)
cmd = 'javac -cp ' +  lib_dir + '/commons-logging.jar -d ' + bin_dir + ' ' + '@' + src_java_files
status = os.system(cmd)
if status != 0:
    print('FAILURE! stopped JUCP build: JAVA')
    sys.exit(1)

## Prepare jar MANIFEST file
os.chdir(dir)
cmd = 'cp manifest.template ' + dir + '/manifest.txt'
os.system(cmd)
cmd = 'cp manifest_perf.template ' + dir + '/manifest_perf.txt'
os.system(cmd)
cmd = 'sed -i \"s/Implementation-Version: .*/Implementation-Version: ' + str(git_version) + '/\" ' + dir + '/manifest.txt'
os.system(cmd)

## Create JUCP Jar
print('Creating JUCP jar...')
os.chdir(bin_dir)
cmd = 'jar -cvfm ' + target + ' ' + dir + '/manifest.txt org ' + native_libs
status = os.system(cmd)
if status != 0:
    print('FAILURE! stopped JUCP build: jar')
    sys.exit(1)

print('\nJUCP Build completed SUCCESSFULLY!\n')

if examples:
	os.chdir(dir)
	src_path = '/org/ucx/jucx/examples'
	os.makedirs(bin_dir + src_path)
	example_java_dir = dir+'/examples'
	example_java_files = example_java_dir + src_path + '/*.java'
	dependency = bin_dir + '/' + target
	
	cmd = 'javac -cp ' + dependency + ' -d ' + bin_dir + ' ' + example_java_files
	os.system(cmd)

if tests:
	os.chdir(dir)
        src_path = '/org/ucx/jucx/tests'
	test_java_dir = dir + '/src/test'
	test_java_files = dir + '/test_files.txt'
	find_cmd2 = find_cmd + ' -not -name \"Perftest.java\"'
	os.system(find_cmd2.format(test_java_dir, test_java_files))
        os.makedirs(bin_dir + src_path)
        cmd = 'javac -classpath ' + dependency + ' -d ' + bin_dir + ' @' + test_java_files
        os.system(cmd)
	
	cmd = 'jar -cvfm perf.jar ' + dir + '/manifest_perf.txt'
	os.system(cmd)
	dependency += ':' + bin_dir + '/perf.jar'
	
	cmd = 'javac -classpath ' + dependency + ' -d ' + bin_dir + ' '


sys.exit(0)

