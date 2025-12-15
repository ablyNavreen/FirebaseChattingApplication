// FcmData.kt

// Main request body for the FCM Legacy API
data class FcmRequest(
    // The recipient's unique FCM token (obtained from their device)
    val to: String,
    // Key/value pairs for handling custom logic in the receiving app
    val data: FcmData? = null,
    // Notification data for displaying a system notification
    val notification: FcmNotification? = null
)

// Notification content structure
data class FcmNotification(
    val title: String,
    val body: String
)

// Response structure (simplified for basic checks)
data class FcmResponse(
    val success: Int,
    val failure: Int
)

data class FcmData(
    val senderId: String,
    val senderName: String,
    val senderToken: String,
    val senderGender: String,
)