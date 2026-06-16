package com.example.studyswipe.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.studyswipe.model.Subject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        MatchEntity::class,
        MessageEntity::class,
        SubjectEntity::class,
        UserSubjectEntity::class,
        LikeEntity::class,
        DislikeEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun chatDao(): ChatDao
    abstract fun subjectDao(): SubjectDao

    companion object {
        // @Volatile = valoarea e intotdeauna citita din/scrisa in memoria principala
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            // synchronized = doar un thread poate executa acest bloc la un moment dat

            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "studyswipe_database" // numele fisierului .db de pe telefon
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        // Seed subjects whenever the database is opened
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val dbInstance = getInstance(context)
                                val subjectEntities = Subject.entries.map {
                                    SubjectEntity(id = it.name, name = it.name, displayName = it.displayName)
                                }
                                dbInstance.subjectDao().insertAll(subjectEntities)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
