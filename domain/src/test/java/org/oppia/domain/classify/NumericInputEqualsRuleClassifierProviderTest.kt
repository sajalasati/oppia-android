package org.oppia.domain.classify

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.model.InteractionObject
import org.oppia.domain.classify.rules.numericinput.NumericInputEqualsRuleClassifierProvider
import org.robolectric.annotation.Config
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.test.fail

/** Tests for [NumericInputEqualsRuleClassifierProvider]. */

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class NumericInputEqualsRuleClassifierProviderTest {

  private val NON_NEGATIVE_REAL_VALUE_1_5 = createReal(value = 1.5)
  private val REAL_VALUE_RANGE_VALUE_1 = createReal(value = 1.000051)
  private val REAL_VALUE_RANGE_VALUE_2 = createReal(value = 1.000052)
  private val REAL_VALUE_RANGE_VALUE_3 = createReal(value = 1.000061)
  private val NON_NEGATIVE_REAL_VALUE_3_5 = createReal(value = 3.5)
  private val NEGATIVE_REAL_VALUE_1_5 = createReal(value = -1.5)
  private val NEGATIVE_REAL_VALUE_3_5 = createReal(value = -3.5)
  private val STRING_VALUE = createString(value = "test")


  @Inject
  internal lateinit var numericInputEqualsRuleClassifierProvider:
    NumericInputEqualsRuleClassifierProvider

  private val inputEqualsRuleClassifier by lazy {
    numericInputEqualsRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  //testNonNegativeRealAnswer_nonNegativeRealInput_sameExactValue_bothValuesMatch DONE
  //testNegativeRealAnswer_negativeRealInput_sameExactValue_bothValuesMatch DONE
  //testRealAnswer_realInput_valuesInRange_bothValuesMatch DONE

  //testNonNegativeRealAnswer_nonNegativeRealInput_valueOutOfRange_valuesDoNotMatch DONE
  //testNegativeRealAnswer_negativeRealInput_valueOutOfRange_valuesDoNotMatch DONE
  //testNegativeRealAnswer_nonNegativeRealInput_valueOutOfRange_valuesDoNotMatch DONE
  //testRealAnswer_realInput_valueAtRange_valuesDoNotMatch DONE

  //testRealAnswer_missingInput_throwsException DONE
  //testRealAnswer_stringInput_throwsException DONE

  @Test
  fun testNonNegativeRealAnswer_nonNegativeRealInput_sameExactValue_bothValuesMatch() {
    val inputs = mapOf("x" to NON_NEGATIVE_REAL_VALUE_1_5)

    val matches = inputEqualsRuleClassifier.matches(answer = NON_NEGATIVE_REAL_VALUE_1_5, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testNegativeRealAnswer_negativeRealInput_sameExactValue_bothValuesMatch() {
    val inputs = mapOf("x" to NEGATIVE_REAL_VALUE_1_5)

    val matches = inputEqualsRuleClassifier.matches(answer = NEGATIVE_REAL_VALUE_1_5, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testNonNegativeRealAnswer_nonNegativeRealInput_valuesInRange_bothValuesMatch() {
    val inputs = mapOf("x" to REAL_VALUE_RANGE_VALUE_1)

    val matches = inputEqualsRuleClassifier.matches(answer = REAL_VALUE_RANGE_VALUE_2, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testNonNegativeRealAnswer_nonNegativeRealInput_valueOutOfRange_valuesDoNotMatch() {
    val inputs = mapOf("x" to NON_NEGATIVE_REAL_VALUE_1_5)

    val matches = inputEqualsRuleClassifier.matches(answer = NON_NEGATIVE_REAL_VALUE_3_5, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testNegativeRealAnswer_negativeRealInput_valueOutOfRange_valuesDoNotMatch() {
    val inputs = mapOf("x" to NEGATIVE_REAL_VALUE_1_5)

    val matches = inputEqualsRuleClassifier.matches(answer = NEGATIVE_REAL_VALUE_3_5, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testNegativeRealAnswer_nonNegativeRealInput_valueOutOfRange_valuesDoNotMatch() {
    val inputs = mapOf("x" to NON_NEGATIVE_REAL_VALUE_3_5)

    val matches = inputEqualsRuleClassifier.matches(answer = NEGATIVE_REAL_VALUE_3_5, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testNonNegativeRealAnswer_nonNegativeRealInput_valueAtRange_valuesDoNotMatch() {
    val inputs = mapOf("x" to REAL_VALUE_RANGE_VALUE_1)

    val matches = inputEqualsRuleClassifier.matches(answer = REAL_VALUE_RANGE_VALUE_3, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun ttestRealAnswer_missingInput_throwsException() {
    val inputs = mapOf("y" to NON_NEGATIVE_REAL_VALUE_1_5)

    val exception = assertThrows(IllegalStateException::class) {
      inputEqualsRuleClassifier.matches(answer = NON_NEGATIVE_REAL_VALUE_1_5, inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x'")
  }

  @Test
  fun testRealAnswer_stringInput_throwsException() {
    val inputs = mapOf("x" to STRING_VALUE)

    val exception = assertThrows(IllegalStateException::class) {
      inputEqualsRuleClassifier.matches(answer = NON_NEGATIVE_REAL_VALUE_1_5, inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type REAL not NORMALIZED_STRING")
  }


  private fun createReal(value: Double): InteractionObject {
    return InteractionObject.newBuilder().setReal(value).build()
  }

  private fun createString(value: String): InteractionObject {
    return InteractionObject.newBuilder().setNormalizedString(value).build()
  }

  private fun setUpTestApplicationComponent() {
    DaggerNumericInputEqualsRuleClassifierProviderTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

    // TODO(#89): Move to a common test library.
   private fun <T: Throwable> assertThrows(type: KClass<T>, operation: () -> Unit): T {
      try {
        operation()
        fail("Expected to encounter exception of $type")
      } catch (t: Throwable) {
        if (type.isInstance(t)) {
          return type.cast(t)
        }
        // Unexpected exception; throw it.
        throw t
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: NumericInputEqualsRuleClassifierProviderTest)
  }
}