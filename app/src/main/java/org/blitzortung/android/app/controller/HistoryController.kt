package org.blitzortung.android.app.controller

import android.app.Activity
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import org.blitzortung.android.app.AppService
import org.blitzortung.android.app.R
import org.blitzortung.android.data.DataChannel
import org.blitzortung.android.data.DataHandler
import org.blitzortung.android.data.provider.result.ResultEvent
import org.blitzortung.android.protocol.Event
import java.util.*


class HistoryController(activity: Activity, private val buttonHandler: ButtonColumnHandler<ImageButton>) {

    private var appService: AppService? = null

    private var dataHandler: DataHandler? = null

    private val buttons: MutableCollection<ImageButton> = arrayListOf()

    private lateinit var historyRewind: ImageButton

    private lateinit var historyForward: ImageButton

    private lateinit var goRealtime: ImageButton

    val dataConsumer = { event: Event ->
        if (event is ResultEvent && !event.failed) {
            setRealtimeData(event.containsRealtimeData())
        }
    }

    init {
        setupHistoryRewindButton(activity)
        setupHistoryForwardButton(activity)
        setupGoRealtimeButton(activity)

        setRealtimeData(true)
    }

    fun setRealtimeData(realtimeData: Boolean) {
        if (dataHandler?.isCapableOfHistoricalData ?: false) {
            historyRewind.visibility = View.VISIBLE
            val historyButtonsVisibility = if (realtimeData) View.INVISIBLE else View.VISIBLE
            historyForward.visibility = historyButtonsVisibility
            goRealtime.visibility = historyButtonsVisibility
        } else {
            historyRewind.visibility = View.INVISIBLE
            historyForward.visibility = View.INVISIBLE
            goRealtime.visibility = View.INVISIBLE
        }
        updateButtonColumn()
    }

    private fun setupHistoryRewindButton(activity: Activity) {
        historyRewind = addButtonWithAction(activity, R.id.historyRew, { v ->
            if (dataHandler?.rewInterval() ?: false) {
                disableButtonColumn()
                historyForward.visibility = View.VISIBLE
                goRealtime.visibility = View.VISIBLE
                updateButtonColumn()
                updateData()
            } else {
                val toast = Toast.makeText(activity.baseContext, activity.resources.getText(R.string.historic_timestep_limit_reached), Toast.LENGTH_SHORT)
                toast.show()
            }
        })
    }

    private fun setupHistoryForwardButton(activity: Activity) {
        historyForward = addButtonWithAction(activity, R.id.historyFfwd, { v ->
            if (dataHandler?.ffwdInterval() ?: false) {
                if (dataHandler?.isRealtime ?: false) {
                    configureForRealtimeOperation()
                } else {
                    dataHandler?.updateData()
                }
            }
        })
    }

    private fun setupGoRealtimeButton(activity: Activity) {
        goRealtime = addButtonWithAction(activity, R.id.goRealtime, { v ->
            if (dataHandler?.goRealtime() ?: false) {
                configureForRealtimeOperation()
            }
        })
    }

    private fun addButtonWithAction(activity: Activity, id: Int, action: (View) -> Unit ): ImageButton {
        val button = activity.findViewById(id) as ImageButton
        buttons.add(button)
        button.visibility = View.INVISIBLE
        button.setOnClickListener(action)

        return button
    }

    private fun configureForRealtimeOperation() {
        disableButtonColumn()
        historyForward.visibility = View.INVISIBLE
        goRealtime.visibility = View.INVISIBLE
        updateButtonColumn()
        appService?.restart()
    }

    private fun updateButtonColumn() {
        buttonHandler.updateButtonColumn()
    }

    fun getButtons(): Collection<ImageButton> {
        return buttons.toList()
    }

    private fun disableButtonColumn() {
        buttonHandler.lockButtonColumn()
    }

    private fun updateData() {
        dataHandler?.updateData(setOf(DataChannel.STRIKES))
    }

    fun setAppService(appService: AppService?) {
        this.appService = appService
        dataHandler = appService?.dataHandler()
    }
}
