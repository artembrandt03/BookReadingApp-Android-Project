package com.example.mobile_dev_project.di

import android.content.Context
import com.example.mobile_dev_project.data.repository.TtsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**This is a Hilt module based on TTS Reository
 * We just tell Hilt how to construct it
 */

@Module
@InstallIn(SingletonComponent::class)
object TtsModule {

    @Provides
    @Singleton
    fun provideTtsRepository(
        @ApplicationContext context: Context
    ): TtsRepository = TtsRepository(context)
}