package org.oppia.domain.classify.rules.dragAndDropSortInput

import org.oppia.app.model.Fraction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.ListOfSetsOfHtmlStrings
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.GenericRuleClassifier
import org.oppia.domain.classify.rules.RuleClassifierProvider
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether a fraction has a denominator equal to the specified value per the
 * fraction input interaction.
 *
 * https://github.com/oppia/oppia/blob/37285a/extensions/interactions/FractionInput/directives/fraction-input-rules.service.ts#L55
 */
internal class DragDropSortInputHasElementXAtPositionY @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider,
  GenericRuleClassifier.MultiTypeDoubleInputMatcher<ListOfSetsOfHtmlStrings, String, Int> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createDoubleInputClassifier(
      InteractionObject.ObjectTypeCase.LIST_OF_SETS_OF_HTML_STRING,
      InteractionObject.ObjectTypeCase.NORMALIZED_STRING,
      "x",
      InteractionObject.ObjectTypeCase.NON_NEGATIVE_INT,
      "y",
      this
    )
  }

  // TODO(#210): Add tests for this classifier.
  override fun matches(answer: ListOfSetsOfHtmlStrings, firstInput: String, secondInput: Int): Boolean {
    val index: Int = answer.setOfHtmlStringsList.indexOf(firstInput)
    return if (index != -1) {
      index + 1 == secondInput
    } else {
      false
    }
  }
}