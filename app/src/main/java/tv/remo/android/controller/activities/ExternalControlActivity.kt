package tv.remo.android.controller.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.btelman.controlsdk.enums.Operation
import tv.remo.android.controller.R
import tv.remo.android.controller.RemoApplication
import tv.remo.android.controller.ServiceInterface
import tv.remo.android.controller.databinding.ActivityExternalControlBinding

/**
 * Activity that external applications can use to start a stream
 *
 * Right now, this directly modifies the preferences for the whole application. At some point it
 * might be nice to have the preferences sent by the external application apply only to the stream
 * that it starts.
 */
class ExternalControlActivity: AppCompatActivity() {
    private val log = RemoApplication.getLogger(this)
    private lateinit var binding: ActivityExternalControlBinding
    private var serviceInterface: ServiceInterface? = null
    private var isStreamRunning: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log.v("onCreate()")
        binding = ActivityExternalControlBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupControlSDK()
    }

    override fun onStart() {
        super.onStart()
        binding.denyButton.setOnClickListener() {
//            serviceInterface?.changeStreamState(Operation.NOT_OK)
            finish();
        }

        binding.channelNameText.text = getString(R.string.channelNameString, "loading...")
    }

    override fun onDestroy() {
        super.onDestroy()
        log.v("onDestroy()")
        serviceInterface?.destroy()
        serviceInterface = null
    }

    private val onServiceBind: (Operation) -> Unit = { serviceBoundStatus ->
        log.v("serviceBoundStatus: $serviceBoundStatus")
        binding.startStreamButton.isEnabled = serviceBoundStatus == Operation.OK
    }

    private val onServiceStatus: (Operation) -> Unit = { streamRunningStatus ->
        log.v("streamRunningStatus: $streamRunningStatus")
        if (streamRunningStatus == Operation.NOT_OK) isStreamRunning = false
        else if (streamRunningStatus == Operation.NOT_OK) isStreamRunning = true
    }

    private fun setupControlSDK() {
        serviceInterface = ServiceInterface(this, onServiceBind, onServiceStatus)
        serviceInterface?.setup()
    }
}
