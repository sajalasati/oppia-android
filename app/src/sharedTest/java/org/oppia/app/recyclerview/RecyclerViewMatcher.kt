package org.oppia.app.recyclerview

import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.CoreMatchers
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

// Reference Link: https://github.com/dannyroa/espresso-samples/blob/master/RecyclerView/app/src/androidTest/java/com/dannyroa/espresso_samples/recyclerview/RecyclerViewMatcher.java
class RecyclerViewMatcher {
  companion object {
    /**
     * This function returns a Matcher for an item inside RecyclerView from a specified position.
     */
    fun atPosition(recyclerViewId: Int, position: Int): Matcher<View> {
      return atPositionOnView(recyclerViewId, position, -1)
    }

    /**
     * This function returns a Matcher for a specific view within the item inside RecyclerView from a specified position.
     */
    fun atPositionOnView(recyclerViewId: Int, position: Int, targetViewId: Int): Matcher<View> {
      return object : TypeSafeMatcher<View>() {
        var resources: Resources? = null
        var childView: View? = null

        override fun describeTo(description: Description) {
          var idDescription = Integer.toString(recyclerViewId)
          if (this.resources != null) {
            idDescription = try {
              this.resources!!.getResourceName(recyclerViewId)
            } catch (var4: Resources.NotFoundException) {
              String.format(
                "%s (resource name not found)",
                recyclerViewId
              )
            }
          }
          description.appendText("with id: $idDescription")
        }

        public override fun matchesSafely(view: View): Boolean {
          this.resources = view.resources
          if (childView == null) {
            val recyclerView = view.rootView.findViewById<View>(recyclerViewId) as RecyclerView
            if (recyclerView.id == recyclerViewId) {
              childView = recyclerView.findViewHolderForAdapterPosition(position)!!.itemView
            } else {
              return false
            }
          }
          return if (targetViewId == -1) {
            view === childView
          } else {
            val targetView = childView!!.findViewById<View>(targetViewId)
            view === targetView
          }
        }
      }
    }

    /** Returns item count ViewAssertion for a recycler view. */
    fun hasItemCount(count: Int): ViewAssertion {
      return RecyclerViewItemCountAssertion(count)
    }

    /** Returns span count ViewAssertion for a recycler view that use GridLayoutManager. */
    fun hasGridItemCount(count: Int): ViewAssertion {
      return RecyclerViewGridItemCountAssertion(count)
    }
  }

  private class RecyclerViewItemCountAssertion(private val count: Int) : ViewAssertion {

    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
      if (noViewFoundException != null) {
        throw noViewFoundException
      }

      check(view is RecyclerView) { "The asserted view is not RecyclerView" }

      checkNotNull(view.adapter) { "No adapter is assigned to RecyclerView" }

      ViewMatchers.assertThat(
        "RecyclerView item count",
        view.adapter!!.itemCount,
        CoreMatchers.equalTo(count)
      )
    }
  }

  private class RecyclerViewGridItemCountAssertion(private val count: Int) : ViewAssertion {

    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
      if (noViewFoundException != null) {
        throw noViewFoundException
      }

      check(view is RecyclerView) { "The asserted view is not RecyclerView" }

      check(view.layoutManager is GridLayoutManager) { "RecyclerView must use GridLayoutManager" }

      val spanCount = (view.layoutManager as GridLayoutManager).spanCount

      ViewMatchers.assertThat(
        "RecyclerViewGrid span count",
        spanCount,
        CoreMatchers.equalTo(count)
      )
    }
  }
}
