package contentediting

import java.util.concurrent.ConcurrentHashMap

class RevisionCache(val revisionGetter: RevisionGetter) {
    companion object {
        val CACHE = ConcurrentHashMap<RevisionKey, BaseRevision>()
    }

    fun getFrozen(kind: ContentKind, sha: Sha): BaseRevision? {
        val key = RevisionKey(kind, sha)
        var frozenRevision = CACHE.get(key)
        val missed = frozenRevision == null
        if (missed) {
            val thawedRevision = revisionGetter.get(key)
            if (thawedRevision != null) {
                frozenRevision = toFrozen(thawedRevision)
                CACHE[key] = frozenRevision
            } else {
                // TODO: FRS snapshots
            }
        }
        // TODO: Stats
        return frozenRevision
    }

    fun getManyFrozen(keys: List<RevisionKey>): List<BaseRevision?> {
        val frozenRevisions = mutableListOf<BaseRevision?>()
        val missing = mutableListOf<Pair<Int, RevisionKey>>()
        for ((index, key) in keys.withIndex()) {
            val frozenRevision = CACHE.get(key)
            if (frozenRevision != null) {
                frozenRevisions.add(frozenRevision)
            } else {
                frozenRevisions.add(null)
                missing.add(Pair(index, key))
            }
        }

        val missingRevisions = revisionGetter.getMany(missing.map { it.second })
        for ((i, rev) in missingRevisions.withIndex()) {
            frozenRevisions[missing[i].first] = rev
            if (rev != null) {
                CACHE[missing[i].second] = rev
            }
        }

        return frozenRevisions
    }

    fun toFrozen(thawedRevision: BaseRevision): BaseRevision {
        return thawedRevision // TODO
    }

    class Factory(val revisionGetter: RevisionGetter) {
        fun create(): RevisionCache {
            return RevisionCache(revisionGetter)
        }
    }
}