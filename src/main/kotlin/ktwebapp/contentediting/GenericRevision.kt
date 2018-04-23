package contentediting

class GenericRevision(
    override val contentKind: ContentKind,
    override val contentId: String,
    override val sha: Sha
) : BaseRevision {

}
