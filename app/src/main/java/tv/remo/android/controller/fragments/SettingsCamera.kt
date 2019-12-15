package tv.remo.android.controller.fragments
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import tv.remo.android.controller.Camera2Util
import tv.remo.android.controller.R
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.controller.sdk.utils.ValueUtil
import tv.remo.android.settingsutil.fragments.BasePreferenceFragmentCompat
import java.util.*

class SettingsCamera : BasePreferenceFragmentCompat(
        R.xml.settings_camera,
        R.string.cameraSettingsEnableKey
){
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
                super.onCreatePreferences(savedInstanceState, rootKey)
                RemoSettingsUtil.with(context!!){
                        val currCameraId = it.cameraDeviceId.getPref()
                        checkForCamera2SupportAndReact(it.cameraDeviceId.getPref())
                        updateUIFromCameraSelection(currCameraId)
                        listenAndReplaceIfEmpty(R.string.ffmpegOutputOptionsPrefsKey,
                                R.string.ffmpegDefaultOutputOptions)
                        listenAndReplaceIfEmpty(R.string.ffmpegInputOptionsPrefsKey,
                                R.string.ffmpegDefaultInputOptions)
                        listenAndReplaceIfEmpty(R.string.ffmpegFilterAddition,
                                R.string.ffmpegDefaultFilterOptions, true)
                        listenToPref<ListPreference>(R.string.cameraDeviceIdKey){ _, value ->
                                (value as String).toInt().also { value ->
                                        updateUIFromCameraSelection(value)
                                        checkForCamera2SupportAndReact(value)
                                }

                                true
                        }
                }
        }

        private fun updateUIFromCameraSelection(newValue: Int) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        val sizes = try {
                                Camera2Util.GetCameraSizes(requireContext(), newValue)
                        } catch (e: IndexOutOfBoundsException) {
                                ArrayList<Pair<Int, Int>>().also {
                                        it.add(Pair(640,480))
                                }
                        }
                        setNewResolutionList(sizes)
                }
        }

        private fun setNewResolutionList(sizes: ArrayList<Pair<Int, Int>>) {
                val resolutionList = findPreference<ListPreference>(
                        getString(R.string.cameraResolutionKey)
                )
                resolutionList?.let {
                        val blacklist = listOf(Pair(720,480), Pair(1440, 1080))
                        sizes.removeAll {pair ->
                                blacklist.contains(pair)
                        }
                        val values = Array<CharSequence>(sizes.size) {
                                "0x0"
                        }
                        val userFacingValues = Array<CharSequence>(sizes.size) {
                                "0x0"
                        }
                        val knownWorkingResolutions = listOf(Pair(640, 480), Pair(1280,720), Pair(1280,960), Pair(1920,1080))
                        sizes.forEachIndexed{ i, pair ->
                                var appendText = ""
                                val width = pair.first
                                val height = pair.second
                                if(width == 640 && height == 480)
                                        appendText = "(recommended)"
                                if(width > 1280 || height > 720)
                                        appendText = "(may be slow)"
                                if(!knownWorkingResolutions.contains(pair))
                                        appendText = "(not tested)"
                                val gcm = ValueUtil.gcm(width, height)
                                values[i] = "${width}x${height}"
                                userFacingValues[i] = "${width}x${height}(${width / gcm}:${height / gcm}) $appendText"
                        }
                        it.entries = userFacingValues.reversedArray()
                        it.entryValues = values.reversedArray()
                        if(!it.entryValues.contains(it.value))
                                it.value = it.entryValues[0].toString()
                }
        }

        private fun checkForCamera2SupportAndReact(cameraId : Int) : Boolean{
                val supportsCamera2 = validateCamera2Support(context!!,
                        cameraId)
                val disabledPref = findPreference<Preference>(getString(R.string.camera2features))
                disabledPref?.isVisible = !supportsCamera2
                val featureSwitch = findPreference<SwitchPreferenceCompat>(getString(R.string.useCamera2))
                featureSwitch?.isEnabled = supportsCamera2
                featureSwitch?.isChecked = supportsCamera2
                if(!supportsCamera2)
                        findPreference<ListPreference>(
                                getString(R.string.cameraResolutionKey)
                        )?.let {
                              it.value = "640x480"
                        }
                return supportsCamera2
        }

        private fun listenAndReplaceIfEmpty(prefRes : Int, defaultValue : Int, allowBlank : Boolean = false) {
                listenToPref<EditTextPreference>(prefRes){
                                preference, newValue ->
                        if(newValue.toString().isBlank() && !allowBlank || newValue.toString().isEmpty()){
                                (preference as EditTextPreference).text =
                                        getString(defaultValue)
                                false
                        }
                        else{
                                true
                        }
                }
        }

        fun <T : Preference> listenToPref(prefRes : Int, func : (Preference, Any) -> Boolean){
                findPreference<T>(getString(prefRes))
                        ?.setOnPreferenceChangeListener(func)
        }

        private fun validateCamera2Support(context: Context, cameraId: Int): Boolean {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        try {
                                val cm =
                                        (context.getSystemService(Context.CAMERA_SERVICE) as CameraManager)
                                val hardwareLevel = cm.getCameraCharacteristics(
                                        cm.cameraIdList[cameraId]
                                )[CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL]
                                return hardwareLevel != CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY
                                        && hardwareLevel != CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED
                        } catch (_: Exception) {

                        }
                }
                return false
        }
}
