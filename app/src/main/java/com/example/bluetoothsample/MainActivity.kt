package com.example.bluetoothsample

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bluetoothsample.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.io.IOException
import java.io.InputStream
import java.util.UUID


class MainActivity : AppCompatActivity(), BluetoothListener, BTItemClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var discoveryReceiver: DiscoveryReceiver
    private lateinit var intentFilter: IntentFilter
    private lateinit var myBluetoothAdapter: MyBluetoothAdapter
    private var listMyDevice = mutableListOf<BluetoothDevice>()
    private var uniqueDevices = HashSet<BluetoothDevice>()
    private val TAG = "MainActivity"

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize MyBluetoothAdapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        discoveryReceiver = DiscoveryReceiver(this)
        intentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        binding.bluetoothRecycleview.layoutManager = LinearLayoutManager(this)
        myBluetoothAdapter = MyBluetoothAdapter(listMyDevice, this)
        binding.bluetoothRecycleview.adapter = myBluetoothAdapter
        checkSupportBT()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkSupportBT() {
        if (bluetoothAdapter == null) {
            showToast("Device doesn't support Bluetooth")
            finish()
        } else {
            binding.btnBluetooth.setOnClickListener {
                enableBT()
            }
            binding.btnBluetoothList.setOnClickListener {
                startBluetoothDiscovery()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun enableBT() {
        val permissions: Array<String> = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        val missingPermissions = permissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 1)
        } else {
            if (!bluetoothAdapter.isEnabled) {
                enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            } else {
                showToast("Bluetooth is already on")
            }
        }

    }


    @RequiresApi(Build.VERSION_CODES.S)
    private fun startBluetoothDiscovery() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        registerReceiver(discoveryReceiver, intentFilter)
        bluetoothAdapter.startDiscovery()
        Log.e(TAG, "startDiscovery: ${bluetoothAdapter.startDiscovery()}")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            } else {
                showToast("Permission denied")
            }
        }
    }

    private val enableBluetoothLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                showToast("Bluetooth turned on")
            } else {
                showToast("Bluetooth enabling canceled")
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(discoveryReceiver)
    }

    private fun showToast(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun deviceFound(device: BluetoothDevice?) {
        if (device != null) {
            if (uniqueDevices.add(device)) {
                listMyDevice.add(device)
                myBluetoothAdapter.notifyDataSetChanged()

            }
        }
    }

    override fun discoveryFinished() {
        TODO("Not yet implemented")
    }

    override fun myBTDevice(device: BluetoothDevice?) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request Bluetooth connection and scanning permissions
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                    ),
                    1
                )
                return
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request Bluetooth connection and scanning permissions
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    1
                )
                return
            }
        }
        showToast(device!!.address)
        Log.e(TAG, "myBTDevice-uuids: ${device.uuids}")
        val sppUuid = UUID.fromString("0000112f-0000-1000-8000-00805F9B34FB")
        val sppUuid1 = UUID.fromString("a82efa21-ae5c-3dde-9bbc-f16da7b16c5a")
        val sppUuid12 = UUID.fromString("8989063a-c9af-463a-b3f1-f21d9b2b827b")
        var socket: BluetoothSocket? = null
        var connected = false
        //                                                   0000110fa-0000-1000-8000-00805F9B34FB
        //                                                   00001105-0000-1000-8000-00805F9B34FB
        //                                                   00001115-0000-1000-8000-00805F9B34FB
        //                                                   00001116-0000-1000-8000-00805F9B34FB
        //                                                   0000110e-0000-1000-8000-00805F9B34FB
        //                                                   0000112f-0000-1000-8000-00805F9B34FB
        //                                                   00001112-0000-1000-8000-00805F9B34FB
        //                                                   0000111f-0000-1000-8000-00805F9B34FB
        //                                                   00001132-0000-1000-8000-00805F9B34FB
        //                                                   a82efa21-ae5c-3dde-9bbc-f16da7b16c5a
        //                                                   8989063a-c9af-463a-b3f1-f21d9b2b827b


        device.uuids?.forEach { parcelUuid ->
            val uuid = parcelUuid.uuid
            val socket = device.createRfcommSocketToServiceRecord(uuid)
            socket?.use { socket ->
                try {
                    // Attempt to connect to the remote device
                    socket.connect()
                    showToast("Connection successful to ${device.address}")
                    showToast("Device connect ${socket.connect()}")

                    // TODO: Implement your communication logic here

                } catch (e: IOException) {
                    // Connection failed, handle the exception
                    showToast("Connection failed: $e")
                    Log.e(TAG, "myBTDevice: $e")

                } finally {
                    try {
                        socket.close()
                    } catch (e: IOException) {
                        // Handle the exception during socket closure
                        showToast("Socket closure failed: $e")
                        Log.e(TAG, "myBTDevice: $e")
                    }
                }
            }

        }
        pairDevice(device)

    }
    private fun pairDevice(device: BluetoothDevice) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        if (device.bondState == BluetoothDevice.BOND_BONDED) {
            showToast("Device already paired")
            return
        }

        val pairingIntent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
        startActivity(pairingIntent)
    }

    /*override fun myBTDevice(device: BluetoothDevice?) {
        if (device != null) {
            showToast(device.address)
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    showToast("Bluetooth connection permission not granted.")
                    return
                }
            } else {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    showToast("Location permission not granted.")
                    return
                }
            }

            Log.e(TAG, "myBTDevice-uuids: ${device.uuids}")

            // Use a known UUID for the service you want to connect to
            val sppUuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            var socket: BluetoothSocket? = null
            var inputStream: InputStream? = null

            try {
                // Create a BluetoothSocket using the known UUID
                socket = device.createRfcommSocketToServiceRecord(sppUuid)

                // Attempt to connect to the remote device
                socket.connect()
                showToast("Connection successful to ${device.address}")

                // Get the input stream for reading data
                inputStream = socket.inputStream

                // TODO: Implement your communication logic here

            } catch (e: IOException) {
                // Connection failed, handle the exception
                showToast("Connection failed: $e")
                Log.e(TAG, "myBTDevice: $e")

            } finally {
                try {
                    // Close the input stream
                    inputStream?.close()
                    // Close the socket only when you are done with it
                    socket?.close()
                } catch (e: IOException) {
                    // Handle the exception during closure
                    showToast("IOException during closure: $e")
                    Log.e(TAG, "myBTDevice: $e")
                }
            }
        }
    }*/



}
