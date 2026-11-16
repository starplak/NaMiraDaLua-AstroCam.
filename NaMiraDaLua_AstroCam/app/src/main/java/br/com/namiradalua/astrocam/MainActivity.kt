package br.com.namiradalua.astrocam

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var usbManager: UsbManager
    private lateinit var statusText: TextView
    private lateinit var devicesText: TextView
    private val permissionAction = "br.com.namiradalua.astrocam.USB_PERMISSION"

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                permissionAction -> {
                    val device = usbDeviceFrom(intent)
                    val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    statusText.text = if (granted && device != null) {
                        "Permissão USB concedida. Próxima etapa: iniciar o vídeo UVC."
                    } else {
                        "Permissão USB não concedida."
                    }
                    refreshDevices()
                }
                UsbManager.ACTION_USB_DEVICE_ATTACHED,
                UsbManager.ACTION_USB_DEVICE_DETACHED -> refreshDevices()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        buildScreen()
        val filter = IntentFilter().apply {
            addAction(permissionAction)
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(usbReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(usbReceiver, filter)
        }
        refreshDevices()
    }

    private fun buildScreen() {
        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(28, 36, 28, 28)
            setBackgroundColor(0xFF080D1B.toInt())
        }

        val title = TextView(this).apply {
            text = "NA MIRA DA LUA\nASTROCAM"
            textSize = 27f
            gravity = Gravity.CENTER
            setTextColor(0xFFFFFFFF.toInt())
        }
        val subtitle = TextView(this).apply {
            text = "Sua câmera astronômica no Android"
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(0xFF9CCBFF.toInt())
            setPadding(0, 12, 0, 28)
        }
        statusText = TextView(this).apply {
            text = "Preparando conexão USB..."
            textSize = 15f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(0, 16, 0, 16)
        }
        val refresh = Button(this).apply {
            text = "Procurar câmera USB"
            setOnClickListener { refreshDevices() }
        }
        devicesText = TextView(this).apply {
            textSize = 14f
            setTextColor(0xFFE5ECF7.toInt())
            setPadding(0, 18, 0, 18)
        }
        val note = TextView(this).apply {
            text = "Versão inicial: identifica dispositivos USB e solicita autorização. A imagem ao vivo da SVBONY SV105 e os ajustes de imagem serão adicionados na próxima etapa."
            textSize = 13f
            setTextColor(0xFFB5C0D1.toInt())
            setPadding(0, 24, 0, 0)
        }

        listOf(title, subtitle, statusText, refresh, devicesText, note).forEach {
            layout.addView(it, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        scroll.addView(layout)
        setContentView(scroll)
    }

    private fun refreshDevices() {
        val devices = usbManager.deviceList.values.toList()
        if (devices.isEmpty()) {
            devicesText.text = "Nenhum dispositivo USB detectado. Conecte a SVBONY SV105 com um cabo OTG e toque em procurar."
            statusText.text = "Aguardando câmera USB..."
            return
        }
        statusText.text = "Dispositivo(s) USB encontrado(s): ${devices.size}"
        devicesText.text = devices.mapIndexed { index, device ->
            "${index + 1}. ${device.deviceName}\n   Vendor ID: ${device.vendorId} | Product ID: ${device.productId}\n   ${if (usbManager.hasPermission(device)) "Permissão concedida" else "Toque abaixo para autorizar"}"
        }.joinToString("\n\n")
        devices.forEach { device ->
            if (!usbManager.hasPermission(device)) {
                // A autorização é iniciada por botão separado para evitar múltiplas janelas.
            }
        }
        if (devices.size == 1) {
            val authorize = Button(this).apply {
                text = "Autorizar ${devices.first().deviceName}"
                setOnClickListener { requestUsbPermission(devices.first()) }
            }
            (devicesText.parent as? LinearLayout)?.addView(authorize)
        }
    }

    private fun requestUsbPermission(device: UsbDevice) {
        val intent = Intent(permissionAction).setPackage(packageName)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= 31) PendingIntent.FLAG_MUTABLE else 0
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, flags)
        usbManager.requestPermission(device, pendingIntent)
        statusText.text = "Solicitando autorização USB..."
    }

    private fun usbDeviceFrom(intent: Intent): UsbDevice? {
        return if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(usbReceiver) } catch (_: IllegalArgumentException) {}
    }
}
