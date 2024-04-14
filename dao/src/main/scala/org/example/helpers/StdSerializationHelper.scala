package org.example.helpers

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

object StdSerializationHelper extends SerializationHelper {
  override def serialize(value: Any): Array[Byte] = {
    val stream: ByteArrayOutputStream = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(stream)
    oos.writeObject(value)
    oos.close()

    stream.toByteArray
  }

  override def deserialize(serializedBytes: Array[Byte]): Any = {
    val ois = new ObjectInputStream(new ByteArrayInputStream(serializedBytes))
    val value = ois.readObject
    ois.close()

    value
  }
}
