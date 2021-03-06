@file:Suppress("unused")

package jakubweg.mobishit.activity

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.view.View
import jakubweg.mobishit.R

@SuppressLint("Registered")
abstract class DoublePanelActivity : FragmentActivity() {

    abstract val mainFragmentContainerId: Int

    open val secondFragmentContainerId: Int get() = mainFragmentContainerId

    private var isVisibleActivity = false
    override fun onResume() {
        super.onResume()
        isVisibleActivity = true
    }

    override fun onPause() {
        super.onPause()
        isVisibleActivity = false
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (savedInstanceState == null
                && supportFragmentManager.fragments.size == 0) {
            requestNewMainFragment()
        }
    }

    private fun applyNewMainFragment(fragment: Fragment) {
        if (hasSavedInstance) {
            findViewById<View>(mainFragmentContainerId)?.postDelayed({
                applyNewMainFragment(fragment)
            }, 50L)
            return
        }

        supportFragmentManager
                .clearStack()
                .beginTransaction()
                .setCustomAnimations(R.anim.fragment_enter, R.anim.fade_out, R.anim.fade_in, R.anim.fragment_exit)
                .replace(mainFragmentContainerId, fragment)
                .commitAllowingStateLoss()
    }

    fun requestNewMainFragment() {
        getCurrentMainFragment()?.also { applyNewMainFragment(it) }
    }

    fun applyNewDetailsFragment(fragment: Fragment) {
        if (hasSavedInstance) {
            findViewById<View>(secondFragmentContainerId)?.postDelayed({
                applyNewDetailsFragment(fragment)
            }, 50L)
            return
        }

        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fragment_enter, R.anim.fade_out, R.anim.fade_in, R.anim.fragment_exit)
                .replace(secondFragmentContainerId, fragment)
                .addToBackStack(null)
                .commitAllowingStateLoss()
    }

    fun applyNewDetailsFragment(sharedView: View, fragment: Fragment) {
        if (hasSavedInstance) {
            findViewById<View>(secondFragmentContainerId)?.postDelayed({
                applyNewDetailsFragment(sharedView, fragment)
            }, 50L)
            return
        }

        supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(R.anim.fragment_enter, R.anim.fade_out, R.anim.fade_in, R.anim.fragment_exit)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                addSharedElement(sharedView, sharedView.transitionName)
            replace(secondFragmentContainerId, fragment)
            addToBackStack(null)
            commitAllowingStateLoss()
        }
    }

    private fun FragmentManager.clearStack() = this.apply {
        try {
            while (popBackStackImmediate()) {
            }
        } catch (e: Exception) {
            e.printStackTrace() //c***
        }
    }


    private var hasSavedInstance = false

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        hasSavedInstance = false
    }

    abstract fun getCurrentMainFragment(): Fragment?
}