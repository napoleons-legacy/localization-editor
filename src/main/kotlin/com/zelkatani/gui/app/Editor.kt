package com.zelkatani.gui.app

import com.zelkatani.gui.controller.DirectoryController
import com.zelkatani.gui.view.DirectoryView
import javafx.stage.Stage
import tornadofx.App

const val APPLICATION_NAME = "Victoria 2 - Localization Editor"
const val PREFERENCES_NAME = "v2-editor"
const val GAME_PATH = "game_path"
const val MOD_PATH = "mod_path"

/**
 * The main GUI application.
 * The first view is the directory selector, which will lead into the editor.
 */
class Editor : App(DirectoryView::class) {
    private val directoryController: DirectoryController by inject()

    init {
        System.setProperty("apple.awt.application.name", APPLICATION_NAME)
    }

    override fun start(stage: Stage) {
        stage.isResizable = false

        directoryController.init()
        super.start(stage)
    }
}