package com.alefglobalintegralproductivityconsulting.alef_app.ui.fragments.information_user.academic

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.alefglobalintegralproductivityconsulting.alef_app.R
import com.alefglobalintegralproductivityconsulting.alef_app.core.StepViewListener
import com.alefglobalintegralproductivityconsulting.alef_app.core.Validators.Companion.onBackPress
import com.alefglobalintegralproductivityconsulting.alef_app.databinding.FragmentAcademicBinding
import com.alefglobalintegralproductivityconsulting.alef_app.ui.fragments.information_user.viewmodel.InfoUserViewModel


class AcademicFragment : Fragment(R.layout.fragment_academic) {

    private lateinit var mBinding: FragmentAcademicBinding
    private val mInfoUserViewModel: InfoUserViewModel by activityViewModels()
    private var listener: StepViewListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is StepViewListener) listener = activity as StepViewListener?
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = FragmentAcademicBinding.bind(view)

        val stepView = arrayListOf(0, R.id.personalFragment)
        onBackPress(requireActivity(), listener, stepView)
        setupTextFields()

        mInfoUserViewModel.getInfoUser().observe(viewLifecycleOwner, { user ->
            Log.d("AcademicFragment", user.toString())
            mBinding.tvExample.text = "Nombre: ${user.name}"
        })
    }

    private fun setupTextFields() {
        with(mBinding) {
            fabReturn.setOnClickListener { listener?.onSelectStepView(0, R.id.personalFragment) }
            fabNext.setOnClickListener {
//                listener?.onSelectStepView(2, R.id.jobFragment)
                Toast.makeText(requireContext(), "En desarrollo", Toast.LENGTH_SHORT).show()
            }
        }
    }

}