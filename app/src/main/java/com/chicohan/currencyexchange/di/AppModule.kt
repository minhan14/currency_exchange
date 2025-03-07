package com.chicohan.currencyexchange.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.chicohan.currencyexchange.R
import com.chicohan.currencyexchange.data.api.CurrencyApi
import com.chicohan.currencyexchange.data.db.dao.ExchangeRateDao
import com.chicohan.currencyexchange.data.db.database.ExchangeRateDatabase
import com.chicohan.currencyexchange.data.repository.ExchangeRateRepository
import com.chicohan.currencyexchange.data.repository.ExchangeRateRepositoryImpl
import com.chicohan.currencyexchange.helper.Constants.BASE_URL
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideChuckerInterceptor(@ApplicationContext context: Context): ChuckerInterceptor {
        return ChuckerInterceptor.Builder(context)
            .collector(ChuckerCollector(context))
            .maxContentLength(250000L)
            .redactHeaders(emptySet())
            .alwaysReadResponseBody(false)
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(chuckerInterceptor: ChuckerInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(chuckerInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()



    @Provides
    @Singleton
    fun provideCurrencyApi(retrofit: Retrofit): CurrencyApi =
        retrofit.create(CurrencyApi::class.java)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ExchangeRateDatabase {
        return Room.databaseBuilder(context, ExchangeRateDatabase::class.java, "currency_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideExchangeRateDao(db: ExchangeRateDatabase): ExchangeRateDao = db.exchangeRateDao()

    @Provides
    @Singleton
    fun provideExcRateRepository(
        dao: ExchangeRateDao,
        api: CurrencyApi,
        @ApplicationContext context: Context
    ): ExchangeRateRepository {
        return ExchangeRateRepositoryImpl(
            api = api,
            currencyDao = dao,
            context = context
        )
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("currency_prefs", Context.MODE_PRIVATE)
    }

    @Singleton
    @Provides
    fun provideGlideInstance(
        @ApplicationContext context: Context
    ) = Glide.with(context).setDefaultRequestOptions(
        RequestOptions()
            .placeholder(R.drawable.ic_launcher_background)
            //  .error(R.drawable.baseline_running_with_errors_24)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
    )

    @Provides
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default


}