package jakubweg.mobishit.fragment

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v7.widget.AppCompatSpinner
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Switch
import jakubweg.mobishit.R
import jakubweg.mobishit.activity.DoublePanelActivity
import jakubweg.mobishit.activity.MarkOptionsListener
import jakubweg.mobishit.db.TermDao
import jakubweg.mobishit.helper.AverageCalculator
import jakubweg.mobishit.helper.MobiregPreferences
import jakubweg.mobishit.helper.ThemeHelper
import jakubweg.mobishit.model.TermsModel
import java.lang.ref.WeakReference


class MarksViewOptionsFragment : BottomSheetDialogFragment() {
    companion object {
        fun newInstance(showSortBySpinner: Boolean) = MarksViewOptionsFragment().apply {
            arguments = Bundle().also { it.putBoolean("chooseOrder", showSortBySpinner) }
        }
    }

    interface OptionsChangedListener {

        fun onOptionsChanged(changedTerm: Boolean,
                             changedOrder: Boolean,
                             changedGrouping: Boolean)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        MobiregPreferences.get(context ?: return).also {
            val term = it.lastSelectedTerm
            val order = it.markSortingOrder
            val grouping = it.groupMarksByParent

            previousOrder = order
            previousIsEnabledGrouping = grouping
            previousTerm = term
        }
    }

    fun showSelf(activity: Activity?): MarksViewOptionsFragment {
        show((activity as? DoublePanelActivity?)?.supportFragmentManager, null)
        return this
    }

    override fun getTheme() = ThemeHelper.getBottomSheetTheme(context!!)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_marks_view_options, container, false)
    }

    private val viewModel
        get() =
            ViewModelProviders.of(this)[TermsModel::class.java]


    override fun onStop() {
        super.onStop()
        notifyAboutChanges()
    }

    private fun notifyAboutChanges() {
        viewModel.savePreferences()
        MarkOptionsListener.notifyOptionsChanged(context,
                previousTerm != viewModel.selectedTermId,
                previousOrder != viewModel.selectedOrderMethod,
                previousIsEnabledGrouping != viewModel.isGroupingByParentsEnabled)
    }

    private var previousTerm = 0
    private var previousOrder = AverageCalculator.ORDER_DEFAULT
    private var previousIsEnabledGrouping = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val weakView = WeakReference<View>(view)
        val setOrderSpinnerVisible = arguments?.getBoolean("chooseOrder", true) ?: true

        viewModel.terms.observe(this, Observer { terms ->
            terms ?: return@Observer
            weakView.get()?.apply {

                findViewById<AppCompatSpinner>(R.id.termSpinner)?.also { spinner ->
                    val adapter = ArrayAdapter<TermDao.TermShortInfo>(context!!,
                            android.R.layout.simple_spinner_item, terms)
                    adapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item)

                    spinner.adapter = adapter
                    if (viewModel.selectedTermId != 0)
                        spinner.setSelection(terms.indexOfFirst { it.id == viewModel.selectedTermId })

                    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) = Unit

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            viewModel.selectedTermId = terms[position].id
                        }
                    }
                }


                if (setOrderSpinnerVisible) {
                    val sortingMethodsNames = AverageCalculator.orderMethodsNames
                    val sortingMethodsIds = AverageCalculator.orderMethodsIds

                    findViewById<AppCompatSpinner>(R.id.sortingSpinner)?.also { spinner ->
                        val adapter = ArrayAdapter<String>(context!!,
                                android.R.layout.simple_spinner_item, sortingMethodsNames)
                        adapter.setDropDownViewResource(
                                android.R.layout.simple_spinner_dropdown_item)

                        spinner.adapter = adapter
                        if (viewModel.selectedOrderMethod != 0)
                            spinner.setSelection(sortingMethodsIds.indexOfFirst { it == viewModel.selectedOrderMethod })

                        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onNothingSelected(parent: AdapterView<*>?) = Unit

                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                viewModel.selectedOrderMethod = sortingMethodsIds[position]
                            }
                        }
                    }


                    findViewById<Switch>(R.id.switchGroupParents)?.also {
                        it.isChecked = viewModel.isGroupingByParentsEnabled
                        it.setOnCheckedChangeListener { _, isChecked ->
                            viewModel.isGroupingByParentsEnabled = isChecked
                        }
                    }
                } else {
                    findViewById<View>(R.id.textSorting)?.visibility = View.INVISIBLE
                    findViewById<View>(R.id.sortingSpinner)?.visibility = View.INVISIBLE
                    findViewById<View>(R.id.switchGroupParents)?.visibility = View.GONE
                }

            }
        })
    }
}