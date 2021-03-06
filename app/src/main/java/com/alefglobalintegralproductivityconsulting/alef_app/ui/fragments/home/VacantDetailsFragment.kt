package com.alefglobalintegralproductivityconsulting.alef_app.ui.fragments.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.alefglobalintegralproductivityconsulting.alef_app.R
import com.alefglobalintegralproductivityconsulting.alef_app.core.AppConstants
import com.alefglobalintegralproductivityconsulting.alef_app.core.utils.Timestamp
import com.alefglobalintegralproductivityconsulting.alef_app.data.model.DAYS
import com.alefglobalintegralproductivityconsulting.alef_app.data.model.Vacant
import com.alefglobalintegralproductivityconsulting.alef_app.data.model.VacantInfoExtra
import com.alefglobalintegralproductivityconsulting.alef_app.databinding.FragmentVacantDetailsBinding
import com.google.gson.Gson
import kotlinx.android.synthetic.main.item_availability.view.*
import kotlinx.android.synthetic.main.item_workday.view.*


class VacantDetailsFragment : Fragment(R.layout.fragment_vacant_details) {

    private lateinit var mBinding: FragmentVacantDetailsBinding

    private var mVacant: Vacant? = null
    private var mVacantInfoExtra: VacantInfoExtra? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = FragmentVacantDetailsBinding.bind(view)

        init()
        setupData()
    }

    private fun init() {
        val gson = Gson()
        val jsonVacant = arguments?.getString(AppConstants.DETAILS_VACANT)
        val jsonVacantInfoExtra = arguments?.getString(AppConstants.VACANT_INFO_EXTRA)

        mVacant = gson.fromJson(jsonVacant, Vacant::class.java)
        mVacantInfoExtra = gson.fromJson(jsonVacantInfoExtra, VacantInfoExtra::class.java)
    }

    private fun setupData() {
        with(mBinding) {
            tvTitleVacantDetails.text = mVacant?.title
            tvCompany.text = mVacant?.company
            tvLocation.text = mVacant?.location

            tvMode.text = mVacantInfoExtra?.mode

            if (mVacantInfoExtra?.companyPaid!!) {
                ivStateCompany.visibility = View.VISIBLE
            } else {
                ivStateCompany.visibility = View.GONE
            }

            if (!mVacantInfoExtra?.workDay.isNullOrEmpty()) {
                val workDayMap = mVacantInfoExtra?.workDay

                if (workDayMap != null) {
                    for ((day, hour) in workDayMap) {
                        val workDays: View = layoutInflater.inflate(
                            R.layout.item_workday,
                            llWorkDay,
                            false
                        )

                        var auxDay = ""
                        when (day) {
                            DAYS.MONDAY.num -> {
                                auxDay = DAYS.MONDAY.day
                            }
                            DAYS.TUESDAY.num -> {
                                auxDay = DAYS.TUESDAY.day
                            }
                            DAYS.WEDNESDAY.num -> {
                                auxDay = DAYS.WEDNESDAY.day
                            }
                            DAYS.THURSDAY.num -> {
                                auxDay = DAYS.THURSDAY.day
                            }
                            DAYS.FRIDAY.num -> {
                                auxDay = DAYS.FRIDAY.day
                            }
                            DAYS.SATURDAY.num -> {
                                auxDay = DAYS.SATURDAY.day
                            }
                            DAYS.SUNDAY.num -> {
                                auxDay = DAYS.SUNDAY.day
                            }
                        }

                        workDays.tvDay.text = auxDay
                        workDays.tvHour.text = hour

                        llWorkDay.addView(workDays)
                    }
                }
            }

            if (!mVacantInfoExtra?.availability.isNullOrEmpty()) {
                llCompanyInfoExtra.visibility = View.VISIBLE
                tvTitleAvailability.visibility = View.VISIBLE
                vAvailability.visibility = View.VISIBLE

                val availabilityMap = mVacantInfoExtra?.availability
                if (availabilityMap != null) {
                    for ((title, checked) in availabilityMap) {
                        val radioButtons: View = layoutInflater.inflate(
                            R.layout.item_availability,
                            llCompanyInfoExtra,
                            false
                        )

                        radioButtons.tvAvailability.text = title

                        if (checked) {
                            radioButtons.rbYes.isChecked = true
                            radioButtons.rbNo.isChecked = false
                        } else {
                            radioButtons.rbYes.isChecked = false
                            radioButtons.rbNo.isChecked = true
                        }

                        llCompanyInfoExtra.addView(radioButtons)
                    }
                }

            } else {
                llCompanyInfoExtra.visibility = View.GONE
                tvTitleAvailability.visibility = View.GONE
                vAvailability.visibility = View.GONE
            }

            val createdAt = (mVacant?.timestamp?.time?.div(1000L))?.let {
                Timestamp.getTimeAgo(it.toInt())
            }

            tvTimestamp.text = createdAt
            tvDescription.text = mVacant?.description

            if ((mVacant?.firstSalary != -1 || mVacant?.secondSalary != -1) && mVacant?.paymentMethod!!.isNotEmpty()) {
                llSalary.visibility = View.VISIBLE

                if (mVacant?.firstSalary != -1 && mVacant?.secondSalary != -1) {
                    tvSalary.text = "$${mVacant?.firstSalary} - $${mVacant?.secondSalary} MXN"
                } else if (mVacant?.firstSalary != -1) {
                    tvSalary.text = "$${mVacant?.firstSalary} MXN"
                } else if (mVacant?.secondSalary != -1) {
                    tvSalary.text = "$${mVacant?.secondSalary} MXN"
                }

                tvPayments.text = mVacant?.paymentMethod
            } else {
                llSalary.visibility = View.GONE
            }

            cbFavorite.isChecked = mVacant?.isFavorite!!
            cbFavorite.setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    Toast.makeText(context, "Agregado a favoritos", Toast.LENGTH_SHORT).show()
                }
            }

            btnApply.setOnClickListener {
                Toast.makeText(context, "Postularme", Toast.LENGTH_SHORT).show()
            }

            ibShare.setOnClickListener {
                shareVacant()
            }
        }
    }

    private fun shareVacant() {
        val url = "https://www.google.com.mx/"
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.putExtra(
            Intent.EXTRA_TEXT,
            "Hey ve una vacante que te puede interesar: ${mVacant?.title} - Visitanos en $url"
        )
        intent.type = "text/plain"
        startActivity(Intent.createChooser(intent, "Compartir por:"))
    }

}