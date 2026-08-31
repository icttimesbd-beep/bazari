package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.EventPlanDao
import com.example.data.local.dao.HistoryDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.ProductMemoryDao
import com.example.data.local.dao.QuickTapeNoteDao
import com.example.data.local.dao.ShoppingItemDao
import com.example.data.local.dao.ShoppingListDao
import com.example.data.local.dao.TemplateDao
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.EventExpenseEntity
import com.example.data.local.entity.EventMemberEntity
import com.example.data.local.entity.EventPlanEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ProductMemoryEntity
import com.example.data.local.entity.QuickTapeNoteEntity
import com.example.data.local.entity.ShoppingHistoryEntity
import com.example.data.local.entity.ShoppingItemEntity
import com.example.data.local.entity.ShoppingListEntity
import com.example.data.local.entity.TemplateEntity
import com.example.data.local.entity.TemplateItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        CategoryEntity::class,
        ShoppingListEntity::class,
        ShoppingItemEntity::class,
        TemplateEntity::class,
        TemplateItemEntity::class,
        ProductMemoryEntity::class,
        ShoppingHistoryEntity::class,
        EventPlanEntity::class,
        EventMemberEntity::class,
        EventExpenseEntity::class,
        QuickTapeNoteEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun shoppingItemDao(): ShoppingItemDao
    abstract fun templateDao(): TemplateDao
    abstract fun productMemoryDao(): ProductMemoryDao
    abstract fun historyDao(): HistoryDao
    abstract fun eventPlanDao(): EventPlanDao
    abstract fun quickTapeNoteDao(): QuickTapeNoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bazari_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            scope.launch {
                                INSTANCE?.let { database ->
                                    CatalogDataLoader(context.applicationContext).loadCatalogIfNeeded(database)
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
