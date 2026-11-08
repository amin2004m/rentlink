package rentlink.notification.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rentlink.notification.repository.DeviceTokenRepository;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class PushService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final String credentialsJson;
    private volatile boolean initialized = false;

    public PushService(DeviceTokenRepository deviceTokenRepository,
                       @Value("${firebase.credentials-json}") String credentialsJson) {
        this.deviceTokenRepository = deviceTokenRepository;
        this.credentialsJson = credentialsJson;
    }

    private synchronized boolean ensureInitialized() {
        if (initialized) {
            return true;
        }
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(
                        new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8)));
                FirebaseApp.initializeApp(FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build());
            }
            initialized = true;
        } catch (Exception e) {
            log.warn("Firebase not configured, push notifications disabled: {}", e.getMessage());
        }
        return initialized;
    }

    public void sendToUser(String userId, String title, String body) {
        if (!ensureInitialized()) {
            return;
        }
        deviceTokenRepository.findByUserId(userId).forEach(deviceToken -> {
            try {
                Message message = Message.builder()
                        .setToken(deviceToken.getToken())
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .build();
                FirebaseMessaging.getInstance().send(message);
            } catch (Exception e) {
                log.warn("Failed to send push notification to token {}: {}", deviceToken.getToken(), e.getMessage());
            }
        });
    }
}
