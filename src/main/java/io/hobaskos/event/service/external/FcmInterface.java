package io.hobaskos.event.service.external;

import io.hobaskos.event.domain.external.FcmNotificationMultiPayload;
import io.hobaskos.event.domain.external.FcmNotificationSinglePayload;
import io.hobaskos.event.domain.external.FcmResponse;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface FcmInterface {
    @POST("/fcm/send")
    @Headers("Content-Type: application/json")
    Observable<FcmResponse> sendNotification(@Body FcmNotificationSinglePayload fcmNotificationSinglePayload);

    @POST("/fcm/send")
    @Headers("Content-Type: application/json")
    Observable<FcmResponse> sendNotifications(@Body FcmNotificationMultiPayload fcmNotificationMultiPayload);
}
