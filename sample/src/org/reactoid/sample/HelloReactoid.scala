package org.reactoid.sample

import android.content.Context
import android.view.View
import org.reactoid.{ViewBuilder, DefaultViewBuilder}
import org.reactoid.all._
import org.scaloid.common._
import rx.core.Obs
import rx.{Rx, Var}

import scala.collection.mutable.ListBuffer
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

  def update[T](job: => Unit, delay: Int = 4000): Unit = {
    Future {
      while (true) {
        Thread.sleep(delay)
        job
      }
    }
  }

  var o: Obs = null
}

import org.reactoid.sample.AsyncIOSimulator._
import DefaultViewBuilder._

class HelloReactoid extends SActivity {

  onCreate {
    contentView = new SScrollView {
      new SVerticalLayout {
        val lyout = this

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

        def demo3() = {
          case class Item(title: Var[String] = Var(""), unitPrice: Var[Double] = Var(0.0), quantity: Var[Int] = Var(1)) {
            val price = Rx(quantity() * unitPrice())
          }

          implicit class ItemViewBuilder(i: Item) extends ViewBuilder {
            def newView: View = {
              new SVerticalLayout {
                i.title.newView.here
                i.unitPrice.newView.here
                i.quantity.newView.here
                i.price.newView.here
                //                TextView(i.title)
                //                EditText(i.unitPrice)
                //                EditText(i.quantity)
                //                TextView(i.price)
              }
            }
          }

          //          implicit class ItemAppendable(rx: Var[List[Item]]) extends Appender {
          //            override def append(): Unit = rx() = rx() :+ Item()
          //          }

          val cart = Var(List(
            Item("Tomato", 0.5, 1),
            Item("Apple", 0.99, 3),
            Item("Pear", 1.05, 3),
            Item("Strawberry", 3.0, 1)
          ))

          update {
            val u = cart()(2).unitPrice
            u() = u() * 0.95
          }

          def build[T](obj: Var[T])(implicit ev: Var[T] => ViewBuilder) = {
            o = Obs(obj) {
              lyout.removeAllViews()
              lyout += obj.newView
            }
          }

          build(cart)
        }

        demo3()
      }.padding(20.dip).here
    }
  }
}
