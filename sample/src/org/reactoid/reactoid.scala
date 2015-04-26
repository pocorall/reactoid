package org.reactoid

import java.util.concurrent._
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.View.OnClickListener
import org.scaloid.common._
import rx._
import rx.ops._

import scala.concurrent.{Future, ExecutionContext}

trait Observable[T] {
  def observe(variable: Rx[T]): Unit
}

trait widget {

  class Button()(implicit context: android.content.Context, parentVGroup: TraitViewGroup[_] = null)
    extends android.widget.Button(context) with TraitButton[Button] with Observable[CharSequence] {

    def basis = this
    override val parentViewGroup = parentVGroup

    private var obsEv:AnyRef = null   // prevents weakreference looses its link
    def observe(textVar: Rx[CharSequence]):Unit = {
      obsEv = Obs(textVar) {
        runOnUiThread(text = textVar())
      }
    }

    private var bdobsEv:AnyRef = null // prevents weakreference looses its link
    def observeBackgroundDrawable(bdrawable: Rx[Drawable]): Unit = {
      bdobsEv = Obs(bdrawable) {
        runOnUiThread(backgroundDrawable = bdrawable())
      }
    }

    def this(textVar: Rx[CharSequence])(implicit context: Context) = {
      this()
      observe(textVar)
    }

    def this(textVar: Rx[CharSequence], onClickListener: View => Unit)(implicit context: Context) = {
      this()
      observe(textVar)
      this.setOnClickListener(onClickListener)
    }

    def this(textVar: Rx[CharSequence], onClickListener: OnClickListener)(implicit context: Context) = {
      this()
      observe(textVar)
      this.setOnClickListener(onClickListener)
    }

  }

  object Button {
    def apply[LP <: ViewGroupLayoutParams[_, Button]]()(implicit context: android.content.Context, defaultLayoutParam: Button => LP): Button = {
      val v = new Button
      v.<<.parent.+=(v)
      v
    }

    def apply[LP <: ViewGroupLayoutParams[_, Button]](txt: Rx[CharSequence])(implicit context: Context, defaultLayoutParam: (Button) => LP): Button = {
      val v = new Button(txt)
      v.<<.parent.+=(v)
      v
    }

    def apply[LP <: ViewGroupLayoutParams[_, Button]](text: Rx[CharSequence], onClickListener: (View) => Unit)(implicit context: Context, defaultLayoutParam: (Button) => LP): Button = {
      apply(text, func2ViewOnClickListener(onClickListener))
    }

    def apply[LP <: ViewGroupLayoutParams[_, Button]](text: Rx[CharSequence], onClickListener: OnClickListener)(implicit context: Context, defaultLayoutParam: (Button) => LP): Button = {
      val v = new Button(text)
      v.setOnClickListener(onClickListener)
      v.<<.parent.+=(v)
      v
    }

  }


  class TextView()(implicit context: android.content.Context, parentVGroup: TraitViewGroup[_] = null)
    extends android.widget.TextView(context) with TraitTextView[TextView] with Observable[CharSequence] {

    def basis = this
    override val parentViewGroup = parentVGroup

    private var obsEv:AnyRef = null  // prevents weakreference looses its link
    def observe(textVar: Rx[CharSequence]):Unit = {
      obsEv = Obs(textVar) {
        runOnUiThread(text = textVar())
      }
    }

    def this(text: CharSequence)(implicit context: Context) = {
      this()
      this.text = text
    }

    def this(text: CharSequence, onClickListener: View => Unit)(implicit context: Context) = {
      this()
      this.text = text
      this.setOnClickListener(onClickListener)
    }

    def this(text: CharSequence, onClickListener: OnClickListener)(implicit context: Context) = {
      this()
      this.text = text
      this.setOnClickListener(onClickListener)
    }

  }

  object TextView {
    def apply[LP <: ViewGroupLayoutParams[_, TextView]]()(implicit context: android.content.Context, defaultLayoutParam: TextView => LP): TextView = {
      val v = new TextView
      v.<<.parent.+=(v)
      v
    }

    def apply[LP <: ViewGroupLayoutParams[_, TextView]](txt: CharSequence)(implicit context: Context, defaultLayoutParam: (TextView) => LP): TextView = {
      val v = new TextView
      v text txt
      v.<<.parent.+=(v)
      v
    }

    def apply[LP <: ViewGroupLayoutParams[_, TextView]](text: CharSequence, onClickListener: (View) => Unit)(implicit context: Context, defaultLayoutParam: (TextView) => LP): TextView = {
      apply(text, func2ViewOnClickListener(onClickListener))
    }

    def apply[LP <: ViewGroupLayoutParams[_, TextView]](text: CharSequence, onClickListener: OnClickListener)(implicit context: Context, defaultLayoutParam: (TextView) => LP): TextView = {
      val v = new TextView
      v.text = text
      v.setOnClickListener(onClickListener)
      v.<<.parent.+=(v)
      v
    }

  }

  class EditText(implicit context:Context, parentVGroup: TraitViewGroup[_] = null) extends android.widget.EditText(context) with TraitEditText[EditText] {

    def basis = this
    override val parentViewGroup = parentVGroup

    private val _textVar = Var[CharSequence](text.toString)

    def textVar = _textVar

    def textVar(obs: Observable[CharSequence]): EditText = {
      obs.observe(_textVar)
      this
    }

    onTextChanged {
      _textVar() = text.toString
    }

    def this(text: CharSequence)(implicit context: Context) = {
      this()
      this.text = text
    }

    def this(text: CharSequence, onClickListener: View => Unit)(implicit context: Context) = {
      this()
      this.text = text
      this.setOnClickListener(onClickListener)
    }

    def this(text: CharSequence, onClickListener: OnClickListener)(implicit context: Context) = {
      this()
      this.text = text
      this.setOnClickListener(onClickListener)
    }
  }

  object EditText {
    def apply[LP <: ViewGroupLayoutParams[_, EditText]]()(implicit context: android.content.Context, defaultLayoutParam: EditText => LP): EditText = {
      val v = new EditText
      v.<<.parent.+=(v)
      v
    }


    def apply[LP <: ViewGroupLayoutParams[_, EditText]](txt: CharSequence)(implicit context: Context, defaultLayoutParam: (EditText) => LP): EditText = {
      val v = new EditText
      v text txt
      v.<<.parent.+=(v)
      v
    }
  }

}

object widget extends widget

trait util {
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

  /**
   * For example, there are an
   */
  val dirtyCheckExecutionContext = ExecutionContext.fromExecutor(
    new ThreadPoolExecutor(1, 1, 1000, TimeUnit.SECONDS, new DirtyQueue[Runnable]))

  def whenDirty[T](f: => T): Future[T] = Future(f)(dirtyCheckExecutionContext)

  import scala.concurrent.ExecutionContext.Implicits.global

  import scala.language.implicitConversions

  implicit def futureCharSeq2Rx[CharSequence](future: => Future[CharSequence]): Rx[CharSequence] = Rx(future).async("".asInstanceOf[CharSequence])

  implicit def charSeq2Rx[CharSequence](seq: => CharSequence): Rx[CharSequence] = Rx(seq)
}

object util extends util

object all extends widget with util