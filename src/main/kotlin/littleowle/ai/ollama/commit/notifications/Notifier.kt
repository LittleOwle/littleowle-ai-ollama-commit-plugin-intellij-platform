package littleowle.ai.ollama.commit.notifications

import littleowle.ai.ollama.commit.Icons
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project

private const val IMPORTANT_GROUP_ID = "littleowle.ai.ollama.commit.notification.important"
private const val GENERAL_GROUP_ID = "littleowle.ai.ollama.commit.notification.general"

fun sendNotification(notification : Notification, project : Project? = null) {
    val groupId = when(notification.type) {
        Notification.Type.PERSISTENT -> IMPORTANT_GROUP_ID
        Notification.Type.TRANSIENT -> GENERAL_GROUP_ID
    }

    val notificationManager = NotificationGroupManager
        .getInstance()
        .getNotificationGroup(groupId)

    val intellijNotification = notificationManager.createNotification(
        notification.title ?: "",
        notification.message,
        NotificationType.INFORMATION
    )

    intellijNotification.icon = Icons.LITTLEOWLE_LOGO

    notification.actions.forEach { action ->
        intellijNotification.addAction(DumbAwareAction.create(action.title) {
            action.run() {
                intellijNotification.expire()
            }
        })
    }

    intellijNotification.notify(project)
}
