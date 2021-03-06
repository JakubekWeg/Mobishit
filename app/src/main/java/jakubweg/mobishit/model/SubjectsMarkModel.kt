package jakubweg.mobishit.model

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.util.SparseArray
import jakubweg.mobishit.db.AverageCacheData
import jakubweg.mobishit.db.MarkDao
import jakubweg.mobishit.helper.AverageCalculator
import jakubweg.mobishit.helper.MobiregPreferences

class SubjectsMarkModel(application: Application)
    : BaseViewModel(application) {

    var shouldReorderEverything = false

    private var mSubjectId = 0
    fun init(subjectId: Int) {
        require(subjectId != 0) { "Subject id must be not equal to 0" }
        if (mSubjectId == subjectId) return
        check(mSubjectId == 0) { "SubjectsMarkModel.init was already called!" }
        mSubjectId = subjectId
    }

    private val preferences = MobiregPreferences.get(application)

    private val mMarks = MutableLiveData<SparseArray<List<MarkDao.MarkShortInfo>>>()

    val marks get() = handleBackground(mMarks).asImmutable

    private val mAverages = MutableLiveData<SparseArray<AverageCacheData>>()

    val averages get() = handleBackground(mAverages).asImmutable


    override fun doInBackground() {
        check(mSubjectId != 0) { "SubjectsMarkModel.init not called!" }

        val (marksMap, averagesMap) =
                AverageCalculator.getMarksAndCalculateAverage(
                        context, mSubjectId,
                        preferences.markSortingOrder,
                        preferences.groupMarksByParent)

        mAverages.postValue(averagesMap)
        mMarks.postValue(marksMap)
    }
}