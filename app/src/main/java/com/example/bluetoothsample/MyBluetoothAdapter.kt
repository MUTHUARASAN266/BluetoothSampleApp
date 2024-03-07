package com.example.bluetoothsample

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothsample.databinding.BluetoothListBinding

class MyBluetoothAdapter(
    private val device: List<BluetoothDevice>,
    private val btItemClickListener: BTItemClickListener
) : RecyclerView.Adapter<MyBluetoothAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: BluetoothListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(device: BluetoothDevice) {

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
                if (ActivityCompat.checkSelfPermission(
                        binding.root.context,
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
            } else {
                if (ActivityCompat.checkSelfPermission(
                        binding.root.context,
                        Manifest.permission.ACCESS_FINE_LOCATION
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
            }

            binding.bluetoothName.text = device.name ?: ("Unnamed Device " + device.address)
            binding.bluetoothName.setOnClickListener {
                btItemClickListener.myBTDevice(device)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater =
            BluetoothListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(layoutInflater)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val myDevice = device[position]
        holder.bind(myDevice)
    }

    override fun getItemCount(): Int {
        return device.size
    }
}