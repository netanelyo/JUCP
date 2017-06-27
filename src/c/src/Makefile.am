JDK = $(JAVA_HOME)
topdir=../../

# additional include paths necessary to compile the C library
AM_CPPFLAGS = -fPIC -DPIC -DGIT_VERSION=\"`git describe --long --tags --always --dirty`\" -Werror \
		-I$(JDK)/include -I$(JDK)/include/linux \
		-I$(topdir)ucx/src/
		
AM_CXXFLAGS = -std=c++11 
###############################################################################
# THE LIBRARIES TO BUILD
###############################################################################

lib_LTLIBRARIES = libjucp.la

libjucp_la_SOURCES = Bridge.cc Worker.cc UcpRequest.cc Msg.cc

libjucp_la_LDFLAGS = -shared -rdynamic -Wl,-rpath,$(topdir)ucx/install/lib

libjucp_la_LIBADD = -L$(topdir)ucx/install/lib -lucs -luct -lucp

libjucp_la_DEPENDENCIES = Makefile.am Makefile.in Makefile

cov:
	PATH=/.autodirect/app/Coverity/cov-analysis-linux64-7.0.1/bin:$$PATH && rm -rf $(PWD)/cov-build && make clean && cov-build --dir $(PWD)/cov-build make all && cov-analyze --dir $(PWD)/cov-build && cov-format-errors --dir $(PWD)/cov-build --html-output $(PWD)/cov-build/c/output/errors/

clean:
	rm -rf *.o *.lo *.la

###############################################################################
