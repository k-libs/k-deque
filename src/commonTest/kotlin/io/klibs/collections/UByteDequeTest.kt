package io.klibs.collections

import kotlin.test.Test
import kotlin.test.assertEquals

class UByteDequeTest {

  @Test
  @OptIn(ExperimentalUnsignedTypes::class)
  fun copyToArray_t1() {
    val tgt = ubyteDequeOf()
    assertEquals(0, tgt.copyToArray().size)
  }

  @Test
  @OptIn(ExperimentalUnsignedTypes::class)
  fun copyToArray_t2() {
    val tgt = ubyteDequeOf(1u)
    val arr = tgt.copyToArray()
    assertEquals(1, arr.size)
    assertEquals(1u, arr[0])
  }

  @Test
  @OptIn(ExperimentalUnsignedTypes::class)
  fun copyToArray_t3() {
    val tgt = ubyteDequeOf(1u, 2u)
    val arr = tgt.copyToArray()
    assertEquals(2, arr.size)
    assertEquals(1u, arr[0])
    assertEquals(2u, arr[1])
  }

}