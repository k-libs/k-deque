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

/**
 * # Deque of Bytes
 *
 * Creates a new deque instance wrapping the given byte values.
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
 * @param values Bytes to pre-populate the new Deque with.
 *
 * @return A new [ByteDeque] instance wrapping the given values.
 */
@JsExport
@OptIn(ExperimentalJsExport::class)
fun byteDequeOf(vararg values: Byte): ByteDeque {
  val out = ByteDeque(values.size)
  for (v in values)
    out += v
  return out
}

/**
 * # Deque of Shorts
 *
 * Creates a new deque instance wrapping the given short values.
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
 * @param values Shorts to pre-populate the new Deque with.
 *
 * @return A new [ShortDeque] instance wrapping the given values.
 */
@JsExport
@OptIn(ExperimentalJsExport::class)
fun shortDequeOf(vararg values: Short): ShortDeque {
  val out = ShortDeque(values.size)
  for (v in values)
    out += v
  return out
}

/**
 * # Deque of Ints
 *
 * Creates a new deque instance wrapping the given int values.
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
 * @param values Ints to pre-populate the new Deque with.
 *
 * @return A new [IntDeque] instance wrapping the given values.
 */
@JsExport
@OptIn(ExperimentalJsExport::class)
fun intDequeOf(vararg values: Int): IntDeque {
  val out = IntDeque(values.size)
  for (v in values)
    out += v
  return out
}

/**
 * # Deque of Longs
 *
 * Creates a new deque instance wrapping the given long values.
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
 * @param values Longs to pre-populate the new Deque with.
 *
 * @return A new [LongDeque] instance wrapping the given values.
 */
@JsExport
@OptIn(ExperimentalJsExport::class)
fun longDequeOf(vararg values: Long): LongDeque {
  val out = LongDeque(values.size)
  for (v in values)
    out += v
  return out
}

/**
 * # Deque of UBytes
 *
 * Creates a new deque instance wrapping the given UByte values.
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
 * @param values UByte values to pre-populate the new Deque with.
 *
 * @return A new [UByteDeque] instance wrapping the given values.
 */
@OptIn(ExperimentalUnsignedTypes::class)
fun ubyteDequeOf(vararg values: UByte): UByteDeque {
  val out = UByteDeque(values.size)
  for (v in values)
    out += v
  return out
}

/**
 * # Deque of UShorts
 *
 * Creates a new deque instance wrapping the given UShort values.
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
 * @param values UShort values to pre-populate the new Deque with.
 *
 * @return A new [UShortDeque] instance wrapping the given values.
 */
@OptIn(ExperimentalUnsignedTypes::class)
fun ushortDequeOf(vararg values: UShort): UShortDeque {
  val out = UShortDeque(values.size)
  for (v in values)
    out += v
  return out
}

/**
 * # Deque of UInts
 *
 * Creates a new deque instance wrapping the given UInt values.
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
 * @param values UInt values to pre-populate the new Deque with.
 *
 * @return A new [UIntDeque] instance wrapping the given values.
 */
@OptIn(ExperimentalUnsignedTypes::class)
fun uintDequeOf(vararg values: UInt): UIntDeque {
  val out = UIntDeque(values.size)
  for (v in values)
    out += v
  return out
}

/**
 * # Deque of ULongs
 *
 * Creates a new deque instance wrapping the given ULong values.
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
 * @param values ULong values to pre-populate the new Deque with.
 *
 * @return A new [ULongDeque] instance wrapping the given values.
 */
@OptIn(ExperimentalUnsignedTypes::class)
fun ulongDequeOf(vararg values: ULong): ULongDeque {
  val out = ULongDeque(values.size)
  for (v in values)
    out += v
  return out
}
