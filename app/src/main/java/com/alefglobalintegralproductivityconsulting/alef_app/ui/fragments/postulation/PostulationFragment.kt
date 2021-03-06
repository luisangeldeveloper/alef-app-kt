package com.alefglobalintegralproductivityconsulting.alef_app.ui.fragments.postulation

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.alefglobalintegralproductivityconsulting.alef_app.R
import com.alefglobalintegralproductivityconsulting.alef_app.core.Result
import com.alefglobalintegralproductivityconsulting.alef_app.data.model.Postulation
import com.alefglobalintegralproductivityconsulting.alef_app.data.remote.postulation.RemotePostulationDataSource
import com.alefglobalintegralproductivityconsulting.alef_app.databinding.FragmentPostulationBinding
import com.alefglobalintegralproductivityconsulting.alef_app.domain.postulation.PostulationRepoImpl
import com.alefglobalintegralproductivityconsulting.alef_app.presentation.postulation.PostulationViewModel
import com.alefglobalintegralproductivityconsulting.alef_app.presentation.postulation.PostulationViewModelFactory
import com.alefglobalintegralproductivityconsulting.alef_app.ui.fragments.postulation.adapter.PostulationAdapter

class PostulationFragment : Fragment(R.layout.fragment_postulation),
    PostulationAdapter.OnPostulationClickListener {

    private lateinit var mBinding: FragmentPostulationBinding
    private lateinit var mAdapter: PostulationAdapter

    private val mViewModel by viewModels<PostulationViewModel> {
        PostulationViewModelFactory(
            PostulationRepoImpl(RemotePostulationDataSource())
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = FragmentPostulationBinding.bind(view)

        setupPostulations()
    }

    private fun setupPostulations() {
        mViewModel.fetchPostulations().observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Failure -> {
                    mBinding.llLoading.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Ocurrio un error: ${result.exception}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Result.Loading -> {
                    mBinding.llLoading.visibility = View.VISIBLE
                }
                is Result.Success -> {
                    mBinding.llLoading.visibility = View.GONE

                    if (result.data.isEmpty()) {
                        mBinding.llDisconnected.visibility = View.VISIBLE
                        mBinding.llConnected.visibility = View.GONE
                        return@observe
                    } else {
                        mBinding.llDisconnected.visibility = View.GONE
                        mBinding.llConnected.visibility = View.VISIBLE
                    }

                    mAdapter = PostulationAdapter(result.data, this)
                    mBinding.rvPostulation.adapter = mAdapter
                }
            }

        }
    }

    override fun onPostulationClick(postulation: Postulation) {
        Log.d("PostulationFragment", postulation.toString())
    }

}