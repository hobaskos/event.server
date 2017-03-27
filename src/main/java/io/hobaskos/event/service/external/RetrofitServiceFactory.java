package io.hobaskos.event.service.external;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class RetrofitServiceFactory {

    static <S> S create(Class<S> serviceClass, Interceptor interceptor, String url) {
        return new Retrofit.Builder()
                .baseUrl(url)
                .client(new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(serviceClass);
    }
}
