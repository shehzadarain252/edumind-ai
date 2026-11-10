package com.edumind.ai.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// ---------- Entities ----------

@Entity(tableName = "saved_answers")
data class SavedAnswerEntity(
    @PrimaryKey val id: String,
    val question: String,
    val answer: String,
    val subject: String?,
    val source: String,
    val timestamp: Long,
    val isFavorite: Boolean = false,
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val role: String, // "USER" or "AI"
    val text: String,
    val timestamp: Long,
    val subject: String?,
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val lastUpdated: Long,
)

@Entity(tableName = "planner_tasks")
data class PlannerTaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val dateMillis: Long,
    val durationMinutes: Int,
    val done: Boolean = false,
)

/** Outbox for Contact Us / Feedback messages (stored on-device). */
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val subject: String,
    val message: String,
    val type: String, // "contact" or "feedback"
    val timestamp: Long,
)

// ---------- DAOs ----------

@Dao
interface SavedAnswersDao {
    @Query("SELECT * FROM saved_answers ORDER BY timestamp DESC")
    fun getAll(): Flow<List<SavedAnswerEntity>>

    @Query("SELECT * FROM saved_answers WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<SavedAnswerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(answer: SavedAnswerEntity)

    @Update
    suspend fun update(answer: SavedAnswerEntity)

    @Delete
    suspend fun delete(answer: SavedAnswerEntity)

    @Query("DELETE FROM saved_answers WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM conversations ORDER BY lastUpdated DESC")
    fun getConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessages(conversationId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE conversationId = :conversationId")
    suspend fun deleteConversationMessages(conversationId: String)

    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun deleteConversation(conversationId: String)
}

@Dao
interface PlannerDao {
    @Query("SELECT * FROM planner_tasks ORDER BY dateMillis ASC")
    fun getAll(): Flow<List<PlannerTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: PlannerTaskEntity)

    @Update
    suspend fun update(task: PlannerTaskEntity)

    @Delete
    suspend fun delete(task: PlannerTaskEntity)
}

@Dao
interface MessagesDao {
    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAll(): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    @Delete
    suspend fun delete(message: MessageEntity)
}

// ---------- Database ----------

@Database(
    entities = [
        SavedAnswerEntity::class,
        ChatMessageEntity::class,
        ConversationEntity::class,
        PlannerTaskEntity::class,
        MessageEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class EduMindDatabase : RoomDatabase() {
    abstract fun savedAnswersDao(): SavedAnswersDao
    abstract fun chatDao(): ChatDao
    abstract fun plannerDao(): PlannerDao
    abstract fun messagesDao(): MessagesDao

    companion object {
        @Volatile
        private var INSTANCE: EduMindDatabase? = null

        fun getInstance(context: Context): EduMindDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    EduMindDatabase::class.java,
                    "edumind.db",
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
