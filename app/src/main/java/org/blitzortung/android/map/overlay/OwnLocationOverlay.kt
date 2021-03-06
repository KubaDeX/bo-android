package org.blitzortung.android.map.overlay

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.preference.PreferenceManager
import com.google.android.maps.ItemizedOverlay
import org.blitzortung.android.app.R
import org.blitzortung.android.app.view.PreferenceKey
import org.blitzortung.android.location.LocationEvent
import org.blitzortung.android.map.OwnMapView
import org.blitzortung.android.map.components.LayerOverlayComponent

class OwnLocationOverlay(context: Context, mapView: OwnMapView) : ItemizedOverlay<OwnLocationOverlayItem>(OwnLocationOverlay.DEFAULT_DRAWABLE), SharedPreferences.OnSharedPreferenceChangeListener, LayerOverlay {

    private val layerOverlayComponent: LayerOverlayComponent

    private var item: OwnLocationOverlayItem? = null

    private var zoomLevel: Int = 0
    val locationEventConsumer: (LocationEvent) -> Unit = { event ->
        val location = event.location

        item = if (location != null) OwnLocationOverlayItem(location, 25000f) else item

        populate()
        refresh()
    }

    init {

        layerOverlayComponent = LayerOverlayComponent(context.resources.getString(R.string.own_location_layer))

        item = null

        populate()

        mapView.addZoomListener { newZoomLevel ->
            if (newZoomLevel != zoomLevel) {
                zoomLevel = newZoomLevel
                refresh()
            }
        }

        zoomLevel = mapView.zoomLevel

        mapView.overlays.add(this)

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.registerOnSharedPreferenceChangeListener(this)
        onSharedPreferenceChanged(preferences, PreferenceKey.SHOW_LOCATION.toString())

        refresh()
    }

    override fun draw(canvas: Canvas?, mapView: com.google.android.maps.MapView?, shadow: Boolean) {
        if (!shadow) {
            super.draw(canvas, mapView, false)
        }
    }

    private fun refresh() {
        if (item != null) {
            item!!.setMarker(ShapeDrawable(OwnLocationShape((zoomLevel + 1).toFloat())))
        }
    }

    override fun createItem(i: Int): OwnLocationOverlayItem {
        return item!!
    }

    override fun size(): Int {
        return if (item == null) 0 else 1
    }

    fun enableOwnLocation() {
    }

    fun disableOwnLocation() {
        item = null
        populate()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, keyString: String) {
        onSharedPreferenceChanged(sharedPreferences, PreferenceKey.fromString(keyString))
    }

    private fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: PreferenceKey) {
        if (key == PreferenceKey.SHOW_LOCATION) {
            val showLocation = sharedPreferences.getBoolean(key.toString(), false)

            if (showLocation) {
                enableOwnLocation()
            } else {
                disableOwnLocation()
            }
        }
    }

    override val name: String
        get() = layerOverlayComponent.name

    override var enabled: Boolean
        get() = layerOverlayComponent.enabled
        set(value) {
            layerOverlayComponent.enabled = value
        }

    override var visible: Boolean
        get() = layerOverlayComponent.visible
        set(value) {
            layerOverlayComponent.visible = value
        }

    companion object {

        private val DEFAULT_DRAWABLE: Drawable

        init {
            val shape = OwnLocationShape(1f)
            DEFAULT_DRAWABLE = ShapeDrawable(shape)
        }
    }
}
