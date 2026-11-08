package rentlink.notification.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import rentlink.notification.model.DeviceToken;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends MongoRepository<DeviceToken, String> {

    List<DeviceToken> findByUserId(String userId);

    Optional<DeviceToken> findByToken(String token);
}