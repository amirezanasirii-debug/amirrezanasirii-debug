package com.example.data.ai

import com.example.BuildConfig
import com.example.data.model.HadithEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class HadithAssistantService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun answerResearchQuery(
        userQuestion: String,
        retrievedHadiths: List<HadithEntity>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                // در صورت تنظیم نبودن کلید، پاسخ بومی مستند بر اساس روایات استخراج‌شده دیتابیس
                return@withContext Result.success(generateOfflineSummary(userQuestion, retrievedHadiths))
            }

            val contextBuilder = StringBuilder()
            contextBuilder.append("روایات استخراج‌شده از منابع برنامه جهت استناد علمی:\n")
            retrievedHadiths.take(6).forEachIndexed { index, hadith ->
                contextBuilder.append("${index + 1}. [منبع: ${hadith.sourceReference}] (${if (hadith.collection == "SHIA") "مذهب شیعه" else "مذهب اهل‌سنت"})\n")
                contextBuilder.append("متن عربی: ${hadith.textArabic}\n")
                contextBuilder.append("ترجمه فارسی: ${hadith.textPersian}\n")
                contextBuilder.append("سند: ${hadith.isnadChain}\n\n")
            }

            val systemInstruction = """
                تو دستیار پژوهشی و کتابدار تخصصی نرم‌افزار «جامع‌الأحادیث منصور» هستی.
                وظیفه تو پاسخگویی علمی و تحلیلی به پرسش‌های حدیثی کاربر صرفاً بر پایه متن روایات مستند زیر است.
                قوانین الزامی و خطوط قرمز:
                ۱. هرگز حدیث یا سندی جعل نکن.
                ۲. تفاوت منابع شیعه و اهل‌سنت را با احترام و بدون تعصب مذهبی با ذکر صریح منبع تفکیک کن.
                ۳. اگر اطلاعات کافی در روایات ارائه شده نیست، صریحاً بگو «در محدوده روایات حاضر، شواهد کافی یافت نشد».
                ۴. از صدور فتوا یا حکم فقهی یا جعل درجه رجالی خودداری نما.
                ۵. تمام پاسخ باید با فارسی روان و شیوا و مستند به نام کتاب و شماره حدیث باشد.
            """.trimIndent()

            val combinedPrompt = """
                $systemInstruction

                $contextBuilder

                پرسش کاربر:
                $userQuestion
            """.trimIndent()

            val requestBodyMap = mapOf(
                "contents" to listOf(
                    mapOf(
                        "parts" to listOf(
                            mapOf("text" to combinedPrompt)
                        )
                    )
                )
            )

            val jsonPayload = moshi.adapter(Map::class.java).toJson(requestBodyMap)
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(jsonPayload.toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.success(generateOfflineSummary(userQuestion, retrievedHadiths))
            }

            val responseString = response.body?.string() ?: ""
            val parsedResponse = moshi.adapter(Map::class.java).fromJson(responseString)
            @Suppress("UNCHECKED_CAST")
            val candidates = parsedResponse?.get("candidates") as? List<Map<String, Any>>
            val firstCandidate = candidates?.firstOrNull()
            @Suppress("UNCHECKED_CAST")
            val content = firstCandidate?.get("content") as? Map<String, Any>
            @Suppress("UNCHECKED_CAST")
            val parts = content?.get("parts") as? List<Map<String, Any>>
            val text = parts?.firstOrNull()?.get("text") as? String

            if (!text.isNullOrBlank()) {
                Result.success(text.trim())
            } else {
                Result.success(generateOfflineSummary(userQuestion, retrievedHadiths))
            }
        } catch (e: Exception) {
            Result.success(generateOfflineSummary(userQuestion, retrievedHadiths))
        }
    }

    private fun generateOfflineSummary(question: String, hadiths: List<HadithEntity>): String {
        if (hadiths.isEmpty()) {
            return "با توجه به جستجوی محلی، روایتی مرتبط با «$question» در بانک داده یافت نشد. لطفاً کلیدواژه‌های دیگری را امتحان کنید یا دامنه فیلتر کتابخانه را تغییر دهید."
        }
        val builder = StringBuilder()
        builder.append("نتایج مستند استخراج‌شده از منابع کتابخانه پیرامون موضوع «").append(question).append("»:\n\n")
        hadiths.take(4).forEachIndexed { i, h ->
            val sect = if (h.collection == "SHIA") "کتابخانه شیعه" else "کتابخانه اهل‌سنت"
            builder.append("${i + 1}. [${h.sourceReference}] - $sect\n")
            builder.append("متن شریف: ").append(h.textArabic).append("\n")
            builder.append("ترجمه فارسی: ").append(h.textPersian).append("\n\n")
        }
        builder.append("توضیح پژوهشی: روایات فوق به صورت آفلاین از پایگاه‌داده Room استخراج گردیده‌اند.")
        return builder.toString()
    }
}
