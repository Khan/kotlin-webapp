package contentediting

class TopicRevision(
        override val contentKind: ContentKind,
        override val contentId: String,
        override val sha: Sha,
        val childData: List<ContentDescriptor>
) : BaseRevision {
}
