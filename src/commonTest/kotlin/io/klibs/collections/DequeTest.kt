package io.klibs.collections

import kotlin.test.Test
import kotlin.test.assertEquals

class DequeTest {

  @Test
  fun copyToArray_t1() {
    val tgt = dequeOf<String>()
    assertEquals(0, tgt.copyToArray(::Array).size)
  }

  @Test
  fun copyToArray_t2() {
    val tgt = dequeOf("hello")
    val arr = tgt.copyToArray(::Array)
    assertEquals(1, arr.size)
    assertEquals("hello", arr[0])
  }

  @Test
  fun copyToArray_t3() {
    val tgt = dequeOf("hello", "world")
    val arr = tgt.copyToArray(::Array)
    assertEquals(2, arr.size)
    assertEquals("hello", arr[0])
    assertEquals("world", arr[1])
  }

  @Test
  fun copyToArray_t4() {
    val tgt = Deque<String>(3)
    tgt.pushFirst("henlo")
    tgt.pushFirst("little")
    tgt.pushFirst("poppet")

    val arr = tgt.copyToArray(::Array)
    assertEquals(3, arr.size)
    assertEquals("poppet", arr[0])
    assertEquals("little", arr[1])
    assertEquals("henlo", arr[2])
  }

}