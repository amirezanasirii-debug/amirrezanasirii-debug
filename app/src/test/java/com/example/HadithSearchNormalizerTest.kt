package com.example

import com.example.data.seed.InitialHadithData
import com.example.data.util.HadithTextNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HadithSearchNormalizerTest {

    @Test
    fun testDiacriticsRemoval() {
        val originalArabic = "إِنَّمَا الْأَعْمَالُ بِالنِّيَّاتِ"
        val normalized = HadithTextNormalizer.normalize(originalArabic)
        // بررسی حذف تنوین، تشدید، حرکات و یکسان‌سازی الف
        assertTrue(normalized.contains("انما"))
        assertTrue(normalized.contains("الاعمال"))
        assertTrue(normalized.contains("بالنیات"))
    }

    @Test
    fun testPersianArabicKafAndYehNormalization() {
        val arabicText = "علي بن أبي طالب كرم الله وجهه"
        val normalized = HadithTextNormalizer.normalize(arabicText)
        // ی و ک باید یکسان شوند
        assertTrue(normalized.contains("علی"))
        assertTrue(normalized.contains("ابی"))
        assertTrue(normalized.contains("کرم"))
    }

    @Test
    fun testSeedDataIntegrity() {
        val books = InitialHadithData.getInitialBooks()
        val shiaBooks = books.filter { it.collection == "SHIA" }
        val sunniBooks = books.filter { it.collection == "SUNNI" }

        assertTrue(shiaBooks.isNotEmpty())
        assertTrue(sunniBooks.isNotEmpty())

        val hadiths = InitialHadithData.getInitialHadiths()
        assertTrue(hadiths.size >= 10)
        hadiths.forEach { h ->
            assertTrue(h.textArabic.isNotBlank())
            assertTrue(h.textPersian.isNotBlank())
            assertTrue(h.isnadChain.isNotBlank())
            assertTrue(h.searchableText.isNotBlank())
        }

        val narrators = InitialHadithData.getInitialNarrators()
        assertTrue(narrators.isNotEmpty())
        narrators.forEach { n ->
            assertTrue(n.name.isNotBlank())
            assertTrue(n.scholarlyAssessment.isNotBlank())
        }

        val crossRefs = InitialHadithData.getInitialCrossRefs()
        assertTrue(crossRefs.isNotEmpty())
        crossRefs.forEach { ref ->
            assertTrue(hadiths.any { it.id == ref.hadithId })
            assertTrue(narrators.any { it.id == ref.narratorId })
        }
    }
}
