package contentediting

data class ContentDescriptor(val kind: ContentKind, val id: String) {
    val kindAndId: String

    init {
        kindAndId = kind.toString() + ":" + id
    }

    constructor(kindAndId: String) : this(ContentKind.from(kindAndId.split(":")[0]), kindAndId.split(":")[1]) {
    }

    override fun toString(): String {
        return kindAndId
    }

    companion object {
        val ROOT = ContentDescriptor(ContentKind.TOPIC, "x00000000")
    }
}
