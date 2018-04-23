package contentediting

import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.Entity
import com.google.cloud.datastore.Key
import org.joda.time.DateTime
import org.joda.time.Duration
import kotlin.concurrent.thread

class RevisionGetter(val datastore: Datastore) {
    fun get(key: RevisionKey): BaseRevision? {
        return toRevision(datastore.get(createDbKey(key)))
    }

    fun getMany(keys: Iterable<RevisionKey>): List<BaseRevision?> {
        val dbKeys = keys.map { createDbKey(it) }
        val result = mutableListOf<Entity>()
        var i = 0
        while (i < dbKeys.size) {
            val start = DateTime.now()
            val batch = datastore.fetch(dbKeys.subList(i, Math.min(i + 1000, dbKeys.size)))
            result.addAll(batch)
            i += batch.size
            println(batch.size.toString() + " in " + (DateTime.now().millis - start.millis))
        }
        return result.map { toRevision(it) }
    }

    fun createDbKey(key: RevisionKey): Key {
        return Key.newBuilder("khan-academy", key.kind.toString() + "Revision", key.sha.toString()).build()
    }

    fun toRevision(entity: Entity): GenericRevision? {
        if (entity.contains("content_kind") && entity.contains("content_id") && entity.contains("sha")) {
            return GenericRevision(
                    ContentKind.from(entity.getString("content_kind")),
                    entity.getString("content_id"),
                    Sha(entity.getString("sha")))
        } else {
            return null;
        }
    }
}
