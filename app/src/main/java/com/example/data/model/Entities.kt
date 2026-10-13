package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * موجودیت کتب روایی (شیعه و اهل‌سنت)
 */
@Entity(
    tableName = "books",
    indices = [
        Index(value = ["collection"]),
        Index(value = ["orderIndex"])
    ]
)
data class BookEntity(
    @PrimaryKey val id: String,
    val collection: String, // "SHIA" یا "SUNNI"
    val title: String,
    val author: String,
    val century: String,
    val hadithCount: Int,
    val description: String,
    val orderIndex: Int
)

/**
 * موجودیت ابواب و فصول با رابطه کلید خارجی به کتاب والد
 */
@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["bookId"]),
        Index(value = ["orderIndex"])
    ]
)
data class ChapterEntity(
    @PrimaryKey val id: String,
    val bookId: String,
    val titleArabic: String,
    val titlePersian: String,
    val orderIndex: Int
)

/**
 * موجودیت احادیث شریف با کلید خارجی به کتاب و باب مربوطه
 */
@Entity(
    tableName = "hadiths",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["bookId"]),
        Index(value = ["chapterId"]),
        Index(value = ["collection"]),
        Index(value = ["numberInBook"])
    ]
)
data class HadithEntity(
    @PrimaryKey val id: Long,
    val bookId: String,
    val chapterId: String,
    val collection: String, // "SHIA" یا "SUNNI"
    val numberInBook: Int,
    val textArabic: String,
    val textPersian: String,
    val isnadChain: String,
    val sourceReference: String,
    val grade: String,
    val searchableText: String
)

/**
 * موجودیت مشخصات راویان و اسناد (طبقه، توثیق رجالی و زندگی‌نامه)
 */
@Entity(
    tableName = "narrators",
    indices = [
        Index(value = ["name"]),
        Index(value = ["era"])
    ]
)
data class NarratorEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val familiarName: String, // کنیه، لقب یا شهرت
    val era: String, // طبقه روایی (صحابی، تابعی، از اصحاب ائمه و...)
    val scholarlyAssessment: String, // توثیق و اعتبار سندی رجالی
    val notes: String
)

/**
 * جدول واسط چندبه‌چند (Junction Table) بین احادیث و راویان در سلسله سند
 */
@Entity(
    tableName = "hadith_narrators",
    primaryKeys = ["hadithId", "narratorId"],
    foreignKeys = [
        ForeignKey(
            entity = HadithEntity::class,
            parentColumns = ["id"],
            childColumns = ["hadithId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = NarratorEntity::class,
            parentColumns = ["id"],
            childColumns = ["narratorId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["hadithId"]),
        Index(value = ["narratorId"]),
        Index(value = ["positionInChain"])
    ]
)
data class HadithNarratorCrossRef(
    val hadithId: Long,
    val narratorId: Long,
    val positionInChain: Int // جایگاه حلقه روایت در سند (۱ = راوی نخست، ۲ = راوی دوم و...)
)

/**
 * نشان‌شده‌ها با کلید خارجی به جدول احادیث
 */
@Entity(
    tableName = "bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = HadithEntity::class,
            parentColumns = ["id"],
            childColumns = ["hadithId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["hadithId"])
    ]
)
data class BookmarkEntity(
    @PrimaryKey val hadithId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null
)

/**
 * یادداشت‌های پژوهشی با کلید خارجی به جدول احادیث
 */
@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = HadithEntity::class,
            parentColumns = ["id"],
            childColumns = ["hadithId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["hadithId"])
    ]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hadithId: Long,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * لیست مقایسه روایات با کلید خارجی به جدول احادیث
 */
@Entity(
    tableName = "comparisons",
    foreignKeys = [
        ForeignKey(
            entity = HadithEntity::class,
            parentColumns = ["id"],
            childColumns = ["hadithId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["hadithId"])
    ]
)
data class ComparisonItemEntity(
    @PrimaryKey val hadithId: Long,
    val timestamp: Long = System.currentTimeMillis()
)

// ==========================================
// مدل‌های رابطه رابطه‌ای (Relations)
// ==========================================

/**
 * حدیث به همراه فهرست راویان استخراج‌شده از جدول واسط
 */
data class HadithWithNarrators(
    @Embedded val hadith: HadithEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = HadithNarratorCrossRef::class,
            parentColumn = "hadithId",
            entityColumn = "narratorId"
        )
    )
    val narrators: List<NarratorEntity>
)

/**
 * باب به همراه تمامی احادیث مندرج در آن
 */
data class ChapterWithHadiths(
    @Embedded val chapter: ChapterEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "chapterId"
    )
    val hadiths: List<HadithEntity>
)

/**
 * کتاب به همراه تمامی ابواب آن
 */
data class BookWithChapters(
    @Embedded val book: BookEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "bookId"
    )
    val chapters: List<ChapterEntity>
)
