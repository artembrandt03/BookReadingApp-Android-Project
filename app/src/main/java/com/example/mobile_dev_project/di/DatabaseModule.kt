package com.example.mobile_dev_project.di

import android.content.Context
import com.example.mobile_dev_project.data.repository.FileRepository
import com.example.mobile_dev_project.data.db.AppDatabase
import com.example.mobile_dev_project.data.db.dao.BookDao
import com.example.mobile_dev_project.data.db.dao.ChapterDao
import com.example.mobile_dev_project.data.db.dao.ReadingProgressDao
import com.example.mobile_dev_project.data.repository.BookRepository
import com.example.mobile_dev_project.data.repository.ChapterRepository
import com.example.mobile_dev_project.data.repository.HtmlParserRepository
import com.example.mobile_dev_project.data.repository.ReadingProgressRepository
import com.example.mobile_dev_project.data.repository.SearchRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

/**
 * Hilt module that tells Hilt how to construct our singletons.
 * Logic for each @Provides is basically: "When someone asks for X, build it like this."
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    //For Database & DAOs

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    @Provides fun provideBookDao(db: AppDatabase): BookDao = db.bookDao()
    @Provides fun provideChapterDao(db: AppDatabase): ChapterDao = db.chapterDao()
    @Provides fun provideReadingProgressDao(db: AppDatabase): ReadingProgressDao = db.readingProgressDao()

    //some other app-scoped services we already use

    @Provides @Singleton
    fun provideFileRepository(client: OkHttpClient): FileRepository = FileRepository(client)

    @Provides @Singleton
    fun provideHtmlParserRepository(
        @ApplicationContext context: Context,
        bookRepository: BookRepository,
        chapterRepository: ChapterRepository
    ): HtmlParserRepository = HtmlParserRepository(context, bookRepository,
        chapterRepository)



    //Repositories wrapping the DAOs

    @Provides @Singleton
    fun provideBookRepository(dao: BookDao): BookRepository = BookRepository(dao)

    @Provides @Singleton
    fun provideChapterRepository(dao: ChapterDao): ChapterRepository = ChapterRepository(dao)

    @Provides @Singleton
    fun provideReadingProgressRepository(dao: ReadingProgressDao): ReadingProgressRepository = ReadingProgressRepository(dao)

    @Provides @Singleton
    fun provideSearchRepository(): SearchRepository = SearchRepository()
}

