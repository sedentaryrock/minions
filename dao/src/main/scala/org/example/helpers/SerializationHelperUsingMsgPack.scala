package org.example.helpers

import org.msgpack.core.MessagePack

object SerializationHelperUsingMsgPack extends SerializationHelper {
  override def deserialize(bytes: Array[Byte]): Any = {

  }

  override def serialize(value: Any): Array[Byte] = ???
}
