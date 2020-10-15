package tv.remo.android.controller.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import kotlinx.android.synthetic.main.activity_settings.*
import tv.remo.android.controller.R
import tv.remo.android.settingsutil.interfaces.SwitchBarCapableActivity
import tv.remo.android.settingsutil.views.SwitchBar
import kotlin.system.exitProcess


class SettingsActivity : AppCompatActivity(), NavController.OnDestinationChangedListener, SwitchBarCapableActivity {
    private var first = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        NavigationUI.setupActionBarWithNavController(this, NavHostFragment.findNavController(nav_host_fragment))
        NavHostFragment.findNavController(nav_host_fragment).addOnDestinationChangedListener (this)
    }

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        if(destination.id == R.id.settingsEntryFragment){
            if(!first){
                //completely restart the app, killing anything that would have survived
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                exitProcess(0)
            }
            first = false
        }
    }

    override fun onSupportNavigateUp() : Boolean {
        return NavHostFragment.findNavController(nav_host_fragment).navigateUp()
    }

    override fun getSwitchBar(): SwitchBar {
        return switch_bar
    }

    companion object{
        fun getIntent(context: Context) : Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }
}
