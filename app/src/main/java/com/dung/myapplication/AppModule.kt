package com.dung.myapplication

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@Qualifier
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.BINARY)
annotation class GlobalCoroutineScope

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Để trống hoặc thêm các @Provides sau này
}
