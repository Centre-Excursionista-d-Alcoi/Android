package com.arnyminerz.cea.app.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.prof.rssparser.Article
import com.prof.rssparser.Parser
import kotlinx.coroutines.launch

class NewsViewModel(application: Application) : AndroidViewModel(application) {
    val news = mutableStateOf<List<Article>?>(null)

    private val parser = Parser.Builder()
        .context(getApplication())
        // .charset(Charset.forName("ISO-8859-7"))
        .cacheExpirationMillis(24L * 60L * 60L * 1000L) // one day
        .build()

    fun loadNews() {
        viewModelScope.launch {
            val channel = parser.getChannel("https://centrexcursionistalcoi.org/feed/")
            news.value = channel.articles
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NewsViewModel(application) as T
    }
}