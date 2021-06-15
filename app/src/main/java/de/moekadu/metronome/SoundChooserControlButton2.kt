/*
 * Copyright 2020 Michael Moessner
 *
 * This file is part of Metronome.
 *
 * Metronome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Metronome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Metronome.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.moekadu.metronome

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.Log

@SuppressLint("ViewConstructor")
class SoundChooserControlButton2(
        context: Context, val note: NoteListItem, volumeColor: Int,
        noteColor: ColorStateList?, noteHighlightColor: ColorStateList?) : NoteView(context) {

    private var isMovingToTarget = false
    private var cachedAnimationDuration = 0L
    private var cachedFadeOut = false

    var isBeingDragged = false
    var eventXOnDown = 0f
        private set
    var eventYOnDown = 0f
        private set
    var translationXInit = 0f
        private set
    var translationYInit = 0f
        private set
    var translationXTarget = 0f
        private set
    var translationYTarget = 0f
        private set
    var moveToTargetOnDelete = false
    val uid = note.uid

    var leftBoundToSwitchPosition = 0f
    var rightBoundToSwitchPosition = 0f

    var isActive = false

    var volume = note.volume
        private set
    var noteId = note.id
        private set


    init {
        setBackgroundResource(R.drawable.control_button_background)
        this.elevation = elevation
        this.volumeColor = volumeColor
        this.showNumbers = true
        this.noteColor = noteColor
        this.noteColor = noteHighlightColor
        val noteList = ArrayList<NoteListItem>()
        noteList.add(note)
        setNoteList(noteList)
        highlightNote(0, true)
        visibility = GONE
    }

    fun set(noteListItem: NoteListItem) {
        require(noteListItem.uid == uid)
        setNoteId(0, noteListItem.id)
        setVolume(0, noteListItem.volume)
        noteId = noteListItem.id
        volume = noteListItem.volume
    }

    fun containsCoordinates(xCo: Float, yCo: Float): Boolean {
        //Log.v("Metronome", "SoundChooserControlButton2.containsCoordinates: xCo=$xCo, x=$x, y=$y")
        return xCo > x && xCo < x + width && yCo > y && yCo < y + height
    }

    fun coordinateXWithinBoundsToKeepPosition(coX: Float): Boolean {
//        Log.v("Metronome", "SoundChooserControlButton2.coordinateXWihtinBoundsToKeepPosition: coX=$coX, leftBound=$leftBoundToSwitchPosition, rightBound=$rightBoundToSwitchPosition")
        return coX in leftBoundToSwitchPosition..rightBoundToSwitchPosition
    }
    fun centerXWithinBoundsToKeepPosition(): Boolean {
        val centerX = x + 0.5f * width
        return coordinateXWithinBoundsToKeepPosition(centerX)
    }

    fun animateAllNotes() {
        for (i in 0 until size)
            animateNote(i)
    }

    fun startDragging(eventX: Float, eventY: Float) {
        eventXOnDown = eventX
        eventYOnDown = eventY
        translationXInit = translationX
        translationYInit = translationY
        isBeingDragged = true
    }

    fun stopDragging() {
      isBeingDragged = false
    }

    fun isAtTargetPosition(): Boolean {
        return translationXTarget == translationX && translationYTarget == translationY
    }

    fun setTargetTranslation(xCo: Float, yCo: Float) {
        if (xCo == translationXTarget && yCo == translationYTarget)
            return

        translationXTarget = xCo
        translationYTarget = yCo

        if (isMovingToTarget)
            moveToTarget(cachedAnimationDuration, cachedFadeOut)
    }

    fun moveToTarget(animationDuration: Long = 0L, fadeOut: Boolean = false, endAction: (()->Unit)? = null) {
        // don't interrupt running animations if it matches the settings
        if (isMovingToTarget && endAction == null && fadeOut == cachedFadeOut)
            return

        if (animationDuration == 0L || visibility != VISIBLE) {
            translationX = translationXTarget
            translationY = translationYTarget
            if (endAction != null)
                endAction()
        }
        else if (translationX != translationXTarget || translationY != translationYTarget || fadeOut) {
            animate().cancel()
            val alphaEnd = if (fadeOut) 0.0f else alpha
//            Log.v("Metronome", "SoundChooserControlButton2.moveTotarget, alpha=$alpha, alphaEnd=$alphaEnd")
            animate()
                    .setDuration(animationDuration)
                    .alpha(alphaEnd)
                    .translationX(translationXTarget)
                    .translationY(translationYTarget)
                    .withStartAction {
                        isMovingToTarget = true
                        cachedAnimationDuration = animationDuration
                        cachedFadeOut = fadeOut
                    }
                    .withEndAction {
                        isMovingToTarget = false
                        if (endAction != null)
                            endAction()
                    }
        }
        else if (endAction != null) {
            endAction()
        }
    }
}