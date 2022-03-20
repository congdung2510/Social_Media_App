package com.is1423.socialmedia.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAP66xHYY:APA91bE87KCbd0NpHYZKFQbIAkMM4YRathZbKpBH4y4r0f95DAMreMcs6sMdL9EL1J8yka-cF4WDPd1xgJBoIByeGj7nNa8Snkdpn48I2Bg3SlSdNGGDqXoU1pnAmMrahC-iocMZCW7a"
    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
