package np.com.naxa.staffattendance.common.network;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.TimeUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


@SuppressLint("LogNotTimber")
public class ConnectionTest {
    private long startTime;
    private long endTime;
    private long fileSize;

    private static ConnectionTest INSTANCE;
    private OkHttpClient client = new OkHttpClient();

    // bandwidth in kbps
    private int POOR_BANDWIDTH = 150;
    private int AVERAGE_BANDWIDTH = 550;
    private int GOOD_BANDWIDTH = 2000;

    private String TAG = "ConnectionTest";

    public static ConnectionTest getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new ConnectionTest();
        }

        return INSTANCE;
    }


    public void download(ConnectionTestCallback callback) {
        callback.onStart();
        startTime = System.currentTimeMillis();

        Request request = new Request.Builder()
                .url("https://fieldsight.s3.amazonaws.com/logo/Asia_P3_Hub.jpg")
                .build();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                double timeTakenMills = Math.floor(System.currentTimeMillis() - startTime);
                if (timeTakenMills >= 10000) {
                    callback.message("Taking longer than expected");
                    timer.cancel();
                }
            }
        }, 0, 1000);


        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        e.printStackTrace();
                        callback.networkQuality(NetworkSpeed.UNKNOWN);
                        callback.onEnd();
                    }


                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (!response.isSuccessful()){
                            callback.networkQuality(NetworkSpeed.UNKNOWN);
                            callback.onEnd();
                            return;
//                            throw new IOException("Unexpected code " + response);
                        }


                        Headers responseHeaders = response.headers();
                        for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                            Log.d(TAG, responseHeaders.name(i) + ": " + responseHeaders.value(i));
                        }

                        try (InputStream input = response.body().byteStream()) {
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            byte[] buffer = new byte[1024];

                            while (input.read(buffer) != -1) {
                                bos.write(buffer);
                            }
                            byte[] docBuffer = bos.toByteArray();
                            fileSize = bos.size();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            callback.networkQuality(NetworkSpeed.UNKNOWN);
                        } catch (SocketTimeoutException e) {
                            e.printStackTrace();
                            callback.networkQuality(NetworkSpeed.UNKNOWN);

                        }


                        endTime = System.currentTimeMillis();

                        // calculate how long it took by subtracting endtime from starttime
                        double timeTakenMills = Math.floor(endTime - startTime);  // time taken in milliseconds
                        double timeTakenSecs = timeTakenMills / 1000;  // divide by 1000 to get time in seconds
                        final int kilobytePerSec = (int) Math.round(1024 / timeTakenSecs);

                        if (kilobytePerSec <= POOR_BANDWIDTH) {
                            callback.networkQuality(NetworkSpeed.POOR);
                        } else if (kilobytePerSec <= GOOD_BANDWIDTH) {
                            callback.networkQuality(NetworkSpeed.AVERAGE);
                        } else {
                            callback.networkQuality(NetworkSpeed.GOOD);
                        }


                        // get the download speed by dividing the file size by time taken to download
                        double speed = fileSize / timeTakenMills;

                        Log.d(TAG, "Time taken in secs: " + timeTakenSecs);
                        Log.d(TAG, "kilobyte per sec: " + kilobytePerSec);
                        Log.d(TAG, "Download Speed: " + speed);
                        Log.d(TAG, "File size: " + fileSize);
                        try {
                            Thread.sleep(2000);
                            callback.onEnd();
                        } catch (InterruptedException e) {
                            callback.onEnd();
                            e.printStackTrace();
                        }

                    }
                });
    }


    public interface ConnectionTestCallback {
        void networkQuality(NetworkSpeed networkSpeed);

        void onStart();

        void onEnd();

        void message(String message);
    }

    public enum NetworkSpeed {
        POOR,
        AVERAGE,
        GOOD,
        UNKNOWN
    }
}