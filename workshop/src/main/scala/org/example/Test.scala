package org.example

import java.util.stream.IntStream

object Test {

  def main(args: Array[String]): Unit = {
    val ints :IntStream = IntStream.rangeClosed(0, Integer.MAX_VALUE)

    ints.filter(i => i !=0).map(i => i).forEach(i => if (i == Integer.MAX_VALUE) println("Last Value " + i));
    println("This is first line")
  }

}
