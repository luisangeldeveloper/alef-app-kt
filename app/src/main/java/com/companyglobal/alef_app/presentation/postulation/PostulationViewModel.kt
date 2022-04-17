package com.companyglobal.alef_app.presentation.postulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import com.companyglobal.alef_app.core.Result
import com.companyglobal.alef_app.data.model.postulation.RequestPostulation
import com.companyglobal.alef_app.domain.postulation.PostulationRepo
import kotlinx.coroutines.Dispatchers

class PostulationViewModel(private val repo: PostulationRepo) : ViewModel() {

    fun getPostulation(idVacant: String) =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())

            try {
                emit(Result.Success(repo.getPostulation(idVacant)))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }

        }

    fun createPostulation(idVacant: String, requestPostulation: RequestPostulation) =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())

            try {
                emit(Result.Success(repo.createPostulation(idVacant, requestPostulation)))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }

        }

    fun fetchPostulations() = liveData(Dispatchers.IO) {
        emit(Result.Loading())

        try {
            emit(Result.Success(repo.getPostulations()))
        } catch (e: Exception) {
            emit(Result.Failure(e))
        }

    }

}

class PostulationViewModelFactory(private val repo: PostulationRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(PostulationRepo::class.java).newInstance(repo)
    }
}