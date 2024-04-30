package org.progs.gitview.model

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.WeakChangeListener
import javafx.scene.control.TextFormatter.Change


/**
 * ブランチ一覧モデル
 */
class BranchListModel(
    private val repositoryModel: RepositoryModel
) {
    /*
     *  カレントブランチ
     */
    val currentBranch get() = repositoryModel.currentBranch ?: ""

    /*
     * 表示選択されたローカルブランチ一覧
     */
    var selectedBranchList: List<LocalBranchModel> = emptyList()

    private fun updateSelectedBranchList() {
        selectedBranchList = localBranchList.filter { it.isSelected }
        updateSelectedBranchListListeners.forEach { listener ->
            listener.changed(null, null, selectedBranchList        )
        }
    }

    private val updateSelectedBranchListListeners = mutableListOf<ChangeListener<List<LocalBranchModel>>>()

    fun addSelectedBranchListChangeHandler( selectedBranchListChangeHandler: (List<LocalBranchModel>) -> Unit ) {
        val listener = ChangeListener { _, _, list -> selectedBranchListChangeHandler(list) }
        updateSelectedBranchListListeners.add(listener)
    }

    private fun releaseSelectedBranchList() {
        selectedBranchList = emptyList()
    }

    /*
     * ローカルブランチ一覧
     */

    private val localBranchListProperty = SimpleObjectProperty<List<LocalBranchModel>>(emptyList())
    var localBranchList: List<LocalBranchModel> by localBranchListProperty::value

    fun updateLocalBranchList() {
        localBranchList = repositoryModel.localBranchList.map { branch ->
            LocalBranchModel(this, branch).apply {
                addSelectionListener { updateSelectedBranchList() } }
        }
        updateSelectedBranchList()
    }

    private val localBranchListListeners = mutableListOf<ChangeListener<List<LocalBranchModel>>>()

    fun addLocalBranchListChangeListener( handler: (List<LocalBranchModel>) -> Unit ) {
        val listener = ChangeListener { _, _, list -> handler(list) }
        localBranchListListeners.add(listener)
        localBranchListProperty.addListener(WeakChangeListener(listener))
    }

    private fun releaseLocalBranchList() {
        localBranchListProperty.value = emptyList()
    }

    /*
     * リモートブランチ一覧
     */

    private val remoteBranchListProperty = SimpleObjectProperty<List<RemoteBranchModel>>(emptyList())
    var remoteBranchList: List<RemoteBranchModel> by remoteBranchListProperty::value

    fun updateRemoteBranchList() {
        remoteBranchList = repositoryModel.remoteBranchList.map { branch ->
            RemoteBranchModel(this, branch) }
    }

    private val remoteBranchListListeners = mutableListOf<ChangeListener<List<RemoteBranchModel>>>()

    fun addRemoteBranchListChangeListener( handler: (List<RemoteBranchModel>) -> Unit ) {
        val listener = ChangeListener { _, _, list -> handler(list) }
        remoteBranchListListeners.add(listener)
        remoteBranchListProperty.addListener(WeakChangeListener(listener))
    }

    private fun releaseRemoteBranchList() {
        remoteBranchListProperty.value = emptyList()
    }


    /*
     * タグ一覧
     */

    private val tagListProperty = SimpleObjectProperty<List<TagModel>>(emptyList())
    var tagList: List<TagModel> by tagListProperty::value

    fun updateTagList() {
        tagList = repositoryModel.tagList.map { tag -> TagModel(tag) }
    }

    private val tagListListeners = mutableListOf<ChangeListener<List<TagModel>>>()

    fun addTagListChangeListener( handler: (List<TagModel>) -> Unit ) {
        val listener = ChangeListener { _, _, list -> handler(list) }
        tagListListeners.add(listener)
        tagListProperty.addListener(WeakChangeListener(listener))
    }

    private fun releaseTagList() {
        tagListProperty.value = emptyList()
    }


    /**
     * 　初期化
     */
    init {
        repositoryModel.addListener {
            updateLocalBranchList()
            updateRemoteBranchList()
            updateTagList()
        }
    }

    /**
     * 閉じる
     */
    fun close() {
        releaseSelectedBranchList()
        releaseLocalBranchList()
        releaseRemoteBranchList()
        releaseTagList()
    }
}