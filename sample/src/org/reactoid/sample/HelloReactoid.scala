package org.reactoid.sample

import org.reactoid.all._
import org.scaloid.common._
import rx.Rx

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
}

import AsyncIOSimulator._

class HelloReactoid extends SActivity {
  def demo1() =
    onCreate {
      contentView = new SVerticalLayout {
        val txt = EditText("Hello").textVar
        Button(txt() + ", world!")
      } padding 20.dip
    }

  def demo2() =
    onCreate {
      contentView = new SVerticalLayout {
        val tv = TextView()
        val txt = EditText().hint("ID").textVar
        val msgRx = validityMessage(txt()):Rx[String]
        tv.observe(msgRx)
      } padding 20.dip
    }

  case class Item(title:Rx[String], unitPrice:Rx[Double], quantity:Rx[Int]) {
    val price = Rx(quantity() * unitPrice())
  }

  var cart = Rx(List(
    Item("Tomato", 0.5, 2),
    Item("Apple", 0.99, 3),
    Item("Pear", 1.05, 1),
    Item("Strawberry", 3.0, 1)
  ))

  def demo3() =
    onCreate {
      contentView = new SVerticalLayout {
        cart().foreach {
          i =>
            TextView(i.title())
            EditText(i.unitPrice)
            EditText(i.quantity().toString)
            TextView((i.unitPrice() * i.quantity()).toString)
        }
      } padding 20.dip
    }

  demo3()
}
