package reactoid.sample

import org.scaloid.common._
import org.reactoid.all._
import android.graphics.Color

import scalaz.Scalaz._

class HelloReactoid extends SActivity {

  onCreate {
    contentView = new SVerticalLayout {
      SButton("A button!")
      val tv = TextView("Me too")
      val tvar = EditText("Edit me").fill.textVar
      tv.textVar_=(tvar)
    } padding 20.dip
  }

}
