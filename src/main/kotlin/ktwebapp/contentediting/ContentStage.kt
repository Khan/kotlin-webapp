package contentediting

import com.google.cloud.datastore.*
import org.python.core.PyDictionary
import org.python.core.PyString
import org.python.modules.cPickle
import java.util.*

class ContentStage(datastore: Datastore, locale: Locale) {
    val snapshot = HashMap<ContentDescriptor, Sha>()

    init {
        val entity = datastore.get(Key.newBuilder("khan-academy", "KeyValueCache", "::ContentStage:$locale").build())
        val pickle = entity.getBlob("value").toByteArray()
        val sb = StringBuilder()
        for (i in 0..pickle.size - 1) {
            sb.append((pickle[i].toInt() + if (pickle[i].toInt() < 0) 256 else 0).toChar())
        }
        val pickleStr = PyString(sb.toString())
        val unpickled = cPickle.loads(pickleStr) as PyDictionary
        for (key in unpickled.keys()) {
            snapshot[ContentDescriptor(key.toString())] = Sha((unpickled[key] as PyDictionary)["sha"].toString())
        }
    }

    fun getSha(descriptor: ContentDescriptor): Sha? {
        return snapshot.get(descriptor)
    }

    class Factory(val datastore: Datastore) {
        fun create(locale: Locale): ContentStage {
            return ContentStage(datastore, locale)
        }
    }
}
