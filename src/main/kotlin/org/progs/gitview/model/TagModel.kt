package org.progs.gitview.model

import org.progs.gitview.git.commit.Id
import org.progs.gitview.git.tag.Tag
import org.progs.gitview.git.tag.TagOperations

class TagModel(
    private val tag: Tag
): TagOperations by tag {
    val name: String = tag.name
    val id: Id = tag.id
}