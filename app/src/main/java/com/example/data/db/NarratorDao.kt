package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.HadithNarratorCrossRef
import com.example.data.model.HadithWithNarrators
import com.example.data.model.NarratorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NarratorDao {

    @Query("SELECT * FROM narrators ORDER BY name ASC")
    fun getAllNarrators(): Flow<List<NarratorEntity>>

    @Query("SELECT * FROM narrators WHERE id = :id LIMIT 1")
    fun getNarratorById(id: Long): Flow<NarratorEntity?>

    @Query("""
        SELECT n.* FROM narrators n
        INNER JOIN hadith_narrators hn ON n.id = hn.narratorId
        WHERE hn.hadithId = :hadithId
        ORDER BY hn.positionInChain ASC
    """)
    fun getNarratorsForHadith(hadithId: Long): Flow<List<NarratorEntity>>

    @Transaction
    @Query("SELECT * FROM hadiths WHERE id = :hadithId LIMIT 1")
    fun getHadithWithNarrators(hadithId: Long): Flow<HadithWithNarrators?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNarrators(narrators: List<NarratorEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRefs(crossRefs: List<HadithNarratorCrossRef>)

    @Query("SELECT COUNT(*) FROM narrators")
    suspend fun getNarratorCount(): Int
}
