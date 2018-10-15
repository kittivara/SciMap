package th.ac.kmitl.science.scimap.service

import io.reactivex.Observable
import retrofit2.Call
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

public interface KmitlMapService {

    @GET("areas")
    fun listAllAreas(): Call<Array<Area>>

    @GET("areas/{id}")
    fun getArea(@Path("id") id: Int): Call<Area>

    @GET("areas/{id}/buildings")
    fun getBuildingsOfArea(@Path("id") id: Int): Call<Array<Building>>

    /**
     * Factory class for convenient creation of the Api Service interface
     */

    companion object {
        fun create(): KmitlMapService {
            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://kmitl-map.herokuapp.com")
                    .build()

            return retrofit.create(KmitlMapService::class.java)
        }
    }
}