package dictionary.me.com.dictionary;

import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GetDataService {

    @GET("/translate?from=eng&dest=bg&format=json&tm=false&page=1")
    Object getTranslation(@Query("phrase") String address);
    //void getPositionByZip(@Query("address") String address, Callback<String> cb);
}
