package org.reactoid.util

import java.util.concurrent.{TimeUnit, BlockingQueue}
import java.{util => ut}

/**
 * A BlockingQueue that remembers only the latest element. This is used for dirtyCheckExecutionContext.
 */
class DirtyQueue[T <: AnyRef] extends ut.AbstractQueue[T] with BlockingQueue[T] {
  private var obj: T = null.asInstanceOf[T]

  override def peek(): T = obj

  override def offer(e: T): Boolean = {
    obj = e
    true
  }

  override def poll(): T = obj

  override def size(): Int = if (obj == null) 0 else 1

  override def iterator(): ut.Iterator[T] = {
    val list = new ut.ArrayList[T]()
    list.add(obj)
    list.iterator()
  }

  override def drainTo(c: ut.Collection[_ >: T]): Int =
    if (obj == null) 0
    else {
      obj = null.asInstanceOf[T]
      c.add(obj)
      1
    }

  override def drainTo(c: ut.Collection[_ >: T], maxElements: Int): Int =
    drainTo(c)

  override def take(): T = {
    val retObj = obj
    obj = null.asInstanceOf[T]
    retObj
  }

  override def put(e: T): Unit = offer(e)

  override def remainingCapacity(): Int = 1

  override def offer(e: T, timeout: Long, unit: TimeUnit): Boolean = offer(e)

  override def poll(timeout: Long, unit: TimeUnit): T = obj
}