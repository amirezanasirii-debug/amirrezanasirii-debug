package com.example.data.util

object HadithTextNormalizer {

    private val DIACRITICS_REGEX = Regex("[\\u0617-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06DC\\u06DF-\\u06E8\\u06EA-\\u06ED]")

    /**
     * نرمال‌سازی متون عربی و فارسی برای جستجوی دقیق، سریع و بدون اعراب.
     * اعراب، تنوین‌ها، تشدیدها و تفاوت‌های نگارشی حروف ی/ک و انواع الف را یکسان می‌سازد.
     */
    fun normalize(text: String): String {
        if (text.isBlank()) return ""

        var normalized = DIACRITICS_REGEX.replace(text, "")

        // یکسان‌سازی انواع الف
        normalized = normalized.replace('أ', 'ا')
            .replace('إ', 'ا')
            .replace('آ', 'ا')
            .replace('ٱ', 'ا')

        // یکسان‌سازی ی و ک فارسی و عربی
        normalized = normalized.replace('ي', 'ی')
            .replace('ى', 'ی')
            .replace('ك', 'ک')

        // یکسان‌سازی تاء تأنیث و هاء
        normalized = normalized.replace('ة', 'ه')

        // حذف کشیدگی حروف (تطویل)
        normalized = normalized.replace("ـ", "")

        return normalized.trim().lowercase()
    }
}
