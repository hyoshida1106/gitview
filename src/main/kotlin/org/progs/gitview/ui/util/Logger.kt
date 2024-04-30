package org.progs.gitview.ui.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun myLogger(cls: Any): Logger { return LoggerFactory.getLogger(cls.javaClass.name) }
