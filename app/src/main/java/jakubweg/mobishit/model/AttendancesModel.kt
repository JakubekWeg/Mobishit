package jakubweg.mobishit.model

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import jakubweg.mobishit.db.AppDatabase
import jakubweg.mobishit.db.AttendanceDao
import jakubweg.mobishit.db.TermDao
import jakubweg.mobishit.helper.DateHelper
import java.util.*

class AttendancesModel(application: Application)
    : BaseViewModel(application) {

    private val mAttendanceData = MutableLiveData<List<AttendanceDao.AttendanceCountInfoHolder>>()

    val attendanceStats
        get() = handleBackground(mAttendanceData).asImmutable


    override fun doInBackground() {

        val db = AppDatabase.getAppDatabase(context)
        val dao = db.attendanceDao

        val terms = db.termDao.getTerms()

        val stats = mutableListOf<AttendanceDao.AttendanceCountInfoHolder>()

        terms.forEach {
            val events = dao.getAttendancesByTerm(it.id)
            if (events.isEmpty())
                return@forEach

            stats.add(AttendanceDao.AttendanceCountInfoHolder(
                    TermDao.getNiceTermName(it.type, it.name),
                    it.startDate, it.endDate, events))
        }

        val firstDay = dao.getFirstAttendanceDay() ?: -1
        val lastDay = dao.getLastAttendanceDay() ?: -1

        if (firstDay < 0 || lastDay < 0) {
            mAttendanceData.postValue(emptyList())
            return
        }

        val calendar = Calendar.getInstance()!!
                .also { it.timeInMillis = firstDay }


        calendar.set(Calendar.DAY_OF_MONTH, 1)
        while (calendar.timeInMillis <= lastDay) {
            val start = calendar.timeInMillis
            val name = "${DateHelper.months[calendar[Calendar.MONTH]]} ${calendar[Calendar.YEAR]}"

            calendar.add(Calendar.MONTH, 1)

            val end = calendar.timeInMillis - 24 * 60 * 60 * 1000


            val list = dao.getAttendancesBetweenDates(start, end)

            if (list.isNotEmpty())
                stats.add(AttendanceDao.AttendanceCountInfoHolder(
                        name, start, end, list))
        }

        mAttendanceData.postValue(stats)
    }

}
