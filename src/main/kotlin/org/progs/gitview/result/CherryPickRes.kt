package org.progs.gitview.result

import org.eclipse.jgit.api.CherryPickResult
import org.eclipse.jgit.api.CherryPickResult.CherryPickStatus.*


class CherryPickRes internal constructor(
    result: CherryPickResult
) {
    enum class Status { OK, FAILED, CONFLICT }

    val status = when (result.status) {
        OK -> { Status.OK }
        CONFLICTING -> { Status.CONFLICT }
        FAILED, null -> { Status.FAILED }
    }
    val refs = result.cherryPickedRefs
    val failingPaths = result.failingPaths
    val newHead = result.newHead
}

val CherryPickResult.result get() = CherryPickRes(this)