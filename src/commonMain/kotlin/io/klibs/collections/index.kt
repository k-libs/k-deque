package io.klibs.collections

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * # Deque of Values
 *
 * Creates a new deque instance wrapping the given values.
 *
 * The returned deque will have a size and capacity equal to the number of
 * values passed to this function.
 *
 * **Example**
 * ```
 * val deque = dequeOf(1, 2, 3, 4, 5)
 *
 * deque // Deque{1, 2, 3, 4, 5}
 * ```
 *
 * @param values Values to pre-populate the new Deque with.
 *
 * @return A new [Deque] instance wrapping the given values.
 */
@JsExport
@OptIn(ExperimentalJsExport::class)
fun <T> dequeOf(vararg values: T): Deque<T> {
  val out = Deque<T>(values.size)

  for (v in values)
    out += v

  return out
}

fun byteDequeOf(vararg values: Byte): ByteDeque {
  val out = ByteDeque(values.size)
  for (v in values)
    out += v
  return out
}

fun shortDequeOf(vararg values: Short): ShortDeque {
  val out = ShortDeque(values.size)
  for (v in values)
    out += v
  return out
}

fun intDequeOf(vararg values: Int): IntDeque {
  val out = IntDeque(values.size)
  for (v in values)
    out += v
  return out
}

fun longDequeOf(vararg values: Long): LongDeque {
  val out = LongDeque(values.size)
  for (v in values)
    out += v
  return out
}

@OptIn(ExperimentalUnsignedTypes::class)
fun ubyteDequeOf(vararg values: UByte): UByteDeque {
  val out = UByteDeque(values.size)
  for (v in values)
    out += v
  return out
}

@OptIn(ExperimentalUnsignedTypes::class)
fun ushortDequeOf(vararg values: UShort): UShortDeque {
  val out = UShortDeque(values.size)
  for (v in values)
    out += v
  return out
}

@OptIn(ExperimentalUnsignedTypes::class)
fun uintDequeOf(vararg values: UInt): UIntDeque {
  val out = UIntDeque(values.size)
  for (v in values)
    out += v
  return out
}

@OptIn(ExperimentalUnsignedTypes::class)
fun ulongDequeOf(vararg values: ULong): ULongDeque {
  val out = ULongDeque(values.size)
  for (v in values)
    out += v
  return out
}
