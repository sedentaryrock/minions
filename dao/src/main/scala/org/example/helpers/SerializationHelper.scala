package org.example.helpers

trait SerializationHelper {
  def serialize(value: Any): Array[Byte]
  def deserialize(bytes: Array[Byte]): Any
}
