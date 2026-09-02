package com.ackileo.telematics.utils
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

enum class DrivingEvent {
    IDLE, HARSH_BRAKING, RAPID_ACCELERATION, SHARP_CORNERING, PHONE_USAGE, NORMAL
}

data class SensorTelemetry(
    val accelerometerX: Float? = null,
    val accelerometerY: Float? = null,
    val accelerometerZ: Float? = null,
    val gyroscopeMagnitude: Float? = null,
    val timestampMillis: Long = 0L,
)

interface DrivingSensorManagerPort {
    val events: StateFlow<DrivingEvent>
    val telemetry: StateFlow<SensorTelemetry>
    fun startMonitoring()
    fun stopMonitoring()
}

@Singleton
class DrivingSensorManager @Inject constructor(
    @ApplicationContext private val context: Context
) : SensorEventListener, DrivingSensorManagerPort {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private val _events = MutableStateFlow(DrivingEvent.IDLE)
    override val events = _events.asStateFlow()

    private val _telemetry = MutableStateFlow(SensorTelemetry())
    override val telemetry = _telemetry.asStateFlow()

    // Thresholds (Adjustable based on testing)
    private val ACCEL_THRESHOLD = 3.5f // m/s^2
    private val BRAKE_THRESHOLD = -3.5f // m/s^2
    private val GYRO_THRESHOLD = 1.2f   // rad/s (Phone handling detection)
    private val SHARP_CORNERING_THRESHOLD = 3.0f

    override fun startMonitoring() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun stopMonitoring() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                _telemetry.value = _telemetry.value.copy(
                    accelerometerX = event.values[0],
                    accelerometerY = event.values[1],
                    accelerometerZ = event.values[2],
                    timestampMillis = event.timestamp,
                )
                detectBrakingAndAccel(event.values[1], event.values[2]) // Y and Z axes
                detectSharpCornering(event.values[0])
            }
            Sensor.TYPE_GYROSCOPE -> {
                val magnitude = sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2])
                _telemetry.value = _telemetry.value.copy(
                    gyroscopeMagnitude = magnitude,
                    timestampMillis = event.timestamp,
                )
                detectPhoneUsage(event.values[0], event.values[1], event.values[2])
            }
        }
    }

    private fun detectSharpCornering(x: Float) {
        if (x > SHARP_CORNERING_THRESHOLD || x < -SHARP_CORNERING_THRESHOLD) {
            _events.value = DrivingEvent.SHARP_CORNERING
        }
    }

    private fun detectBrakingAndAccel(y: Float, z: Float) {
        // Most telematics use the Y-axis (forward/backward) when phone is in mount
        // We use the combined magnitude of forward movement for flexibility
        when {
            y > ACCEL_THRESHOLD -> _events.value = DrivingEvent.RAPID_ACCELERATION
            y < BRAKE_THRESHOLD -> _events.value = DrivingEvent.HARSH_BRAKING
            else -> {
                if (_events.value != DrivingEvent.PHONE_USAGE) {
                    _events.value = DrivingEvent.NORMAL
                }
            }
        }
    }

    private fun detectPhoneUsage(x: Float, y: Float, z: Float) {
        // Calculate angular velocity magnitude
        val magnitude = sqrt(x * x + y * y + z * z)

        // High rotation while driving typically indicates the phone is being handled
        if (magnitude > GYRO_THRESHOLD) {
            _events.value = DrivingEvent.PHONE_USAGE
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
// new
