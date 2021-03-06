package np.com.naxa.staffattendance;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import np.com.naxa.staffattendance.data.APIClient;
import np.com.naxa.staffattendance.data.ApiInterface;
import np.com.naxa.staffattendance.pojo.BankPojo;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class FormCall {

    public Observable<List<String>> getDesignation() {
        ApiInterface apiService = APIClient.getUploadClient().create(ApiInterface.class);
        return apiService.getDesignation()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMapIterable(new Func1<ArrayList<ArrayList<String>>, Iterable<ArrayList<String>>>() {
                    @Override
                    public Iterable<ArrayList<String>> call(ArrayList<ArrayList<String>> arrayLists) {
                        return arrayLists;
                    }
                })
                .flatMapIterable(new Func1<ArrayList<String>, Iterable<String>>() {
                    @Override
                    public Iterable<String> call(ArrayList<String> strings) {
                        return strings;
                    }
                }).filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return !TextUtils.isDigitsOnly(s);
                    }
                })
                .toList();
    }

    public Observable<List<String>> getBankList(  ) {
        ApiInterface apiService = APIClient.getUploadClient().create(ApiInterface.class);
        return apiService.getBankist()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMapIterable(new Func1<ArrayList<BankPojo>, Iterable<BankPojo>>() {
                    @Override
                    public Iterable<BankPojo> call(ArrayList<BankPojo> bankPojos) {
                        return bankPojos;
                    }
                })
                .flatMap(new Func1<BankPojo, Observable<String>>() {
                    @Override
                    public Observable<String> call(BankPojo bankPojo) {
                        return Observable.just(bankPojo.getName());
                    }
                }).toList();

    }


    public interface DesignationListener {
        void designation(ArrayList<ArrayList<String>> arrayLists);
    }

    public interface BankListListener {
        void bankList(ArrayList<BankPojo> arrayLists);
    }
}
