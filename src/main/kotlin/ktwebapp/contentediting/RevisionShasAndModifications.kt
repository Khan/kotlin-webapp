package contentediting

import contentediting.ContentKind.*
import java.util.*

class RevisionShasAndModifications(
    val revisionCache: RevisionCache,
    val stageFactory: ContentStage.Factory,
    val commitFactory: ContentCommit.Factory
) {
    data class TreeNode(val sha: Sha) {
        var directlyModified: Boolean? = null
        var descendantsModified: Boolean? = null
        var publishedSha: Sha? = null
        var children: List<ContentDescriptor>? = null
        var publishedChildren: List<ContentDescriptor>? = null
        var lintErrors: Boolean? = null
    }

    fun getRevisionShasAndModifications(locale: Locale): Map<ContentDescriptor, TreeNode> {
        val stage = stageFactory.create(locale)
        val commit = commitFactory.create(Sha("TODO"))
        val commitSnapshot = commit.snapshot
        val editSnapshot = HashMap(commitSnapshot)
        for ((desc, sha) in stage.snapshot.entries) {
            editSnapshot[desc] = sha
        }

        fun bulkLoad(snapshot: Map<ContentDescriptor, Sha>): Map<ContentDescriptor, BaseRevision> {
            val keys = snapshot.entries
                    .filter { ContentKind.values().contains(it.key.kind) }
                    .map { RevisionKey(it.key.kind, it.value) }
            val revisionList = revisionCache.getManyFrozen(keys)
            val revisions = mutableMapOf<ContentDescriptor, BaseRevision>()
            for (revision in revisionList) {
                if (revision != null) {
                    revisions[ContentDescriptor(revision.contentKind, revision.contentId)] = revision
                }
            }
            return revisions
        }

        val editRevisions = bulkLoad(editSnapshot)
        val commitRevisions = bulkLoad(commitSnapshot)

        val treeNodes = mutableMapOf<ContentDescriptor, TreeNode>()

        fun setDirectlyModified(node: TreeNode, directlyModified: Boolean, publishedSha: Sha?) {
            if (directlyModified) {
                node.directlyModified = true
                node.publishedSha = publishedSha
            }
        }

        fun traverseTopicTree(revision: BaseRevision): TreeNode? {
            val desc = ContentDescriptor(revision.contentKind, revision.contentId)
            val publishedSha = commitSnapshot.get(desc)
            val excluded = false //revision.contentKind != TOPIC && revision.doNotPublish
            val hasLintErrors = false //TODO
            val directlyModified = (revision.sha != publishedSha && !excluded && !hasLintErrors)
            var differingPublishedRevision: BaseRevision? = null
            if (directlyModified && publishedSha != null) {
                differingPublishedRevision = commitRevisions.get(desc)
            }
            if (differingPublishedRevision != null) {
                // TODO
            }
            // TODO: Permissions

            val existingTreeNode = treeNodes.get(desc)
            if (existingTreeNode != null) {
                if (!(existingTreeNode.directlyModified ?: false)) {
                    setDirectlyModified(existingTreeNode, directlyModified, publishedSha)
                }
                return existingTreeNode
            }

            var descendantsModified = false
            val children = mutableListOf<ContentDescriptor>()
            for (child in (revision as? TopicRevision)?.childData.orEmpty()) {
                val childRevision = editRevisions.get(child)
                if (childRevision != null) {
                    val childTreeNode = traverseTopicTree(childRevision)
                    descendantsModified = (childTreeNode?.directlyModified ?: false) || (childTreeNode?.descendantsModified ?: false)
                    children.add(child)
                }
            }

            val publishedChildren: List<ContentDescriptor>
            if (differingPublishedRevision != null) {
                publishedChildren = ((differingPublishedRevision as? TopicRevision)?.childData.orEmpty())
                        .filter { it in commitRevisions }
            } else if (publishedSha != null) {
                publishedChildren = children
            } else {
                publishedChildren = listOf()
            }

            val treeNode = TreeNode(revision.sha)
            setDirectlyModified(treeNode, directlyModified, publishedSha)
            if (descendantsModified) {
                treeNode.descendantsModified = descendantsModified
            }
            if (children.isNotEmpty()) {
                treeNode.children = children
            }
            if (publishedChildren.isNotEmpty()) {
                treeNode.publishedChildren = publishedChildren
            }
            if (hasLintErrors) {
                treeNode.lintErrors = true
            }

            treeNodes[desc] = treeNode
            return treeNode
        }

        val root = editRevisions[ContentDescriptor.ROOT]!!
        traverseTopicTree(root)
        return treeNodes
    }
}
