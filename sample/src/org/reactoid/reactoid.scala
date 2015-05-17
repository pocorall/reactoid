package org.reactoid

import java.util.concurrent._

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.View.OnClickListener
import org.scaloid.common._
import rx._
import rx.core.Propagator
import rx.ops._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

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


  trait TrTextView[T <: android.widget.TextView] extends TraitTextView[T] with Observable[CharSequence] {

    private var obsEv:AnyRef = null  // prevents weakreference looses its link
    def observe(textVar: Rx[CharSequence]):Unit = {
      obsEv = Obs(textVar) {
        runOnUiThread(basis.setText(textVar()))
      }
    }
  }

  class TextView()(implicit context: android.content.Context, parentVGroup: TraitViewGroup[_] = null)
    extends android.widget.TextView(context) with TraitTextView[TextView] with TrTextView[TextView] {

    def basis = this
    override val parentViewGroup = parentVGroup

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


  trait ObjTextView[V <: android.widget.TextView with TrTextView[V]] {
    protected def create()(implicit context: android.content.Context): V

    def apply[LP <: ViewGroupLayoutParams[_, V]]()(implicit context: android.content.Context, defaultLayoutParam: V => LP): V = {
      val v = create()
      v.<<.parent.+=(v)
      v
    }
    def apply[LP <: ViewGroupLayoutParams[_, V]](anyVar: Rx[Any])(implicit context: Context, defaultLayoutParam: V => LP): V = {
      val v = apply()
      //v text anyVar().toString
      v.observe(Rx(anyVar().toString))
      v
    }

    def apply[LP <: ViewGroupLayoutParams[_, V]](txt: CharSequence)(implicit context: Context, defaultLayoutParam: V => LP): V = {
      val v = apply()
      v text txt
      v
    }

    def apply[LP <: ViewGroupLayoutParams[_, V]](text: CharSequence, onClickListener: (View) => Unit)(implicit context: Context, defaultLayoutParam: V => LP): V = {
      apply(text, func2ViewOnClickListener(onClickListener))
    }

    def apply[LP <: ViewGroupLayoutParams[_, V]](text: CharSequence, onClickListener: OnClickListener)(implicit context: Context, defaultLayoutParam: V => LP): V = {
      val v = create()
      v.text = text
      v.setOnClickListener(onClickListener)
      v.<<.parent.+=(v)
      v
    }
  }

  object TextView extends ObjTextView[TextView] {
    protected def create()(implicit context: android.content.Context) = new TextView
  }

  trait TrEditText[V <: android.widget.EditText] extends TraitEditText[V] with TrTextView[V] {
    implicit protected val contxt: Context

    private val _textVar = Var[CharSequence](text.toString)

    def textVar = _textVar

    def textVar(obs: Observable[CharSequence]): V = {
      obs.observe(_textVar)
      basis
    }

    onTextChanged {
      _textVar() = text.toString
    }
  }

  class EditText(implicit protected val contxt: Context, parentVGroup: TraitViewGroup[_] = null) extends android.widget.EditText(contxt) with TrEditText[EditText] {
    def basis = this

    override val parentViewGroup = parentVGroup

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

  object EditText extends ObjTextView[EditText] {
    protected def create()(implicit context: android.content.Context) = new EditText
  }

}

object widget extends widget

trait support {
  /** Execution context holder for dirty check. This is designed to have different executionContext from
    * ordinary Future calls.
    */
  case class DirtyCheckExecutionContext(executor: ExecutionContext)

  implicit lazy val dirtyCheckExecutor = DirtyCheckExecutionContext(ExecutionContext.fromExecutor(
    new ThreadPoolExecutor(1, 1, 1000, TimeUnit.SECONDS, new util.DirtyQueue[Runnable])))

  def whenDirty[T](f: => T)(implicit dirtyCheckExecutionContext: DirtyCheckExecutionContext): Future[T] =
    Future(f)(dirtyCheckExecutionContext.executor)

  implicit def futureCharSeq2Rx[CharSequence](future: => Future[CharSequence])(implicit executor: ExecutionContext): Rx[CharSequence] =
    Rx(future).async("".asInstanceOf[CharSequence])

  implicit def any2Rx[T](seq: => T): Rx[T] = Rx(seq)
}

object support extends support

object all extends widget with support