package com.example.ecohackaton.ui.login

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothSocket
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.registerReceiver
import com.example.ecohackaton.databinding.FragmentApLoginBinding

import com.example.ecohackaton.R
import java.io.IOException
import java.util.UUID

class ApLoginFragment : Fragment() {

    private lateinit var loginViewModel: LoginViewModel
    private var _binding: FragmentApLoginBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothSocket: BluetoothSocket? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentApLoginBinding.inflate(inflater, container, false)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        Log.d("Bluetooth", "BluetoothAdapter: $bluetoothAdapter")



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        checkBluetooth()

        val usernameEditText = binding.username
        val passwordEditText = binding.password
        val loginButton = binding.login
        val loadingProgressBar = binding.loading

        loginViewModel.loginFormState.observe(viewLifecycleOwner,
            Observer { loginFormState ->
                if (loginFormState == null) {
                    return@Observer
                }
                loginButton.isEnabled = loginFormState.isDataValid
                loginFormState.usernameError?.let {
                    usernameEditText.error = getString(it)
                }
                loginFormState.passwordError?.let {
                    passwordEditText.error = getString(it)
                }
            })

        loginViewModel.loginResult.observe(viewLifecycleOwner,
            Observer { loginResult ->
                loginResult ?: return@Observer
                loadingProgressBar.visibility = View.GONE
                loginResult.error?.let {
                    showLoginFailed(it)
                }
                loginResult.success?.let {
                    updateUiWithUser(it)
                }
            })

        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                loginViewModel.loginDataChanged(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
        }
        usernameEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginViewModel.login(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
            false
        }

        loginButton.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE
//            loginViewModel.login(
//                usernameEditText.text.toString(),
//                passwordEditText.text.toString()
//            )
            val ap = usernameEditText.text.toString()
            val password = usernameEditText.text.toString()
            bluetoothSocket?.outputStream?.write("$ap $password".toByteArray())
        }
    }

    private fun checkBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            binding.status.text = "Bluetooth выключен"
            Log.d("Bluetooth", "Bluetooth выключен")
        } else {
            binding.status.text = "Bluetooth включен"
            Log.d("Bluetooth", "Bluetooth включен")
        }

        val device = bluetoothAdapter.getRemoteDevice("20:15:08:17:27:45")
        Log.d("Bluetooth", "device: $device")
        try {
            val bluetoothSocket = if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("Bluetooth", "BluetoothSocket: нет разрешения")
                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_CONNECT),
                    0)
                return
            } else {
                device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
            }
            Log.d("Bluetooth", "BluetoothSocket: $bluetoothSocket")
            bluetoothSocket.connect()

            Log.d("Bluetooth", "Успешно подключено к устройству с MAC-адресом '20:15:08:17:27:45'")
            binding.status.text = "Подключено к устройству с MAC-адресом '20:15:08:17:27:45'"
        } catch (e: IOException) {
            Log.e("Bluetooth", "Ошибка при подключении к устройству с MAC-адресом '20:15:08:17:27:45\"'", e)
            binding.status.text = "Ошибка при подключении к устройству с MAC-адресом '20:15:08:17:27:45\"'"
        }
        Log.d("Bluetooth", "BluetoothAdapter.state: ${bluetoothAdapter.state}")
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome) + model.displayName
        // TODO : initiate successful logged in experience
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, welcome, Toast.LENGTH_LONG).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}