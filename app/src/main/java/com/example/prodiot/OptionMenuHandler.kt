import android.content.Context
import android.content.Intent
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.prodiot.*

class OptionMenuHandler(private val context: Context) {
    private val logoutHelper: LogoutHelper = LogoutHelper(context)

    fun handleItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                logoutHelper.logout()
                true
            }
            R.id.menu_info -> {
                context.startActivity(Intent(context, AppUserInfo::class.java))
                (context as AppCompatActivity).overridePendingTransition(R.anim.left_in, R.anim.left_out)
                true
            }
            R.id.step_manager -> {
                context.startActivity(Intent(context, CodeStepWrite::class.java))
                (context as AppCompatActivity).overridePendingTransition(R.anim.left_in, R.anim.left_out)
                true
            }
            else -> false
        }
    }
}
