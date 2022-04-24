package com.companyglobal.alef_app.ui.fragments.home

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.MatrixCursor
import android.os.Build
import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.companyglobal.alef_app.R
import com.companyglobal.alef_app.core.AppConstants
import com.companyglobal.alef_app.core.OnVacantClickListener
import com.companyglobal.alef_app.core.Result
import com.companyglobal.alef_app.core.utils.Validators
import com.companyglobal.alef_app.data.model.Vacant
import com.companyglobal.alef_app.data.remote.home.RemoteHomeDataSource
import com.companyglobal.alef_app.databinding.FragmentHomeBinding
import com.companyglobal.alef_app.domain.home.HomeRepoImpl
import com.companyglobal.alef_app.presentation.home.HomeViewModel
import com.companyglobal.alef_app.presentation.home.HomeViewModelFactory
import com.companyglobal.alef_app.services.auth.RetrofitClientAuth
import com.companyglobal.alef_app.ui.LoginActivity
import com.companyglobal.alef_app.ui.SearchActivity
import com.companyglobal.alef_app.ui.fragments.home.adapter.VacantAdapter
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment(R.layout.fragment_home), VacantAdapter.OnVacantClickListener {

    private lateinit var mBinding: FragmentHomeBinding
    private var listener: OnVacantClickListener? = null

    private lateinit var mAdapter: VacantAdapter

    private var isLogin: Boolean = false

    private val mViewModel by viewModels<HomeViewModel> {
        HomeViewModelFactory(
            HomeRepoImpl(RemoteHomeDataSource(RetrofitClientAuth.webServiceAuth))
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is OnVacantClickListener) listener = activity as OnVacantClickListener?
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = FragmentHomeBinding.bind(view)
        hideKeyboard()

        onBackPress()
        setupArguments()

        mBinding.swipeRefreshLayout.setOnRefreshListener {
            mBinding.swipeRefreshLayout.isRefreshing = true
            mBinding.llLoading.visibility = View.VISIBLE
            mBinding.llConnected.visibility = View.GONE
            setupVacancies(true)
        }

        setupVacancies(false)
        setupTextField()
        setupSearch()
    }

    private fun setupSearch() {
        with(mBinding) {
            val from = arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1)
            val to = intArrayOf(R.id.tvItemSuggestion)
            val cursorAdapter = SimpleCursorAdapter(
                context,
                R.layout.item_suggestions,
                null,
                from,
                to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
            )
            val suggestions = listOf(
                "Aguascalientes", "Baja California", "Baja California Sur", "Campeche", "Coahuila",
                "Colima", "Chiapas", "Chihuahua", "Durango", "Distrito Federal", "Guanajuato",
                "Guerrero", "Hidalgo", "Jalisco", "México", "Michoacán", "Morelos", "Nayarit",
                "Nuevo León", "Oaxaca", "Puebla", "Querétaro", "Quintana Roo", "San Luis Potosí",
                "Sinaloa", "Sonora", "Tabasco", "Tamaulipas", "Tlaxcala", "Veracruz", "Yucatán",
                "Zacatecas"
            )

            svLocation.suggestionsAdapter = cursorAdapter
            svLocation.onActionViewExpanded()
            svLocation.isFocusable = false
            svLocation.clearFocus()

            svLocation.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    hideKeyboard()
                    return false
                }

                override fun onQueryTextChange(query: String?): Boolean {
                    val cursor =
                        MatrixCursor(arrayOf(BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1))
                    query?.let {
                        suggestions.forEachIndexed { index, suggestion ->
                            if (suggestion.contains(query, true))
                                cursor.addRow(arrayOf(index, suggestion))
                        }
                    }

                    cursorAdapter.changeCursor(cursor)
                    return true
                }
            })

            svLocation.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
                override fun onSuggestionSelect(position: Int): Boolean {
                    hideKeyboard()
                    return false
                }

                @SuppressLint("Range")
                override fun onSuggestionClick(position: Int): Boolean {
                    val cursor = svLocation.suggestionsAdapter.getItem(position) as Cursor
                    val selection =
                        cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1))
                    svLocation.setQuery(selection, false)
                    return true
                }
            })
        }
    }

    private fun setupTextField() {
        with(mBinding) {
            btnSearch.setOnClickListener {
                val search = etSearch.text.toString().trim()
                val location = svLocation.query.toString().trim()

                val i = Intent(requireContext(), SearchActivity::class.java)
                i.putExtra(AppConstants.SEARCH_GENERAL, search)
                i.putExtra(AppConstants.SEARCH_LOCATION, location)
                startActivity(i)
            }
        }
    }

    private fun setupArguments() {
        arguments?.getBoolean(AppConstants.IS_LOGIN_USER)?.let {
            isLogin = it
            if (isLogin) {
                llTestCompany.visibility = View.VISIBLE
            } else {
                llTestCompany.visibility = View.GONE
            }
        }
    }

    private fun setupVacancies(isRefresh: Boolean) {

        mViewModel.fetchVacancies().observe(viewLifecycleOwner) { result ->
            Log.d("TAG",  result.toString())
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

                    mAdapter = VacantAdapter(result.data, this)
                    mBinding.rvVacancies.adapter = mAdapter
                }
            }
        }

        if (isRefresh) {
            mBinding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onVacantClick(vacant: Vacant) {
        val gson = Gson()
        val jsonVacant = gson.toJson(vacant)
//
//        val bundle = bundleOf(
//            AppConstants.DETAILS_VACANT to jsonVacant,
//            AppConstants.VACANT_INFO_EXTRA to jsonVacantInfoExtra
//        )
//        findNavController().navigate(R.id.action_nav_home_to_vacantDetailsFragment, bundle)

        listener?.onVacantDetails(jsonVacant, false)
    }

    private fun hideKeyboard() {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(mBinding.root.windowToken, 0)
    }

    private fun onBackPress() {
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isLogin) {
                        activity?.finish()
                    } else {
                        val i = Intent(requireContext(), LoginActivity::class.java)
                        startActivity(i)
                        activity?.finish()
                    }
                }
            }

        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)
    }
}