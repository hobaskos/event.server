package io.hobaskos.event.service.external;

import io.hobaskos.event.config.JHipsterProperties;
import io.hobaskos.event.domain.external.FcmNotification;
import io.hobaskos.event.domain.external.FcmNotificationMultiPayload;
import io.hobaskos.event.domain.external.FcmNotificationSinglePayload;
import io.hobaskos.event.domain.external.FcmResponse;
import io.reactivex.Observable;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

@Service
public class FcmService {

    private final String SERVER_KEY;
    private final String SERVER_URL;
    private final String SENDER_ID;

    private final Logger log = LoggerFactory.getLogger(FcmService.class);

    private FcmInterface fcmInterface;

    @Inject
    public FcmService(JHipsterProperties jHipsterProperties) {
        SERVER_KEY = jHipsterProperties.getFcm().getServerKey();
        SERVER_URL = jHipsterProperties.getFcm().getServerUrl();
        SENDER_ID = jHipsterProperties.getFcm().getSenderId();
    }

    public Observable<FcmResponse> sendNotificationToDevice(FcmNotification notification, String deviceToken) {
        if (fcmInterface == null) { fcmInterface = createFcmService(); }

        return fcmInterface.sendNotification(new FcmNotificationSinglePayload(deviceToken, notification));
    }

    public Observable<FcmResponse> sendNotificationsToDevices(Set<String> deviceTokens, FcmNotification notification) {
        if (fcmInterface == null) {fcmInterface = createFcmService(); }

        return fcmInterface.sendNotifications(new FcmNotificationMultiPayload(deviceTokens, notification));
    }

    private FcmInterface createFcmService()  {
        return RetrofitServiceFactory.create(FcmInterface.class,
            chain -> {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder()
                    .header("Authorization", "key=" + SERVER_KEY)
                    .method(original.method(), original.body());
                Request request = requestBuilder.build();
                return chain.proceed(request);
            },
            SERVER_URL);
    }
}
