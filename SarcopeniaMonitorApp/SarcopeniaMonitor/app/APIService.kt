import com.example.sarcopeniamonitor.MyRecord
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("api/messages/")
    fun getMessages(): Call<List<Message>>

    @POST("api/messages/")
    fun postMessage(@Body message: Message): Call<Message>
}