package com.noto.app.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import com.noto.app.BaseActivity
import com.noto.app.R
import com.noto.app.databinding.NoteListWidgetConfigActivityBinding
import com.noto.app.label.labelItem
import com.noto.app.library.SelectLibraryDialogFragment
import com.noto.app.util.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class NoteListWidgetConfigActivity : BaseActivity() {

    private val viewModel by viewModel<NoteListWidgetConfigViewModel> { parametersOf(appWidgetId) }

    private val appWidgetId by lazy {
        intent?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }

    private val libraryId by lazy { intent?.getLongExtra(Constants.LibraryId, 0) ?: 0 }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NoteListWidgetConfigActivityBinding.inflate(layoutInflater).withBinding {
            setContentView(root)
            if (libraryId == 0L)
                showSelectLibraryDialog(false)
            else
                viewModel.getWidgetData(libraryId)
            setupState()
            setupListeners()
        }
    }

    private fun NoteListWidgetConfigActivityBinding.setupState() {
        setResult(Activity.RESULT_CANCELED)
        widget.lvContent.dividerHeight = 16.dp
        widget.lvContent.setPaddingRelative(8.dp, 16.dp, 8.dp, 100.dp)
        widget.root.clipToOutline = true

        viewModel.isWidgetCreated
            .onEach { isCreated ->
                if (isCreated) {
                    tb.title = stringResource(R.string.edit_notes_widget)
                    btnCreate.text = stringResource(R.string.done)
                }
            }
            .launchIn(lifecycleScope)

        combine(viewModel.library, viewModel.notes, viewModel.labels) { library, notes, labels ->
            val filteredNotes = notes.filter { it.second.containsAll(labels.filterSelected()) }
            val color = colorResource(library.color.toResource())
            val colorStateList = color.toColorStateList()
            tvFilterLabels.isVisible = labels.isNotEmpty()
            rv.isVisible = labels.isNotEmpty()
            listOf(swWidgetHeader, swEditWidget, swAppIcon, swNewLibrary)
                .onEach { it.setupColors(thumbCheckedColor = color, trackCheckedColor = color) }
            if (colorStateList != null) {
                sWidgetRadius.trackActiveTintList = colorStateList
                sWidgetRadius.thumbTintList = colorStateList
                sWidgetRadius.tickInactiveTintList = colorStateList
            }
            if (filteredNotes.isEmpty()) {
                widget.lvContent.isVisible = false
            } else {
                widget.lvContent.isVisible = true
                widget.lvContent.adapter = NoteListWidgetAdapter(
                    this@NoteListWidgetConfigActivity,
                    R.layout.widget_l,
                    filteredNotes,
                    library.isShowNoteCreationDate,
                    library.color,
                    library.notePreviewSize,
                )
            }

            rv.withModels {
                labels.forEach { entry ->
                    labelItem {
                        id(entry.key.id)
                        label(entry.key)
                        isSelected(entry.value)
                        color(library.color)
                        backgroundColor(attributeColoResource(R.attr.notoSurfaceColor))
                        onClickListener { _ ->
                            if (entry.value)
                                viewModel.deselectLabel(entry.key.id)
                            else
                                viewModel.selectLabel(entry.key.id)
                        }
                        onLongClickListener { _ -> false }
                    }
                }
            }
        }.launchIn(lifecycleScope)

        viewModel.isWidgetHeaderEnabled
            .onEach { isEnabled ->
                swWidgetHeader.isChecked = isEnabled
                swAppIcon.isVisible = isEnabled
                swEditWidget.isVisible = isEnabled
            }
            .launchIn(lifecycleScope)

        viewModel.isEditWidgetButtonEnabled
            .onEach { isEnabled ->
                swEditWidget.isChecked = isEnabled
            }
            .launchIn(lifecycleScope)

        viewModel.isAppIconEnabled
            .onEach { isEnabled ->
                swAppIcon.isChecked = isEnabled
            }
            .launchIn(lifecycleScope)

        viewModel.isNewLibraryButtonEnabled
            .onEach { isEnabled ->
                swNewLibrary.isChecked = isEnabled
            }
            .launchIn(lifecycleScope)

        viewModel.widgetRadius
            .onEach { radius ->
                sWidgetRadius.value = radius.toFloat()
            }
            .launchIn(lifecycleScope)
    }

    private fun NoteListWidgetConfigActivityBinding.setupListeners() {
        tvSelectLibrary.setOnClickListener {
            showSelectLibraryDialog(true)
        }

        swWidgetHeader.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setIsWidgetHeaderEnabled(isChecked)
        }

        swEditWidget.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setIsEditWidgetButtonEnabled(isChecked)
        }

        swAppIcon.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setIsAppIconEnabled(isChecked)
        }

        swNewLibrary.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setIsNewLibraryButtonEnabled(isChecked)
        }

        sWidgetRadius.addOnChangeListener { _, value, _ ->
            viewModel.setWidgetRadius(value.toInt())
        }

        btnCreate.setOnClickListener {
//            sendBroadcast() Maybe we can send broadcast to NoteListWidgetProvider instead of updating it manually
            viewModel.createOrUpdateWidget()
            val appWidgetManager = AppWidgetManager.getInstance(this@NoteListWidgetConfigActivity)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.lv)
            appWidgetManager.updateAppWidget(
                appWidgetId,
                createNoteListWidgetRemoteViews(
                    appWidgetId,
                    viewModel.isWidgetHeaderEnabled.value,
                    viewModel.isEditWidgetButtonEnabled.value,
                    viewModel.isAppIconEnabled.value,
                    viewModel.isNewLibraryButtonEnabled.value,
                    viewModel.widgetRadius.value,
                    viewModel.library.value,
                    viewModel.notes.value.isEmpty(),
                )
            )
            val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }
    }

    private fun showSelectLibraryDialog(isDismissible: Boolean) {
        val args = bundleOf(Constants.LibraryId to 0L, Constants.IsDismissible to isDismissible)
        SelectLibraryDialogFragment { libraryId -> viewModel.getWidgetData(libraryId) }
            .apply { arguments = args }
            .show(supportFragmentManager, null)
    }
}