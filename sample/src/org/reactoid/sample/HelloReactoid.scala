package org.reactoid.sample

import org.reactoid.all._
import org.scaloid.common._
import rx.{Var, Rx}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions


/**
 * Provides utilities simulate asynchronous IO. The actual IO can be TCP, disk operation or anything you name.
 */
object AsyncIOSimulator {
  private var i = 0

  /** Simulates server connection. */
  def isValidID(str: CharSequence): Future[Boolean] = whenDirty {
    i += 1
    val v = i % 3
    Thread.sleep(2000) // simulates latency
    if (v == 0) throw new Exception("Connection Timeout")
    v == 1
  }

  def validityMessage(str: CharSequence): Future[String] =
    isValidID(str).map {
      valid => if (valid) "Valid ID" else "ID already exists"
    }.recover { case cause => cause.getMessage }

  def update[T](rx: Var[T], transform :T => T, delay: Int = 4000): Unit = {
    Future {
      while(true) {
        Thread.sleep(delay)
        rx() = transform(rx())
      }
    }
  }
}

import org.reactoid.sample.AsyncIOSimulator._

class HelloReactoid extends SActivity {
  onCreate {
    contentView = new SVerticalLayout {
      def demo1() = {
        val txt = EditText("Hello").textVar
        Button(txt() + ", world!")
      }

      def demo2() = {
        val tv = TextView()
        val txt = EditText().hint("ID").textVar
        val msgRx = validityMessage(txt()): Rx[String]
        tv.observe(msgRx)
      }

      case class Item(title: Var[String], unitPrice: Var[Double], quantity: Var[Int]) {
        val price = Rx(quantity() * unitPrice())
      }

      var cart = List(
        Item("Tomato", 0.5, 1),
        Item("Apple", 0.99, 3),
        Item("Pear", 1.05, 3),
        Item("Strawberry", 3.0, 1)
      )
      update(cart(2).unitPrice, (_:Double) * 0.95)

      def demo3() = {
        cart.foreach {
          i =>
            TextView(i.title)
            EditText(i.unitPrice)
            EditText(i.quantity)
            TextView(i.price)
        }
      }

      demo3()
    } padding 20.dip
  }
}
