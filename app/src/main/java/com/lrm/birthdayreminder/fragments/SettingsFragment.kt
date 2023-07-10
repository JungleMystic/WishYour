package com.lrm.birthdayreminder.fragments

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.lrm.birthdayreminder.BirthdayApplication
import com.lrm.birthdayreminder.data.SettingsDataStore
import com.lrm.birthdayreminder.databinding.FragmentSettingsBinding
import com.lrm.birthdayreminder.viewModel.EventViewModel
import com.lrm.birthdayreminder.viewModel.EventViewModelFactory
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SettingsFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private val viewModel: EventViewModel by activityViewModels {
        EventViewModelFactory(
            (activity?.application as BirthdayApplication).database.eventDao()
        )
    }

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val _pickedTime = MutableLiveData<String>("")
    private val pickedTime: LiveData<String> get() = _pickedTime

    companion object {
        const val NOTIFICATION_PERMISSION_CODE = 100
    }

    private lateinit var notificationDataStore: SettingsDataStore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pickedTime.observe(viewLifecycleOwner) {time ->
            binding.time.text = time
        }

        notificationDataStore = SettingsDataStore(requireContext())
        notificationDataStore.preferenceFlow.asLiveData().observe(this.viewLifecycleOwner) {value ->
            binding.notificationSwitch.isChecked = value
        }

        notificationDataStore.preferenceFlow2.asLiveData().observe(this.viewLifecycleOwner) {value ->
            _pickedTime.value = value
        }

        binding.notificationSwitch.setOnClickListener {

            if (binding.notificationSwitch.isChecked) {
                if (hasPermission()) {
                    lifecycleScope.launch {
                        notificationDataStore.saveNotificationSwitchMode(
                            binding.notificationSwitch.isChecked,
                            requireContext()
                        )
                    }
                    Toast.makeText(requireContext(), "Notifications turned on", Toast.LENGTH_SHORT)
                        .show()
                    val time = getTime()
                    viewModel.setTime(time)
                    lifecycleScope.launch {
                        notificationDataStore.saveNotificationTime(
                            pickedTime.value.toString(), requireContext()
                        )
                    }
                    Log.i("EventViewModel", "scheduleNotification: $pickedTime")
                    Log.i("EventViewModel", "notification switch: ${binding.notificationSwitch.isChecked}")
                    viewModel.scheduleNotification()
                } else {
                    requestPermission()
                }
            } else {
                viewModel.cancelWork()
                Toast.makeText(requireContext(), "Notifications turned off", Toast.LENGTH_SHORT)
                    .show()
                lifecycleScope.launch {
                    notificationDataStore.saveNotificationSwitchMode(
                        binding.notificationSwitch.isChecked,
                        requireContext()
                    )
                }
                Log.i("EventViewModel", "scheduleNotification: $pickedTime")
                Log.i("EventViewModel", "notification switch: ${binding.notificationSwitch.isChecked}")
            }
        }

        binding.setButton.setOnClickListener {
            if (binding.notificationSwitch.isChecked) {
                val time = getTime()
                viewModel.setTime(time)
                lifecycleScope.launch {
                    notificationDataStore.saveNotificationTime(
                        pickedTime.value.toString(), requireContext()
                    )
                }
                viewModel.scheduleNotification()
            } else {
                Toast.makeText(requireContext(), "Turn on Notifications", Toast.LENGTH_SHORT).show()
            }
        }


    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.permissionPermanentlyDenied(this, perms.first())) {
            SettingsDialog.Builder(requireContext()).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()
    }

    private fun hasPermission() =
        EasyPermissions.hasPermissions(requireContext(), Manifest.permission.POST_NOTIFICATIONS)

    private fun requestPermission() {
        EasyPermissions.requestPermissions(
            this,
            "Permission is required to show notifications",
            NOTIFICATION_PERMISSION_CODE,
            Manifest.permission.POST_NOTIFICATIONS
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        //EasyPermissions handles the request result
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun getTime(): Long {
        val hours = binding.timePicker.hour
        val minutes = binding.timePicker.minute

        val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

        val calendar = Calendar.getInstance()
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, hours)
            set(Calendar.MINUTE, minutes)
        }

        val timeInMilli = calendar.timeInMillis

        _pickedTime.value = formatter.format(timeInMilli)

        return timeInMilli
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}