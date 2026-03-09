package com.avf.vending.domain.model

enum class DispenseStatus {
    /** Payment not yet confirmed — dispense hasn't started. */
    NOT_STARTED,
    /** Command sent to hardware; waiting for sensor acknowledgement. */
    DISPENSING,
    /** Hardware confirmed product dropped (sensor triggered). */
    SUCCESS,
    /** Hardware returned an error (motor jam, slot empty, sensor miss). */
    FAILED,
    /** Hardware did not respond within the timeout window. */
    TIMEOUT,
}
