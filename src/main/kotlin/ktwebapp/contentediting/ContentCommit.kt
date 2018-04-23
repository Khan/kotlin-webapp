package contentediting

import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.Key
import com.google.cloud.datastore.Query
import com.google.cloud.datastore.StructuredQuery
import org.json.JSONObject
import java.nio.charset.Charset
import java.util.*
import java.util.zip.Inflater

class ContentCommit(datastore: Datastore, val sha: Sha) {
    val snapshot = HashMap<ContentDescriptor, Sha>()

    init {
        val commitKey = Key.newBuilder("khan-academy", "ContentCommit", sha.toString()).build()
        val commit = datastore.get(commitKey)
        val shardQuery = Query.newEntityQueryBuilder().setKind("ContentCommitShard").setFilter(StructuredQuery.PropertyFilter.hasAncestor(commitKey)).build()
        val shardResult = datastore.run(shardQuery)
        val shards = shardResult.asSequence().toList().sortedBy({ it.key.id }).map { it.getBlob("data").toByteArray() }
        val compressed = ByteArray(shards.sumBy { it.size })
        var soFar = 0
        for (shard in shards) {
            System.arraycopy(shard, 0, compressed, soFar, shard.size)
            soFar += shard.size
        }
        val decompressed = ByteArray(10000000)
        val inflater = Inflater()
        inflater.setInput(compressed)
        val length = inflater.inflate(decompressed)
        val json = JSONObject(String(decompressed, 0, length, Charset.forName("ascii")))
        for (key in json.keys()) {
            snapshot[ContentDescriptor(key)] = Sha(json[key].toString())
        }
    }

    class Factory(val datastore: Datastore) {
        fun create(sha: Sha): ContentCommit {
            return ContentCommit(datastore, sha)
        }
    }
}
