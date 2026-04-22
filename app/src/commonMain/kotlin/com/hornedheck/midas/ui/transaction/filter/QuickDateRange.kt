package com.hornedheck.midas.ui.transaction.filter

import midas.app.generated.resources.Res
import midas.app.generated.resources.filter_quick_past_month
import midas.app.generated.resources.filter_quick_past_week
import midas.app.generated.resources.filter_quick_this_month
import midas.app.generated.resources.filter_quick_this_week
import org.jetbrains.compose.resources.StringResource

enum class QuickDateRange(
    val label: StringResource,
) {
    THIS_WEEK(Res.string.filter_quick_this_week),
    PAST_WEEK(Res.string.filter_quick_past_week),
    THIS_MONTH(Res.string.filter_quick_this_month),
    PAST_MONTH(Res.string.filter_quick_past_month),
}
