package org.reactoid

import android.content.Context
import android.view.View
import android.view.View.OnClickListener
import org.scaloid.common._
import rx._

trait widget {
  class TextView()(implicit context: android.content.Context, parentVGroup: TraitViewGroup[_] = null)
    extends android.widget.TextView(context) with TraitTextView[TextView] {

    def basis = this
    override val parentViewGroup = parentVGroup

    def textVar(textVar: Var[CharSequence]):Unit = {
      Obs(textVar) {
        text = textVar()
        println("REACTOID: CHANGED")
      }
    }

    def textVar:Var[CharSequence] = ???

    def textVar_=(newTextVar: Var[CharSequence]):Unit = {
      textVar(newTextVar)
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

object all extends widget {

}