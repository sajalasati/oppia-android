package org.oppia.app.home

import android.app.Application
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import dagger.Component
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.activity.ActivityComponent
import org.oppia.app.application.ActivityComponentFactory
import org.oppia.app.application.ApplicationComponent
import org.oppia.app.application.ApplicationInjector
import org.oppia.app.application.ApplicationInjectorProvider
import org.oppia.app.application.ApplicationModule
import org.oppia.app.application.ApplicationStartupListenerModule
import org.oppia.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.app.profile.ProfileChooserActivity
import org.oppia.app.profileprogress.ProfileProgressActivity
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.shim.ViewBindingShimModule
import org.oppia.app.testing.HomeInjectionActivity
import org.oppia.app.topic.TopicActivity
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.domain.classify.InteractionsModule
import org.oppia.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.domain.question.QuestionModule
import org.oppia.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.domain.topic.TEST_STORY_ID_0
import org.oppia.domain.topic.TEST_TOPIC_ID_0
import org.oppia.testing.TestAccessibilityModule
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.testing.profile.ProfileTestHelper
import org.oppia.util.caching.testing.CachingTestModule
import org.oppia.util.gcsresource.GcsResourceModule
import org.oppia.util.logging.LoggerModule
import org.oppia.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.util.parser.GlideImageLoaderModule
import org.oppia.util.parser.HtmlParserEntityTypeModule
import org.oppia.util.parser.ImageParsingModule
import org.oppia.util.system.OppiaClock
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import javax.inject.Singleton

// Time: Tue Apr 23 2019 23:22:00
private const val EVENING_TIMESTAMP = 1556061720000

// Time: Wed Apr 24 2019 08:22:00
private const val MORNING_TIMESTAMP = 1556094120000

// Time: Tue Apr 23 2019 14:22:00
private const val AFTERNOON_TIMESTAMP = 1556029320000

/** Tests for [HomeActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = HomeActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class HomeActivityTest {

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var context: Context

  private val internalProfileId: Int = 1
  private lateinit var oppiaClock: OppiaClock

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
    FirebaseApp.initializeApp(context)
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun getApplicationDependencies() {
    launch(HomeInjectionActivity::class.java).use {
      it.onActivity { activity ->
        oppiaClock = activity.oppiaClock
      }
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  // TODO (#973): Fix HomeActivityTest
  @Ignore
  fun testHomeActivity_clickNavigationDrawerHamburger_clickOnHeader_opensProfileProgressActivity() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withContentDescription(R.string.drawer_open_content_description)).check(
        matches(isCompletelyDisplayed())
      ).perform(click())
      onView(withId(R.id.nav_header_profile_name)).perform(click())
      intended(hasComponent(ProfileProgressActivity::class.java.name))
      intended(
        hasExtra(
          ProfileProgressActivity.PROFILE_PROGRESS_ACTIVITY_PROFILE_ID_KEY,
          internalProfileId
        )
      )
    }
  }

  @Test
  @Ignore(
    "This test case is incorrect as it depends on internalProfileId " +
      "which is not guaranteed to be 0 for admin."
  )
  fun testHomeActivity_recyclerViewIndex0_withProfileId0_displayProfileName_profileNameDisplayedSuccessfully() { // ktlint-disable max-line-length
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(
        atPositionOnView(
          R.id.home_recycler_view,
          0,
          R.id.profile_name_textview
        )
      ).check(matches(withText("Admin!")))
    }
  }

  @Test
  // TODO(#973): Fix HomeActivityTest
  @Ignore
  fun testHomeActivity_recyclerViewIndex0_displayGreetingMessageBasedOnTime_goodMorningMessageDisplayedSuccessful() { // ktlint-disable max-line-length
    getApplicationDependencies()
    oppiaClock.setCurrentTimeMs(MORNING_TIMESTAMP)
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPositionOnView(
          R.id.home_recycler_view,
          0,
          R.id.welcome_text_view
        )
      ).check(matches(withText("Good morning,")))
    }
  }

  @Test
  // TODO (#973): Fix HomeActivityTest
  @Ignore
  fun testHomeActivity_recyclerViewIndex0_displayGreetingMessageBasedOnTime_goodAfternoonMessageDisplayedSuccessful() { // ktlint-disable max-line-length
    getApplicationDependencies()
    oppiaClock.setCurrentTimeMs(AFTERNOON_TIMESTAMP)
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPositionOnView(
          R.id.home_recycler_view,
          0,
          R.id.welcome_text_view
        )
      ).check(matches(withText("Good afternoon,")))
    }
  }

  @Test
  // TODO (#973): Fix HomeActivityTest
  @Ignore
  fun testHomeActivity_recyclerViewIndex0_displayGreetingMessageBasedOnTime_goodEveningMessageDisplayedSuccessful() { // ktlint-disable max-line-length
    getApplicationDependencies()
    oppiaClock.setCurrentTimeMs(EVENING_TIMESTAMP)
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPositionOnView(
          R.id.home_recycler_view,
          0,
          R.id.welcome_text_view
        )
      ).check(matches(withText("Good evening,")))
    }
  }

  @Test
  // TODO (#973): Fix HomeActivityTest
  @Ignore
  fun testHomeActivity_recyclerViewIndex0_configurationChange_displaysWelcomeMessageCorrectly() {
    launch<HomeActivity>(createHomeActivityIntent(0)).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPositionOnView(
          R.id.home_recycler_view,
          0,
          R.id.profile_name_textview
        )
      ).check(matches(withText("Admin!")))
    }
  }

  @Test
  fun testHomeActivity_recyclerViewIndex1_displaysRecentlyPlayedStoriesText() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(
        atPositionOnView(
          R.id.home_recycler_view,
          1,
          R.id.recently_played_stories_text_view
        )
      ).check(
        matches(
          withText(R.string.recently_played_stories)
        )
      )
    }
  }

  @Test
  fun testHomeActivity_recyclerViewIndex1_displaysViewAllText() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(atPositionOnView(R.id.home_recycler_view, 1, R.id.view_all_text_view)).check(
        matches(
          withText(R.string.view_all)
        )
      )
    }
  }

  @Test
  fun testHomeActivity_recyclerViewIndex1_clickViewAll_opensRecentlyPlayedActivity() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(
        atPositionOnView(
          R.id.home_recycler_view, 1, R.id.view_all_text_view
        )
      ).perform(click())
      intended(hasComponent(RecentlyPlayedActivity::class.java.name))
    }
  }

  @Test
  // TODO (#973): Fix HomeActivityTest
  @Ignore
  fun testHomeActivity_recyclerViewIndex1_promotedCard_chapterNameIsCorrect() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(
        allOf(
          withId(R.id.promoted_story_list_recycler_view),
          withParent(
            atPosition(R.id.home_recycler_view, 1)
          )
        )
      ).check(matches(hasDescendant(withText(containsString("Prototype Exploration")))))
    }
  }

  @Test
  // TODO (#973): Fix HomeActivityTest
  @Ignore
  fun testHomeActivity_recyclerViewIndex1_promotedCard_storyNameIsCorrect() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(atPositionOnView(R.id.home_recycler_view, 1, R.id.story_name_text_view)).check(
        matches(
          withText(containsString("First Story"))
        )
      )
    }
  }

  @Test
  // TODO (#973): Fix HomeActivityTest
  @Ignore
  fun testHomeActivity_recyclerViewIndex1_configurationChange_promotedCard_storyNameIsCorrect() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(atPositionOnView(R.id.home_recycler_view, 1, R.id.story_name_text_view)).check(
        matches(
          withText(containsString("First Story"))
        )
      )
    }
  }

  @Test
  // TODO (#973): Fix HomeActivityTest
  @Ignore
  fun testHomeActivity_recyclerViewIndex1_clickPromotedStory_opensTopicActivity() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(
        allOf(
          withId(R.id.promoted_story_list_recycler_view),
          withParent(
            atPosition(R.id.home_recycler_view, 1)
          )
        )
      ).perform(click())
      intended(hasComponent(TopicActivity::class.java.name))
      intended(hasExtra(TopicActivity.getProfileIdKey(), internalProfileId))
      intended(hasExtra(TopicActivity.getTopicIdKey(), TEST_TOPIC_ID_0))
      intended(hasExtra(TopicActivity.getStoryIdKey(), TEST_STORY_ID_0))
    }
  }

  @Test
  // TODO (#973): Fix HomeActivityTest
  @Ignore
  fun testHomeActivity_recyclerViewIndex1_promotedCard_topicNameIsCorrect() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(1)
      )
      onView(atPositionOnView(R.id.home_recycler_view, 1, R.id.topic_name_text_view)).check(
        matches(
          withText(containsString("FIRST TEST TOPIC"))
        )
      )
    }
  }

  @Test
  fun testHomeActivity_recyclerViewIndex3_topicSummary_topicNameIsCorrect() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(3)
      )
      onView(atPositionOnView(R.id.home_recycler_view, 3, R.id.topic_name_text_view)).check(
        matches(
          withText(containsString("First Test Topic"))
        )
      )
    }
  }

  @Test
  fun testHomeActivity_recyclerViewIndex3_topicSummary_lessonCountIsCorrect() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(3)
      )
      onView(
        atPositionOnView(
          R.id.home_recycler_view,
          3, R.id.lesson_count_text_view
        )
      ).check(
        matches(
          withText(containsString("5 Lessons"))
        )
      )
    }
  }

  @Test
  fun testHomeActivity_recyclerViewIndex4_topicSummary_topicNameIsCorrect() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(4)
      )
      onView(atPositionOnView(R.id.home_recycler_view, 4, R.id.topic_name_text_view)).check(
        matches(
          withText(containsString("Second Test Topic"))
        )
      )
    }
  }

  @Test
  fun testHomeActivity_recyclerViewIndex4_topicSummary_lessonCountIsCorrect() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(4)
      )
      onView(
        atPositionOnView(
          R.id.home_recycler_view,
          4, R.id.lesson_count_text_view
        )
      ).check(
        matches(
          withText(containsString("1 Lesson"))
        )
      )
    }
  }

  @Test
  fun testHomeActivity_recyclerViewIndex4_topicSummary_configurationChange_lessonCountIsCorrect() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(4)
      )
      onView(
        atPositionOnView(
          R.id.home_recycler_view,
          4, R.id.lesson_count_text_view
        )
      ).check(
        matches(
          withText(containsString("1 Lesson"))
        )
      )
    }
  }

  @Test
  fun testHomeActivity_recyclerViewIndex3_clickTopicSummary_opensTopicActivity() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(3)
      )
      onView(atPosition(R.id.home_recycler_view, 3)).perform(click())
      intended(hasComponent(TopicActivity::class.java.name))
      intended(hasExtra(TopicActivity.getTopicIdKey(), TEST_TOPIC_ID_0))
    }
  }

  @Test
  // TODO (#973): Fix HomeActivityTest
  @Ignore
  fun testHomeActivity_onBackPressed_showsExitToProfileChooserDialog() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      pressBack()
      onView(withText(R.string.home_activity_back_dialog_message)).check(matches(isDisplayed()))
    }
  }

  @Test
  // TODO (#973): Fix HomeActivityTest
  @Ignore
  fun testHomeActivity_onBackPressed_orientationChange_showsExitToProfileChooserDialog() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      pressBack()
      onView(isRoot()).perform(orientationLandscape())
      onView(withText(R.string.home_activity_back_dialog_message)).check(matches(isDisplayed()))
    }
  }

  @Test
  // TODO (#973): Fix HomeActivityTest
  @Ignore
  fun testHomeActivity_onBackPressed_clickExit_checkOpensProfileActivity() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      pressBack()
      onView(withText(R.string.home_activity_back_dialog_exit)).perform(click())
      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  @Test
  fun testHomeActivity_checkSpanForItem0_spanSizeIsTwoOrThree() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      if (context.resources.getBoolean(R.bool.isTablet)) {
        onView(withId(R.id.home_recycler_view)).check(hasGridItemCount(3, 0))
      } else {
        onView(withId(R.id.home_recycler_view)).check(hasGridItemCount(2, 0))
      }
    }
  }

  @Test
  fun testHomeActivity_checkSpanForItem4_spanSizeIsOne() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(withId(R.id.home_recycler_view)).check(hasGridItemCount(1, 4))
    }
  }

  @Test
  // TODO (#973): Fix HomeActivityTest
  @Ignore
  fun testHomeActivity_configurationChange_checkSpanForItem0_spanSizeIsThreeOrFour() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(isRoot()).perform(orientationLandscape())
      if (context.resources.getBoolean(R.bool.isTablet)) {
        onView(withId(R.id.home_recycler_view)).check(hasGridItemCount(4, 0))
      } else {
        onView(withId(R.id.home_recycler_view)).check(hasGridItemCount(3, 0))
      }
    }
  }

  @Test
  fun testHomeActivity_configurationChange_checkSpanForItem4_spanSizeIsOne() {
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId)).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.home_recycler_view)).check(hasGridItemCount(1, 4))
    }
  }

  private fun createHomeActivityIntent(profileId: Int): Intent {
    return HomeActivity.createHomeActivity(ApplicationProvider.getApplicationContext(), profileId)
  }

  /** Returns span count ViewAssertion for a recycler view that use GridLayoutManager. */
  private fun hasGridItemCount(spanCount: Int, position: Int): ViewAssertion {
    return RecyclerViewGridItemCountAssertion(spanCount, position)
  }

  private fun waitForTheView(viewMatcher: Matcher<View>): ViewInteraction {
    return onView(isRoot()).perform(waitForMatch(viewMatcher, 30000L))
  }

// TODO(#59): Remove these waits once we can ensure that the production executors are not depended on in tests.
//  Sleeping is really bad practice in Espresso tests, and can lead to test flakiness. It shouldn't be necessary if we
//  use a test executor service with a counting idle resource, but right now Gradle mixes dependencies such that both
//  the test and production blocking executors are being used. The latter cannot be updated to notify Espresso of any
//  active coroutines, so the test attempts to assert state before it's ready. This artificial delay in the Espresso
//  thread helps to counter that.
  /**
   * Perform action of waiting for a specific matcher to finish. Adapted from:
   * https://stackoverflow.com/a/22563297/3689782.
   */
  private fun waitForMatch(viewMatcher: Matcher<View>, millis: Long): ViewAction {
    return object : ViewAction {
      override fun getDescription(): String {
        return "wait for a specific view with matcher <$viewMatcher> during $millis millis."
      }

      override fun getConstraints(): Matcher<View> {
        return isRoot()
      }

      override fun perform(uiController: UiController?, view: View?) {
        checkNotNull(uiController)
        uiController.loopMainThreadUntilIdle()
        val startTime = System.currentTimeMillis()
        val endTime = startTime + millis

        do {
          if (TreeIterables.breadthFirstViewTraversal(view).any { viewMatcher.matches(it) }) {
            return
          }
          uiController.loopMainThreadForAtLeast(50)
        } while (System.currentTimeMillis() < endTime)

        // Couldn't match in time.
        throw PerformException.Builder()
          .withActionDescription(description)
          .withViewDescription(HumanReadables.describe(view))
          .withCause(TimeoutException())
          .build()
      }
    }
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  // TODO(#1675): Add NetworkModule once data module is migrated off of Moshi.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      TestAccessibilityModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent, ApplicationInjector {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(homeActivityTest: HomeActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerHomeActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(homeActivityTest: HomeActivityTest) {
      component.inject(homeActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }

  /** Custom class to check number of spans occupied by an item at a given position. */
  private class RecyclerViewGridItemCountAssertion(
    private val count: Int,
    private val position: Int
  ) : ViewAssertion {
    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
      if (noViewFoundException != null) {
        throw noViewFoundException
      }
      check(view is RecyclerView) { "The asserted view is not RecyclerView" }
      check(view.layoutManager is GridLayoutManager) { "RecyclerView must use GridLayoutManager" }
      val spanCount = (view.layoutManager as GridLayoutManager)
        .spanSizeLookup.getSpanSize(position)
      assertThat(
        "RecyclerViewGrid span count", spanCount, equalTo(count)
      )
    }
  }
}
