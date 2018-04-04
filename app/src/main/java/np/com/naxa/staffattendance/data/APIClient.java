package np.com.naxa.staffattendance.data;


import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {

    private static final String TAG = "APIClient";

    public static final String BASE_URL = "http://app.fieldsight.org/";
    private static Retrofit retrofit = null;
    private static OkHttpClient okHttpClient;

    public static ApiInterface getAPIService(Context context) {
        return APIClient.getClient(context).create(ApiInterface.class);
    }


    public static Retrofit getClient(Context context) {


        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .client(createOkHttpClient())
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }


        return retrofit;
    }


    private static OkHttpClient createOkHttpClient() {
        Dispatcher dispatcher = new Dispatcher();

        Interceptor authorization = new Interceptor() {
            @Override
            public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
                Request request = chain.request().newBuilder()
                        .addHeader("Authorization", TokenMananger.getToken()).build();
                return chain.proceed(request);
            }
        };

        okHttpClient = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .connectTimeout(4,TimeUnit.MINUTES)
                .readTimeout(4,TimeUnit.MINUTES)
                .addInterceptor(authorization)
                .build();


        okHttpClient.dispatcher().setIdleCallback(new Runnable() {
            @Override
            public void run() {
//                Timber.i("the number of running calls has reached zero");
                Log.d(TAG, "run: "+ "the number of running calls has reached zero");
            }
        });


        return okHttpClient;
    }


}

