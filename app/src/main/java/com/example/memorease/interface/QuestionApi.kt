import com.example.memorease.data.QuestionRequest
import com.example.memorease.data.QuestionResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call

interface QuestionApi {
    @POST("generate-question")
    fun generateQuestion(@Body request: QuestionRequest): Call<QuestionResponse>
}
