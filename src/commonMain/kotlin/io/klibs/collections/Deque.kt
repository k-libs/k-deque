package io.klibs.collections

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * # Generic Deque
 *
 * Deque type that accepts a generic type of values.
 *
 * @param T Type the values held by this Deque instance.
 *
 * @author Elizabeth Paige Harper - https://github.com/foxcapades
 * @since v0.1.0
 *
 * @constructor Constructs a new Deque instance.
 *
 * @param initialCapacity Initial capacity of the internal data container
 * backing the created deque.
 *
 * Up to this many values may be added to the deque before the internal data
 * container needs to be resized and reallocated.
 *
 * @param scaleFactor Internal storage reallocation scale factor.
 *
 * If/when this Deque determines it needs to resize its internal data
 * container to hold additional values being pushed, the internal data
 * container will be resized at a rate of `capacity * scaleFactor`.
 *
 * If the scale factor returns a value that is equal to or less than the
 * current capacity, the resize will fall back to a size of `capacity + 1`.
 *
 * **Default**: `1.5`
 *
 * @param maxSize Maximum size this deque can grow to.
 *
 * If adding a value to this Deque would push the size or capacity of the
 * deque to be greater than [maxSize] then an exception will be thrown.
 *
 * **Default**: `2,147,483,647` (Int.MAX_VALUE)
 */
@JsExport
@OptIn(ExperimentalJsExport::class)
open class Deque<T>(
  initialCapacity: Int = 16,
  val scaleFactor: Float = 1.5F,
  val maxSize: Int = 2_147_483_647,
) {

  protected var buffer = arrayOfNulls<Any>(initialCapacity)

  /**
   * Index of the 'first' element in the deque, which may or may not be the
   * first element in the backing array.
   *
   * When the deque is accessed by a given index (external index), the
   * [realHead] value is used to calculate the actual index (internal index).
   *
   * In the following examples, the `|` character represents the [realHead]
   * position and the value underneath it is the actual [realHead] value for the
   * example.
   *
   * *Compacted*
   * ```
   * Deque{1, 2, 3, 4, 5, 6, 0, 0, 0}
   *       |
   *       0
   * ```
   *
   * *Uncompacted*
   * ```
   * Deque{4, 5, 6, 0, 0, 0, 1, 2, 3}
   *                         |
   *                         6
   * ```
   */
  protected var realHead = 0

  /**
   * Indicator whether the data in the buffer is currently 'in line' or, in
   * other words, the data does not cross over from the back to the front of the
   * buffer array.
   *
   * Example 1:
   * ```
   * buffer // [0, 0, 0, 4, 5, 6, 0]
   * require(isInline == true)
   * ```
   *
   * Example 2:
   * ```
   * buffer // [6, 0, 0, 0, 0, 4, 5]
   * require(isInline == false)
   * ```
   */
  protected inline val isInline
    get() = realHead + size < buffer.size

  /**
   * Currently allocated capacity.
   *
   * Values may be added to this deque until [size] == [capacity] before the
   * deque will reallocate a larger backing buffer.
   *
   * **Example**
   * ```
   * // Create a deque with an initial capacity value of `9`
   * val deque = Deque(9)        // deque == {0, 0, 0, 0, 0, 0, 0, 0, 0}
   *
   * // Even though we have not yet inserted any elements, the capacity is 9
   * assert(deque.capacity == 9)
   *
   * // Add some elements to the deque
   * deque += [1, 2, 3, 4, 5, 6] // deque == {1, 2, 3, 4, 5, 6, 0, 0, 0}
   *
   * // Deque capacity will still be 9 as we have not yet inserted enough
   * // elements to require a capacity increase
   * assert(deque.capacity == 9)
   *
   * // Increase the capacity to 12
   * deque.ensureCapacity(12)    // deque == {1, 2, 3, 4, 5, 6, 0, 0, 0, 0, 0, 0}
   * ```
   *
   * The backing buffer is always increased to at minimum hold the number of
   * values being inserted, but the rate of increase may scale the size of the
   * capacity faster than that.
   *
   * This is to avoid many repeated re-allocations of the backing container.
   * To put it simply, it would be very expensive to use a deque (or [ArrayList]
   * for that matter) if every new element required a resize.
   */
  val capacity
    get() = buffer.size

  /**
   * Number of elements in this deque.
   *
   * **Example**
   * ```
   * // Create a deque with an initial capacity value of `9`
   * val deque = Deque(9)        // deque == {0, 0, 0, 0, 0, 0, 0, 0, 0}
   *
   * // Deque size will be 0 as no elements have been inserted.
   * assert(deque.size == 0)
   *
   * // Add some elements to the deque
   * deque += [1, 2, 3, 4, 5, 6] // deque == {1, 2, 3, 4, 5, 6, 0, 0, 0}
   *
   * // Deque size will now be 6 as we appended 6 elements to the empty deque.
   * assert(deque.size == 6)
   * ```
   */
  var size = 0
    private set

  /**
   * The amount of space left in this queue's currently allocated backing data
   * container.
   *
   * Inserting a number of elements greater than [freeSpace] into this queue
   * will cause the deque's backing container to be resized to accommodate the
   * new values.
   *
   * This value is calculated as `capacity - size`.
   */
  val freeSpace
    get() = buffer.size - size

  /**
   * Index of the last element in this deque.
   *
   * If this deque is empty, this value will be `-1`.
   */
  inline val lastIndex
    get() = size - 1

  /**
   * Removes the first item from this deque and returns it.
   *
   * **Example**
   * ```
   * val deque = dequeOf(1, 2)
   *
   * assert(deque.popFirst() == 1)
   * assert(deque.popFirst() == 2)
   * deque.popFirst() // Exception!
   * ```
   *
   * @return The value that was previously the first item in this deque.
   *
   * @throws NoSuchElementException If this deque is empty.
   */
  fun popFirst() = if (isEmpty()) throw NoSuchElementException() else unsafePopFirst()

  /**
   * Removes and returns the first item in this deque or returns `null` if this
   * deque is empty.
   *
   * **Example**
   * ```
   * val deque = dequeOf(1)
   *
   * assert(deque.popFirstOrNull() == 1)
   * assert(deque.popFirstOrNull() == null)
   * ```
   *
   * @return Either the value that was previously the first item in this deque,
   * if this deque was non-empty, or `null` if this deque was empty.
   */
  fun popFirstOrNull() = if (isEmpty()) null else unsafePopFirst()

  /**
   * Removes and returns the first item in this deque or returns [value] if this
   * deque is empty.
   *
   * **Example**
   * ```
   * val deque = dequeOf(1)
   *
   * assert(deque.popFirstOr(6) == 1)
   * assert(deque.popFirstOr(6) == 6)
   * ```
   *
   * @param value Alternative or default value to return when this deque is
   * empty.
   *
   * @return Either the value that was previously the first item in this deque,
   * if this deque was non-empty, or [value] if this deque was empty.
   */
  fun popFirstOr(value: T) = if (isEmpty()) value else unsafePopFirst()

  /**
   * Removes and returns the first item in this deque or returns the value
   * returned by [fn] if this deque is empty.
   *
   * **Example**
   * ```
   * val deque = dequeOf(1)
   *
   * assert(deque.popFirstOrGet { 6 } == 1)
   * assert(deque.popFirstOrGet { 6 } == 6)
   * ```
   *
   * @param fn Function that will be used to retrieve the value to return when
   * this deque is empty.
   *
   * This function will not be called if this deque is not empty.
   *
   * @return Either the value that was previously the first item in this deque,
   * if this deque was non-empty, or the value returned by [fn] if this deque
   * was empty.
   */
  fun popFirstOrGet(fn: () -> T) = if (isEmpty()) fn() else unsafePopFirst()

  /**
   * Removes the first item from this deque and returns it.
   *
   * **Example**
   * ```
   * val deque = dequeOf(1)
   *
   * assert(deque.popFirstOrThrow { RuntimeException() } == 1)
   * deque.popFirstOrThrow { RuntimeException() } // Exception!
   * ```
   *
   * @param fn Function that will be used to retrieve the exception to throw
   * when this deque is empty.
   *
   * This function will not be called if this deque is not empty.
   *
   * @return The value that was previously the first item in this deque.
   *
   * @throws Throwable If this deque is empty.
   */
  fun popFirstOrThrow(fn: () -> Throwable) = if (isEmpty()) throw fn() else unsafePopFirst()

  @Suppress("UNCHECKED_CAST")
  private fun unsafePopFirst(): T {
    val c = buffer[realHead] as T

    buffer[realHead] = null
    realHead = incremented(realHead)
    size--

    return c
  }

  /**
   * Returns the first item in this deque.
   *
   * **Example**
   * ```
   * val deque = dequeOf(1)
   *
   * assert(deque.peekFirst() == 1)
   * ```
   *
   * @return The first item in this deque.
   *
   * @throws NoSuchElementException If this deque is empty.
   */
  fun peekFirst() = if (isEmpty()) throw NoSuchElementException() else unsafePeekFirst()

  /**
   * Returns the first item in this deque if it is not empty, otherwise returns
   * `null`.
   *
   * **Example**
   * ```
   * val deque = dequeOf(1)
   *
   * assert(deque.peekFirstOrNull() == 1)
   *
   * deque.clear()
   *
   * assert(deque.peekFirstOrNull() == null)
   * ```
   *
   * @return Either the first item in this deque if this deque is non-empty, or
   * `null` if this deque is empty.
   */
  fun peekFirstOrNull() = if (isEmpty()) null else unsafePeekFirst()

  /**
   * Returns the first item in this deque if it is not empty, otherwise returns
   * the given [value].
   *
   * **Example**
   * ```
   * val deque = dequeOf(1)
   *
   * assert(deque.peekFirstOr(6) == 1)
   *
   * deque.clear()
   *
   * assert(deque.peekFirstOr(6) == 6)
   * ```
   *
   * @param value Alternative or default value to return when this deque is
   * empty.
   *
   * @return Either the first item in this deque if this deque is non-empty, or
   * [value] if this deque is empty.
   */
  fun peekFirstOr(value: T) = if (isEmpty()) value else unsafePeekFirst()

  /**
   * Returns the first item in this deque if it is not empty, otherwise returns
   * the value returned by the given function.
   *
   * **Example**
   * ```
   * val deque = dequeOf(1)
   *
   * assert(deque.peekFirstOrGet { 6 } == 1)
   *
   * deque.clear()
   *
   * assert(deque.peekFirstOrGet { 6 } == 6)
   * ```
   *
   * @param fn Function that will be used to retrieve the value to return when
   * this deque is empty.
   *
   * This function will not be called if this deque is not empty.
   *
   * @return Either the first item in this deque if this deque is non-empty, or
   * the value returned by [fn] if this deque is empty.
   */
  fun peekFirstOrGet(fn: () -> T) = if (isEmpty()) fn() else unsafePeekFirst()

  /**
   * Returns the first item in this deque if it is not empty, otherwise returns
   * the value returned by the given function.
   *
   * **Example**
   * ```
   * val deque = dequeOf(1)
   *
   * assert(deque.peekFirstOrThrow { IllegalStateException() } == 1)
   *
   * deque.clear()
   *
   * deque.peekFirstOrThrow { IllegalStateException() } // Exception!
   * ```
   *
   * @param fn Function that will be used to retrieve the exception to throw
   * when this deque is empty.
   *
   * This function will not be called if this deque is not empty.
   *
   * @return The first item in this deque if this deque is non-empty.
   *
   * @throws Throwable If this deque is empty.
   */
  fun peekFirstOrThrow(fn: () -> Throwable) = if (isEmpty()) throw fn() else unsafePeekFirst()

  @Suppress("UNCHECKED_CAST")
  private fun unsafePeekFirst() = buffer[realHead] as T

  /**
   * Removes the last item from this deque and returns it.
   *
   * **Example**
   * ```
   * val deque = dequeOf(1, 2)
   *
   * assert(deque.popLast() == 2)
   * assert(deque.popLast() == 1)
   * deque.popLast() // Exception!
   * ```
   *
   * @return The value that was previously the last item in this deque.
   *
   * @throws NoSuchElementException If this deque is empty.
   */
  fun popLast() = if (isEmpty()) throw NoSuchElementException() else unsafePopLast()

  /**
   * Removes and returns the last item in this deque or returns `null` if this
   * deque is empty.
   *
   * **Example**
   * ```
   * val deque = dequeOf(1)
   *
   * assert(deque.popLastOrNull() == 1)
   * assert(deque.popLastOrNull() == null)
   * ```
   *
   * @return Either the value that was previously the last item in this deque,
   * if this deque was non-empty, or `null` if this deque was empty.
   */
  fun popLastOrNull() = if (isEmpty()) null else unsafePopLast()

  /**
   * Removes and returns the last item in this deque or returns [value] if this
   * deque is empty.
   *
   * **Example**
   * ```
   * val deque = dequeOf(1)
   *
   * assert(deque.popLastOr(6) == 1)
   * assert(deque.popLastOr(6) == 6)
   * ```
   *
   * @param value Alternative or default value to return when this deque is
   * empty.
   *
   * @return Either the value that was previously the last item in this deque,
   * if this deque was non-empty, or [value] if this deque was empty.
   */
  fun popLastOr(value: T) = if (isEmpty()) value else unsafePopLast()

  /**
   * Removes and returns the last item in this deque or returns the value
   * returned by [fn] if this deque is empty.
   *
   * **Example**
   * ```
   * val deque = dequeOf(1)
   *
   * assert(deque.popLastOrGet { 6 } == 1)
   * assert(deque.popLastOrGet { 6 } == 6)
   * ```
   *
   * @param fn Function that will be used to retrieve the value to return when
   * this deque is empty.
   *
   * This function will not be called if this deque is not empty.
   *
   * @return Either the value that was previously the last item in this deque,
   * if this deque was non-empty, or the value returned by [fn] if this deque
   * was empty.
   */
  fun popLastOrGet(fn: () -> T) = if (isEmpty()) fn() else unsafePopLast()

  /**
   * Removes the last item from this deque and returns it.
   *
   * **Example**
   * ```
   * val deque = dequeOf(1)
   *
   * assert(deque.popLastOrThrow { RuntimeException() } == 1)
   * deque.popLastOrThrow { RuntimeException() } // Exception!
   * ```
   *
   * @param fn Function that will be used to retrieve the exception to throw
   * when this deque is empty.
   *
   * This function will not be called if this deque is not empty.
   *
   * @return The value that was previously the last item in this deque.
   *
   * @throws Throwable If this deque is empty.
   */
  fun popLastOrThrow(fn: () -> Throwable) = if (isEmpty()) throw fn() else unsafePopLast()

  @Suppress("UNCHECKED_CAST")
  private fun unsafePopLast(): T {
    val i = internalIndex(lastIndex)
    val c = buffer[i]

    buffer[i] = null
    size--

    return c as T
  }

  /**
   * Returns the last item in this deque.
   *
   * **Example**
   * ```
   * val deque = dequeOf(1, 2)
   *
   * assert(deque.peekLast() == 2)
   * ```
   *
   * @return The last item in this deque.
   *
   * @throws NoSuchElementException If this deque is empty.
   */
  fun peekLast() = if (isEmpty()) throw NoSuchElementException() else unsafePeekLast()

  /**
   * Returns the last item in this deque if it is not empty, otherwise returns
   * `null`.
   *
   * **Example**
   * ```
   * val deque = dequeOf(1)
   *
   * assert(deque.peekLastOrNull() == 1)
   *
   * deque.clear()
   *
   * assert(deque.peekLastOrNull() == null)
   * ```
   *
   * @return Either the last item in this deque if this deque is non-empty, or
   * `null` if this deque is empty.
   */
  fun peekLastOrNull() = if (isEmpty()) null else unsafePeekLast()

  /**
   * Returns the last item in this deque if it is not empty, otherwise returns
   * the given [value].
   *
   * **Example**
   * ```
   * val deque = dequeOf(1)
   *
   * assert(deque.peekLastOr(6) == 1)
   *
   * deque.clear()
   *
   * assert(deque.peekLastOr(6) == 6)
   * ```
   *
   * @param value Alternative or default value to return when this deque is
   * empty.
   *
   * @return Either the last item in this deque if this deque is non-empty, or
   * [value] if this deque is empty.
   */
  fun peekLastOr(value: T) = if (isEmpty()) value else unsafePeekLast()

  /**
   * Returns the last item in this deque if it is not empty, otherwise returns
   * the value returned by the given function.
   *
   * **Example**
   * ```
   * val deque = dequeOf(1)
   *
   * assert(deque.peekLastOrGet { 6 } == 1)
   *
   * deque.clear()
   *
   * assert(deque.peekLastOrGet { 6 } == 6)
   * ```
   *
   * @param fn Function that will be used to retrieve the value to return when
   * this deque is empty.
   *
   * This function will not be called if this deque is not empty.
   *
   * @return Either the last item in this deque if this deque is non-empty, or
   * the value returned by [fn] if this deque is empty.
   */
  fun peekLastOrGet(fn: () -> T) = if (isEmpty()) fn() else unsafePeekLast()

  /**
   * Returns the last item in this deque if it is not empty, otherwise returns
   * the value returned by the given function.
   *
   * **Example**
   * ```
   * val deque = dequeOf(1)
   *
   * assert(deque.peekLastOrThrow { IllegalStateException() } == 1)
   *
   * deque.clear()
   *
   * deque.peekLastOrThrow { IllegalStateException() } // Exception!
   * ```
   *
   * @param fn Function that will be used to retrieve the exception to throw
   * when this deque is empty.
   *
   * This function will not be called if this deque is not empty.
   *
   * @return The last item in this deque if this deque is non-empty.
   *
   * @throws Throwable If this deque is empty.
   */
  fun peekLastOrThrow(fn: () -> Throwable) = if (isEmpty()) throw fn() else unsafePeekLast()

  @Suppress("UNCHECKED_CAST")
  private fun unsafePeekLast() = buffer[internalIndex(lastIndex)] as T

  /**
   * Pushes the given value onto the front of this deque.
   *
   * **Example**
   * ```
   * val deque = dequeOf(1, 2, 3) // Deque{1, 2, 3}
   * deque.pushFirst(4)           // Deque{4, 1, 2, 3}
   * ```
   *
   * @param value Value to push onto the front of this deque.
   */
  fun pushFirst(value: T) {
    ensureCapacity(size + 1)
    realHead = decremented(realHead)
    buffer[realHead] = value
    size++
  }

  /**
   * Pushes the given value onto the front of this deque.
   *
   * **Example**
   * ```
   * val deque = dequeOf(1, 2, 3) // Deque{1, 2, 3}
   * deque.prepend(4)             // Deque{4, 1, 2, 3}
   * ```
   *
   * @param value Value to push onto the front of this deque.
   */
  inline fun prepend(value: T) = pushFirst(value)

  /**
   * Pushes the given value onto the back of this deque.
   *
   * **Example**
   * ```
   * val deque = dequeOf(1, 2, 3) // Deque{1, 2, 3}
   * deque.pushLast(4)            // Deque{1, 2, 3, 4}
   * ```
   *
   * @param value Value to push onto the back of this deque.
   */
  fun pushLast(value: T) {
    ensureCapacity(size + 1)
    buffer[internalIndex(size)] = value
    size++
  }

  /**
   * Pushes the given value onto the back of this deque.
   *
   * **Example**
   * ```
   * val deque = dequeOf(1, 2, 3) // Deque{1, 2, 3}
   * deque.append(4)              // Deque{1, 2, 3, 4}
   * ```
   *
   * @param value Value to push onto the back of this deque.
   */
  inline fun append(value: T) = pushLast(value)

  /**
   * Inserts the given value into this deque at the specified index.
   *
   * **Example**
   * ```
   * val deque = dequeOf(1, 2, 3, 4) // Deque{1, 2, 3, 4}
   * deque.insert(2, 9)              // Deque{1, 2, 9, 3, 4}
   * ```
   *
   * @param index Index at which the value should be inserted.
   *
   * Must be a positive value between zero and [size] (inclusive).
   *
   * @param value Value to insert.
   *
   * @throws IndexOutOfBoundsException If the given index is less than zero or
   * greater than [size].
   */
  fun insert(index: Int, value: T) {
    // If they want to insert at the front, then that's just a prepend call.
    if (index == 0)
      return pushFirst(value)

    // If they want to insert in the position after last, then that's just an
    // append call.
    if (index == size)
      return pushLast(value)

    // If the size is out of bounds, throw an exception.
    if (index !in 0 .. size)
      throw IndexOutOfBoundsException("Attempted to insert a value at position $index of a Deque of size $size")

    // Make sure we have room for the new value.
    ensureCapacity(size + 1)

    // If the given index is closer to the head of the queue than the tail
    if (index < (size + 1) shr 1) {
      /* The insertion index is closer to the head of the deque than the tail,
       * so we will be shifting the front portion of the queue 'back' by one
       * index to make room for the new value. */

      /* Figure out the new head position by rolling back by one.  Since the
       * current head position may be at 0, this may put the head at the tail
       * end of the backing array. */
      val newHead = decremented(realHead)

      /* Figure out the insertion index for the new value.  We are moving data
       * backwards to make room for it so decrement it by one position.  (Again,
       * this may roll it around to the back of the backing array from position
       * `0`). */
      val insert  = decremented(internalIndex(index))

      /* If the insertion position is greater than or equal to the current head
       * position, then all but possibly the first element have stayed on the
       * same side of the backing array, meaning we can do a simple array copy
       * to move the data backwards (except for the head which _may_ have rolled
       * around to the tail of the backing array)
       *
       * Examples:
       * (| = old head, ^ = insert position)
       *
       *  |
       * [1, 2, 3, 4, 5, _]  // Before
       * [2, 3, _, 4, 5, 1]  // After
       *        ^
       *     |
       * [_, 1, 2, 3, 4, 5]  // Before
       * [1, 2, 3, _, 4, 5]  // After
       *           ^
       *        |
       * [5, _, 1, 2, 3, 4]  // Before
       * [5, 1, 2, 3, _, 4]  // After
       *              ^
       *           |
       * [4, 5, _, 1, 2, 3]  // Before
       * [4, 5, 1, 2, 3, _]  // After
       *                 ^
       */
      if (insert >= realHead) {

        // Because the head of the deque may have rolled around to the tail end
        // of the backing buffer, we will do that copy first as a separate step.
        buffer[newHead] = buffer[realHead]

        // New we can copy everything else that is moving back one position.
        // 1. target = container (we're copying to the same target)
        // 2. offset = realHead (we're copying everything after the current head
        //             backwards 1 spot so the old head will now contain the
        //             value that was at head + 1)
        // 3. start  = realHead + 1 (the first value we will copy back 1, so it
        //             will now be in the old head position)
        // 4. end    = insert position + 1 (exclusive index of the last value
        //             to copy, so everything from `start` to 1 before this
        //             index will be shifted backwards by 1.
        buffer.copyInto(buffer, realHead, realHead + 1, insert + 1)
      }

      /* If the insertion position is less than the current head position, then
       * the backing array is or will go all wonky, and we need to do multiple
       * array copies to shift everything around.
       *
       * Example
       *
       * This example deque will be used throughout the comments in the code in
       * this block to detail the steps to get from the "Before" state to the
       * "After" state.
       *
       * (| = old head, ^ = insert position)
       *
       *              |
       * [3, 4, 5, _, 1, 2]  // Before
       * [4, _, 5, 1, 2, 3]  // After
       *     ^
       */
      else {

        /* Since our head is somewhere near the tail end of the backing buffer,
         * and our tail is somewhere before that, we know that the current empty
         * slot(s) in our buffer are somewhere between the tail and head.
         *
         * So we can safely shift everything from the right of the blank space
         * to the end of the buffer left one position.  This will leave the last
         * slot in the buffer empty.
         *
         * Before = [3, 4, 5, _, 1, 2]
         * After  = [3, 4, 5, 1, 2, _]
         *
         * 1. target = buffer (we're copying from and to the same array)
         * 2. offset = the position right before our current deque 'head'
         *             element.  This means all the data will be moved left by
         *             one slot.
         * 3. start  = start with the element currently at the head of the
         *             deque.
         * 4. end    = the position after the last index in the buffer array.
         */
        buffer.copyInto(buffer, realHead - 1, realHead, buffer.size)

        /* Now copy the value from the front of the backing buffer to the
         * 'previous' buffer slot (meaning roll it around to the end).
         *
         * Before = [3, 4, 5, 1, 2, _]
         * After  = [_, 4, 5, 1, 2, 3]
         */
        buffer[buffer.size - 1] = buffer[0]

        /* Now, move any values on the front portion of the buffer between (inc)
         * position 1 and the insert position.  If the insert position happens
         * to fall on position 0 in the buffer, nothing will be moved.
         *
         * When we are done the target insert location will be known to be or
         * have been made to be empty.
         *
         * Before = [_, 4, 5, 1, 2, 3]
         * After  = [4, _, 5, 1, 2, 3]
         */
        if (insert > 0)
          /* 1. target = buffer (we're copying from and to the same array)
           * 2. offset = 0, we want to shift values left to fill the first slot
           *             in the buffer, only shifting those values that are from
           *             slot 1 to the insert slot, leaving the insert slot
           *             'empty'.
           * 3. start  = 1, start copying from position 1
           * 4. stop   = one past the insert position
           */
          buffer.copyInto(buffer, 0, 1, insert + 1)
      }

      // At this point, one way or another, we have moved the data around in our
      // internal buffer to make room for the item we want to insert.

      // Set our new value.
      buffer[insert] = value

      // Update the internal head position
      realHead = newHead
    }

    // If the given index is closer to the tail of the deque than the head
    else {
      // The insertion index is closer to the tail end of the deque, so we will
      // be shifting data at the back of the deque left one position to make
      // room for the new value being inserted.

      // Position of the value we will be inserting
      val insert = internalIndex(index)

      // New position of the last value in the deque
      val newTail = internalIndex(size)

      /* If the insertion index is before the new tail position, then we can
       * safely move the current values in the backing array forward one slot
       * in one array copy.
       *
       * Examples
       * (| = new tail, ^ = insert position)
       *
       *                 |
       * [1, 2, 3, 4, 5, _]  // Before
       * [1, 2, 3, _, 4, 5]  // After
       *           ^
       *              |
       * [2, 3, 4, 5, _, 1]  // Before
       * [2, 3, _, 4, 5, 1]  // After
       *        ^
       *           |
       * [3, 4, 5, _, 1, 2]  // Before
       * [3, _, 4, 5, 1, 2]  // After
       *     ^
       *        |
       * [4, 5, _, 1, 2, 3]  // Before
       * [_, 4, 5, 1, 2, 3]  // After
       *  ^
       */
      if (insert < newTail) {

        /* Move everything from the insert point to the new tail left one slot.
         *
         * 1. target = buffer (we're copying to and from the same array)
         * 2. offset = insert + 1 (our new position is 1 after our current
         *             position)
         * 3. start  = insert (start copying from the position we need to clear
         *             to insert the new value)
         * 4. end    = newTail (exclusive end location, so we want to copy every
         *             thing from `insert` until 1 before the new tail position
         *             to the left one slot.
         */
        buffer.copyInto(buffer, insert + 1, insert, newTail)
      }

      /* If the insertion index is after the new tail position, then the backing
       * array is or will go all wonky and we have to do multiple array copies
       * to get everything sorted out.
       *
       * Example
       *
       * The following example deque will be used in the comments in the else
       * block below to help visualize the steps that are being taken on the
       * backing array as they happen.
       *
       * (| = new tail, ^ = insert position)
       *
       *        |
       * [6, 7, _, 1, 2, 3, 4, 5]  // Before
       * [5, 6, 7, 1, 2, 3, _, 4]  // After
       *                    ^
       */
      else {

        /* Move the data at the head of our backing array left by one slot
         *
         * Before = [6, 7, _, 1, 2, 3, 4, 5]
         * After  = [_, 6, 7, 1, 2, 3, 4, 5]
         */
        buffer.copyInto(buffer, 1, 0, newTail)

        /* Move the value from the end of the buffer to the front of the buffer.
         *
         * Before = [_, 6, 7, 1, 2, 3, 4, 5]
         * After  = [5, 6, 7, 1, 2, 3, 4, _]
         */
        buffer[0] = buffer[buffer.size - 1]

        /* Move the remaining items that are after our insert position at the
         * tail of the buffer left by 1 position.
         *
         * Before = [5, 6, 7, 1, 2, 3, 4, _]
         * After  = [5, 6, 7, 1, 2, 3, _, 4]
         */
        buffer.copyInto(buffer, insert + 1, insert, buffer.size - 1)
      }

      // Finally, insert our value into the now available slot.
      buffer[insert] = value
    }

    size++
  }

  /**
   * Removes and returns the element at the given index from this deque.
   *
   * **Example**
   * ```
   * val deque = dequeOf(1, 2, 3, 4, 5)
   * val value = deque.popAt(2)
   *
   * value  // 3
   * deque  // Deque{1, 2, 4, 5}
   * ```
   *
   * @param index Index of the item to remove from the deque and return.
   *
   * Indexes are zero based.
   *
   * @return The value that was previously stored in this deque at the given
   * index.
   */
  @Suppress("UNCHECKED_CAST")
  fun popAt(index: Int): T {
    // If the index is out of bounds, throw an exception.
    if (index < 0 || index > lastIndex)
      throw IndexOutOfBoundsException("Attempted to pop a value at position $index of a Deque of size $size")

    // If the index is zero then we are just popping the first item.
    if (index == 0)
      return popFirst()

    // If the index is the last index then we are just popping the last item.
    if (index == lastIndex)
      return popLast()

    // Get the internal index of the value
    val vii = internalIndex(index)

    // Get the value to return
    val out = buffer[vii]

    // If the given index is closer to the head of the deque
    if (index < (size + 1) shr 1) {
      /* The removal index is closer to the head of the deque than the tail
       * then the smallest move is to shift the items to the right of the
       * removal index to the left by one.
       *
       * Example:
       *
       * (| = removal index, _ = empty deque position)
       *
       *                 |
       * Before = [1, 2, 3, 4, 5, 6]
       * After  = [_, 1, 2, 4, 5, 6]
       */

      /* Figure out the new head position by incrementing by one.  Since the
       * current head position could be the last slot in the buffer, this may
       * result with a new head position of zero */
      val newHead = incremented(realHead)

      /* If the removal position is greater than (to the left of) the real head
       * position then we can do a simple shift with only one array copy to move
       * the data to the left by one position.
       *
       * Examples:
       * (| = old head, v = removal index, _ = empty deque position)
       *
       *           |     v
       * Before = [1, 2, 3, 4, 5, 6, 7]
       * After  = [_, 1, 2, 4, 5, 6, 7]
       *
       *              |     v
       * Before = [7, 1, 2, 3, 4, 5, 6]
       * After  = [7, _, 1, 2, 4, 5, 6]
       *
       *                 |     v
       * Before = [6, 7, 1, 2, 3, 4, 5]
       * After  = [6, 7, _, 1, 2, 4, 5]
       *
       *                    |     v
       * Before = [5, 6, 7, 1, 2, 3, 4]
       * After  = [5, 6, 7, _, 1, 2, 4]
       *
       *                       |     v
       * Before = [4, 5, 6, 7, 1, 2, 3]
       * After  = [4, 5, 6, 7, _, 1, 2]
       */
      if (vii > realHead) {
        // Shift the data to the left by one slot
        buffer.copyInto(buffer, newHead, realHead, vii)
      }

      /* Else, if the removal position is less than (to the right of) the real
       * head position, then we have to do a couple of shifts to sort ourselves
       * out.
       *
       * Examples:
       * (| = old head, v = removal index, _ = empty deque position)
       *
       *           v           |
       * Before = [3, 4, 5, 6, 1, 2]
       * After  = [2, 4, 5, 6, _, 1]
       *
       *              v           |
       * Before = [2, 3, 4, 5, 6, 1]
       * After  = [1, 2, 4, 5, 6, _]
       */
      else {
        // Shift the data on the right of the buffer left by one position.  This
        // will leave the first position in the buffer empty.
        buffer.copyInto(buffer, 1, 0, vii)

        // Copy the value from the tail of the buffer around to the head of the
        // buffer to fill the position we just emptied.
        buffer[0] = buffer[buffer.size - 1]

        // If there are still values to shift on the left side of the buffer
        if (realHead < buffer.size - 1)
          // Shift the deque head values that are at the tail end of the buffer
          // to the left by 1 to fill the slot that was emptied by copying the
          // buffer tail around to the buffer head.
          buffer.copyInto(buffer, realHead + 1, realHead, buffer.size - 1)
      }

      // Clear the previous head slot
      buffer[realHead] = null

      // Adjust the head position
      realHead = newHead
    }

    // Else, if the given index is closer to the tail of the deque (or is in the
    // middle)
    else {
      /* The removal index is closer to the tail of the deque than the head, so
       * the smallest move is to shift the items to the left of the removal
       * index right by one.
       *
       * Examples:
       * (| = old head, v = removal index, _ = empty deque position)
       *
       *           |           v
       * Before = [1, 2, 3, 4, 5, 6, 7, 8, 9]
       * After  = [1, 2, 3, 4, 6, 7, 8, 9, _]
       *
       *              |           v
       * Before = [9, 1, 2, 3, 4, 5, 6, 7, 8]
       * After  = [_, 1, 2, 3, 4, 6, 7, 8, 9]
       *
       *                 |           v
       * Before = [8, 9, 1, 2, 3, 4, 5, 6, 7]
       * After  = [9, _, 1, 2, 3, 4, 6, 7, 8]
       *
       *                    |           v
       * Before = [7, 8, 9, 1, 2, 3, 4, 5, 6]
       * After  = [8, 9, _, 1, 2, 3, 4, 6, 7]
       *
       *                       |           v
       * Before = [6, 7, 8, 9, 1, 2, 3, 4, 5]
       * After  = [7, 8, 9, _, 1, 2, 3, 4, 6]
       *
       *           v              |
       * Before = [5, 6, 7, 8, 9, 1, 2, 3, 4]
       * After  = [6, 7, 8, 9, _, 1, 2, 3, 4]
       *
       *              v              |
       * Before = [4, 5, 6, 7, 8, 9, 1, 2, 3]
       * After  = [4, 6, 7, 8, 9, _, 1, 2, 3]
       *
       *                 v              |
       * Before = [3, 4, 5, 6, 7, 8, 9, 1, 2]
       * After  = [3, 4, 6, 7, 8, 9, _, 1, 2]
       *
       *                    v              |
       * Before = [2, 3, 4, 5, 6, 7, 8, 9, 1]
       * After  = [2, 3, 4, 6, 7, 8, 9, _, 1]
       */

      val oldTail = internalIndex(lastIndex)

      /* If the removal position is before (to the right of) the real tail of
       * the deque, then we can do a simple shift to sort things out.
       *
       * Examples:
       * (| = old tail, v = removal index, _ = empty deque position)
       *
       *           v           |
       * Before = [5, 6, 7, 8, 9, 1, 2, 3, 4]
       * After  = [6, 7, 8, 9, _, 1, 2, 3, 4]
       *
       *              v           |
       * Before = [4, 5, 6, 7, 8, 9, 1, 2, 3]
       * After  = [4, 6, 7, 8, 9, _, 1, 2, 3]
       *
       *                 v           |
       * Before = [3, 4, 5, 6, 7, 8, 9, 1, 2]
       * After  = [3, 4, 6, 7, 8, 9, _, 1, 2]
       *
       *                    v           |
       * Before = [2, 3, 4, 5, 6, 7, 8, 9, 1]
       * After  = [2, 3, 4, 6, 7, 8, 9, _, 1]
       *
       *                       v           |
       * Before = [1, 2, 3, 4, 5, 6, 7, 8, 9]
       * After  = [1, 2, 3, 4, 6, 7, 8, 9, _]
       */
      if (vii < oldTail) {
        buffer.copyInto(buffer, vii, vii + 1, oldTail + 1)
      }

      /* If the removal position is after (to the left of) the real tail of the
       * deque, then we have to do a couple of copies to sort the buffer out.
       *
       * Examples:
       * (| = old tail, v = removal index, _ = empty deque position)
       *
       *           |              v
       * Before = [9, 1, 2, 3, 4, 5, 6, 7, 8]
       * After  = [_, 1, 2, 3, 4, 6, 7, 8, 9]
       *
       *              |              v
       * Before = [8, 9, 1, 2, 3, 4, 5, 6, 7]
       * After  = [9, _, 1, 2, 3, 4, 6, 7, 8]
       *
       *                 |              v
       * Before = [7, 8, 9, 1, 2, 3, 4, 5, 6]
       * After  = [8, 9, _, 1, 2, 3, 4, 6, 7]
       *
       *                    |              v
       * Before = [6, 7, 8, 9, 1, 2, 3, 4, 5]
       * After  = [7, 8, 9, _, 1, 2, 3, 4, 6]
       */
      else {
        /* Step one is to shift the data on the left side of the deletion index
         * to the right by one.  This will leave an empty slot at the end of the
         * internal buffer.
         *
         * Before = [8, 9, 1, 2, 3, 4, 5, 6, 7]
         * After  = [8, 9, 1, 2, 3, 4, 6, 7, _]
         */
        buffer.copyInto(buffer, vii, vii + 1, buffer.size)

        /* Step 2 is to copy the value from the head of the buffer to the tail
         * of the buffer continue the shift
         *
         * Before = [8, 9, 1, 2, 3, 4, 6, 7, _]
         * After  = [_, 9, 1, 2, 3, 4, 6, 7, 8]
         */
        buffer[buffer.size - 1] = buffer[0]

        /**
         * Step 3 is to shift any remaining deque tail values at the head of the
         * buffer to the right by one to move the blank space between the deque
         * tail and deque head.
         *
         * Before = [_, 9, 1, 2, 3, 4, 6, 7, 8]
         * After  = [9, _, 1, 2, 3, 4, 6, 7, 8]
         */
        if (oldTail > 0)
          buffer.copyInto(buffer, 0, 1, oldTail + 1)
      }

      // Clear the old tail position
      buffer[oldTail] = null
    }

    // Reduce the size by 1
    size--

    return out as T
  }

  /**
   * Gets the value at the given index from this deque.
   *
   * If the given [index] is less than zero or greater than [lastIndex] an
   * exception will be thrown.
   *
   * @param index Index of the item to return in this deque.
   *
   * @return The item at the given index.
   *
   * @throws IndexOutOfBoundsException If [index] is less than zero or greater
   * than [lastIndex].
   */
  @Suppress("UNCHECKED_CAST")
  operator fun get(index: Int) = buffer[toValidInternalIndex(index)] as T

  /**
   * Sets the value at the given index from this deque.
   *
   * If the given [index] is less than zero or greater than [lastIndex] an
   * exception will be thrown.
   *
   * @param index Index of the item in this deque that will be replaced with
   * [value].
   *
   * @param value Value to set at the given [index].
   *
   * @throws IndexOutOfBoundsException If [index] is less than zero or greater
   * than [lastIndex].
   */
  operator fun set(index: Int, value: T) {
    buffer[toValidInternalIndex(index)] = value
  }

  inline operator fun plusAssign(value: T) = pushLast(value)

  /**
   * Returns true if this deque contains zero items.
   *
   * @return `true` if this deque contains zero items, otherwise `false`.
   */
  inline fun isEmpty() = size == 0

  /**
   * Returns true if this deque contains one or more items.
   *
   * @return `true` if this deque contains one or more items, otherwise `false`.
   */
  inline fun isNotEmpty() = size != 0

  /**
   * Tests whether this deque is currently at capacity, or in other words is the
   * number of items in this deque equal to the size of the underlying data
   * container.
   *
   * If [atCapacity] returns true, adding more items to this deque will cause it
   * to attempt to increase the size of its underlying data container.
   *
   * @return `true` if this deque is currently at capacity, otherwise `false`.
   */
  inline fun atCapacity() = size == capacity

  /**
   * Removes each item off of this deque one at a time and calls the given
   * function with each removed item.
   *
   * **Example 1**
   * ```
   * val deque = dequeOf(1, 2, 3)
   *
   * // Prints:
   * // 1
   * // 2
   * // 3
   * deque.popEach { println(it) }
   *
   * require(deque.size == 0)
   * ```
   *
   * **Example 2**
   * ```
   * val deque = dequeOf(1, 2, 3)
   *
   * // Prints:
   * // 3
   * // 2
   * // 1
   * deque.popEach(true) { println(it) }
   *
   * require(deque.size == 0)
   * ```
   *
   * @param reverse Whether the iteration should happen in reverse from the back
   * of the deque or forwards from the front of the deque.
   *
   * @param fn Function to call with each item removed from this deque.
   */
  fun popEach(reverse: Boolean = false, fn: (value: T) -> Unit) {
    if (reverse)
      while (isNotEmpty())
        fn(unsafePopLast())
    else
      while (isNotEmpty())
        fn(unsafePopFirst())
  }

  /**
   * Removes each item off of this deque one at a time and calls the given
   * function with each removed item along with the index of that item in the
   * deque.
   *
   *
   * **Example 1**
   * ```
   * val deque = dequeOf('a', 'b', 'c')
   *
   * // Prints:
   * // 1 -> a
   * // 2 -> b
   * // 3 -> c
   * deque.popEachIndexed { i, it -> println("$i -> $it") }
   *
   * require(deque.size == 0)
   * ```
   *
   * **Example 2**
   * ```
   * val deque = dequeOf('a', 'b', 'c')
   *
   * // Prints:
   * // 3 -> 'c'
   * // 2 -> 'b'
   * // 1 -> 'a'
   * deque.popEachIndexed(true) { i, it -> println("$i -> $it") }
   *
   * require(deque.size == 0)
   * ```
   *
   * @param reverse Whether the iteration should happen in reverse from the back
   * of the deque or forwards from the front of the deque.
   *
   * @param fn Function to call with each item removed from this deque.
   */
  fun popEachIndexed(reverse: Boolean = false, fn: (index: Int, value: T) -> Unit) {
    if (reverse) {
      var i = size
      while (isNotEmpty())
        fn(--i, unsafePopLast())
    } else {
      var i = 0
      while (isNotEmpty())
        fn(i++, unsafePopFirst())
    }
  }

  /**
   * Calls the given function with each item in this deque.
   *
   * **Example 1**
   * ```
   * val deque = dequeOf(1, 2, 3)
   *
   * // Prints:
   * // 1
   * // 2
   * // 3
   * deque.peekEach { println(it) }
   *
   * require(deque.size == 3)
   * ```
   *
   * **Example 2**
   * ```
   * val deque = dequeOf(1, 2, 3)
   *
   * // Prints:
   * // 3
   * // 2
   * // 1
   * deque.peekEach(true) { println(it) }
   *
   * require(deque.size == 3)
   * ```
   *
   * @param reverse Whether the iteration should happen in reverse from the back
   * of the deque or forwards from the front of the deque.
   *
   * @param fn Function to call with each item in this deque.
   */
  fun peekEach(reverse: Boolean = false, fn: (value: T) -> Unit) {
    if (reverse)
      for (i in lastIndex downTo 0)
        fn(get(i))
    else
      for (i in 0 .. lastIndex)
        fn(get(i))
  }

  /**
   * Calls the given function with each item in this deque and its index.
   *
   *
   * **Example 1**
   * ```
   * val deque = dequeOf('a', 'b', 'c')
   *
   * // Prints:
   * // 1 -> a
   * // 2 -> b
   * // 3 -> c
   * deque.peekEachIndexed { i, it -> println("$i -> $it") }
   *
   * require(deque.size == 3)
   * ```
   *
   * **Example 2**
   * ```
   * val deque = dequeOf('a', 'b', 'c')
   *
   * // Prints:
   * // 3 -> 'c'
   * // 2 -> 'b'
   * // 1 -> 'a'
   * deque.peekEachIndexed(true) { i, it -> println("$i -> $it") }
   *
   * require(deque.size == 3)
   * ```
   *
   * @param reverse Whether the iteration should happen in reverse from the back
   * of the deque or forwards from the front of the deque.
   *
   * @param fn Function to call with each item in this deque.
   */
  fun peekEachIndexed(reverse: Boolean, fn: (index: Int, value: T) -> Unit) {
    if (reverse)
      for (i in lastIndex downTo 0)
        fn(i, get(i))
    else
      for (i in 0 .. lastIndex)
        fn(i, get(i))
  }

  /**
   * Tests whether this deque contains at least one instance of the given value.
   *
   * @param value Value to test for the existence of.
   *
   * @return `true` if this deque contains at least one instance of the given
   * [value], otherwise `false` if the given `value` does not appear in this
   * deque.
   */
  operator fun contains(value: T): Boolean {
    for (i in 0 .. lastIndex)
      if (buffer[internalIndex(i)] == value)
        return true

    return false
  }

  /**
   * Returns an iterator over the elements in this deque.
   */
  operator fun iterator() = Iterator()

  /**
   * Returns an iterator over the elements in this deque in reverse, starting
   * from the last element and ending with the first.
   */
  fun reverseIterator() = ReverseIterator()

  /**
   * Clears all elements from this dequeue, leaving it empty, but with the same
   * allocated capacity.
   *
   * **Example**
   * ```
   * val deque = dequeOf(1, 2, 3)
   *
   * require(deque.size == 3)
   * require(deque.capacity == 3)
   *
   * deque.clear()
   *
   * require(deque.size == 0)
   * require(deque.capacity == 3)
   * ```
   */
  fun clear() {
    for (i in buffer.indices)
      buffer[i] = null

    realHead = 0
    size = 0
  }

  /**
   * Ensures that this deque has at least the given capacity allocated.
   *
   * If the current capacity of this deque is less than the given value, the
   * underlying container will be resized to have a capacity of *at least*
   * [minCapacity].
   *
   * If the current capacity of this deque is already greater than or equal to
   * the given value, this method does nothing.
   *
   * @param minCapacity Minimum capacity this deque must have.
   */
  fun ensureCapacity(minCapacity: Int) {
    when {
      // If the min capacity given is less than zero, then it is invalid
      minCapacity < 0
      -> throw IllegalArgumentException("Passed a negative value to Deque.ensureCapacity")

      // If the min capacity given is greater than the max allowed size for this
      // deque, then it is invalid.
      minCapacity > maxSize
      -> throw IllegalArgumentException("Attempted to set the min capacity of a deque to a value that is greater than the deque's max allowed size.")

      // If the min capacity given is less than or equal to the current
      // capacity, then do nothing.
      minCapacity <= buffer.size
      -> {
      }

      // If the deque is currently empty, then just quick swap the buffer with a
      // new array.
      isEmpty()
      -> buffer = arrayOfNulls(minCapacity)

      // We have data in our buffer and the given min capacity is greater than
      // our current capacity (but less than or equal to [maxSize]).  Copy the
      // data into a new, larger buffer.
      else
      -> copyElements(newCapacity(minCapacity))
    }
  }

  /**
   * Returns the contents of this deque as an array.
   *
   * @return An array of size [size] containing the contents of this deque.
   */
  fun copyToArray(provider: (size: Int, init: (i: Int) -> Unit) -> Array<T>) = provider(size, ::get)

  override fun toString() = "Deque(size=$size, capacity=$capacity)"

  override fun equals(other: Any?) = if (other is Deque<*>) buffer.contentEquals(buffer) else false

  override fun hashCode() = buffer.contentHashCode()

  /**
   * Valid Internal Index
   *
   * Returns a valid internal index for the given external index.
   *
   * If the external index is out of bounds an exception will be thrown.
   *
   * @param i External index.
   *
   * @return A valid internal index.
   *
   * @throws IndexOutOfBoundsException If the given external index is less than
   * `0` or greater than `lastIndex`.
   */
  protected inline fun toValidInternalIndex(i: Int) =
    if (i in 0 .. lastIndex)
      internalIndex(i)
    else
      throw IndexOutOfBoundsException("Attempted to access item [$i] in a deque with a size of [$size]")

  protected inline fun positiveMod(i: Int) = if (i >= buffer.size) i - buffer.size else i
  protected inline fun negativeMod(i: Int) = if (i < 0) i + buffer.size else i
  protected inline fun internalIndex(i: Int) = positiveMod(realHead + i)
  protected inline fun incremented(i: Int) = if (i == buffer.size - 1) 0 else i + 1
  protected inline fun decremented(i: Int) = if (i == 0) buffer.size - 1 else i - 1

  /**
   * Copies the data currently in the backing buffer into a new buffer of size
   * [newCapacity].
   *
   * The new buffer will be inlined, meaning the head of the buffer will be
   * moved to position `0` and the following data will fill the following
   * positions in the array without looping back around.
   *
   * **Example**
   * ```
   * newCap   = 8
   * previous = [4, 5, 6, 1, 2, 3]
   * new      = [1, 2, 3, 4, 5, 6, 0, 0]
   * ```
   *
   * This method does not check to see if the resize is necessary ahead of time
   * as it is only called when the necessity of a resize has already been
   * determined.
   *
   * @param newCapacity New capacity for [buffer].
   */
  protected fun copyElements(newCapacity: Int) {
    val new = arrayOfNulls<Any>(newCapacity)

    if (isInline) {
      buffer.copyInto(new, 0, realHead, realHead + size)
    } else {
      val spliff = buffer.size - realHead
      buffer.copyInto(new, 0, realHead, buffer.size)
      buffer.copyInto(new, spliff, 0, size - spliff)
    }

    realHead = 0
    buffer = new
  }

  /**
   * Calculate the new capacity for the deque based on the current size and the
   * given [minCapacity] value.
   *
   * @param minCapacity Minimum size that the deque must be after the capacity
   * increase.
   *
   * @return The new capacity value for the deque
   */
  protected inline fun newCapacity(minCapacity: Int) = max(min((buffer.size * scaleFactor).toInt(), maxSize), minCapacity)

  /**
   * Min Value
   *
   * Returns the lesser of the two input values.
   *
   * @param a Value 1
   *
   * @param b Value 2
   *
   * @return The lesser value of [a] and [b].
   */
  protected inline fun min(a: Int, b: Int) = if (a < b) a else b

  /**
   * Max Value
   *
   * Returns the greater of the two input values.
   *
   * @param a Value 1
   *
   * @param b Value 2
   *
   * @return The greater value of [a] and [b].
   */
  protected inline fun max(a: Int, b: Int) = if (a < b) b else a

  inner class Iterator {
    private var index = -1
    operator fun hasNext() = index < lastIndex
    operator fun next() = get(++index)
  }

  inner class ReverseIterator {
    private var index = size
    operator fun hasNext() = index > 0
    operator fun next() = get(--index)
  }
}
