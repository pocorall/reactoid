package reactoid.sample

import org.reactoid.all._
import org.scaloid.common._
import rx.core.Rx
import rx.ops._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
 * Provides utilities simulate asynchronous IO. The actual IO can be TCP, disk operation or anything you name.
 */
object AsyncIOSimulator {
  private var i = 0

  /** Simulates server connection. */
  def isValidID(str: CharSequence): Future[Boolean] = whenDirty {
    i += 1
    val v = i % 3
    Thread.sleep(2000)
    if (v == 0) throw new Exception("Connection Timeout")
    v == 1
  }
}

import AsyncIOSimulator._

class HelloReactoid extends SActivity {
  def demo1() =
    onCreate {
      contentView = new SVerticalLayout {
        val bt = Button()
        val tv = TextView()
        val tvar = EditText("Edit me").fill.textVar(tv).textVar
        bt.observe(Rx("[[" + tvar() + "]]"))
      } padding 20.dip
    }

  def demo2() =
    onCreate {
      contentView = new SVerticalLayout {
        val tv = TextView()
        val tvar = EditText().hint("ID").textVar
        tv.observe {
          isValidID(tvar()).map {
            valid => if (valid) "Valid ID" else "ID already exists"
          }.recover { case cause => cause.getMessage }
        }
      } padding 20.dip
    }

  demo2()
}
