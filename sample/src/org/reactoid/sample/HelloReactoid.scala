package org.reactoid.sample

import android.content.Context
import android.view.View
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

/**
 * Typeclass that can create a view
 */
trait ViewBuilder {
  def newView: View
}

/**
 * Typeclass that can append an element
 */
trait Appender {
  def append(): Unit
}

trait DefaultViewBuilder {

  implicit class SeqViewBuilder[T, S[Q] <: Seq[Q], V <: View](seq: S[T])(implicit ev: T => ViewBuilder, ctx: Context) extends ViewBuilder {
    def newView: View =
      new SVerticalLayout {
        seq.foreach(i => this += i.newView)
      }
  }

  implicit class ListViewBuilder[T, S[Q] <: List[Q], V <: View](list: S[T])(implicit ev: T => ViewBuilder, ctx: Context) extends ViewBuilder {
    def newView: View =
      new SVerticalLayout {
        list.foreach {
          i =>
            this += i.newView
        }
      }
  }

//  implicit class RxViewBuilder[T, S[Q] <: Rx[Q], V <: View](rx: S[T])(implicit ev: T => ViewBuilder, ctx: Context) extends ViewBuilder {
//    def newView: View =
//      new SVerticalLayout {
//        this += rx().newView
//        TextView("RXVIEW")
//      }
//  }

  implicit class EditableSeqViewBuilder[T](rxSeq: Var[List[T]])(implicit viewEv: T => ViewBuilder, appender: Var[List[T]] => Appender, ctx: Context) extends ViewBuilder {
    def newView: View =
      new SVerticalLayout {
        val seq = rxSeq()
        seq.indices.foreach {
          i =>
            this += seq(i).newView
            this += new SLinearLayout {
              if (i > 0) SButton("↑", rxSeq() = seq.updated(i, seq(i - 1)).updated(i - 1, seq(i))).wrap
              if (i < seq.length - 1) SButton("↓", rxSeq() = seq.updated(i, seq(i + 1)).updated(i + 1, seq(i))).wrap
              SButton("-", rxSeq() = seq.patch(i, Nil, 1)).wrap
            }
        }
        SButton("+", rxSeq.append())
      }
  }

  implicit class EditableStringViewBuilder(varStr: Var[String])(implicit ctx: Context) extends ViewBuilder {
    def newView: View =
      new SVerticalLayout {
        Seq(1,2,3)
      }
  }

}

object DefaultViewBuilder extends DefaultViewBuilder

import DefaultViewBuilder._

class HelloReactoid extends SActivity {

  onCreate {
    contentView = new SVerticalLayout {
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
        case class Item(title: Var[String], unitPrice: Var[Double], quantity: Var[Int]) {
          val price = Rx(quantity() * unitPrice())
        }

        implicit class ItemViewBuilder(i: Item) extends ViewBuilder {
          def newView: View = {
            new SVerticalLayout {
              TextView(i.title)
              EditText(i.unitPrice)
              EditText(i.quantity)
              TextView(i.price)
            }
          }
        }

        implicit class ItemAppendable(rx: Var[List[Item]]) extends Appender {
          override def append(): Unit = ???
        }

        val cart = Var(List(
          Item("Tomato", 0.5, 1),
          Item("Apple", 0.99, 3),
          Item("Pear", 1.05, 3),
          Item("Strawberry", 3.0, 1)
        ))


        val a = Map(3 -> "a").view.map { case (k, v) => v + k }
        val b = a.force

        val lb = new ListBuffer()
        val cba = augmentString("abc").reverse
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
    } padding 20.dip
  }
}
