/*
 * MsgPool.h
 *
 *  Created on: Aug 6, 2017
 *      Author: netanelyo
 */

#ifndef MSGPOOL_H_
#define MSGPOOL_H_

#include "Msg.h"
#include <list>
#include <unordered_map>

class MsgPool {
public:
	MsgPool(uint64_t size = poolSize);
	Msg* get(void* buff, int size, jbyteArray arr = nullptr);

	Msg* operator[](void* index) { auto x = poolMap[index]; }

private:
	std::list<Msg*> pool;
	std::unordered_map<void*, Msg*> poolMap;

	static const uint64_t poolSize = 0x200;
};


#endif /* MSGPOOL_H_ */
