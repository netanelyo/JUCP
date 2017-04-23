file=$1
gcc -o $file $file".c" -lucp -lucs -I/.autodirect/mtrswgwork/netanelyo/workspace/jucx/src/ucx/install/include  -L/.autodirect/mtrswgwork/netanelyo/workspace/jucx/src/ucx/install/lib/ -Wl,-rpath,/.autodirect/mtrswgwork/netanelyo/workspace/jucx/src/ucx/install/lib/ -g -O0
