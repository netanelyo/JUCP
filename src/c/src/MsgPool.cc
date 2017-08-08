

MsgPool::MsgPool(uint64_t size) : pool(size) {
	for (int i = 0; i < size ; i++)
	{
		pool[i] = new Msg(nullptr, 0);
	}
}

Msg* MsgPool::get(void* buff, int size, jbyteArray arr) {
}
